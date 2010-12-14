package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;
import uk.ac.cam.ch.wwmm.oscar.tools.IStandoffTable;
import uk.ac.cam.ch.wwmm.oscar.tools.StandoffTable;

/**Finds sentences in lists of tokens.
 *
 * @author ptc24
 *
 */
public final class SentenceSplitter {

    private Set<String> splitTokens;
    private NonSentenceEndings impossibleAfter;


    private static SentenceSplitter defaultInstance;

    private static synchronized SentenceSplitter getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new SentenceSplitter();
        }
        return defaultInstance;
    }

    private SentenceSplitter() {
        splitTokens = new HashSet<String>();
        splitTokens.add(".");
        splitTokens.add("?");
        splitTokens.add("!");
        splitTokens.add("\"");
        impossibleAfter = new NonSentenceEndings();
    }



    /**Splits a list of tokens into a list of sentences.
     *
     * @param tokens The list of tokens.
     * @return The list of list of tokens, corresponding to the sentences.
     */
    public static List<List<IToken>> makeSentences(List<IToken> tokens) {
        return getDefaultInstance().makeSentencesInternal(tokens);
    }

    private List<List<IToken>> makeSentencesInternal(List<IToken> tokens) {
        List<List<IToken>> sentences = new ArrayList<List<IToken>>();
        List<IToken> sentence = new ArrayList<IToken>();
        sentences.add(sentence);
        List<IToken> prevSentence = null;
        for(IToken t : tokens) {
            IStandoffTable sot = t.getDoc().getStandoffTable();
            if (sentence.isEmpty()
                    && sot instanceof StandoffTable
                    && XMLStrings.getInstance().isCitationReferenceUnderStyle(sot.getElemAtOffset(t.getStart()))) {
                prevSentence.add(t);
            } else {
                sentence.add(t);
            }
            boolean split = false;
            String value = t.getValue();
            if (splitTokens.contains(value)) {
                split = true;
                IToken next = t.getNAfter(1);
                IToken prev = t.getNAfter(-1);
                if (next != null && prev != null) {
                    String nextStr = next.getValue();
                    String prevStr = prev.getValue();
                    if ("\"".equals(value) && !splitTokens.contains(prevStr)) {
                        split = false;
                    } else if (impossibleAfter.contains(prevStr)) {
                        split = false;
                    } else if (splitTokens.contains(nextStr)) {
                        split = false;
                    } else if(t.getEnd() == next.getStart() &&
                            sot instanceof StandoffTable &&
                            XMLStrings.getInstance().isCitationReferenceUnderStyle(
                                    ((StandoffTable)sot).getElemAtOffset(
                                            next.getStart()
                                    )
                            )) {
                        split = false;
                    } else if (nextStr.matches("[a-z]+")) {
                        split = false;
                    }
                }
            }
            if (split) {
                prevSentence = sentence;
                sentence = new ArrayList<IToken>();
                sentences.add(sentence);
            }
        }
        if (sentence.isEmpty()) {
            sentences.remove(sentence);
        }
        return sentences;
    }

}
