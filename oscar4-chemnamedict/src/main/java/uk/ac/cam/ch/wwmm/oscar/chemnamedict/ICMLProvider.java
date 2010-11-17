package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.util.Set;

import nu.xom.Element;

/**
 * Extension of the {@link IChemNameDict} interface to return hits
 * in CML format.
 *
 * @author egonw
 */
public interface ICMLProvider {

	public Set<Element> getCML(String queryName);

	public Element getCMLforShortestSMILES(String queryName);

}