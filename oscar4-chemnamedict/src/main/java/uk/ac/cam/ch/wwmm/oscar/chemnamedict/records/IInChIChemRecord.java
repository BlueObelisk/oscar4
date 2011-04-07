package uk.ac.cam.ch.wwmm.oscar.chemnamedict.records;

/**
 * A {@link IChemRecord} that has support for an InChI identifier.
 *
 * @author egonw
 */
public interface IInChIChemRecord extends IChemRecord {

	/**
	 * Sets the InChI for this chemical compound.
	 *
	 * @param inchi
	 */
	public abstract void setInChI(String inchi);

	/**
	 * Returns the InChI for this compound.
	 *
	 * @return A {@link String} representation of the InChI.
	 */
	public abstract String getInChI();

}