package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;
import uk.ac.cam.ch.wwmm.oscar.tools.IStandoffTable;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**Finds sentences in lists of tokens.
 *
 * @author ptc24
 *
 */
public final class SentenceFinder {

    private static final SentenceFinder SINGLETON_INSTANCE = new SentenceFinder();

    private final SentenceSplitTokens splitTokens;
    private final NonSentenceEndings nonSentenceEndings;

    private SentenceFinder() {
        splitTokens = new SentenceSplitTokens();
        nonSentenceEndings = new NonSentenceEndings();
    }

    /**Splits a list of tokens into a list of sentences.
     *
     * @param tokens The list of tokens.
     * @return The list of list of tokens, corresponding to the sentences.
     */
    public static List<Sentence> makeSentences(List<IToken> tokens) {
        return SINGLETON_INSTANCE.makeSentencesInternal(tokens);
    }

    private List<Sentence> makeSentencesInternal(List<IToken> tokenList) {
        List<Sentence> sentenceList = new ArrayList<Sentence>();
        Sentence currentSentence = new Sentence();
        sentenceList.add(currentSentence);
        Sentence prevSentence = null;
        for (IToken token : tokenList) {
            IStandoffTable standoffTable = token.getDoc().getStandoffTable();
            if (currentSentence.isEmpty()
                    && isCitationReference(token, standoffTable)) {
                prevSentence.addToken(token);
            } else {
                currentSentence.addToken(token);
            }
            boolean split = false;
            String value = token.getValue();
            if (splitTokens.contains(value)) {
                split = true;
                IToken next = token.getNAfter(1);
                IToken prev = token.getNAfter(-1);
                if (next != null && prev != null) {
                    String nextStr = next.getValue();
                    String prevStr = prev.getValue();
                    if ("\"".equals(value) && !splitTokens.contains(prevStr)) {
                        split = false;
                    } else if (nonSentenceEndings.contains(prevStr)) {
                        split = false;
                    } else if (splitTokens.contains(nextStr)) {
                        split = false;
                    } else if (token.getEnd() == next.getStart() &&
                            isCitationReference(next, standoffTable)) {
                        split = false;
                    } else if (StringTools.isLowerCaseWord(nextStr)) {
                        split = false;
                    }
                }
            }
            if (split) {
                prevSentence = currentSentence;
                currentSentence = new Sentence();
                sentenceList.add(currentSentence);
            }
        }
        if (currentSentence.isEmpty()) {
            sentenceList.remove(currentSentence);
        }
        return sentenceList;
    }

    private Boolean isCitationReference(IToken token, IStandoffTable standoffTable) {
        return XMLStrings.getInstance().isCitationReferenceUnderStyle(standoffTable.getElemAtOffset(token.getStart()));
    }

}
