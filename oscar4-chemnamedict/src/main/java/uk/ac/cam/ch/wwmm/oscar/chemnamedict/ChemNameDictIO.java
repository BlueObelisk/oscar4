package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Serializer;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.ChemRecord;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.ChemRecordIO;
import uk.ac.cam.ch.wwmm.oscar.tools.XOMTools;

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

	private static Document toXML(IChemNameDict dictionary) throws Exception {
		Element cnde = new Element("newchemnamedict");

		Element stops = new Element("stops");
		for(String s : dictionary.getStopWords()) {
			Element stop = new Element("stop");
			stop.appendChild(s);
			stops.appendChild(stop);
		}
		cnde.appendChild(stops);

		Element orphans = new Element("orphanNames");
		for(String n : dictionary.getOrphanNames()) {
			Element name = new Element("name");
			name.appendChild(n);
			orphans.appendChild(name);
		}
		cnde.appendChild(orphans);

		Element records = new Element("records");
		for(ChemRecord record : dictionary.getChemRecords()) {
			records.appendChild(ChemRecordIO.toXML(record));
		}
		cnde.appendChild(records);

		return new Document(cnde);
	}
	
	public static void readXML(Document doc, IMutableChemNameDict dictionary) throws Exception {
		Element root = doc.getRootElement();
		if(root.getLocalName().equals("newchemnamedict")) {
			for(int i=0;i<root.getChildCount();i++) {
				if(root.getChild(i) instanceof Element) {
					Element elem = (Element)root.getChild(i);
					if(elem.getLocalName().equals("stops")) {
						for(int j=0;j<elem.getChildCount();j++) {
							if(elem.getChild(j) instanceof Element)
								dictionary.addStopWord(elem.getChild(j).getValue());
						}
					} else if(elem.getLocalName().equals("orphanNames")) {
						for(int j=0;j<elem.getChildCount();j++) {
							if(elem.getChild(j) instanceof Element)
								dictionary.addName(elem.getChild(j).getValue());
						}					
					} else if(elem.getLocalName().equals("records")) {
						for(int j=0;j<elem.getChildCount();j++) {
							if(elem.getChild(j) instanceof Element) {
								dictionary.addChemRecord(
									xmlToRecord((Element)elem.getChild(j))
								);
							}
						}										
					}
				}
			}			
		} else {
			throw new Exception();
		}
	}
	
	private static ChemRecord xmlToRecord(Element elem) throws Exception {
		if(!elem.getLocalName().equals("record")) throw new Exception();
		ChemRecord record = new ChemRecord();
		Elements inchis = elem.getChildElements("InChI");
		if(inchis.size() != 1) throw new Exception();
		record.inchi = inchis.get(0).getValue();
		Elements smiless = elem.getChildElements("SMILES");
		if(smiless.size() > 1) {
			throw new Exception();
		} else if(smiless.size() == 1) {
			record.smiles = smiless.get(0).getValue();
		}
		Elements names = elem.getChildElements("name");
		for(int i=0;i<names.size();i++) {
			record.names.add(names.get(i).getValue());
		}
		Elements ontIDs = elem.getChildElements("ontID");
		for(int i=0;i<ontIDs.size();i++) {
			record.ontIDs.add(ontIDs.get(i).getValue());
		}		
		return record;
	}
	
	public static void writeToFile(File f, IChemNameDict dictionary) throws Exception {
		writeToFile(new FileOutputStream(f), dictionary);
	}
	
	public static synchronized void writeToFile(OutputStream outStr,
			IChemNameDict dictionary) throws Exception {
		Serializer s = new Serializer(outStr);
		s.setIndent(2);
		s.write(toXML(dictionary));
	}
	
	public static void readFromFile(File f, IMutableChemNameDict dictionary) throws Exception {
		Document doc = new Builder().build(f);
		readXML(doc, dictionary);
	}
	
	public static int makeHash(IChemNameDict dictionary) throws Exception {
		return XOMTools.documentHash(toXML(dictionary));
	}

}
