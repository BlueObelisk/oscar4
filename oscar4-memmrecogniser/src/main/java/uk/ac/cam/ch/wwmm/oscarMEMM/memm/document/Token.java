package uk.ac.cam.ch.wwmm.oscarMEMM.memm.document;

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
	
	String value;
	/**The character offset at the start of the token */
	int start;
	/**The character offset (caret position) at the end of the token */
	int end;
	int id;
	ProcessingDocument doc;
	public TokenSequence tokenSequence;
	
	Element neElem;
	private String [] geniaData = null;
	private List<Token> chunk;
	private String chunkType;
	
	/** The B/I/O tag, when inline annotation is digested */
	String bioTag;
	
	//private List<String> tokenReps;
	//private List<String> extraReps;
	
	public Token(String value, int start, int end, ProcessingDocument doc, String bioTag, Element neElem) {
		this.start = start;
		this.end = end;
		this.value = value;
		this.doc = doc;
		this.bioTag = bioTag;
		this.neElem = neElem;
	}
	
	//public boolean isRef() {
	//	return XMLStrings.isCitationReferenceUnderStyle(nr.cleanST.getElemAtOffset(start));
	//}
	
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
//			return null;
		}
		else if(tokenSequence.getTokens().size() <= pos || pos <= 0) return null;
		return tokenSequence.getTokens().get(pos);
	}
	
	/**Gets the XPoint corresponding to the start of the token.
	 * 
	 * @return The XPoint corresponding to the start of the token.
	 */
	/// Commented out 29/01/10

//	public String getStartXPoint() {
//		return doc.standoffTable.getLeftPointAtOffset(start);
//	}
	
	/**Gets the XPoint corresponding to the end of the token.
	 * 
	 * @return The XPoint corresponding to the end of the token.
	 */
	/// Commented out 29/01/10
//	public String getEndXPoint() {
//		return doc.standoffTable.getRightPointAtOffset(end);
//	}
	
	/**Gets the XPoint corresponding to a point several characters
	 * from the start of the token.
	 * 
	 * @param offsetFromStart The number of characters between the start of the
	 * token, and the desired XPoint.
	 * @return The XPoint.
	 */
	/// Commented out 29/01/10

//	public String getEndXPoint(int offsetFromStart) {
//		return doc.standoffTable.getRightPointAtOffset(start + offsetFromStart);
//	}
	
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

	
	public void setId(int id) {
		this.id = id;
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

	
	/**Gets the information obtained from the Genia tagger for this token.
	 * 
	 * @return The information obtained from the Genia tagger for this token.
	 */
	public String[] getGeniaData() {
		return geniaData;
	}
	
	/**Records information obtained from the Genia tagger in this token.
	 * 
	 * @param geniaData The information obtained from the Genia tagger.
	 */
	public void setGeniaData(String[] geniaData) {
		this.geniaData = geniaData;
	}
	
	/**Gets the Element corresponding to the named entity (if any) that this
	 * token is a part of.
	 * 
	 * @return The Element corresponding to the named entity (if any) that this
	 * token is a part of.
	 */
	public Element getNeElem() {
		return neElem;
	}
	
	/**Gets the list of tokens corresponding to the chunk that contains this
	 * token. The Genia tagger must first have been run to set this information.
	 * 
	 * @see NewGeniaRunner
	 * 
	 * @return The list of tokens corresponding to the chunk that contains
	 * this token.
	 */
	public List<Token> getChunk() {
		return chunk;
	}
	
	/**Sets the list of tokens corresponding to the chunk that contains this
	 * token.
	 * 
	 * @param chunk The list of tokens corresponding to the chunk that contains
	 * this token.
	 */
	public void setChunk(List<Token> chunk) {
		this.chunk = chunk;
	}
	
	/**Gets the type of the chunk that contains this token, or null. The Genia
	 * tagger must first have been run to set this information.
	 * 
	 * @return The type of the chunk that contains this token, or null.
	 */
	public String getChunkType() {
		return chunkType;
	}
	
	/**Sets the type of the chunk that contains this token.
	 * 
	 * @param chunkType The type of the chunk that contains this token.
	 */
	public void setChunkType(String chunkType) {
		this.chunkType = chunkType;
	}


	

}
