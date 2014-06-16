package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.IChemRecord;

/**
 * 
 * @author egonw
 * @author dmj30
 *
 */
public interface IChemNameDict {

	/**
	 * Returns the URI for the dictionary. 
	 */
	public URI getURI();

	/**
	 * Checks if the given query name is contained within
	 * the dictionary. 
	 */
	public boolean hasName(String queryName);

	/**
	 * Checks if the given ontology identifier is contained
	 * within the dictionary. 
	 */
	public boolean hasOntologyIdentifier(String identifier);

	/**
	 * Returns a set containing all of the names in the
	 * dictionary that define a chemical structure
	 * corresponding to the given Standard InChI.
	 */
	public Set<String> getNames(String stdInchi);

	/**
	 * Returns a set containing all of the names contained
	 * in the dictionary.
	 */
	public Set<String> getNames();

	/**
	 * Returns a set containing all of the {@link IChemRecord}s
	 * contained in the dictionary. 
	 */
	public Set<IChemRecord> getChemRecords();

	/**
	 * Returns the language employed by the dictionary.
	 */
	public Locale getLanguage();
}