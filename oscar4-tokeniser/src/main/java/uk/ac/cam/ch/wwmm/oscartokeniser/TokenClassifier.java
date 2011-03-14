/*
 * Originally RParser.java
 *
 * Created on 13 August 2004, 18:50
 */

package uk.ac.cam.ch.wwmm.oscartokeniser;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

/** A regex-based parser, finds chemical formulae etcetera.
 *
 * @author  caw47, annexed by ptc24
 * @author dmj30
 */
public class TokenClassifier {

    private static final String REGEX_FILENAME = "tokenLevelRegularExpressions.xml";
	private static final Logger LOG = LoggerFactory.getLogger(TokenClassifier.class);
    private static ResourceGetter rg = new ResourceGetter(TokenClassifier.class.getClassLoader(),"/uk/ac/cam/ch/wwmm/oscartokeniser/");
    private static TokenClassifier defaultInstance;

    private final Map<String,TokenClass> tokenLevelRegexs;


    public TokenClassifier(Map <String, TokenClass> tokenLevelRegexes) {
    	Map <String, TokenClass> copy = new HashMap<String, TokenClass>(tokenLevelRegexes);
    	this.tokenLevelRegexs = Collections.unmodifiableMap(copy);
    }

    @Deprecated
    //TODO this isn't called - do we need it?
    public static void reinitialise() {
        defaultInstance = null;
        getDefaultInstance();
    }

    /**
     * Get the default TokenClassifier
     */
    public static synchronized TokenClassifier getDefaultInstance()  {
        if (defaultInstance == null) {
        	Document sourceDoc;
    		try {
    			sourceDoc = rg.getXMLDocument(REGEX_FILENAME);
    		} catch (ParsingException e) {
    			throw new OscarInitialisationException("failed to load TokenClassifier", e);
    		} catch (IOException e) {
    			throw new OscarInitialisationException("failed to load TokenClassifier", e);
    		} 
            Map<String, TokenClass> tokenLevelRegexes;
			try {
				tokenLevelRegexes = readXML(sourceDoc);
			} catch (DataFormatException e) {
				throw new OscarInitialisationException("failed to load TokenClassifier", e);
			} 
    		defaultInstance = new TokenClassifier(tokenLevelRegexes);
        }
        return defaultInstance;
    }


    /**
     * Read in XML file, construct DOM and build RPNode tree
     * @param document XOM Document containing regular expressions for parsing
     * @throws DataFormatException 
     */
    public static Map <String, TokenClass> readXML(Document document) throws DataFormatException {
    	LOG.debug("Initialising tlrs... ");
    	Map<String, TokenClass> tokenLevelRegexes = new HashMap<String, TokenClass>();
        Map<String, String> nodeDict = new HashMap<String,String>();

        tokenLevelRegexes = new LinkedHashMap<String, TokenClass>();
        Elements tlrElems = document.getRootElement().getFirstChildElement("tlrs").getChildElements("tlr");
        for (int i = 0; i < tlrElems.size(); i++) {
            
        	Element elem = tlrElems.get(i);
        	NamedEntityType type = NamedEntityType.valueOf(elem.getAttributeValue("type"));
        	String regex = getDefText(elem.getAttributeValue("idref"), nodeDict, document);
        	String name = elem.getAttributeValue("name");
        	TokenClass tlr = new TokenClass(type, regex, name);
        	
            if (tokenLevelRegexes.containsKey(tlr.getName())) {
            	LOG.warn("Duplicate TokenLevelRegex defined: "+tlr.getName());
            } else {
                tokenLevelRegexes.put(tlr.getName(), tlr);
            }
        }

        LOG.debug("tlrs initialised");
        return tokenLevelRegexes;
    }


    private static String getNodeText(Element node, Map<String, String> nodeDict, Document tlrDoc) throws DataFormatException {
        StringBuffer txt = new StringBuffer();
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChild(i);
            if(child instanceof Text) {
                txt.append(child.getValue());
            } else if(child instanceof Element) {
                Element childElem = (Element)child;
                if(childElem.getLocalName().equals("insert")) {
                    String tag = childElem.getAttributeValue("idref");
                    txt.append(getDefText(tag, nodeDict, tlrDoc));
                }
            }
        }

        return txt.toString();
    }

    
    private static String getDefText(String idref, Map<String, String> nodeDict, Document tlrDoc) throws DataFormatException {
        if(nodeDict.containsKey(idref)) {
            return nodeDict.get(idref);
        }
        Element defElem;
        Nodes defNodes = tlrDoc.query("//def[@id=\"" + idref + "\"]");
        if(defNodes.size() == 1) {
            defElem = (Element)defNodes.get(0);
        } else {
        	throw new DataFormatException("too many definitions for " + idref);
        }
        StringBuffer retval = new StringBuffer("");
        if(defElem.getAttributeValue("type").equals("const")) {
            retval.append(getNodeText(defElem, nodeDict, tlrDoc));
        } else if(defElem.getAttributeValue("type").equals("list")) {
            retval.append("(");
            int i = 0;
            Elements items = defElem.getChildElements("item");
            for (int j = 0; j < items.size(); j++) {
                if(i>0) {
                    retval.append("|");
                }
                retval.append(getNodeText(items.get(j), nodeDict, tlrDoc));
                i++;
            }
            retval.append(")");
        }
        nodeDict.put(idref, retval.toString());
        return retval.toString();
    }
    
    
    Map<String, TokenClass> getTokenLevelRegexes() {
    	return tokenLevelRegexs;
    }

    
    /**Sees if the token matches one of the tlrs.
     *
     * @param token
     * @return The named entity types found, or an empty set.
     */
    public Set<NamedEntityType> classifyToken(String token) {
        /* 
         * use of Collections.emptySet() and Collections.singleton() avoids creating
         * unnecessary construction of HashSets and improves performance
         */
    	Set<NamedEntityType> results = Collections.emptySet();
        for (TokenClass tokenLevelRegex : tokenLevelRegexs.values()) {
            if (tokenLevelRegex.isMatch(token)) {
                if (results.isEmpty()) {
                    results = Collections.singleton(tokenLevelRegex.getType());
                } else {
                    if (results.size() == 1) {
                        results = new HashSet<NamedEntityType>(results);
                    }
                    results.add(tokenLevelRegex.getType());
                }
            }
        }
        return results;
    }

    /**
     * Checks if the given text is a match to the specified token-level regex
     *  
     */
    public boolean isTokenLevelRegexMatch(String surface, String tlrName) {
        TokenClass tokenClass = tokenLevelRegexs.get(tlrName);
        if (tokenClass == null) {
            throw new IllegalArgumentException("unknown token-level regex: " + tlrName);
        }
        return tokenClass.isMatch(surface);
    }

    
}
