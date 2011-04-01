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
public final class XOMBasedProcessingDocument implements IProcessingDocument {

	public Document doc;
	public StandoffTable standoffTable;
	public List<ITokenSequence> tokenSequences;
	public Map<Integer,IToken> tokensByStart;
	public Map<Integer,IToken> tokensByEnd;

	public XOMBasedProcessingDocument() {

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
	public List<ITokenSequence> getTokenSequences() {
		return tokenSequences;
	}



	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument#getDoc()
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
	 */
	@Deprecated
	//TODO this isn't called - do we need it?
	public IToken getTokenByStart(String leftXPoint) {
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
	 */
	@Deprecated
	//TODO this isn't called - do we need it?
	public IToken getTokenByEnd(String rightXPoint) {
		int offset = standoffTable.getOffsetAtXPoint(rightXPoint);
		if (tokensByEnd.containsKey(offset))
			return tokensByEnd.get(offset);
		return null;
	}


	public Map<Integer,IToken> getTokensByStart() {
		return tokensByStart;
	}

	public Map<Integer,IToken> getTokensByEnd() {
		return tokensByEnd;
	}

	/**
	 * Sets the StandoffTable associated with the document.
	 * 
	 * @param sot The StandoffTable for the document.
	 */
	public void setStandoffTable(StandoffTable sot) {
		standoffTable = sot;
		
	}
}
