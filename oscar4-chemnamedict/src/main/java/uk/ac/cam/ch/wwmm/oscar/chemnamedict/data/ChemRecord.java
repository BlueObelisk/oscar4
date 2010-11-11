package uk.ac.cam.ch.wwmm.oscar.chemnamedict.data;

import java.util.HashSet;
import java.util.Set;

import nu.xom.Element;

public class ChemRecord {

	public String inchi;
	public String smiles;
	public Set<String> names;
	public Set<String> ontIDs;

	public ChemRecord() {
		inchi = null;
		smiles = null;
		names = new HashSet<String>();
		ontIDs = new HashSet<String>();
	}
}
