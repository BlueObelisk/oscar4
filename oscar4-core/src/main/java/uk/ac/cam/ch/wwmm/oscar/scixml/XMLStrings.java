package uk.ac.cam.ch.wwmm.oscar.scixml;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.XPathContext;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

/** Strings and methods to specific to SciXML. 
 * 
 */
public final class XMLStrings {
	
	private static XMLStrings myInstance = null;

	protected XPathContext xpc = new XPathContext();
	
	public String PAPER = "PAPER";
	public String TITLE = "TITLE";
	public String HEADER = "HEADER";
	public String ABSTRACT = "ABSTRACT";
	public String BODY = "BODY";
	public String PARAGRAPH = "P"; /* NB "P" is hardcoded into regexes.xml, too. Be careful when changing this! */
	public String ITALICS = "IT";
	public String DIV = "DIV";
	public String COMPOUNDREF_ID_ATTRIBUTE = "ID";
	public String EQN = "EQN";
	
	public String FORMATTING_XPATH = "//IT|//SB|//SP|//B|//SCP|//SANS";
	//These two are protected, as they should be accessed via getChemicalPlaces
	protected String CHEMICAL_PLACES_XPATH = "//P|//ABSTRACT|//TITLE|//CURRENT_TITLE|//HEADER";
	protected String CHEMICAL_EXCLUDE_XPATH = null;
	//public String CHEMICAL_EXCLUDE_XPATH = "//TITLE";
	public String SMALL_CHEMICAL_PLACES_XPATH = "//P|//ABSTRACT|/PAPER/CURRENT_TITLE|/PAPER/TITLE|//HEADER";
//	public String SMALL_CHEMICAL_PLACES_XPATH = "//P|//ABSTRACT|//CURRENT_TITLE|//HEADER";
	public String EXPERIMENTAL_SECTION_XPATH = "/PAPER/BODY/DIV[HEADER[starts-with(text(),'Experimental')]]";
	public String EXPERIMENTAL_PARAS_XPATH = "/PAPER/BODY/DIV/HEADER[starts-with(text(),'Experimental')]/..//P";
	public String ALL_PARAS_XPATH = "/PAPER/BODY/DIV/..//P";
	public String COMPOUNDREF_XPATH = "XREF[@TYPE=\"COMPOUND\"]";	
	public String TITLE_XPATH ="/PAPER/TITLE|/PAPER/CURRENT_TITLE";
	public String JOURNAL_NAME_XPATH = "/PAPER/METADATA/JOURNAL/NAME";
	
	public String STYLE_MARKUP = "IT B SB SP LATEX SCP SANS ROMAN TYPE UN DUMMY ne";
	public String BLOCK_MARKUP = "P HEADER TITLE ABSTRACT CURRENT_TITLE";
	/* These aren't part of SciXML, but are here for convenience */
	public String SPEC_PROP_MARKUP = "spectrum property";
	HashSet<String> styleMarkup = new HashSet<String>();
	HashSet<String> blockMarkup = new HashSet<String>();
	HashSet<String> specPropMarkup = new HashSet<String>();

	public XMLStrings(String s) {
		if (s != null){
			try {
				loadStrings(s);
			} catch (ParsingException e) {
				throw new OscarInitialisationException("failed to load XML strings schema: " + s, e);
			} catch (IOException e) {
				throw new OscarInitialisationException("failed to load XML strings schema: " + s, e);
			}
		}
		String [] styleMarkupArray = STYLE_MARKUP.split("\\s+");
		String [] blockMarkupArray = BLOCK_MARKUP.split("\\s+");
		String [] specPropMarkupArray = SPEC_PROP_MARKUP.split("\\s+");
		for (int i = 0; i < styleMarkupArray.length; i++) styleMarkup.add(styleMarkupArray[i]);
		for (int i = 0; i < blockMarkupArray.length; i++) blockMarkup.add(blockMarkupArray[i]);
		for (int i = 0; i < specPropMarkupArray.length; i++) specPropMarkup.add(specPropMarkupArray[i]);
	}
	
	private void loadStrings(String schemaName) throws ParsingException, IOException {
		ResourceGetter rg = new ResourceGetter(XMLStrings.class.getClassLoader(),"uk/ac/cam/ch/wwmm/oscar3Memm/scixml/");
		Document doc = rg.getXMLDocument(schemaName + ".xml");
		Elements ee = doc.getRootElement().getChildElements();
		Map<String,String> varMap = new HashMap<String,String>();
		for (int i = 0; i < ee.size(); i++) {
			Element e = ee.get(i);
			if(e.getLocalName().equals("xpc")) {
				xpc.addNamespace(e.getAttributeValue("prefix"), e.getAttributeValue("uri"));
			} else if(e.getLocalName().equals("string")) {
				varMap.put(e.getAttributeValue("name"), e.getAttributeValue("value"));
			}
		}
		if(varMap.containsKey("PAPER")) PAPER = varMap.get("PAPER");
		if(varMap.containsKey("TITLE")) TITLE = varMap.get("TITLE");
		if(varMap.containsKey("ABSTRACT")) ABSTRACT = varMap.get("ABSTRACT");
		if(varMap.containsKey("BODY")) BODY = varMap.get("BODY");
		if(varMap.containsKey("HEADER")) HEADER = varMap.get("HEADER");
		if(varMap.containsKey("PARAGRAPH")) PARAGRAPH = varMap.get("PARAGRAPH");
		if(varMap.containsKey("ITALICS")) ITALICS = varMap.get("ITALICS");
		if(varMap.containsKey("DIV")) DIV = varMap.get("DIV");
		if(varMap.containsKey("COMPOUNDREF_ID_ATTRIBUTE")) COMPOUNDREF_ID_ATTRIBUTE = varMap.get("COMPOUNDREF_ID_ATTRIBUTE");
		if(varMap.containsKey("EQN")) EQN = varMap.get("EQN");
		if(varMap.containsKey("FORMATTING_XPATH")) FORMATTING_XPATH = varMap.get("FORMATTING_XPATH");
		if(varMap.containsKey("CHEMICAL_PLACES_XPATH")) CHEMICAL_PLACES_XPATH = varMap.get("CHEMICAL_PLACES_XPATH");
		if(varMap.containsKey("CHEMICAL_EXCLUDE_XPATH")) CHEMICAL_EXCLUDE_XPATH = varMap.get("CHEMICAL_EXCLUDE_XPATH");
		if(varMap.containsKey("CHEMICAL_EXCLUDE_XPATH")) CHEMICAL_EXCLUDE_XPATH = varMap.get("CHEMICAL_EXCLUDE_XPATH");
		if(varMap.containsKey("SMALL_CHEMICAL_PLACES_XPATH")) SMALL_CHEMICAL_PLACES_XPATH = varMap.get("SMALL_CHEMICAL_PLACES_XPATH");
		if(varMap.containsKey("EXPERIMENTAL_SECTION_XPATH")) EXPERIMENTAL_SECTION_XPATH = varMap.get("EXPERIMENTAL_SECTION_XPATH");
		if(varMap.containsKey("EXPERIMENTAL_PARAS_XPATH")) EXPERIMENTAL_PARAS_XPATH = varMap.get("EXPERIMENTAL_PARAS_XPATH");
		if(varMap.containsKey("ALL_PARAS_XPATH")) ALL_PARAS_XPATH = varMap.get("ALL_PARAS_XPATH");
		if(varMap.containsKey("COMPOUNDREF_XPATH")) COMPOUNDREF_XPATH = varMap.get("COMPOUNDREF_XPATH");
		if(varMap.containsKey("TITLE_XPATH")) TITLE_XPATH = varMap.get("TITLE_XPATH");
		if(varMap.containsKey("JOURNAL_NAME_XPATH")) JOURNAL_NAME_XPATH = varMap.get("JOURNAL_NAME_XPATH");
		if(varMap.containsKey("STYLE_MARKUP")) STYLE_MARKUP = varMap.get("STYLE_MARKUP");
		if(varMap.containsKey("BLOCK_MARKUP")) BLOCK_MARKUP = varMap.get("BLOCK_MARKUP");
		if(varMap.containsKey("SPEC_PROP_MARKUP")) SPEC_PROP_MARKUP = varMap.get("SPEC_PROP_MARKUP");
		
	}
	
	public static synchronized XMLStrings getDefaultInstance() {
		if(myInstance == null) {
			myInstance = new XMLStrings(null);				
		}
		return myInstance;
	}
	
	/** Initialises the singleton associated with this class. For convenience at startup.
	 */
	//TODO this isn't called - do we need it? 
	public static void init() {
		getDefaultInstance();
	}
	
	/**Given an element, find the ancestor element that is not a style markup
	 * element.
	 * 
	 * @param elem The element to query.
	 * @return The ancestor element.
	 */
	public Element getElemBelowStyleMarkup(Element elem) {
		while(styleMarkup.contains(elem.getLocalName())) elem = (Element)elem.getParent();
		return elem;
	}

	
	/**Checks to see if the given element is a citation reference element.
	 * 
	 * @param e The element to query.
	 * @return The result
	 */
	public Boolean isCitationReference(Element e) {
		return e.getLocalName().equals("REF") || e.getLocalName().equals("PUBREF");
	}
	
	/**Checks to see if the given element, or the first non-style ancestor of
	 * the element, is a citation reference element.
	 * 
	 * @param e The element to query.
	 * @return The result
	 */
	public Boolean isCitationReferenceUnderStyle(Element e) {
		return isCitationReference(getElemBelowStyleMarkup(e));
	}

	
	/**Checks to see if the given element is an equation element.
	 * 
	 * @param e The element to query.
	 * @return The result
	 */
	public Boolean isEquation(Element e) {
		return e.getLocalName().equals(EQN);
	}

	/**Checks to see if the given element is a compound reference element.
	 * 
	 * @param e The element to query.
	 * @return The result
	 */
	public Boolean isCompoundReference(Element e) {
		return e.getLocalName().equals("XREF") && e.getAttributeValue("TYPE").equals("COMPOUND");
	}

	/**Checks to see if the given element, or the first non-style ancestor of
	 * the element, is a compound reference element.
	 * 
	 * @param e The element to query.
	 * @return The result
	 */
	public Boolean isCompoundReferenceUnderStyle(Element e) {
		return isCompoundReference(getElemBelowStyleMarkup(e));
	}
	
	public Nodes getChemicalPlaces(Document doc) {
		Nodes positiveNodes = doc.query(SMALL_CHEMICAL_PLACES_XPATH, xpc);
		if(CHEMICAL_EXCLUDE_XPATH != null) {
			Nodes negativeNodes = doc.query(CHEMICAL_EXCLUDE_XPATH, xpc);
			Nodes resultNodes = new Nodes();
			for (int i = 0; i < positiveNodes.size(); i++) {
				if(!negativeNodes.contains(positiveNodes.get(i))) {
					resultNodes.append(positiveNodes.get(i));
				}
			}
			return resultNodes;
		} else {
			return positiveNodes;
		}
	}
	
	public XPathContext getXpc() {
		return xpc;
	}
}
