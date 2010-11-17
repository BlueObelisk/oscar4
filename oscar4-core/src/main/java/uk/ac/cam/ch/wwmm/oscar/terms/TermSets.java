package uk.ac.cam.ch.wwmm.oscar.terms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/** A data class to hold several lists of words used in the name recogniser.
 * 
 * @author ptc24
 *
 */
public final class TermSets {

	private final Logger logger = Logger.getLogger(TermSets.class);

	private static TermSets myInstance;
	
	private Set<String> stopWords;
	private Set<String> usrDictWords;
	private Set<String> closedClass;
	private Set<String> chemAses;
	private Set<String> nonChemAses;
	private Set<String> noSplitPrefixes;
	private Set<String> splitSuffixes;
	private Set<String> elements;
	private Set<String> ligands;
	private Set<String> reactWords;
	private Pattern endingInElementPattern;

	
	private static ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar/terms/");

	
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
	
	public static Set<String> getSplitSuffixes() {
		return getInstance().splitSuffixes;
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
	
		
	private TermSets() throws Exception {
		logger.debug("Initialising term sets... ");
		stopWords = getTermSet("stopwords.txt");
		usrDictWords = getTermSet("usrDictWords.txt", false);
		noSplitPrefixes = getTermSet("noSplitPrefixes.txt");
		splitSuffixes = getTermSet("splitSuffixes.txt");
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
	
		logger.debug("term sets initialised");
	}

}
