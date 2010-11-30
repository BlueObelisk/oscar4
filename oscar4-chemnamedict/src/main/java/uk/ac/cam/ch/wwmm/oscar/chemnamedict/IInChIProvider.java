package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.util.Set;

/**
 * Extension of the {@link IChemNameDict} interface to return hits
 * in InChI.
 *
 * @author egonw
 */
public interface IInChIProvider {

	public Set<String> getInChI(String queryName);

}