package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

import nu.xom.Element;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

/**A tokenised representation of a piece of text, as made by the Tokeniser
 * class.
 *
 * @author ptc24
 *
 */
public final class TokenSequence implements ITokenSequence {

    private String surface;
    private int offset;
    private IProcessingDocument doc;
    private List<IToken> tokens;
    private Element elem;

    public TokenSequence(String sourceString, int offset, IProcessingDocument doc, List<IToken> tokens) {
        this.surface = sourceString;
        this.offset = offset;
        this.doc = doc;
        this.tokens = tokens;
    }

    /* (non-Javadoc)
      * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getSourceString()
      */
    public String getSurface() {
        return surface;
    }

    /* (non-Javadoc)
      * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getDoc()
      */
    public IProcessingDocument getDoc() {
        return doc;
    }

    /* (non-Javadoc)
      * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getOffset()
      */
    public int getOffset() {
        return offset;
    }

    /* (non-Javadoc)
      * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getTokens()
      */
    public List<IToken> getTokens() {
        return tokens;
    }

    /* (non-Javadoc)
      * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getTokens(int, int)
      */
    public List<IToken> getTokens(int from, int to) {
        return tokens.subList(from, to+1);
    }

    /* (non-Javadoc)
      * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getToken(int)
      */
    public IToken getToken(int i) {
        return tokens.get(i);
    }

    public void setElem(Element elem) {
        this.elem = elem;
    }

    /**Gets the XML element (or null) containing the named entity information
     * that was used during tokenisation.
     *
     * @return The XML element.
     */
    public Element getElem() {
        return elem;
    }

    /* (non-Javadoc)
      * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#size()
      */
    public int size() {
        return tokens.size();
    }

    /* (non-Javadoc)
      * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getTokenStringList()
      */
    public List<String> getTokenStringList() {
        List<String> tl = new ArrayList<String>();
        for(IToken t : tokens) {
            tl.add(t.getValue());
        }
        return tl;
    }

    /* (non-Javadoc)
      * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getSubstring(int, int)
      */
    public String getSubstring(int startToken, int endToken) {
        if (endToken >= size()) {
            endToken = size() - 1;
        }
        int startOffset = tokens.get(startToken).getStart();
        int endOffset = tokens.get(endToken).getEnd();
        return surface.substring(startOffset - offset, endOffset - offset);
    }

    /* (non-Javadoc)
      * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getStringAtOffsets(int, int)
      */
    public String getStringAtOffsets(int start, int end) {
        return surface.substring(start - offset, end - offset);
    }

    /* (non-Javadoc)
      * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getAfterHyphens()
      */
    public Set<String> getAfterHyphens() {
        Set<String> afterHyphens = new HashSet<String>();
        for (int i = 1; i < tokens.size(); i++) {
            if (i < tokens.size()-1
                    && tokens.get(i).getValue().length() == 1
                    && StringTools.isHyphen(tokens.get(i).getValue())
                    && "O".equals(tokens.get(i).getBioTag())
                    && "O".equals(tokens.get(i+1).getBioTag())
                    && !"O".equals(tokens.get(i-1).getBioTag())
                    && tokens.get(i).getStart() == tokens.get(i-1).getEnd()
                    && tokens.get(i).getEnd() == tokens.get(i+1).getStart()
                    ) {
                afterHyphens.add(tokens.get(i+1).getValue());
            } else if ("O".equals(tokens.get(i).getBioTag())
                    && "B-CPR".equals(tokens.get(i-1).getBioTag())
                    && tokens.get(i).getStart() == tokens.get(i-1).getEnd()
                    ) {
                afterHyphens.add(tokens.get(i).getValue());
            }
        }
        return afterHyphens;
    }

    /* (non-Javadoc)
      * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getNes()
      */
    public Map<NamedEntityType,List<List<String>>> getNes() {
    	
        Map<NamedEntityType,List<List<String>>> neMap = new HashMap<NamedEntityType,List<List<String>>>();
        NamedEntityType namedEntityType = null;
        List<String> neTokens = null;
        for (IToken t : tokens) {
            if (namedEntityType == null) {
                if (!"O".equals(t.getBioTag())) {
                    neTokens = new ArrayList<String>();
                    // Trim of the B- in the BIO tag
                    namedEntityType = NamedEntityType.valueOf(t.getBioTag().substring(2));
                    neTokens.add(t.getValue());
                    if (!neMap.containsKey(namedEntityType)) {
                        neMap.put(namedEntityType, new ArrayList<List<String>>());
                    }
                    neMap.get(namedEntityType).add(neTokens);
                }
            } else {
                if ("O".equals(t.getBioTag())) {
                    namedEntityType = null;
                    neTokens = null;
                } else if (t.getBioTag().startsWith("B-")) {
                    neTokens = new ArrayList<String>();
                    // Trim of the B- in the BIO tag
                    namedEntityType = NamedEntityType.valueOf(t.getBioTag().substring(2));
                    neTokens.add(t.getValue());
                    if (!neMap.containsKey(namedEntityType)) {
                        neMap.put(namedEntityType, new ArrayList<List<String>>());
                    }
                    neMap.get(namedEntityType).add(neTokens);
                    // Must be I- something then
                } else {
                    neTokens.add(t.getValue());
                }
            }
        }
        return neMap;
    }

    /* (non-Javadoc)
      * @see uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence#getNonNes()
      */
    public List<String> getNonNes() {
        List<String> nonNes = new ArrayList<String>();
        for (IToken token : tokens) {
            if ("O".equals(token.getBioTag())) {
                nonNes.add(token.getValue());
            }
        }
        return nonNes;
    }

}
