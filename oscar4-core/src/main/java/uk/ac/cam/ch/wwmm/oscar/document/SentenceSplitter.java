package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;
import uk.ac.cam.ch.wwmm.oscar.tools.IStandoffTable;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**Finds sentences in lists of tokens.
 *
 * @author ptc24
 *
 */
public final class SentenceSplitter {

    private static final SentenceSplitter SINGLETON_INSTANCE = new SentenceSplitter();

    private final Set<String> splitTokens;
    private final NonSentenceEndings nonSentenceEndings;

    private SentenceSplitter() {
        splitTokens = new HashSet<String>();
        splitTokens.add(".");
        splitTokens.add("?");
        splitTokens.add("!");
        splitTokens.add("\"");
        nonSentenceEndings = new NonSentenceEndings();
    }

    /**Splits a list of tokens into a list of sentences.
     *
     * @param tokens The list of tokens.
     * @return The list of list of tokens, corresponding to the sentences.
     */
    public static List<List<IToken>> makeSentences(List<IToken> tokens) {
        return SINGLETON_INSTANCE.makeSentencesInternal(tokens);
    }

    private List<List<IToken>> makeSentencesInternal(List<IToken> tokenList) {
        List<List<IToken>> sentenceList = new ArrayList<List<IToken>>();
        List<IToken> currentSentence = new ArrayList<IToken>();
        sentenceList.add(currentSentence);
        List<IToken> prevSentence = null;
        for (IToken token : tokenList) {
            IStandoffTable standoffTable = token.getDoc().getStandoffTable();
            if (currentSentence.isEmpty()
                    && isCitationReference(token, standoffTable)) {
                prevSentence.add(token);
            } else {
                currentSentence.add(token);
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
                currentSentence = new ArrayList<IToken>();
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
