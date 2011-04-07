package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.util.Set;

/**
 * Extension of the {@link IChemNameDict} interface to return hits
 * in InChI.
 *
 * @author egonw
 * @author dmj30
 */
public interface IInChIProvider {

	/**
	 * Returns a set containing all of the known InChIs for
	 * the given query name.
	 */
	public Set<String> getInchis(String queryName);

}