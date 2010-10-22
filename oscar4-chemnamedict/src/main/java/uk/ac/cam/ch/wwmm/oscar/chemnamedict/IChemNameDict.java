package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.net.URI;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.ChemRecord;

public interface IChemNameDict {

	public URI getURI();

	public boolean hasStopWord(String queryWord);

	public Set<String> getStopWords();

	public boolean hasName(String queryName);

	public Set<String> getSMILES(String queryName);

	public String getShortestSMILES(String queryName);

	public Set<String> getInChI(String queryName);

	public String getInChIforShortestSMILES(String queryName);

	public Set<String> getNames(String inchi);

	public Set<String> getNames();

	public Set<String> getOrphanNames();

	public Set<ChemRecord> getChemRecords();

}