/*
 * Originally RParser.java
 *
 * Created on 13 August 2004, 18:50
 */

package uk.ac.cam.ch.wwmm.oscarpattern.tokenanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Text;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.tools.XOMTools;
import uk.ac.cam.ch.wwmm.oscarpattern.finder.DFAFinder;
/** A regex-based parser, finds chemical formulae etcetera.
 *
 * @author  caw47, annexed by ptc24
 */
public class TLRHolder {
	
	private static ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscarpattern/tokenanalysis/");
	
	// Singleton instance
    private static TLRHolder myInstance = null;
       
    private List<TokenLevelRegex> tlrs;
    
    private Map<String,String> nodeDict;
    
    private Document doc;
    
    /**
     * Private constructor for singleton pattern
     */
    private TLRHolder() {
    }
    
	public static void reinitialise() throws Exception {
		myInstance = null;
		getInstance();
	}
    
    /**
     * Get an instance of the singleton.
     */
    public static TLRHolder getInstance()  {
    	try {
        if(myInstance == null) {
            myInstance = new TLRHolder();
            myInstance.readXML(rg.getXMLDocument("tlrs.xml"));
        }
        
        return myInstance; 
    	} catch (Exception e) {
    		throw new Error(e);
    	}
    }
    
    
    /**
     * Read in XML file, construct DOM and build RPNode tree
     * @param document XOM Document containing regular expressions for parsing
     */
    private void readXML(Document document) throws Exception {
    	Logger logger = Logger.getLogger(DFAFinder.class);
    	logger.debug("Initialising tlrs... ");
    	doc = document;
    	nodeDict = new HashMap<String,String>();

    	tlrs = new ArrayList<TokenLevelRegex>();
    	Elements tlrElems = doc.getRootElement().getFirstChildElement("tlrs").getChildElements("tlr");
    	for(int i=0;i<tlrElems.size();i++) {
    		TokenLevelRegex tlr = new TokenLevelRegex(tlrElems.get(i), this);
    		if(!OscarProperties.getInstance().useFormulaRegex && 
    				("formulaRegex".equals(tlr.getName()) ||
    						"groupFormulaRegex".equals(tlr.getName()))) continue;
    		if(OscarProperties.getInstance().useWordShapeHeuristic && 
    				("potentialAcronymRegex".equals(tlr.getName()))) continue;
    		tlrs.add(tlr);
    	}      

    	nodeDict = null;
    	
    	System.gc();
    	logger.debug("tlrs initialised");
    }

    // Methods to find and parse nodes
    
    /** Find a node in doc with specified type and id */
    Node findNode(String targetType, String targetId) {
        Nodes nodes = doc.query("//node");
        for(int i=0; i<nodes.size(); i++) {
            String type = ((Element)nodes.get(i)).getAttributeValue("type");
            String id = ((Element)nodes.get(i)).getAttributeValue("id");
            if((type.equals(targetType)) & (id.equals(targetId))) {
                return nodes.get(i);
            }
        }
        return null;
    }
          
    String getNodeText(Element node) {
        StringBuffer txt = new StringBuffer();
        for(int i=0;i<node.getChildCount();i++) {
        	Node child = node.getChild(i);
        	if(child instanceof Text) {
                    txt.append(child.getValue());
        	} else if(child instanceof Element) {
        		Element childElem = (Element)child;
        		if(childElem.getLocalName().equals("insert")) {
        			String tag = childElem.getAttributeValue("idref");
        			txt.append(getDefText(tag));
        		}
            }
        }
        
        return txt.toString();
    }
    
    String getDefText(String idref) {
    	if(nodeDict.containsKey(idref)) {
    		return nodeDict.get(idref);
    	}
    	Element defElem = null;
    	Nodes defNodes = doc.query("//def[@id=\"" + idref + "\"]");
    	if(defNodes.size() == 1) {
    		defElem = (Element)defNodes.get(0);
    	}
        StringBuffer retval = new StringBuffer("");
        if(defElem.getAttributeValue("type").equals("const")) {
            retval.append(getNodeText(defElem));
        } else if(defElem.getAttributeValue("type").equals("list")) {
            retval.append("(");
            int i = 0;
            Elements items = defElem.getChildElements("item");
            for(int j=0;j<items.size();j++) {
                if(i>0) {
                    retval.append("|");
                }
                retval.append(getNodeText(items.get(j)));
                i++;
            }
            retval.append(")");
        }
        nodeDict.put(idref, retval.toString());
        return retval.toString();
    }
    
    /**Sees if the token matches one of the tlrs.
     * 
     * @param token
     * @return The named entity type found, or null.
     */
    public Set<TokenLevelRegex> parseToken(String token) {
    	//System.out.println("tlrs: " + token);
    	Set<TokenLevelRegex> tlrSet = new HashSet<TokenLevelRegex>();
    	for(TokenLevelRegex tlr : tlrs) {
    		//System.out.println(tlr.getName());
    		if(tlr.matches(token)) tlrSet.add(tlr);
    	}
    	//System.out.println("done!");
    	return tlrSet;    	
    }
    
    // FIXME optimality
    public boolean macthesTlr(String token, String tlrName) {
    	for(TokenLevelRegex tlr : tlrs) {
    		if(tlr.getName().equals(tlrName)) {
    			return tlr.matches(token);
    		}
    	}
    	return false;
    }
    
    public int makeHash() {
    	return XOMTools.documentHash(doc);
    }
       
}
