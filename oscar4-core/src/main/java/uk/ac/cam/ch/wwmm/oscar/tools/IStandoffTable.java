package uk.ac.cam.ch.wwmm.oscar.tools;

import nu.xom.Element;

/**
 * @author ptc24
 * @author egonw
 */
public interface IStandoffTable {

	/**Converts an offset into an XPoint suitable for the left (start) of an
	 * annotation.
	 * 
	 * @param offset The offset.
	 * @return The XPoint.
	 */
	public abstract String getLeftPointAtOffset(int offset);

	/**Converts an offset into an XPoint suitable for the right (end) of an
	 * annotation.
	 * 
	 * @param offset The offset.
	 * @return The XPoint.
	 */
	public abstract String getRightPointAtOffset(int offset);

	/**Converts an XPoint into a character offset. For this to work the
	 * XMLSpanTagger must have been run on the XML.
	 * 
	 * @param xPoint The XPoint.
	 * @return The offset.
	 */
	public abstract int getOffsetAtXPoint(String xPoint);

	// FIXME: the below methods needs to be removed from the interface if we want XOM independence

	/**Gets the innermost Element at a particular character offset.
	 * 
	 * @param offset The character offset.
	 * @return The Element.
	 */
	public abstract Element getElemAtOffset(int i);

}