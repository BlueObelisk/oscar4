package uk.ac.cam.ch.wwmm.oscartokeniser;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.ont.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar.terms.TermSets;
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
	
	private static final Logger LOG = LoggerFactory.getLogger(HyphenTokeniser.class);
	
	private static HyphenTokeniser myInstance = null;
	private Set<String> splitSuffixes;
	private Set<String> noSplitPrefixes;
	//splitOnEnDash will become redundant with the normalisation of hyphens
	private static Pattern suffixPrefixPattern = Pattern.compile("mono|di|tri|tetra|penta|hexa|hepta|un|de|re|pre");
	static Pattern propernounHyphenPattern = Pattern.compile("((Mc|Mac)?[A-Z]\\p{Ll}\\p{Ll}\\p{Ll}+(s'|'s)?" + StringTools.hyphensRegex + ")+(Mc|Mac)?[A-Z]\\p{Ll}\\p{Ll}\\p{Ll}+(s'|'s)?");
	private static Pattern lowercaseEitherSidePattern = Pattern.compile("[a-z][a-z]["+StringTools.hyphens+"][a-z]+");
	
	/**Re-initialises the Hyphen tokeniser.
	 * 
	 */
	public static void reinitialise() {
		myInstance = null;
		getInstance();
	}
	
	static HyphenTokeniser getInstance() {
		if (myInstance == null) {
			myInstance = new HyphenTokeniser();
		}
		return myInstance;
	}
	
	private HyphenTokeniser() {
		LOG.debug("Initialising hyphen tokeniser... ");
		splitSuffixes = new HashSet<String>();
		splitSuffixes.addAll(TermSets.getDefaultInstance().getSplitSuffixes());
		noSplitPrefixes = TermSets.getDefaultInstance().getNoSplitPrefixes();
		LOG.debug("hyphen tokeniser initialised");
	}
	
	
	/**Works out where, if anywhere, to split a string which may contain a hyphen
	 * 
	 * @param tokenValue The string to analyse
	 * @return The index of the hyphen to split at, or -1
	 */
	public static int indexOfSplittableHyphen(String tokenValue) {
		return getInstance().indexOfSplittableHyphenInternal(tokenValue);
	}
	
	private int indexOfSplittableHyphenInternal(String tokenValue) {
		//TODO break each tokenisation case into a separate method and unit test
		boolean balancedBrackets = StringTools.bracketsAreBalanced(tokenValue);
		
		//read from the back of the string, keeping at least two characters after the hyphen
		for(int currentIndex=tokenValue.length()-3; currentIndex > 0; currentIndex--) {
			/* Are we looking at a hyphen? */
			if(StringTools.hyphens.indexOf(tokenValue.codePointAt(currentIndex)) != -1) {
				//Don't split on hyphens contained within brackets
				if(balancedBrackets && !StringTools.bracketsAreBalanced(tokenValue.substring(currentIndex+1))) {
					continue;
				}
				
				//the en-dash and em-dash rules will become redundant with string normalisation
				
				//split on en-dash
				if(checkEnDash(tokenValue, currentIndex)) { 
					return currentIndex;
				}
				
				// Always split on em-dashes
				if (checkEmDash(tokenValue, currentIndex)) {
					return currentIndex;				
				}
				
				// Suffixes
				if (suffixContainedInSplitSuffix(tokenValue, currentIndex)) {
					return currentIndex;
				}
				
				if (suffixStartsWithSplitSuffix(tokenValue, currentIndex)) {
					return currentIndex;
				}
				
				// Don't split to give a 1 character prefix
				if(currentIndex == 1) {
					continue;
				}
				
				// Prefixes
				if (precededByNoSplitPrefix(tokenValue, currentIndex)) {
					continue;
				}
				
				if (termMatchesPropernounPattern(tokenValue)) {
					return currentIndex;
				}

				// Check against OntologyTerms content, if token is present with a space where the hyphen is then split
				// Due to the broadness of the lowercaseEitherSideOfHyphen rule and termMatchesPropernounPattern rules it is difficult to find cases where this rule is actually required
				if (termContainedInHyphTokable(tokenValue, currentIndex)) {
					return currentIndex;
				}
				
				/* Check for lowercase either side of the token */
				if (lowercaseEitherSideOfHyphen(tokenValue, currentIndex)) {
					return currentIndex;
				}
			}
		}
		return -1;
	}


	/**
	 * checks if the substring of the term starting two characters before the hyphen
	 * matches the lowercaseEitherSidePattern, i.e.
	 * 
	 *  lowercase lowercase hyphen (lowercase)+ 
	 */
	boolean lowercaseEitherSideOfHyphen(String tokenValue, int currentIndex) {
		Matcher matcher = lowercaseEitherSidePattern.matcher(tokenValue.substring(currentIndex-2));
		return matcher.matches();
	}

	/**
	 * Checks if the string before the hyphen up to the beginning of the string or a non-letter
	 * is described by the set of noSplitPrefixes
	 * @param tokenValue
	 * @param currentIndex
	 * @return
	 */
	private boolean precededByNoSplitPrefix(String tokenValue, int currentIndex) {
		int startingIndex = 0;
		for (int i = currentIndex -1; i >=0; i--) {
			if (!Character.isLetter(tokenValue.charAt(i))){
				startingIndex = i +1 ;
				break;
			}
		}
		String prefix = tokenValue.substring(startingIndex, currentIndex).toLowerCase();
		for (String noSplitPrefix : noSplitPrefixes) {
			if (noSplitPrefix.startsWith("*") && prefix.endsWith(noSplitPrefix.substring(1))){
				return true;
			}
			else if (noSplitPrefix.equals(prefix)){
				return true;
			}
		}
		return false;
	}

	/**
	 * checks if one of the terms from the splitSuffix list occurs after the hyphen 
	 */
	boolean suffixStartsWithSplitSuffix(String tokenValue, int currentIndex) {
		String suffix = tokenValue.substring(currentIndex+1).toLowerCase();
		Matcher m = suffixPrefixPattern.matcher(suffix); 
		//strip certain strings from the beginning of the suffix
		if(m.lookingAt()) {
			suffix = suffix.substring(m.end());
		}

		if(suffix.length() >= 3) {
			if(splitSuffixes.contains(suffix) || 
				(suffix.length() >= 4 && splitSuffixes.contains(suffix.substring(0, suffix.length()-1)))){
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Checks if the term matches the properNounHyphenPattern  
	 */
	boolean termMatchesPropernounPattern(String tokenValue) {
		Matcher m = propernounHyphenPattern.matcher(tokenValue);
		return m.matches();
	}

	/**
	 * Checks if the portion of the string occurring after the hyphen is
	 * contained in the split suffixes list. 
	 */
	boolean suffixContainedInSplitSuffix(String tokenValue, int currentIndex) {
		String suffix = tokenValue.substring(currentIndex+1).toLowerCase();
		return splitSuffixes.contains(suffix);
	}

	/**
	 * Checks if the OntologyTerms contains an equivalent term to the tokenValue
	 * in which the hyphen at the currentIndex has been replaced by a space
	 */
	boolean termContainedInHyphTokable(String tokenValue, int currentIndex) {
		StringBuilder builder = new StringBuilder(tokenValue.length());
		builder.append(StringTools.normaliseName(tokenValue.substring(0,currentIndex)));
		builder.append(" ");
		builder.append(StringTools.normaliseName(tokenValue.substring(currentIndex+1)));
		
		return OntologyTerms.getDefaultInstance().getHyphTokable().contains(builder.toString());
	}

	/**
	 * Checks if the character at the currentIndex is em-dash
	 */
	private boolean checkEmDash(String tokenValue, int currentIndex) {
		return tokenValue.substring(currentIndex, currentIndex+1).equals(StringTools.emDash);
	}

	/**
	 * Checks if the character at the currentIndex is en-dash 
	 * 
	 */
	private boolean checkEnDash(String tokenValue, int currentIndex) {
		return tokenValue.substring(currentIndex, currentIndex+1).equals(StringTools.enDash);
	}
	
}
