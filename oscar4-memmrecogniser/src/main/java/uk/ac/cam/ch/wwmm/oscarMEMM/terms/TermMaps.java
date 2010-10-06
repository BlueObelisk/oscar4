package uk.ac.cam.ch.wwmm.oscarMEMM.terms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**A class to hold several mappings between terms and their identifiers.
 * 
 * @author ptc24
 *
 */
public final class TermMaps {
	
	private Map<String, String> neTerms;
	private Map<String, String> iePatterns;
	private Map<String, String> ontology;
	private Map<String, String> custEnt;
	private Map<String, String> structureTypes;
	private Set<String> suffixes;

	private static ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscarMEMM/terms/");
	private static Pattern definePattern = Pattern.compile("(.*?) = (.*)");

	private static TermMaps myInstance;
	
	/**Initialise the TermMaps singleton, deleting the old one if one already
	 * exists.
	 * 
	 * @throws Exception
	 */
	public static void reinitialise() throws Exception {
		myInstance = null;
		getInstance();
	}
	
	/**Initialise the TermMaps singleton, if this has not already been done.
	 * 
	 */
	public static void init() {
		try {
			if(myInstance == null) {
				myInstance = new TermMaps();
			}
		} catch (Exception e) {
			throw new Error(e);
		}		
	}
	
	private static TermMaps getInstance() {
		try {
		if(myInstance == null) {
			myInstance = new TermMaps();
		}
		return myInstance;
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	private static HashMap<String, String> getTermMap(String filename, boolean concatenateTypes) throws Exception {
		HashMap<String, String> defines = new HashMap<String, String>();
		
		HashMap<String, String> lexicons = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(rg.getStream(filename), "UTF-8"));
		String line = reader.readLine();
		String lexname = "";
		while(line != null) {
			while(line.endsWith(">>>")) {
				line = line.substring(0,line.length()-3);
				line += reader.readLine();
			}
			if(line.length() == 0) {
				// Blank line
			} else if(line.charAt(0) == '#') {
				// Comment
			} else if(line.matches("\\[\\S*\\]")) {
				lexname = line.substring(1, line.length()-1);
			} else {
				for(String d : defines.keySet()) {
					line = line.replace(d, defines.get(d));
				}
				if("DEFINE".equals(lexname)) {
					Matcher m = definePattern.matcher(line);
					if(m.matches()) {
						defines.put(m.group(1), m.group(2));
					}
				} else if(concatenateTypes && lexicons.get(line) != null) {
					String newLexName = StringTools.normaliseName(lexname);
					lexicons.put(line, newLexName += " " + lexicons.get(line));
				} else {
					lexicons.put(line, StringTools.normaliseName(lexname));
					lexicons.put(StringTools.normaliseName(line), StringTools.normaliseName(lexname));
				}
				//if(line.matches(".*[a-z][a-z].*")) lexicons.put(line.toLowerCase(), lexname);
			}			
			line = reader.readLine();
		}
		return lexicons;
	}

	private void digestSuffixes() {
		suffixes = new HashSet<String>();
		for(String s : neTerms.keySet()) {
			String [] ss = s.split("\\s+");
			for(int i=0;i<ss.length;i++) {
				if(ss[i].startsWith("$-")) {
					suffixes.add(ss[i].substring(2));
				}
			}
		}
	}

	/**Makes some hashes of the Oscar3 term map resources, for traceability
	 * purposes.
	 * 
	 * @return A string detailing the various hashes.
	 */
	public static String makeHashes() {
		StringBuffer sb = new StringBuffer();
		sb.append("neTerms: " + StringTools.mapToStableString(getNeTerms()).hashCode() + "\n");
		sb.append("iePatterns: " + StringTools.mapToStableString(getIePatterns()).hashCode() + "\n");
		sb.append("custEnt: " + StringTools.mapToStableString(getCustEnt()).hashCode() + "\n");
		sb.append("structureTypes: " + StringTools.mapToStableString(getStructureTypes()).hashCode() + "\n");
		sb.append("ontology: " + StringTools.mapToStableString(getOntology()).hashCode() + "\n");
		return sb.toString();
	}
	
	private TermMaps() throws Exception {
		if(OscarProperties.getInstance().verbose) System.out.print("Initialising term maps... ");
		neTerms = getTermMap("neTerms.txt", false);
		//add additional neTerms for polymers if set to polymer mode 
		if (OscarProperties.getInstance().polymerMode) {
			Map <String, String> polyNeTerms = getTermMap("polyNeTerms.txt", false);
			neTerms.putAll(polyNeTerms);
		}
		iePatterns = getTermMap("iePatterns.txt", false);
		structureTypes = getTermMap("structureTypes.txt", false);
		custEnt = getTermMap("custEnt.txt", true);
		if(OscarProperties.getInstance().useONT) {
			ontology = getTermMap("ontology.txt", true);
			//add polymer ontology if set to polymer mode
			if (OscarProperties.getInstance().polymerMode) {
				Map<String, String> polyOntology = getTermMap("polyOntology.txt", true);
				ontology.putAll(polyOntology);
			}
		} else {
			ontology = new HashMap<String,String>();
		}
		digestSuffixes();		
		if(OscarProperties.getInstance().verbose) System.out.println("term maps initialised");
	}
	
	/**Gets the term map for neTerms.txt.
	 * 
	 * @return The term map.
	 */
	public static Map<String, String> getNeTerms() {
		return getInstance().neTerms;
	}
	
	/**Gets the term map for iePatterns.txt.
	 * 
	 * @return The term map.
	 */
	public static Map<String, String> getIePatterns() {
		return getInstance().iePatterns;
	}
	
	static Map<String, String> getOntology() {
		return getInstance().ontology;
	}

	/**Gets the term map for custEnt.txt.
	 * 
	 * @return The term map.
	 */
	public static Map<String, String> getCustEnt() {
		return getInstance().custEnt;
	}
	
	/**Gets the term map for structureTypes.txt.
	 * 
	 * @return The term map.
	 */
	public static Map<String, String> getStructureTypes() {
		return getInstance().structureTypes;
	}
	
	/**Gets a collection of suffixes harvested from neTerms.txt.
	 * 
	 * @return A collection of suffixes harvested from neTerms.txt
	 */
	public static Set<String> getSuffixes() {
		return getInstance().suffixes;
	}
}
