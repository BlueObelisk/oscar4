package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.List;

import nu.xom.Element;

/**A token - corresponding to a word, a number, a character of punctuation or
 * suchlike. Not whitespace.
 * 
 * @author ptc24
 *
 */
public final class Token {	
	
	/* Many of these are package visibility so Tokeniser can work on them
	 * easily.
	 */
	
	public String value;
	/**The character offset at the start of the token */
	public int start;
	/**The character offset (caret position) at the end of the token */
	public int end;
	public int id;
	public ProcessingDocument doc;
	public TokenSequence tokenSequence;
	
	public Element neElem;
	private String [] geniaData = null;
	private List<Token> chunk;
	private String chunkType;
	
	/** The B/I/O tag, when inline annotation is digested */
	public String bioTag;
	

	public Token(String value, int start, int end, ProcessingDocument doc, String bioTag, Element neElem) {
		this.start = start;
		this.end = end;
		this.value = value;
		this.doc = doc;
		this.bioTag = bioTag;
		this.neElem = neElem;
	}
	

	/**Gets the <i>n</i>th token after this one, or null. For example, 
	 * getNAfter(1) would get the next token, and getNAfter(-1) would get the
	 * previous one. 
	 * 
	 * @param n The offset (in tokens) from the current token.
	 * @return The token.
	 */
	public Token getNAfter(int n) {
		int pos = n + id;
		if (tokenSequence == null){
			
			System.out.println("I am NULL--- FEAR ME");
			throw new RuntimeException();
		}
		else if(tokenSequence.getTokens().size() <= pos || pos <= 0) return null;
		return tokenSequence.getTokens().get(pos);
	}

	/**Gets the string value of the token.
	 * 
	 * @return The string value of the token.
	 */
	public String getValue() {
		return value;
	}
	
	/**Gets the index of the token in its TokenSequence.
	 * 
	 * @return The index of the token in its TokenSequence.
	 */
	public int getId() {
		return id;
	}


	/**Gets this token's ProcessingDocument.
	 * 
	 * @return This token's ProcessingDocument.
	 */
	public ProcessingDocument getDoc() {
		return doc;
	}
	
	/**Gets the start offset of this token.
	 * 
	 * @return The start offset of this token.
	 */
	public int getStart() {
		return start;
	}

	/**Gets the start offset of this token.
	 * 
	 * @return The end offset of this token.
	 */
	public int getEnd() {
		return end;
	}
	
	/**Gets the BIO tag of this token.
	 * 
	 * @return The BIO tag of this token.
	 */
	public String getBioTag() {
		return bioTag;
	}
	
	void setBioTag(String bioTag) {
		this.bioTag = bioTag;
	}
	
	/**Gets the TokenSequence that contains this token.
	 * 
	 * @return The TokenSequence that contains this token.
	 */

	
	public TokenSequence getTokenSequence() {
		return tokenSequence;
	}

}
