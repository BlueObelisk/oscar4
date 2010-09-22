package uk.ac.cam.ch.wwmm.oscarpattern.document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscarpattern.scixml.XMLStrings;

/**Finds sentences in lists of tokens.
 * 
 * @author ptc24
 *
 */
public final class SentenceSplitter {

	private Set<String> splitTokens;
	private Set<String> impossibleBefore;
	private Set<String> impossibleAfter;
	
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
		impossibleAfter = new HashSet<String>();
		impossibleAfter.add("Fig");
		impossibleAfter.add("al"); // et al.
		impossibleAfter.add("i.e");
		impossibleAfter.add("ie");
		impossibleAfter.add("eg");
		impossibleAfter.add("e.g");
		impossibleAfter.add("ref");
		impossibleAfter.add("Dr");
		impossibleAfter.add("Prof");
		impossibleAfter.add("Sir");	
	}
	
	/**Gets the string corresponding to the sentence. This works by looking
	 * at the underlying text, and getting the part of it that corresponds to
	 * the span between the start of the first token and the end of the last
	 * token. An empty sentence gives an empty string.
	 * 
	 * @param sentence The list of tokens comprising the sentence.
	 * @return The string corresponding to the sentence.
	 */
	public static String sentenceString(List<Token> sentence) {
		if(sentence.size() == 0) return "";
		Token startTok = sentence.get(0);
		Token endTok = sentence.get(sentence.size()-1);
		TokenSequence sentenceTokSeq = startTok.tokenSequence;
		int offset = sentenceTokSeq.getOffset();
		return sentenceTokSeq.getSourceString().substring(startTok.start - offset, endTok.end - offset);
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
					XMLStrings.getInstance().isCitationReferenceUnderStyle(t.doc.standoffTable.getElemAtOffset(t.start))
					&& prevSentence != null) {
				prevSentence.add(t);
			} else {
				sentence.add(t);				
			}
			boolean split = false;
			String value = t.value;
			if(verbose) System.out.println(value);
			if(splitTokens.contains(value)) {
				split = true;
				Token next = t.getNAfter(1);
				Token prev = t.getNAfter(-1);
				if(next != null && prev != null) {
					String nextStr = next.value;
					String prevStr = prev.value;
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
					} else if(t.end == next.start && XMLStrings.getInstance().isCitationReferenceUnderStyle(next.doc.standoffTable.getElemAtOffset(next.start))) {
						if(verbose) System.out.println("E!");
						split = false;
					//} else if(prevStr.matches("[A-Z]")) {
					//	if(verbose) System.out.println("A!");
					//	split = false;
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
