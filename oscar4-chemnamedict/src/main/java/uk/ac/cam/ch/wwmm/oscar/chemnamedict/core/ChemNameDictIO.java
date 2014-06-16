package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.ChemRecord;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.ChemRecordIO;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.IChemRecord;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;

/**
 * Name to structure dictionary, holds active data in memory, a replacement
 * class for OldChemNameDict. 
 * 
 * Improvements include the storage of orphan chemical names (i.e. those with no
 * structure associated) and a simpler XML serialisation (with no need for ID
 * numbers on everything).
 * 
 * ChemNameDict stores Chemical Records, Orphan Names, and Stopwords. 
 * Chemical Records must have an InChI identifier, may have a SMILES string,
 * and an unlimited number of names and ontology identifiers. The InChI
 * identifiers are unique; it is not possible to have two records with the same
 * identifier. Orphan Names are names which have no InChI; a name can only be
 * an Orphan Name if it does not appear as a name in any chemical record.
 * Stopwords are things that the system should not recognise as chemical names.
 * Currently, there is no checking to see if stopwords also appear in other
 * name lists.
 * 
 * Note that in chemical records, the aim is to associate names with InChIs, and
 * ontology identifiers with InChIs, rather than to directly associate names
 * with ontology identifiers. If you need to associate names directly with
 * ontology identifiers, list them as orphan names here, and associate them
 * with their identifiers in ontology.txt.
 * 
 * @author ptc24
 * @author egonw
 */
public final class ChemNameDictIO {

	private static Document toXML(IChemNameDict dictionary) {
		Element cnde = new Element("newchemnamedict");

		Element records = new Element("records");
		for(IChemRecord record : dictionary.getChemRecords()) {
			records.appendChild(ChemRecordIO.toXML(record));
		}
		cnde.appendChild(records);

		return new Document(cnde);
	}
	
	public static void readXML(Document doc, IMutableChemNameDict dictionary) {
		Element root = doc.getRootElement();

		if (!"newchemnamedict".equals(root.getLocalName())) {
			throw new IllegalArgumentException("Root tag name should be 'newchemnamedict' but was '"
						+ root.getLocalName() + "'");
		}

		for (int i = 0; i < root.getChildCount(); i++) {
			if(root.getChild(i) instanceof Element) {
				Element elem = (Element)root.getChild(i);
				if(elem.getLocalName().equals("stops")) {
					for (int j = 0; j < elem.getChildCount(); j++) {
						if(elem.getChild(j) instanceof Element)
							dictionary.addStopWord(elem.getChild(j).getValue());
					}
				} else if(elem.getLocalName().equals("orphanNames")) {
					for (int j = 0; j < elem.getChildCount(); j++) {
						if(elem.getChild(j) instanceof Element)
							dictionary.addName(elem.getChild(j).getValue());
					}					
				} else if(elem.getLocalName().equals("records")) {
					for (int j = 0; j < elem.getChildCount(); j++) {
						if(elem.getChild(j) instanceof Element) {
							dictionary.addChemRecord(
								xmlToRecord((Element)elem.getChild(j))
							);
						}
					}										
				}
			}
		}
	}
	
	private static ChemRecord xmlToRecord(Element elem) {
		if (!"record".equals(elem.getLocalName())) {
			throw new IllegalArgumentException("Tag name should be 'record' but was '" + elem.getLocalName() + "'");
		}

		ChemRecord record = new ChemRecord();		

		Elements stdInchis = elem.getChildElements("StdInChI");
		if (stdInchis.size() != 1) {
			throw new IllegalStateException("stdInchis.size() should be exactly 1, but was " + stdInchis.size());
		}
		record.setStdInChI(stdInchis.get(0).getValue());
		
		Elements stdInchiKeys = elem.getChildElements("StdInChIKey");
		if (stdInchiKeys.size() != 1) {
			throw new IllegalStateException("stdInchiKeys.size() should be exactly 1, but was " + stdInchiKeys.size());
		}
		record.setStdInChIKey(stdInchiKeys.get(0).getValue());
		
		
		Elements smiless = elem.getChildElements("SMILES");

		if (smiless.size() > 1) {
			throw new IllegalStateException("smiless.size() should not more than one, but was " + smiless.size());
		} else if(smiless.size() == 1) {
			record.setSMILES(smiless.get(0).getValue());
		}

		Elements names = elem.getChildElements("name");
		
		for (int i = 0; i < names.size(); i++) {
			record.addName(names.get(i).getValue());
		}
		
		Elements ontIDs = elem.getChildElements("ontID");
		
		for (int i = 0; i < ontIDs.size(); i++) {
			record.addOntologyIdentifier(ontIDs.get(i).getValue());
		}		
		
		return record;
	}
	
	public static void writeToFile(File f, IChemNameDict dictionary) throws IOException {
		writeToFile(new FileOutputStream(f), dictionary);
	}
	
	public static synchronized void writeToFile(OutputStream outStr,
			IChemNameDict dictionary) throws IOException {
		Serializer s = new Serializer(outStr);
		s.setIndent(2);
		s.write(toXML(dictionary));
	}
	
	/*
	 * TODO: Wrap ValidityException and ParsingException into OscarException?
	 * FIXME: This method is never called in OSCAR4 project. Perhaps it should be inlined?
	 */
	public static void readFromFile(File f, IMutableChemNameDict dictionary) throws IOException, ValidityException, ParsingException {
		Document doc = new Builder().build(f);
		readXML(doc, dictionary);
	}
	
	
	@Deprecated
	//TODO this isn't called - do we need it?
	public static int makeHash(IChemNameDict dictionary) {
		return XOMTools.documentHash(toXML(dictionary));
	}

}
