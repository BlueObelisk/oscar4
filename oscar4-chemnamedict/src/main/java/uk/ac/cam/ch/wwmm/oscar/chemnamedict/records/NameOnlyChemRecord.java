package uk.ac.cam.ch.wwmm.oscar.chemnamedict.records;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NameOnlyChemRecord implements IChemRecord {

	private Set<String> names;

	public NameOnlyChemRecord() {
		names = new HashSet<String>();
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.IChemRecord#addName(java.lang.String)
	 */
	public void addName(String name) {
		names.add(name);
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.IChemRecord#addNames(java.util.Collection)
	 */
	public void addNames(Collection<String> names) {
		names.addAll(names);
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.IChemRecord#getNames()
	 */
	public Collection<String> getNames() {
		return Collections.unmodifiableCollection(names);
	}
	
}
