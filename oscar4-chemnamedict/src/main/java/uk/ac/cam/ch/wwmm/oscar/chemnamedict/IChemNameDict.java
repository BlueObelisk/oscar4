package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.util.Set;

public interface IChemNameDict {

	public void addStopWord(String word) throws Exception;

	public boolean hasStopWord(String queryWord);

	public Set<String> getStopWords();

	public void addChemRecord(String inchi, String smiles,
			Set<String> names, Set<String> ontIDs) throws Exception;

	public void addName(String name) throws Exception;

	public void addOntologyId(String ontId, String inchi)
			throws Exception;

	public void addChemical(String name, String smiles, String inchi)
			throws Exception;

	public void importChemNameDict(ChemNameDict cnd) throws Exception;

	public boolean hasName(String queryName);

	public Set<String> getSMILES(String queryName);

	public String getShortestSMILES(String queryName);

	public Set<String> getInChI(String queryName);

	public String getInChIforShortestSMILES(String queryName);

	public Set<String> getNames(String inchi);

	public Set<String> getNames();

	public Set<String> getOntologyIDsFromInChI(String queryInchi);

	public boolean hasOntologyId(String ontId);

	public Set<String> getInchisByOntologyId(String ontId);

}