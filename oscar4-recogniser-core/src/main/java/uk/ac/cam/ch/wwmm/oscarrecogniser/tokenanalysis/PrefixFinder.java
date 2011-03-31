package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;

import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata.ExtractedTrainingData;

/**
 * 
 * @author ptc24
 * @author dmj30
 *
 */
public class PrefixFinder {
	private static String primesRegex = "[" + StringTools.primes + "]*";
	private static String locantRegex = "(\\d+" + primesRegex + "[RSEZDLH]?|" +
	"\\(([RSEZDLH\u00b1]|\\+|" + StringTools.hyphensRegex + ")\\)|" +
		"[DLRSEZ]|" +	//as in "D/L", "R/S" and "E/Z"
			"([CNOS]|Se)\\d*|" +
			"\\d*[" + StringTools.lowerGreek + "]|" +
	"cis|trans|o(rtho)?|m(eta)?|p(ara)?|asym|sym|sec|tert|catena|closo|enantio|ent|endo|exo|" +
	"fac|mer|gluco|nido|aci|erythro|threo|arachno|meso|syn|anti|tele|cine" +
	")" + primesRegex;
	private static String prefixRegex = "(" + locantRegex + "(," + locantRegex + ")*)";
	
	public static Pattern prefixPattern = Pattern.compile(prefixRegex + 
					"[" + StringTools.hyphens + "](\\S*)");
	
	public static Pattern prefixBody = Pattern.compile(prefixRegex);

	
	/**
	 * Determines the chemical prefix at the start of the specified word
	 * 
	 * @param s the word to be checked for a prefix
	 * @param etd
	 * @return the String corresponding to the chemical prefix or null
	 * if none was found
	 */
	public static String getPrefix(String s, ExtractedTrainingData etd) {
		if(prefixPattern.matcher(s).matches()) {
			int idx = findIndexOfHyphen(s);
			// Check if it's a not-splitting word
			if (etd != null) {
				if(etd.getNotForPrefix().contains(s.substring(idx+1))) {
					return null;
				}
			}
			if(idx == 0) {
				return null;
			}	
			return s.substring(0, idx+1);
		}
		return null;		
	}

	private static int findIndexOfHyphen(String s) {
		for (int i = 0; i < StringTools.hyphens.length(); i++) {
			int idx = s.indexOf(StringTools.hyphens.charAt(i));
			if (idx != -1) {
				return idx;
			}
		}
		return -1;
	}

	//TODO redirect callers to getPrefix(String, ExtractedTrainingData)
	public static String getPrefix(String string) {
		return getPrefix(string, null);
	}

}
