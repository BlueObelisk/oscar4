package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.List;
import java.util.Map;

import nu.xom.Document;
import uk.ac.cam.ch.wwmm.oscar.tools.StandoffTable;

/**
 * A document, with data structures to store information such as tokens. This
 * extra information is essential for many document processing tasks. These
 * should be created using the ProcessingDocumentFactory class.
 * 
 * @author ptc24
 * @author egonw
 */
public final class OldProcessingDocument implements IOldProcessingDocument {

	public Document doc;
	public StandoffTable standoffTable;
	public List<TokenSequence> tokenSequences;
	public Map<Integer, Token> tokensByStart;
	public Map<Integer, Token> tokensByEnd;
	List<List<Token>> sentences;

	public OldProcessingDocument() {

	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument#getStandoffTable()
	 */
	public StandoffTable getStandoffTable() {
		return standoffTable;
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument#getTokenSequences()
	 */
	public List<TokenSequence> getTokenSequences() {
		return tokenSequences;
	}



	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument#getDoc()
	 */
	public Document getDoc() {
		return doc;
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument#getTokenByStart(java.lang.String)
	 */
	public IToken getTokenByStart(String leftXPoint) throws Exception {
		int offset = standoffTable.getOffsetAtXPoint(leftXPoint);
		if (tokensByStart.containsKey(offset))
			return tokensByStart.get(offset);
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument#getTokenByEnd(java.lang.String)
	 */
	public IToken getTokenByEnd(String rightXPoint) throws Exception {
		int offset = standoffTable.getOffsetAtXPoint(rightXPoint);
		if (tokensByEnd.containsKey(offset))
			return tokensByEnd.get(offset);
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument#getSentences()
	 */
	public List<List<Token>> getSentences() {
		return sentences;
	}

	public Map<Integer, Token> getTokensByStart() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<Integer, Token> getTokensByEnd() {
		// TODO Auto-generated method stub
		return null;
	}
}
