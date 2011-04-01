package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.List;
import java.util.Map;

import uk.ac.cam.ch.wwmm.oscar.tools.StandoffTable;

/**
 * A document, with data structures to store information such as tokens. This
 * extra information is essential for many document processing tasks. These
 * should be created using the {@link ProcessingDocumentFactory} class.
 * 
 * @author ptc24
 * @author egonw
 * @author dmj30
 */
public interface IProcessingDocument {

	/**
	 * Gets the list of TokenSequences for the document. Note that this should
	 * only be called after the document has been tokenised.
	 * 
	 * @return The list of TokenSequences for the document.
	 */
	List <TokenSequence> getTokenSequences();

	/**
	 * Gets the {@link StandoffTable} associated with the document.
	 * 
	 * @return The {@link StandoffTable} for the document.
	 */
	StandoffTable getStandoffTable();

	Map<Integer,Token> getTokensByStart();

	Map<Integer,Token> getTokensByEnd();

}
