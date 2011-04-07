package uk.ac.cam.ch.wwmm.oscar.chemnamedict.records;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChemRecord implements IInChIChemRecord, ISMILESChemRecord, IOntologyChemRecord, IChemRecord {

	private String inchi;
	private String smiles;
	private Set<String> names;
	private Set<String> ontIDs;

	public ChemRecord() {
		setInChI(null);
		setSMILES(null);
		names = new HashSet<String>();
		ontIDs = new HashSet<String>();
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
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.IOntologyChemRecord#addOntologyIdentifiers(java.util.Collection)
	 */
	public void addOntologyIdentifiers(Collection<String> identifiers) {
		ontIDs.addAll(identifiers);
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.IOntologyChemRecord#addOntologyIdentifier(java.lang.String)
	 */
	public void addOntologyIdentifier(String identifier) {
		ontIDs.add(identifier);
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.IOntologyChemRecord#getOntologyIdentifiers()
	 */
	public Collection<String> getOntologyIdentifiers() {
		return Collections.unmodifiableCollection(ontIDs);
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.IInChIChemRecord#setInChI(java.lang.String)
	 */
	public void setInChI(String inchi) {
		this.inchi = inchi;
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.IInChIChemRecord#getInChI()
	 */
	public String getInChI() {
		return inchi;
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.ISMILESChemRecord#setSMILES(java.lang.String)
	 */
	public void setSMILES(String smiles) {
		this.smiles = smiles;
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.ISMILESChemRecord#getSMILES()
	 */
	public String getSMILES() {
		return smiles;
	}

}
