package uk.ac.cam.ch.wwmm.oscar.chemnamedict.data;

import java.util.HashSet;
import java.util.Set;

import nu.xom.Element;

public 	class ChemRecord {

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
	
	public Element toXML() {
		Element elem = new Element("record");
		
		Element inchiElem = new Element("InChI");
		inchiElem.appendChild(inchi);
		elem.appendChild(inchiElem);
		
		if(smiles != null) {
			Element smilesElem = new Element("SMILES");
			smilesElem.appendChild(smiles);
			elem.appendChild(smilesElem);				
		}
		
		for(String name : names) {
			Element nameElem = new Element("name");
			nameElem.appendChild(name);
			elem.appendChild(nameElem);								
		}

		for(String ontID : ontIDs) {
			Element ontIDElem = new Element("ontID");
			ontIDElem.appendChild(ontID);
			elem.appendChild(ontIDElem);								
		}
		return elem;
	}
}
