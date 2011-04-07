package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.util.Set;

//TODO interface isn't used - do we need it?
public interface ISingleChemNameDict extends IMutableChemNameDict {

	public void addOntologyId(String ontId, String inchi)
			throws Exception;

	public Set<String> getOntologyIDsFromInChI(String queryInchi);

	public boolean hasOntologyIdentifier(String ontId);

	public Set<String> getInchisByOntologyId(String ontId);

}