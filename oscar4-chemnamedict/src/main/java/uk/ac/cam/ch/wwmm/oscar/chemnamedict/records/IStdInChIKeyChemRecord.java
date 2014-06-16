package uk.ac.cam.ch.wwmm.oscar.chemnamedict.records;

/**
 * A {@link IChemRecord} that has support for a Standard InChI Key identifier.
 *
 * @author mjw
 */
public interface IStdInChIKeyChemRecord extends IChemRecord {

	/**
	 * Sets the Standard InChIKey for this chemical compound.
	 *
	 * @param stdInchIKey
	 */
	public abstract void setStdInChIKey(String stdInchIKey);

	/**
	 * Returns the Standard stdInchiKey for this compound.
	 *
	 * @return A {@link String} representation of the Standard InChIKey.
	 */
	public abstract String getStdInChIKey();

}