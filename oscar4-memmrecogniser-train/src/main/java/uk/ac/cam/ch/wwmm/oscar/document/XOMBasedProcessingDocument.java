package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.List;
import java.util.Map;

import nu.xom.Document;
import uk.ac.cam.ch.wwmm.oscar.tools.IStandoffTable;
import uk.ac.cam.ch.wwmm.oscar.tools.StandoffTable;

/**
 * A document, with data structures to store information such as tokens. This
 * extra information is essential for many document processing tasks. These
 * should be created using the ProcessingDocumentFactory class.
 * 
 * @author ptc24
 * @author egonw
 */
public final class XOMBasedProcessingDocument implements IXOMBasedProcessingDocument {

	public Document doc;
	public IStandoffTable standoffTable;
	public List<ITokenSequence> tokenSequences;
	public Map<Integer,IToken> tokensByStart;
	public Map<Integer,IToken> tokensByEnd;

	public XOMBasedProcessingDocument() {

	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument#getStandoffTable()
	 */
	public IStandoffTable getStandoffTable() {
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


	public Map<Integer,IToken> getTokensByStart() {
		return tokensByStart;
	}

	public Map<Integer,IToken> getTokensByEnd() {
		return tokensByEnd;
	}

	public void setStandoffTable(IStandoffTable sot) {
		standoffTable = sot;
		
	}
}
