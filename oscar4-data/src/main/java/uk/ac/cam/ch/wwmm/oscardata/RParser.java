package uk.ac.cam.ch.wwmm.oscardata;

import java.io.IOException;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

/**
 * A regex-based parser, finds chemical data and other forms of notation.
 * 
 * @author caw47, annexed by ptc24
 */
public final class RParser {

	private static final Logger LOG = LoggerFactory.getLogger(RParser.class);
	private static ResourceGetter rg = new ResourceGetter(
			"uk/ac/cam/ch/wwmm/oscardata/");

	// Singleton instance
	private static RParser myInstance = null;

	// Top RPNode of the tree
	private RPNode topNode = null;

	private Document doc;

	/**
	 * Private constructor for singleton pattern
	 */
	private RParser() {
	}

	/**
	 * Clear all existing RParser data and reinitialise.
	 * 
	 */
	@Deprecated
	//TODO this isn't called - do we need it?
	public static void reinitialise() {
		myInstance = null;
		getInstance();
	}

	/**
	 * Initialise the RParser singleton, if it hasn't been initialised already.
	 * 
	 */
	@Deprecated
	//TODO this isn't called - do we need it?
	public static void init() {
		getInstance();
	}

	/**
	 * Get an instance of the singleton.
	 * 
	 */
	static synchronized RParser getInstance() {
		if (myInstance == null) {
			myInstance = new RParser();
			Document doc;
			try {
				doc = rg.getXMLDocument("regexes.xml");
			} catch (ParsingException e) {
				throw new OscarInitialisationException("failed to load RParser regexes", e);
			} catch (IOException e) {
				throw new OscarInitialisationException("failed to load RParser regexes", e);
			}
			myInstance.readXML(doc);
		}

		return myInstance;
	}

	/**
	 * Read in XML file, construct DOM and build RPNode tree
	 * 
	 * @param document
	 *            XOM Document containing regular expressions for parsing
	 */
	private void readXML(Document document) {
		LOG.debug("Initialising data parser... ");
		doc = document;
		// create top RPNode
		topNode = new RPNode(this);

		// find <child> within <top> and add them to topNode
		// Node top = doc.query("//top").get(0);
		Element top = doc.getRootElement().getFirstChildElement("top");
		for (int i = 0; i < top.getChildCount(); i++) {
			Node child = top.getChild(i);
			if (child instanceof Element) {
				Element childElem = (Element) child;
				if ("child".equals(childElem.getLocalName())) {
					String type = childElem.getAttributeValue("type");
					String id = childElem.getAttributeValue("id");
					Element referencedNode = findNode(type, id);
					if (referencedNode == null) {
						continue;
					}
					topNode.addChild(referencedNode);
				}
			}
		}
		LOG.debug("regexes initialised");
	}

	// Methods to find and parse nodes

	/**
	 * Find a &lt;node> element in doc with specified type and id
	 * 
	 * @return the appropriate &lt;node> element or null if none exits
	 */
	Element findNode(String targetType, String targetId) {
		// Nodes nodes = doc.query("//node");
		Elements nodes = doc.getRootElement().getChildElements("node");
		for (int i = 0; i < nodes.size(); i++) {
			String type = nodes.get(i).getAttributeValue("type");
			String id = nodes.get(i).getAttributeValue("id");
			if ((type.equals(targetType)) & (id.equals(targetId))) {
				return nodes.get(i);
			}
		}
		return null;
	}
	
	
	/**
	 *  Find a &lt;def> element in doc with specified id
	 * 
	 * @return the appropriate &lt;node> element or null if none exits
	 */
	Element findDef(String targetId) {
		Elements defs = doc.getRootElement().getChildElements("def");
		for (int i = 0; i < defs.size(); i++) {
			if (defs.get(i).getAttributeValue("id").equals(targetId)) {
				return defs.get(i);
			}
		}
		return null;
	}

	
	/**
	 * Calculates the regex contained in the specified &lt;node> element
	 */
	String getNodeRegex(Element node) {
		StringBuffer txt = new StringBuffer();
		for (int i = 0; i < node.getChildCount(); i++) {
			Node child = node.getChild(i);
			if (child instanceof Text) {
				txt.append(child.getValue());
			}
			else if (child instanceof Element) {
				Element childElem = (Element) child;
				if (childElem.getLocalName().equals("insert")) {
					String tag = childElem.getAttributeValue("idref");
					txt.append(getDefRegex(tag));
				}
			}
		}

		return txt.toString();
	}

	/**
	 * Calculates the regex contained in the @lt;def> element with the specified id  
	 */
	String getDefRegex(String idref) {
		Element defElem = findDef(idref);
		StringBuffer buffer = new StringBuffer();
		if (defElem.getAttributeValue("type").equals("const")) {
			buffer.append(getNodeRegex(defElem));
		}
		else if (defElem.getAttributeValue("type").equals("list")) {
			buffer.append("(");
			int i = 0;
			Elements items = defElem.getChildElements("item");
			for (int j = 0; j < items.size(); j++) {
				if (i > 0) {
					buffer.append("|");
				}
				buffer.append(getNodeRegex(items.get(j)));
				i++;
			}
			buffer.append(")");
		}
		return buffer.toString();
	}

	void parse(Text textNode) {
		topNode.parseXOMText(textNode);
	}
	
	List <DataAnnotation> findData(TokenSequence tokSeq) {
		return topNode.annotateData(tokSeq);
	}
	
}
