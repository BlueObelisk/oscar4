package uk.ac.cam.ch.wwmm.oscar.chemnamedict.records;

/**
 * A {@link IChemRecord} that has support for a Standard InChI identifier.
 *
 * @author mjw
 */
public interface IStdInChIChemRecord extends IChemRecord {

	/**
	 * Sets the Standard InChI for this chemical compound.
	 *
	 * @param stdInchi
	 */
	public abstract void setStdInChI(String stdInchi);

	/**
	 * Returns the Standard InChI for this compound.
	 *
	 * @return A {@link String} representation of the Standard InChI.
	 */
	public abstract String getStdInChI();

}