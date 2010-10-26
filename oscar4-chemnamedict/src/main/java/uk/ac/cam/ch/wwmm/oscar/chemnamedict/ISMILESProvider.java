package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.util.Set;

/**
 * Extension of the {@link IChemNameDict} interface to return hits
 * in SMILES.
 *
 * @author egonw
 */
public interface ISMILESProvider {

	public Set<String> getSMILES(String queryName);

	public String getShortestSMILES(String queryName);

}