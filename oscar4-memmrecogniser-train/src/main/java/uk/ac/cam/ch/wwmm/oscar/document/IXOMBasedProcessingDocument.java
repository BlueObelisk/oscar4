package uk.ac.cam.ch.wwmm.oscar.document;

import uk.ac.cam.ch.wwmm.oscar.tools.IStandoffTable;

public interface IXOMBasedProcessingDocument extends IProcessingDocument {

	/**
	 * Sets the StandoffTable associated with the document.
	 * 
	 * @param sot The StandoffTable for the document.
	 */
	public abstract void setStandoffTable(IStandoffTable sot);

	/**
	 * Gets the token that starts at a given XPoint. Note that this should only
	 * be called after the document has been tokenised.
	 * 
	 * @param leftXPoint
	 *            The XPoint of the start of the token.
	 * @return The token, or null.
	 */
	public abstract IToken getTokenByStart(String leftXPoint);

	/**
	 * Gets the token that ends at a given XPoint. Note that this should only be
	 * called after the document has been tokenised.
	 * 
	 * @param rightXPoint
	 *            The XPoint of the end of the token.
	 * @return The token, or null.
	 */
	public abstract IToken getTokenByEnd(String rightXPoint);

}