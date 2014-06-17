package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.util.Set;

/**
 * Extension of the {@link IChemNameDict} interface to return hits
 * in Standard InChI Key.
 *
 * @author mjw
 */
public interface IStdInChIKeyProvider {

	/**
	 * Returns a set containing all of the known Standard InChI Keys for
	 * the given query name.
	 */
	public Set<String> getStdInchiKeys(String queryName);

}
