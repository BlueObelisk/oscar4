package uk.ac.cam.ch.wwmm.oscar.chemnamedict.records;

/**
 * A {@link IChemRecord} that has support for a Standard InChI Key identifier.
 *
 * @author mjw
 */
public interface IStdInChiKeyChemRecord extends IChemRecord {

	/**
	 * Sets the Standard InChIKey for this chemical compound.
	 *
	 * @param stdInchiKey
	 */
	public abstract void setStdInChiKey(String stdInchiKey);

	/**
	 * Returns the Standard stdInchiKey for this compound.
	 *
	 * @return A {@link String} representation of the Standard InChIKey.
	 */
	public abstract String getStdInChiKey();

}