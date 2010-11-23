package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.List;

import nu.xom.Document;
import uk.ac.cam.ch.wwmm.oscar.tools.StandoffTable;

public interface IProcessingDocument {

	/**
	 * Gets the StandoffTable associated with the document.
	 * 
	 * @return The StandoffTable for the document.
	 */
	public abstract StandoffTable getStandoffTable();

	/**
	 * Gets the list of TokenSequences for the document. Note that this should
	 * only be called after the document has been tokenised.
	 * 
	 * @return The list of TokenSequences for the document.
	 */
	public abstract List<TokenSequence> getTokenSequences();

	/**
	 * Gets the source SciXML document.
	 * 
	 * 
	 * @return The source SciXML document.
	 */
	public abstract Document getDoc();

	/**
	 * Gets the token that starts at a given XPoint. Note that this should only
	 * be called after the document has been tokenised.
	 * 
	 * @param leftXPoint
	 *            The XPoint of the start of the token.
	 * @return The token, or null.
	 * @throws Exception
	 */
	public abstract Token getTokenByStart(String leftXPoint) throws Exception;

	/**
	 * Gets the token that ends at a given XPoint. Note that this should only be
	 * called after the document has been tokenised.
	 * 
	 * @param rightXPoint
	 *            The XPoint of the end of the token.
	 * @return The token, or null.
	 * @throws Exception
	 */
	public abstract Token getTokenByEnd(String rightXPoint) throws Exception;

	/**
	 * Gets a list of list of tokens corresponding to the sentences in the
	 * document.
	 * 
	 * @return The list of list of tokens corresponding to the sentences in the
	 *         document.
	 */
	public abstract List<List<Token>> getSentences();

}