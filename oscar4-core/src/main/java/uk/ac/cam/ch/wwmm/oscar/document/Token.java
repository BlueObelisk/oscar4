package uk.ac.cam.ch.wwmm.oscar.document;

import nu.xom.Element;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;

/**A token - corresponding to a word, a number, a character of punctuation or
 * suchlike. Not whitespace.
 * 
 * @author ptc24
 *
 */
public final class Token {	

    // TODO can we make this class immutable?
	
	private String surface;
	/**The character offset at the start of the token */
	private int start;
	/**The character offset (caret position) at the end of the token */
	private int end;
	private int index;
	private IProcessingDocument doc;
	private TokenSequence tokenSequence;
	
	private Element neElem;
	
	/** The B/I/O tag, when inline annotation is digested */
	private BioType bioType;

	public Token(String surface, int start, int end, IProcessingDocument doc, BioType bioType, Element neElem) {
		this.start = start;
		this.end = end;
		this.surface = surface;
		this.doc = doc;
		this.bioType = bioType;
		this.neElem = neElem;
	}
	

	/**
	 * Gets the <i>n</i>th token after this one, or null. For example, 
	 * getNAfter(1) would get the next token, and getNAfter(-1) would get the
	 * previous one. 
	 * 
	 * @param n The offset (in tokens) from the current token.
	 * @return The token.
	 */
	public Token getNAfter(int n) {
		int pos = n + index;
		if (tokenSequence == null){
			throw new RuntimeException();
		}
		else if(tokenSequence.getTokens().size() <= pos || pos < 0) {
			return null;
		}
		return tokenSequence.getTokens().get(pos);
	}

	/**
	 * Gets the surface string (i.e. text content) of the token.
	 * 
	 * @return The string value of the token.
	 */
	public String getSurface() {
		return surface;
	}

	/**
	 * Sets the surface string (i.e. text content) of the token.
	 */
    public void setSurface(String surface) {
        this.surface = surface;
    }

    /**
     * Gets the index of the token in its TokenSequence.
	 * 
	 * @return The index of the token in its TokenSequence.
	 */
	public int getIndex() {
		return index;
	}

    /**
     * Sets the index of the token in its TokenSequence.
     * 
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Gets this token's ProcessingDocument.
	 * 
	 * @return This token's ProcessingDocument.
	 */
	public IProcessingDocument getDoc() {
		return doc;
	}
	
	/**
	 * Gets the start offset of this token.
	 * 
	 * @return The start offset of this token.
	 */
	public int getStart() {
		return start;
	}

    
	/**
	 * Gets the end offset of this token.
	 * 
	 * @return The end offset of this token.
	 */
	public int getEnd() {
		return end;
	}

    /**
     * Sets the end offset of the token.
     */
    public void setEnd(int end) {
        this.end = end;
    }


    /**
     * Gets the {@link BioType} of this token.
	 * 
	 * @return The BIO type of this token.
	 */
	public BioType getBioType() {
		return bioType;
	}
	
	/**
	 * Sets the {@link BioType} of the token.
	 */
	public void setBioType(BioType bioType) {
		this.bioType = bioType;
	}
	
	
	/**Gets the {@link TokenSequence} that contains this token.
	 * 
	 * @return The TokenSequence that contains this token.
	 */
	public TokenSequence getTokenSequence() {
		return tokenSequence;
	}

    /**
     * Sets the internal reference to the {@link TokenSequence} that
     * contains this token.
     */
    public void setTokenSequence(TokenSequence tokenSequence) {
        this.tokenSequence = tokenSequence;
    }


    public Element getNeElem() {
        return neElem;
    }

    public void setNeElem(Element neElem) {
        this.neElem = neElem;
    }
    
	/**A string representation of the token, for debugging and related purposes.
	 * 
	 */
	@Override
	public String toString() {
		return("[TOKEN:" + bioType + ":" + start+ ":" + end+ ":" + surface + "]");
	}
}
