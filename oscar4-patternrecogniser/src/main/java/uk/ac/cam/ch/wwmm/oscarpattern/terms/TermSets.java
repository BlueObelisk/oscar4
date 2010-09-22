package uk.ac.cam.ch.wwmm.oscarpattern.terms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscarpattern.tools.Oscar3Props;
import uk.ac.cam.ch.wwmm.oscarpattern.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscarpattern.tools.StringTools;

/** A data class to hold several lists of words used in the name recogniser.
 * 
 * @author ptc24
 *
 */
public final class TermSets {

	private static TermSets myInstance;
	
	private Set<String> stopWords;
	private Set<String> usrDictWords;
	private Set<String> closedClass;
	private Set<String> chemAses;
	private Set<String> nonChemAses;
	private Set<String> noSplitPrefixes;
	private Set<String> elements;
	private Set<String> ligands;
	private Set<String> reactWords;
	private Pattern endingInElementPattern;

	
	private static ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscarpattern/terms/");
	
	
	/**Initialise the TermSets singleton, deleting the old one if one already
	 * exists.
	 * 
	 * @throws Exception
	 */	public static void reinitialise() throws Exception {
		myInstance = null;
		getInstance();
	}
	
	/**Initialise the TermSets singleton, if this has not already been done.
	 * 
	 */
	public static void init() {
		try {
			if(myInstance == null) {
				myInstance = new TermSets();
			}
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	private static TermSets getInstance() {
		try {
			if(myInstance == null) {
				myInstance = new TermSets();
			}
			return myInstance;
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	private static HashSet<String> getTermSet(String filename) throws Exception {
		return getTermSet(filename, true);
	}
	
	private static HashSet<String> getTermSet(String filename, boolean normName) throws Exception {
		HashSet<String> dict = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(rg.getStream(filename), "UTF-8"));
		while(reader.ready()) {
			String line = reader.readLine();
			if(normName) line = StringTools.normaliseName(line);
			if(line.length() > 0 && line.charAt(0) != '#') dict.add(line);
		}
		return dict;
	}
	
	/**Gets the term set from stopwords.txt.
	 * 
	 * @return The term set.
	 */
	public static Set<String> getStopWords() {
		return getInstance().stopWords;
	}
	
	/**Gets the term set from usrDictWords.txt.
	 * 
	 * @return The term set.
	 */
	public static Set<String> getUsrDictWords() {
		return getInstance().usrDictWords;
	}

	/**Gets the term set from closedClass.txt.
	 * 
	 * @return The term set.
	 */
	public static Set<String> getClosedClass() {
		return getInstance().closedClass;
	}

	/**Gets the term set from noSplitPrefixes.txt.
	 * 
	 * @return The term set.
	 */
	public static Set<String> getNoSplitPrefixes() {
		return getInstance().noSplitPrefixes;
	}	

	/**Gets the term set from chemAses.txt.
	 * 
	 * @return The term set.
	 */
	public static Set<String> getChemAses() {
		return getInstance().chemAses;
	}
	
	/**Gets the term set from nonChemAses.txt.
	 * 
	 * @return The term set.
	 */
	public static Set<String> getNonChemAses() {
		return getInstance().nonChemAses;
	}
	
	/**Gets the term set from elements.txt.
	 * 
	 * @return The term set.
	 */
	public static Set<String> getElements() {
		return getInstance().elements;
	}

	/**Gets the term set from ligands.txt.
	 * 
	 * @return The term set.
	 */
	public static Set<String> getLigands() {
		return getInstance().ligands;
	}	

	/**Gets the term set from reactWords.txt.
	 * 
	 * @return The term set.
	 */
	public static Set<String> getReactWords() {
		return getInstance().reactWords;
	}	
	
	/**Gets a regular expression that detects whether a word is ending in
	 * an element name. For example "trizinc" or "dialuminium".
	 * 
	 * @return The compiled regular expression.
	 */
	public static Pattern getEndingInElementPattern() {
		return getInstance().endingInElementPattern;
	}
	
	/**Makes some hashes of the Oscar3 term set resources, for traceability
	 * purposes.
	 * 
	 * @return A string detailing the various hashes.
	 */
	public static String makeHashes() {
		StringBuffer sb = new StringBuffer();
		sb.append("stopWords: " + StringTools.collectionToStableString(getStopWords()).hashCode() + "\n");
		sb.append("usrDictWords: " + StringTools.collectionToStableString(getUsrDictWords()).hashCode() + "\n");
		sb.append("noSplitPrefixes: " + StringTools.collectionToStableString(getNoSplitPrefixes()).hashCode() + "\n");
		sb.append("closedClass: " + StringTools.collectionToStableString(getClosedClass()).hashCode() + "\n");
		sb.append("chemAses: " + StringTools.collectionToStableString(getChemAses()).hashCode() + "\n");
		sb.append("nonChemAses: " + StringTools.collectionToStableString(getNonChemAses()).hashCode() + "\n");
		sb.append("elements: " + StringTools.collectionToStableString(getElements()).hashCode() + "\n");
		sb.append("ligands: " + StringTools.collectionToStableString(getLigands()).hashCode() + "\n");
		sb.append("reactWords: " + StringTools.collectionToStableString(getReactWords()).hashCode() + "\n");
		return sb.toString();
	}
	
	private TermSets() throws Exception {
		if(Oscar3Props.getInstance().verbose) System.out.print("Initialising term sets... ");
		stopWords = getTermSet("stopwords.txt");
		usrDictWords = getTermSet("usrDictWords.txt", false);
		noSplitPrefixes = getTermSet("noSplitPrefixes.txt");
		closedClass = getTermSet("closedClass.txt");
		chemAses = getTermSet("chemAses.txt");
		nonChemAses = getTermSet("nonChemAses.txt");
		elements = getTermSet("elements.txt");
		ligands = getTermSet("ligands.txt");
		reactWords = getTermSet("reactWords.txt");
		
		StringBuffer sb = new StringBuffer();
		sb.append(".+(");
		boolean first = true;
		for(String s : elements) {
			if(!s.matches("[a-z]+")) continue;
			if(first) {
				first = false;
			} else {
				sb.append("|");
			}
			sb.append(s);
		}
		sb.append(")");
		endingInElementPattern = Pattern.compile(sb.toString());
	
		if(Oscar3Props.getInstance().verbose) System.out.println("term sets initialised");
	}
	
}
