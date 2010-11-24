package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;

/**Finds sentences in lists of tokens.
 * 
 * @author ptc24
 *
 */
public final class SentenceSplitter {

	private Set<String> splitTokens;
	private Set<String> impossibleBefore;
	private NonSentenceEndings impossibleAfter;
	
	private boolean verbose = false;
	
	
	private static SentenceSplitter myInstance;
	
	private static SentenceSplitter getInstance() {
		if(myInstance == null) return new SentenceSplitter();
		return myInstance;
	}
	
	private SentenceSplitter() {
		splitTokens = new HashSet<String>();
		splitTokens.add(".");
		splitTokens.add("?");
		splitTokens.add("!");
		splitTokens.add("\"");
		impossibleBefore = new HashSet<String>();
		impossibleAfter = new NonSentenceEndings();
	}
	

	
	/**Splits a list of tokens into a list of sentences.
	 * 
	 * @param tokens The list of tokens.
	 * @return The list of list of tokens, corresponding to the sentences.
	 */
	public static List<List<Token>> makeSentences(List<Token> tokens) {
		return getInstance().makeSentencesInternal(tokens);
	}
	
	private List<List<Token>> makeSentencesInternal(List<Token> tokens) {
		List<List<Token>> sentences = new ArrayList<List<Token>>();
		List<Token> sentence = new ArrayList<Token>();
		sentences.add(sentence);
		List<Token> prevSentence = null;
		for(Token t : tokens) {
			if(sentence.size() == 0 && 
					XMLStrings.getInstance().isCitationReferenceUnderStyle(
						t.getDoc().getStandoffTable().getElemAtOffset(
							t.getStart()
						)
					)
					&& prevSentence != null) {
				prevSentence.add(t);
			} else {
				sentence.add(t);				
			}
			boolean split = false;
			String value = t.getValue();
			if(verbose) System.out.println(value);
			if(splitTokens.contains(value)) {
				split = true;
				IToken next = t.getNAfter(1);
				IToken prev = t.getNAfter(-1);
				if(next != null && prev != null) {
					String nextStr = next.getValue();
					String prevStr = prev.getValue();
					if("\"".equals(value) && !splitTokens.contains(prevStr)) {
						if(verbose) System.out.println("A!");
						split = false;
					} else if(impossibleAfter.contains(prevStr)) {
						if(verbose) System.out.println("B!");
						split = false;
					} else if(impossibleBefore.contains(nextStr)) {
						if(verbose) System.out.println("C!");
						split = false;
					} else if(splitTokens.contains(nextStr)) {
						if(verbose) System.out.println("D!");
						split = false;
					} else if(t.getEnd() == next.getStart() &&
							XMLStrings.getInstance().isCitationReferenceUnderStyle(
								next.getDoc().getStandoffTable().getElemAtOffset(
									next.getStart()
								)
							)) {
						if(verbose) System.out.println("E!");
						split = false;
					} else if(nextStr.matches("[a-z]+")) {
						if(verbose) System.out.println("F!");
						split = false;
					}
				} 
			}
			if(split) {
				prevSentence = sentence;
				sentence = new ArrayList<Token>();
				sentences.add(sentence);
			}
		}
		if(sentence.size() == 0) sentences.remove(sentence);
		return sentences;
	}
	
}
