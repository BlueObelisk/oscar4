package uk.ac.cam.ch.wwmm.oscar.chemnamedict.records;

import java.util.Collection;

/**
 * A record reflecting linguistic information around a single chemical.
 *
 * @author egonw
 */
public interface IChemRecord {

	/**
	 * Adds a name to the record.
	 *
	 * @param name a name for the chemical.
	 */
	public abstract void addName(String name);

	/**
	 * Adds multiple names to the record.
	 *
	 * @param names the names for the chemical.
	 */
	public abstract void addNames(Collection<String> names);

	/**
	 * Returns a collection of names for this compound.
	 *
	 * @return a {@link Collection} with the names.
	 */
	public abstract Collection<String> getNames();

}