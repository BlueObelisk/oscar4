package uk.ac.cam.ch.wwmm.oscar.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.cam.ch.wwmm.oscar.scixml.SciXMLDocument;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.xslt.XSLTransform;

/** This hold a list of the Named Entity types used in Oscar.
 * 
 * @author ptc24
 * @author egonw
 */
public class NamedEntityTypes {
	
	public final static String COMPOUND = "CM";
	public final static String COMPOUNDS = "CMS";
	public final static String GROUP = "GP";
	public final static String REACTION = "RN";
	public final static String ADJECTIVE = "CJ";
	public final static String LOCANTPREFIX = "CPR";
	public final static String POTENTIALACRONYM = "AHA";
	public final static String ASE = "ASE";
	public final static String ASES = "ASES";
	public final static String PROPERNOUN = "PN";
	public final static String ONTOLOGY = "ONT";
	public final static String CUSTOM = "CUST";
	public final static String STOP = "STOP";
	public final static String POLYMER = "PM";
	
	private static ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscarMEMM/types/");
	private static NamedEntityTypes myInstance;
	private Document typeDoc;
	private List<String> typeNames;
	private Map<String,List<String>> subTypes;
	private Map<String,Integer> priority;
	
	/**Deletes and re-initialises the singleton.
	 * 
	 * @throws Exception
	 */
	public static void reinitialise() throws Exception {
		myInstance = null;
		getInstance();
	}
	
	private static NamedEntityTypes getInstance() {
		try {
			if(myInstance == null) myInstance = new NamedEntityTypes();
			return myInstance;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Couldn't initialise NETypes");
		}
	}
	
	private NamedEntityTypes() throws Exception {
		typeDoc = rg.getXMLDocument("neTypes.xml");
		typeNames = new ArrayList<String>();
		subTypes = new HashMap<String,List<String>>();
		Nodes a = typeDoc.query("//type");
		for(int i=0;i<a.size();i++) {
			Element e = (Element)a.get(i);
			typeNames.add(e.getAttributeValue("name"));
			List<String> stList = new ArrayList<String>();
			subTypes.put(e.getAttributeValue("name"), stList);
			Elements st = e.getChildElements("subtype");
			for(int j=0;j<st.size();j++) {
				stList.add(st.get(j).getAttributeValue("name"));
			}
		}
		priority = new HashMap<String,Integer>();
		priority.put(STOP, 11);
		priority.put(LOCANTPREFIX, 10);
		priority.put(ADJECTIVE, 9);
		priority.put(REACTION, 8);
		priority.put(POLYMER, 7);
		priority.put(COMPOUND, 6);
		priority.put(ASE, 5);
		priority.put(POTENTIALACRONYM, 4);
		priority.put(CUSTOM, 3);
		priority.put(ONTOLOGY, 2);
		
	}
	
	/**Returns a list of named entity type names.
	 * 
	 * @return The named entity type names.
	 */
	public static List<String> getTypeNames() {
		return getInstance().typeNames;
	}

	/**Returns a list of available subtypes for a named entity type.
	 * 
	 * @param type The named entity type.
	 * @return The subtypes, or null.
	 */
	public static List<String> getSubTypeNames(String type) {
		return getInstance().subTypes.get(type);
	}
	
	private Document getSciXMLDocInternal() {
		try {
			XSLTransform xslt = new XSLTransform(rg.getXMLDocument("typesToSciXML.xsl"));
			return XSLTransform.toDocument(xslt.transform(typeDoc));
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Couldn't make NETypes SciXML");			
		}
	}
	
	/**Converts the NETypes document to a SciXML document for viewing.
	 * 
	 * @return The SciXML document.
	 */
	public static SciXMLDocument getSciXMLDoc() {
		return SciXMLDocument.makeFromDoc(getInstance().getSciXMLDocInternal());
	}
	
	/**Gets an integer to represent the priority of a named entity.
	 * 
	 * @param neType The named entity type.
	 * @return The priority value.
	 */
	public static Integer getPriority(String neType) {
		if(getInstance().priority.containsKey(neType)) {
			return getInstance().priority.get(neType);
		} else {
			return 0;
		}
	}
}
