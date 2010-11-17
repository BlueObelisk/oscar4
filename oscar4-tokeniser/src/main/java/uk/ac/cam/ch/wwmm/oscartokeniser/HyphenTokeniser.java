package uk.ac.cam.ch.wwmm.oscartokeniser;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.oscar.obo.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar.terms.TermSets;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/** Looks for places where tokens can be split on hyphens. Needs to be a separate class
 * because it needs some word lists.
 * 
 * Strategy: look for hyphens, from the rear backwards, leaving two characters padding
 * on the front and rear of the token.
 * Sequentially apply a series of rules:
 * 
 * 1) If what's after the hyphen is on the splitSuffixes list, split!
 * 2) If the letters before the hyphen are on the noSplitPrefixes list, don't split!
 * 3) If the two characters in front of the hyphen are lowercase, and all characters after, split!
 * 4) Don't split!
 */
public final class HyphenTokeniser {
	
	private static HyphenTokeniser myInstance = null;
	private Set<String> splitSuffixes;
	private Set<String> noSplitPrefixes;
	private int minPrefixLength;
	private int maxPrefixLength;
	private boolean splitOnEnDash;
	private static Pattern suffixPrefixPattern = Pattern.compile("mono|di|tri|tetra|penta|hexa|hepta|un|de|re|pre");
	private static Pattern propernounHyphenPattern = Pattern.compile("((Mc|Mac)?[A-Z]\\p{Ll}\\p{Ll}\\p{Ll}+(s'|'s)?" + StringTools.hyphensRegex + ")+(Mc|Mac)?[A-Z]\\p{Ll}\\p{Ll}\\p{Ll}+(s'|'s)?");
	
	/**Re-initialises the Hyphen tokeniser.
	 * 
	 * @throws Exception
	 */
	public static void reinitialise() throws Exception {
		myInstance = null;
		getInstance();
	}
	
	private static HyphenTokeniser getInstance() {
		try {
			if(myInstance == null) myInstance = new HyphenTokeniser();
		} catch (Exception e) {
			throw new Error(e);
		}
		return myInstance;
	}
	
	private HyphenTokeniser() throws Exception {
		Logger logger = Logger.getLogger(HyphenTokeniser.class);
		logger.debug("Initialising hyphen tokeniser... ");
		//splitSuffixes = TermSets.getSplitSuffixes();
		splitSuffixes = new HashSet<String>();
		splitSuffixes.addAll(TermSets.getSplitSuffixes());
		
		/*for(String s : TermSets.getUsrDictWords()) {
			if(!ChemNameDictSingleton.hasName(s) && !ExtractTrainingData.getIntstance().chemicalWords.contains(s)) {
				splitSuffixes.add(s);
			}
		}*/
		
		noSplitPrefixes = TermSets.getNoSplitPrefixes();
		
		minPrefixLength = 1000;
		maxPrefixLength = 0;
		for(String p : noSplitPrefixes) {
			minPrefixLength = Math.min(minPrefixLength, p.length());
			maxPrefixLength = Math.max(maxPrefixLength, p.length());
		}

		splitOnEnDash = OscarProperties.getData().splitOnEnDash;
		logger.debug("hyphen tokeniser initialised");
	}
	
	/** Initialises the singleton associated with this class. For convenience at startup.
	 */
	public static void init() {
		getInstance();
	}
	
	/**Works out where, if anywhere, to split a string which may contain a hyphen
	 * 
	 * @param s The string to analyse
	 * @return The index of the hyphen to split at, or -1
	 */
	public static int indexOfSplittableHyphen(String s) {
		try {
			return getInstance().indexOfSplittableHyphenInternal(s);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	private int indexOfSplittableHyphenInternal(String tokenValue) {
		//TODO break each tokenisation case into a separate method and unit test
		boolean balancedBrackets = StringTools.bracketsAreBalanced(tokenValue);
		for(int currentIndex=tokenValue.length()-3;currentIndex>0;currentIndex--) {
			/* Are we looking at a hyphen? */
			if(StringTools.hyphens.indexOf(tokenValue.codePointAt(currentIndex)) != -1) {
				
				//Don't split on tokens contained within brackets
				if(balancedBrackets && !StringTools.bracketsAreBalanced(tokenValue.substring(currentIndex+1))) continue;
				
				// Split on en-dashes?
				if(splitOnEnDash && tokenValue.substring(currentIndex, currentIndex+1).equals(StringTools.enDash)) return currentIndex;
				
				// Always split on em-dashes
				if(tokenValue.substring(currentIndex, currentIndex+1).equals(StringTools.emDash)) return currentIndex;				
				
				if(OntologyTerms.getHyphTokable().contains(StringTools.normaliseName(tokenValue.substring(0,currentIndex)) + " " + 
						StringTools.normaliseName(tokenValue.substring(currentIndex+1)))) {
					return currentIndex;
				}
				// Suffixes?
				String suffix = tokenValue.substring(currentIndex+1).toLowerCase();
				if(splitSuffixes.contains(suffix)) {
					return currentIndex;
				}
				
				Matcher m = propernounHyphenPattern.matcher(tokenValue);
				if(m.matches()) {
					return currentIndex;
				}
				
				m = suffixPrefixPattern.matcher(suffix); 
				if(m.lookingAt()) {
					//the suffix string is what remains of the string when certain
					//prefixes (defined in suffixPrefixPattern) are removed
					suffix = suffix.substring(m.end());
				}
				while(suffix.length() >= 3) {
					//we check to see if any of the splitSuffixes follow an
					//identified prefix
					if(splitSuffixes.contains(suffix)) {
						return currentIndex;
					}
					suffix = suffix.substring(0, suffix.length()-1);
				}
				
				// No suffix? Then don't examine hyphens in position 1
				if(currentIndex == 1) continue;
				
				// Prefixes
				// check to see if the word contains one of the noSplitPrefixes. If
				// it does, don't tokenise.
				boolean noSplit = false;
				for(int j=minPrefixLength;j<=maxPrefixLength && j<=currentIndex;j++) {
					String prefix = tokenValue.substring(currentIndex-j, currentIndex).toLowerCase();
					if(noSplitPrefixes.contains(prefix)) {
						noSplit = true;
						break;
					}
				}
				if(noSplit) {
					continue;
				}
				
				/* Check for lowercase either side of the token */
				if(tokenValue.substring(currentIndex-2).matches("[a-z][a-z]["+StringTools.hyphens+"][a-z]+")) {
					return currentIndex;
				}
			}
		}
		return -1;
	}
	
}
