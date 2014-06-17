package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.util.Set;

/**
 * Extension of the {@link IChemNameDict} interface to return hits
 * in Standard InChI.
 *
 * @author mjw
 */
public interface IStdInChIProvider {

	/**
	 * Returns a set containing all of the known Standard InChIs for
	 * the given query name.
	 */
	public Set<String> getStdInchis(String queryName);

}
