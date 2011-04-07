package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.util.Set;

import nu.xom.Element;

/**
 * Extension of the {@link IChemNameDict} interface to return hits
 * in CML format.
 *
 * @author egonw
 * @author dmj30
 */
public interface ICMLProvider {

	/**
	 * Returns a set containing all of the known
	 * CML representations for the given query name.
	 */
	public Set<Element> getCML(String queryName);

}