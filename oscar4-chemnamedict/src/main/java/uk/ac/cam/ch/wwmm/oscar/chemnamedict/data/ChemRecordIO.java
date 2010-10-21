package uk.ac.cam.ch.wwmm.oscar.chemnamedict.data;

import nu.xom.Element;

public class ChemRecordIO {

	public static Element toXML(ChemRecord record) {
		Element elem = new Element("record");
		
		Element inchiElem = new Element("InChI");
		inchiElem.appendChild(record.inchi);
		elem.appendChild(inchiElem);
		
		if(record.smiles != null) {
			Element smilesElem = new Element("SMILES");
			smilesElem.appendChild(record.smiles);
			elem.appendChild(smilesElem);				
		}
		
		for(String name : record.names) {
			Element nameElem = new Element("name");
			nameElem.appendChild(name);
			elem.appendChild(nameElem);								
		}

		for(String ontID : record.ontIDs) {
			Element ontIDElem = new Element("ontID");
			ontIDElem.appendChild(ontID);
			elem.appendChild(ontIDElem);								
		}
		return elem;
	}
}
