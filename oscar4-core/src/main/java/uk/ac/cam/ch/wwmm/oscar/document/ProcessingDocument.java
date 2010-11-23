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
public final class ProcessingDocument implements IProcessingDocument {

	public Document doc;
	public StandoffTable standoffTable;
	public List<TokenSequence> tokenSequences;
	public Map<Integer, Token> tokensByStart;
	public Map<Integer, Token> tokensByEnd;
	List<List<Token>> sentences;

	public ProcessingDocument() {

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
	public Token getTokenByStart(String leftXPoint) throws Exception {
		int offset = standoffTable.getOffsetAtXPoint(leftXPoint);
		if (tokensByStart.containsKey(offset))
			return tokensByStart.get(offset);
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument#getTokenByEnd(java.lang.String)
	 */
	public Token getTokenByEnd(String rightXPoint) throws Exception {
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
}
