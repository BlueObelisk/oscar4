package uk.ac.cam.ch.wwmm.oscardata;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Text;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;


/** A regex-based parser, finds chemical data and other forms of notation.
 *
 * @author  caw47, annexed by ptc24
 */
public final class RParser {
	
	private static Logger logger = Logger.getLogger(RParser.class);
	private static ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscardata/");
	
	// Singleton instance
    private static RParser myInstance = null;
     
    // Top RPNode of the tree
    private RPNode topNode = null;
      
    private Map<String,String> nodeDict;
    
    private Document doc;
    
    /**
     * Private constructor for singleton pattern
     */
    private RParser() {
    }
    
    /**Clear all existing RParser data and reinitialise.
     * 
     * @throws Exception
     */
	public static void reinitialise() throws Exception {
		myInstance = null;
		getInstance();
	}

	/**Initialise the RParser singleton, if it hasn't been initialised
	 * already.
	 * 
	 * @throws Exception
	 */
	public static void init() throws Exception {
		getInstance();
	}
    
    /**
     * Get an instance of the singleton.
     * @throws Exception If error parsing XML
     */
    static RParser getInstance()  {
    	try {
        if(myInstance == null) {
            myInstance = new RParser();
            myInstance.readXML(rg.getXMLDocument("regexes.xml"));
        }
        
        return myInstance; 
    	} catch (Exception e) {
    		throw new Error("failed to load data regexes", e);
    	}
    }
    
    
    /**
     * Read in XML file, construct DOM and build RPNode tree
     * @param document XOM Document containing regular expressions for parsing
     */
    private void readXML(Document document) throws Exception {
    	logger.debug("Initialising data parser... ");
    	doc = document;
    	nodeDict = new HashMap<String,String>();
    	// create top RPNode
    	topNode = new RPNode(this);

    	// find <child> within <top> and add them to topNode
    	Node top = doc.query("//top").get(0);
    	for(int i=0;i<top.getChildCount();i++) {
    		Node child = top.getChild(i);
    		if(child instanceof Element) {
    			Element childElem = (Element)child;
    			if("child".equals(childElem.getLocalName())) {
    				// child is of type <child>
    				String type = childElem.getAttributeValue("type");
    				String id = childElem.getAttributeValue("id");
    				// find the <node> the <child> refers to
    				Node node2 = findNode(type, id);
    				if(node2==null)
    					continue;
    				// node2 is the <node> referred to by <child>
    				// get value attribute, if exists, else null
    				topNode.addChild((Element)node2);                        	 
    			}
    		}
    	}

    	nodeDict = null;
    	System.gc();
    	logger.debug("regexes initialised");
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
    
    void parse(Text textNode) {
    	topNode.parseXOMText(textNode);	
    }   
}
