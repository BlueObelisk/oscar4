package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.List;
import java.util.Map;

import uk.ac.cam.ch.wwmm.oscar.tools.StandoffTable;

import nu.xom.Document;

/**
 * A document, with data structures to store information such as tokens. This
 * extra information is essential for many document processing tasks. These
 * should be created using the ProcessingDocumentFactory class.
 * 
 * @author ptc24
 * @author egonw
 */
public final class ProcessingDocument {

	public Document doc;
	public StandoffTable standoffTable;
	public List<TokenSequence> tokenSequences;
	public Map<Integer, Token> tokensByStart;
	public Map<Integer, Token> tokensByEnd;
	List<List<Token>> sentences;

	public ProcessingDocument() {

	}

	/**
	 * Gets the StandoffTable associated with the document.
	 * 
	 * @return The StandoffTable for the document.
	 */
	public StandoffTable getStandoffTable() {
		return standoffTable;
	}

	/**
	 * Gets the list of TokenSequences for the document. Note that this should
	 * only be called after the document has been tokenised.
	 * 
	 * @return The list of TokenSequences for the document.
	 */
	public List<TokenSequence> getTokenSequences() {
		return tokenSequences;
	}



	/**
	 * Gets the source SciXML document.
	 * 
	 * 
	 * @return The source SciXML document.
	 */
	public Document getDoc() {
		return doc;
	}

	/**
	 * Gets the token that starts at a given XPoint. Note that this should only
	 * be called after the document has been tokenised.
	 * 
	 * @param leftXPoint
	 *            The XPoint of the start of the token.
	 * @return The token, or null.
	 * @throws Exception
	 */
	public Token getTokenByStart(String leftXPoint) throws Exception {
		int offset = standoffTable.getOffsetAtXPoint(leftXPoint);
		if (tokensByStart.containsKey(offset))
			return tokensByStart.get(offset);
		return null;
	}

	/**
	 * Gets the token that ends at a given XPoint. Note that this should only be
	 * called after the document has been tokenised.
	 * 
	 * @param rightXPoint
	 *            The XPoint of the end of the token.
	 * @return The token, or null.
	 * @throws Exception
	 */
	public Token getTokenByEnd(String rightXPoint) throws Exception {
		int offset = standoffTable.getOffsetAtXPoint(rightXPoint);
		if (tokensByEnd.containsKey(offset))
			return tokensByEnd.get(offset);
		return null;
	}

	/**
	 * Gets a list of list of tokens corresponding to the sentences in the
	 * document.
	 * 
	 * @return The list of list of tokens corresponding to the sentences in the
	 *         document.
	 */
	public List<List<Token>> getSentences() {
		return sentences;
	}
}
