package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.util.Set;

/**
 * Extension of the {@link IChemNameDict} interface to return hits
 * in SMILES.
 *
 * @author egonw
 * @author dmj30
 */
public interface ISMILESProvider {

	/**
	 * Returns a set containing all known SMILES strings
	 * for the given query name.
	 */
	public Set<String> getAllSmiles(String queryName);

	/**
	 * Returns the shortest known SMILES string for the
	 * given query name, or null if none are known. 
	 */
	public String getShortestSmiles(String queryName);

}