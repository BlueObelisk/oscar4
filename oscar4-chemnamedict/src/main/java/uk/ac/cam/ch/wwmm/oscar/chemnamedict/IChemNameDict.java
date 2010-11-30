package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.net.URI;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.IChemRecord;

public interface IChemNameDict {

	public URI getURI();

	public boolean hasName(String queryName);

	public boolean hasOntologyIdentifier(String identifier);

	public Set<String> getNames(String inchi);

	public Set<String> getNames();

	public Set<IChemRecord> getChemRecords();

}