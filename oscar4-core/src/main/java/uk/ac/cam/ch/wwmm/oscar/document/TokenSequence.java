package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

import nu.xom.Element;
import uk.ac.cam.ch.wwmm.oscar.types.BioTag;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

/**
 * A tokenised representation of a piece of text, as produced
 * by {@link ITokeniser#tokenise(String, IProcessingDocument, int, Element)}.
 *
 * @author ptc24
 *
 */
public final class TokenSequence {

	private static final BioType B_CPR = new BioType(BioTag.B, NamedEntityType.LOCANTPREFIX);

    private String surface;
    private int offset;
    private IProcessingDocument doc;
    private List<Token> tokens;
    private Element elem;

    public TokenSequence(String sourceString, int offset, IProcessingDocument doc, List<Token> tokens) {
        this.surface = sourceString;
        this.offset = offset;
        this.doc = doc;
        this.tokens = tokens;
    }

    /**
     * Gets the string that was tokenised to make this TokenSequence.
	 * 
	 * @return The string that was tokenised to make this TokenSequence.
	 */
    public String getSurface() {
        return surface;
    }

	/**
	 * Gets the {@link IProcessingDocument} (or null) that this TokenSequence was made
	 * from.
	 * 
	 * @return The IProcessingDocument (or null) that this TokenSequence was made
	 * from.
	 */
    public IProcessingDocument getDoc() {
        return doc;
    }

	/**
	 * Gets the start offset of this TokenSequence. If this information was
	 * not supplied during tokenisation, this will be 0.
	 * 
	 * @return The start offset of this TokenSequence.
	 */
    public int getOffset() {
        return offset;
    }

	/**
	 * Gets the list of tokens that comprise this TokenSequence.
	 * 
	 * @return The list of tokens that comprise this TokenSequence.
	 */
    public List<Token> getTokens() {
        return tokens;
    }

	/**
	 * Gets a the sublist of tokens that occur between the given indices.
	 * 
	 * @param from The first token in the sublist (inclusive).
	 * @param to The last token in the sublist (inclusive).
	 * @return The sublist of tokens.
	 */
    public List<Token> getTokens(int from, int to) {
        return tokens.subList(from, to+1);
    }

	/**
	 * Gets a single token.
	 * 
	 * @param i The index of the token to get.
	 * @return The token.
	 */
    public Token getToken(int i) {
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

	/**
	 * Gets the number of tokens in the TokenSequence.
	 * 
	 * @return the number of tokens in the TokenSequence.
	 */
    public int getSize() {
        return tokens.size();
    }

	/**
	 * Gets a list of strings corresponding to the tokens.
	 * 
	 * @return The list of strings corresponding to the tokens.
	 */
    public List<String> getTokenStringList() {
        List<String> tl = new ArrayList<String>();
        for(Token t : tokens) {
            tl.add(t.getSurface());
        }
        return tl;
    }

	/**
	 * Gets a substring of the source string that runs between two tokens 
	 * (inclusive). Note that token indices run from 0 to n.
	 * 
	 * @param startToken The first token (inclusive).
	 * @param endToken The last token (inclusive).
	 * @return The substring.
	 */
    public String getSubstring(int startToken, int endToken) {
        if (endToken >= getSize()) {
            endToken = getSize() - 1;
        }
        int startOffset = tokens.get(startToken).getStart();
        int endOffset = tokens.get(endToken).getEnd();
        return surface.substring(startOffset - offset, endOffset - offset);
    }

    /**
     * Returns the substring of the tokenSequence surface between
     * the specified offsets, which need not correspond to token
     * boundaries.
     */
    public String getStringAtOffsets(int start, int end) {
        return surface.substring(start - offset, end - offset);
    }

	/**
	 * Gets all of the token values of tokens that are hyphenated with 
	 * named entities. For example, this would get "based" in "acetone-based".
	 * 
	 * @return The token values.
	 */
    public Set<String> getAfterHyphens() {
        Set<String> afterHyphens = new HashSet<String>();
        for (int i = 1; i < tokens.size(); i++) {
            if (i < tokens.size()-1
                    && tokens.get(i).getSurface().length() == 1
                    && StringTools.isHyphen(tokens.get(i).getSurface())
                    && BioTag.O == tokens.get(i).getBioType().getBio()
                    && BioTag.O == tokens.get(i+1).getBioType().getBio()
                    && BioTag.O != tokens.get(i-1).getBioType().getBio()
                    && tokens.get(i).getStart() == tokens.get(i-1).getEnd()
                    && tokens.get(i).getEnd() == tokens.get(i+1).getStart()
                    ) {
                afterHyphens.add(tokens.get(i+1).getSurface());
            } else if (BioTag.O == tokens.get(i).getBioType().getBio()
                    && B_CPR == tokens.get(i-1).getBioType()
                    && tokens.get(i).getStart() == tokens.get(i-1).getEnd()
                    ) {
                afterHyphens.add(tokens.get(i).getSurface());
            }
        }
        return afterHyphens;
    }

	/**
	 * Gets all of the named entities in the TokenSequence. This produces a
	 * map, where the keys are the named entity types. The values are a list
	 * of all NEs of the corresponding type, which are represented as lists
	 * of (token surface) strings.
	 * 
	 * @return The named entities.
	 */
    public Map<NamedEntityType,List<List<String>>> getNes() {
    	
        Map<NamedEntityType,List<List<String>>> neMap = new HashMap<NamedEntityType,List<List<String>>>();
        NamedEntityType namedEntityType = null;
        List<String> neTokens = null;
        for (Token t : tokens) {
            if (namedEntityType == null) {
                if (BioTag.O != t.getBioType().getBio()) {
                    neTokens = new ArrayList<String>();
                    // Trim of the B- in the BIO tag
                    namedEntityType = t.getBioType().getType();
                    neTokens.add(t.getSurface());
                    if (!neMap.containsKey(namedEntityType)) {
                        neMap.put(namedEntityType, new ArrayList<List<String>>());
                    }
                    neMap.get(namedEntityType).add(neTokens);
                }
            } else {
                if (BioTag.O == t.getBioType().getBio()) {
                    namedEntityType = null;
                    neTokens = null;
                } else if (t.getBioType().getBio() == BioTag.B) {
                    neTokens = new ArrayList<String>();
                    // Trim of the B- in the BIO tag
                    namedEntityType = t.getBioType().getType();
                    neTokens.add(t.getSurface());
                    if (!neMap.containsKey(namedEntityType)) {
                        neMap.put(namedEntityType, new ArrayList<List<String>>());
                    }
                    neMap.get(namedEntityType).add(neTokens);
                    // Must be I- something then
                } else {
                    neTokens.add(t.getSurface());
                }
            }
        }
        return neMap;
    }

	/**
	 * Gets the string values of all of the non-NE tokens.
	 * 
	 * @return The string values.
	 */
    public List<String> getNonNes() {
        List<String> nonNes = new ArrayList<String>();
        for (Token token : tokens) {
            if (BioTag.O == token.getBioType().getBio()) {
                nonNes.add(token.getSurface());
            }
        }
        return nonNes;
    }

	/**
	 * Returns the token that starts at the given index, or null if no such
	 * token exists. 
	 */
	public Token getTokenByStartIndex(int index) {
		checkIndex(index);
		for (Token token : tokens) {
			if (token.getStart() == index) {
				return token;
			}
		}
		return null;
	}

	private void checkIndex(int index) {
		if (index < offset) {
			throw new ArrayIndexOutOfBoundsException("index " + index + " occurs before the beginning of this token sequence");
		}
		if (index > offset + surface.length()) {
			throw new ArrayIndexOutOfBoundsException("index " + index + " occurs after the end of this token sequence");
		}
	}

	/**
	 * Returns the token that ends at the given index, or null if no such
	 * token exists. 
	 */
	public Token getTokenByEndIndex(int index) {
		checkIndex(index);
		for (Token token : tokens) {
			if (token.getEnd() == index) {
				return token;
			}
		}
		return null;
	}

}
