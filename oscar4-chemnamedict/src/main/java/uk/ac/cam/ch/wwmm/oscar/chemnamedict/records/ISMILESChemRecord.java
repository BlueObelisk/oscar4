package uk.ac.cam.ch.wwmm.oscar.chemnamedict.records;

/**
 * A {@link IChemRecord} that has support for a SMILES representation.
 *
 * @author egonw
 */
public interface ISMILESChemRecord extends IChemRecord {

	/**
	 * Sets the SMILES for this compound.
	 *
	 * @param smiles a {@link String} representation of the SMILES.
	 */
	public abstract void setSMILES(String smiles);

	/**
	 * Returns the SMILES for this compound.
	 *
	 * @return a {@link String} representation of the SMILES.
	 */
	public abstract String getSMILES();

}