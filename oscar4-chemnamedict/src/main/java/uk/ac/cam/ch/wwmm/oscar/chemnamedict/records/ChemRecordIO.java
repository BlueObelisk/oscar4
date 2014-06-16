package uk.ac.cam.ch.wwmm.oscar.chemnamedict.records;

import nu.xom.Element;

public class ChemRecordIO {

	public static Element toXML(IChemRecord record) {
		Element elem = new Element("record");
		
		if (record instanceof IInChIChemRecord) {
			Element inchiElem = new Element("InChI");
			inchiElem.appendChild(
				((IInChIChemRecord)record).getInChI()
			);
			elem.appendChild(inchiElem);
		}
		
		if (record instanceof IStdInChIChemRecord) {
			Element stdInchiElem = new Element("StdInChI");
			stdInchiElem.appendChild(
				((IStdInChIChemRecord)record).getStdInChI()
			);
			elem.appendChild(stdInchiElem);
		}
		
		if (record instanceof IStdInChIKeyChemRecord) {
			Element stdInchiKeyElem = new Element("StdInChIKey");
			stdInchiKeyElem.appendChild(
				((IStdInChIKeyChemRecord)record).getStdInChIKey()
			);
			elem.appendChild(stdInchiKeyElem);
		}
		
		if (record instanceof ISMILESChemRecord) {
			ISMILESChemRecord smilesRecord = (ISMILESChemRecord)record;
			if(smilesRecord.getSMILES() != null) {
				Element smilesElem = new Element("SMILES");
				smilesElem.appendChild(smilesRecord.getSMILES());
				elem.appendChild(smilesElem);				
			}
		}
		
		for(String name : record.getNames()) {
			Element nameElem = new Element("name");
			nameElem.appendChild(name);
			elem.appendChild(nameElem);								
		}

		if (record instanceof IOntologyChemRecord) {
			for(String ontID : ((IOntologyChemRecord)record).getOntologyIdentifiers()) {
				Element ontIDElem = new Element("ontID");
				ontIDElem.appendChild(ontID);
				elem.appendChild(ontIDElem);								
			}
		}
		return elem;
	}
}
