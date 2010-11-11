package uk.ac.cam.ch.wwmm.oscar3.recogniser.document;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar3.Oscar3Props;
import uk.ac.cam.ch.wwmm.oscar3.models.ExtractTrainingData;
import uk.ac.cam.ch.wwmm.oscar3.terms.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar3.terms.TermSets;
import uk.ac.cam.ch.wwmm.ptclib.string.StringTools;

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
	private static Pattern pnHyphPattern = Pattern.compile("((Mc|Mac)?[A-Z]\\p{Ll}\\p{Ll}\\p{Ll}+(s'|'s)?" + StringTools.hyphensRe + ")+(Mc|Mac)?[A-Z]\\p{Ll}\\p{Ll}\\p{Ll}+(s'|'s)?");
	
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
		if(Oscar3Props.getInstance().verbose) System.out.print("Initialising hyphen tokeniser... ");
		//splitSuffixes = TermSets.getSplitSuffixes();
		splitSuffixes = new HashSet<String>();
		splitSuffixes.addAll(ExtractTrainingData.getInstance().afterHyphen);
		
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

		splitOnEnDash = Oscar3Props.getInstance().splitOnEnDash;
		if(Oscar3Props.getInstance().verbose) System.out.println("hyphen tokeniser initialised");
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
	
	private int indexOfSplittableHyphenInternal(String s) {
		boolean balancedBrackets = StringTools.bracketsAreBalanced(s);
		for(int i=s.length()-3;i>0;i--) {
			/* Are we looking at a hyphen? */
			if(StringTools.hyphens.indexOf(s.codePointAt(i)) != -1) {
				//boolean wouldTok = scoreTok(s, i);
				
				//Don't split on tokens contained within brackets
				if(balancedBrackets && !StringTools.bracketsAreBalanced(s.substring(i+1))) continue;
				
				// Split on en-dashes?
				if(splitOnEnDash && s.substring(i, i+1).equals(StringTools.enDash)) return i;
				
				// Always split on em-dashes
				if(s.substring(i, i+1).equals(StringTools.emDash)) return i;				
				
				if(OntologyTerms.getHyphTokable().contains(StringTools.normaliseName(s.substring(0,i)) + " " + 
						StringTools.normaliseName(s.substring(i+1)))) {
					return i;
				}
				// Suffixes?
				String suffix = s.substring(i+1).toLowerCase();
				if(splitSuffixes.contains(suffix)) {
					//if(!wouldTok) System.out.printf("%s %d\n", s, i);
					return i;
				}
				
				Matcher m = pnHyphPattern.matcher(s);
				if(m.matches()) {
					return i;
				}
				
				m = suffixPrefixPattern.matcher(suffix); 
				if(m.lookingAt()) {
					suffix = suffix.substring(m.end());
				}
				while(suffix.length() >= 3) {
					if(splitSuffixes.contains(suffix)) {
						//if(!wouldTok) System.out.printf("%s %d\n", s, i);
						return i;
					}
					suffix = suffix.substring(0, suffix.length()-1);
				}
				
				// No suffix? Then don't examine hyphens in position 1
				if(i == 1) continue;
				
				// Prefixes
				boolean noSplit = false;
				for(int j=minPrefixLength;j<=maxPrefixLength && j<=i;j++) {
					String prefix = s.substring(i-j, i).toLowerCase();
					if(noSplitPrefixes.contains(prefix)) {
						noSplit = true;
						//if(wouldTok) System.out.printf("* %s %d\n", s, i);
						break;
					}
				}
				if(noSplit) {
					//if(wouldTok) System.out.printf("* %s %d\n", s, i);
					continue;
				}
				
				/* Check for lowercase either side of the token */
				if(s.substring(i-2).matches("[a-z][a-z]["+StringTools.hyphens+"][a-z]+")) {
					//if(!wouldTok) System.out.printf("%s %d\n", s, i);
					return i;
				}
				//if(wouldTok) System.out.printf("* %s %d\n", s, i);
			}
		}
		return -1;
	}
	
}
