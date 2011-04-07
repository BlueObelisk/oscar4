package uk.ac.cam.ch.wwmm.oscar.chemnamedict.records;

import java.util.Collection;

/**
 * A {@link IChemRecord} that is associated with one or more
 * ontology identifiers.
 *
 * @author egonw
 */
public interface IOntologyChemRecord extends IChemRecord {

	/**
	 * Adds multiple ontology identifiers to the record.
	 *
	 * @param identifiers a {@link Collection} of ontology identifiers for the chemical.
	 */
	public abstract void addOntologyIdentifiers(Collection<String> identifiers);

	/**
	 * Adds a ontology identifier to the record.
	 *
	 * @param identifier a ontology identifier for the chemical.
	 */
	public abstract void addOntologyIdentifier(String identifier);

	/**
	 * Returns the ontology identifiers for this compound.
	 *
	 * @return a {@link Collection} of ontology identifiers for the chemical.
	 */
	public abstract Collection<String> getOntologyIdentifiers();

}