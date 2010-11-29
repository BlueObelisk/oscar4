package uk.ac.cam.ch.wwmm.oscar.chemnamedict.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChemRecord {

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

	public void addName(String name) {
		names.add(name);
	}

	public void addNames(Collection<String> names) {
		names.addAll(names);
	}

	public Collection<String> getNames() {
		return Collections.unmodifiableCollection(names);
	}
	
	public void addOntologyIdentifiers(Collection<String> identifiers) {
		ontIDs.addAll(identifiers);
	}

	public void addOntologyIdentifier(String identifier) {
		ontIDs.add(identifier);
	}

	public Collection<String> getOntologyIdentifiers() {
		return Collections.unmodifiableCollection(ontIDs);
	}

	public void setInChI(String inchi) {
		this.inchi = inchi;
	}

	public String getInChI() {
		return inchi;
	}

	public void setSMILES(String smiles) {
		this.smiles = smiles;
	}

	public String getSMILES() {
		return smiles;
	}

}
