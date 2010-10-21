package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Serializer;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.ChemRecord;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
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
public final class ChemNameDict implements ISingleChemNameDict {

	private Set<ChemRecord> chemRecords;
	private Map<String,ChemRecord> indexByInchi;
	private Map<String,Set<ChemRecord>> indexByName;
	private Map<String,Set<ChemRecord>> indexByOntID;
	private Set<String> orphanNames;
	private Set<String> stopWords;

	private ReadWriteLock rwLock;

	public ChemNameDict() {
		rwLock = new ReentrantReadWriteLock();
		chemRecords = new HashSet<ChemRecord>();
		indexByInchi = new HashMap<String,ChemRecord>();
		indexByName = new HashMap<String,Set<ChemRecord>>();
		indexByOntID = new HashMap<String,Set<ChemRecord>>();
		orphanNames = new HashSet<String>();
		stopWords = new HashSet<String>();
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#addStopWord(java.lang.String)
	 */
	public void addStopWord(String word) throws Exception {
		try {
			rwLock.writeLock().lock();
			if(word == null || word.trim().length() == 0) throw new Exception();
			stopWords.add(StringTools.normaliseName(word));
		} finally {
			rwLock.writeLock().unlock();			
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#hasStopWord(java.lang.String)
	 */
	public boolean hasStopWord(String queryWord) {
		try {
			rwLock.readLock().lock();
			queryWord = StringTools.normaliseName(queryWord);
			return stopWords.contains(queryWord);
		} finally {
			rwLock.readLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#getStopWords()
	 */
	public Set<String> getStopWords() {
		try {
			rwLock.readLock().lock();
			return new HashSet<String>(stopWords);
		} finally {
			rwLock.readLock().unlock();			
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#addChemRecord(java.lang.String, java.lang.String, java.util.Set, java.util.Set)
	 */
	public void addChemRecord(String inchi, String smiles, Set<String> names, Set<String> ontIDs) throws Exception {
		ChemRecord record = new ChemRecord();
		record.inchi = inchi;
		record.smiles = smiles;
		if(names != null) record.names.addAll(names);
		if(ontIDs != null) record.ontIDs.addAll(ontIDs);
		addChemRecord(record);
	}
	
	private void addChemRecord(ChemRecord record) throws Exception {
		try {
			rwLock.readLock().lock();
			if(record.inchi != null) {
				String inchi = record.inchi;
				if(indexByInchi.containsKey(inchi)) {
					ChemRecord mergeRecord = indexByInchi.get(inchi);
					for(String name : record.names) {
						name = StringTools.normaliseName(name);
						mergeRecord.names.add(name);
						if(!indexByName.containsKey(name)) {
							indexByName.put(name, new HashSet<ChemRecord>());
						}
						indexByName.get(name).add(mergeRecord);
						orphanNames.remove(name);
					}
					for(String ontID : record.ontIDs) {
						mergeRecord.ontIDs.add(ontID);
						if(!indexByOntID.containsKey(ontID)) {
							indexByOntID.put(ontID, new HashSet<ChemRecord>());
						}
						indexByOntID.get(ontID).add(mergeRecord);
					}
					if(record.smiles != null && mergeRecord.smiles == null)
						mergeRecord.smiles = record.smiles;
				} else {
					// Record is new. Add and index
					chemRecords.add(record);
					indexByInchi.put(inchi, record);
					for(String name : record.names) {
						name = StringTools.normaliseName(name);
						if(!indexByName.containsKey(name)) {
							indexByName.put(name, new HashSet<ChemRecord>());
						}
						indexByName.get(name).add(record);
						orphanNames.remove(name);
					}
					for(String ontID : record.ontIDs) {
						if(!indexByOntID.containsKey(ontID)) {
							indexByOntID.put(ontID, new HashSet<ChemRecord>());
						}
						indexByOntID.get(ontID).add(record);
					}
				}
			} else {
				throw new Exception("Record must have an InChI to be added to ChemNameDict");
			}
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#addName(java.lang.String)
	 */
	public void addName(String name) throws Exception {
		try {
			rwLock.readLock().lock();
			if(name == null || name.trim().length() == 0) throw new Exception();
			name = StringTools.normaliseName(name);
			if(!indexByName.containsKey(name)) orphanNames.add(name);
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#addOntologyId(java.lang.String, java.lang.String)
	 */
	public void addOntologyId(String ontId, String inchi) throws Exception {
		if(inchi == null) throw new Exception();
		try {
			rwLock.writeLock().lock();
			ChemRecord record;
			if(indexByInchi.containsKey(inchi)) {
				record = indexByInchi.get(inchi);
			} else {
				record = new ChemRecord();
				record.inchi = inchi;
				chemRecords.add(record);
				indexByInchi.put(inchi, record);
			}
			record.ontIDs.add(ontId);
			if(!indexByOntID.containsKey(ontId)) indexByOntID.put(ontId, new HashSet<ChemRecord>());
			indexByOntID.get(ontId).add(record);			
		} finally {
			rwLock.writeLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#addChemical(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addChemical(String name, String smiles, String inchi) throws Exception {
		if(inchi == null) throw new Exception();
		ChemRecord record = new ChemRecord();
		record.inchi = inchi;
		record.smiles = smiles;
		record.names.add(name);
		addChemRecord(record);
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#importChemNameDict(uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDict)
	 */
	public void importChemNameDict(ChemNameDict cnd) throws Exception {
		try {
			cnd.rwLock.readLock().lock();
			for(ChemRecord record : cnd.chemRecords) {
				addChemRecord(record);
			}
			for(String orphan : cnd.orphanNames) {
				addName(orphan);
			}
			for(String stop : cnd.stopWords) {
				addStopWord(stop);
			}
		} finally {
			cnd.rwLock.readLock().unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#hasName(java.lang.String)
	 */
	public boolean hasName(String queryName) {
		try {
			rwLock.readLock().lock();
			queryName = StringTools.normaliseName(queryName);
			return orphanNames.contains(queryName) || indexByName.containsKey(queryName);
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#getSMILES(java.lang.String)
	 */
	public Set<String> getSMILES(String queryName) {
		try {
			rwLock.readLock().lock();
			queryName = StringTools.normaliseName(queryName);
			if(indexByName.containsKey(queryName)) {
				Set<String> results = new HashSet<String>();
				for(ChemRecord record : indexByName.get(queryName)) {
					if(record.smiles != null) results.add(record.smiles);
				}
				if(results.size() > 0) return results;
				return null;
			} else {
				return null;
			}
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#getShortestSMILES(java.lang.String)
	 */
	public String getShortestSMILES(String queryName) {
		String s = null;
		Set<String> smiles = getSMILES(queryName);
		if(smiles == null) return null;
		for(String smile : smiles) {
			if(s == null || s.length() > smile.length()) s = smile;
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#getInChI(java.lang.String)
	 */
	public Set<String> getInChI(String queryName) {
		try {
			rwLock.readLock().lock();
			queryName = StringTools.normaliseName(queryName);
			if(indexByName.containsKey(queryName)) {
				Set<String> results = new HashSet<String>();
				for(ChemRecord record : indexByName.get(queryName)) {
					assert(record.inchi != null); 
					results.add(record.inchi);
				}
				if(results.size() > 0) return results;
				return null;
			} else {
				return null;
			}
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#getInChIforShortestSMILES(java.lang.String)
	 */
	public String getInChIforShortestSMILES(String queryName) {
		try {
			rwLock.readLock().lock();
			queryName = StringTools.normaliseName(queryName);
			if(indexByName.containsKey(queryName)) {
				String currentInchi = null;
				String currentSmiles = null;
				for(ChemRecord record : indexByName.get(queryName)) {
					assert(record.inchi != null); 
					if(currentInchi == null) {
						currentInchi = record.inchi;
						currentSmiles = record.smiles;
					} else if(record.smiles == null && currentSmiles == null) {
						if(currentInchi.compareTo(record.inchi) > 0) {
							currentInchi = record.inchi;
							currentSmiles = record.smiles;															
						}						
					} else if(record.smiles == null) {
						// Do nothing, we prefer InChIs with associated smiles
					} else if(currentSmiles == null) {
						currentInchi = record.inchi;
						currentSmiles = record.smiles;																					
					} else if(currentSmiles.length() == record.smiles.length()) {
						if(currentSmiles.equals(record.smiles)) {
							if(currentInchi.compareTo(record.inchi) > 0) {
								currentInchi = record.inchi;
								currentSmiles = record.smiles;															
							}
						} else if(currentSmiles.compareTo(record.smiles) > 0) {
							currentInchi = record.inchi;
							currentSmiles = record.smiles;							
						}
					} else if(currentSmiles.length() > record.smiles.length()) {
						currentInchi = record.inchi;
						currentSmiles = record.smiles;
					} //Otherwise do nothing
				}
				return currentInchi;
			} else {
				return null;
			}
		} finally {
			rwLock.readLock().unlock();			
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#getNames(java.lang.String)
	 */
	public Set<String> getNames(String inchi) {
		try {
			rwLock.readLock().lock();
			if(!indexByInchi.containsKey(inchi)) return null;
			Set<String> names = new HashSet<String>(indexByInchi.get(inchi).names);
			if(names.size() == 0) return null;
			return names;
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#getNames()
	 */
	public Set<String> getNames() {
		try {
			rwLock.readLock().lock();
			Set<String> results = new HashSet<String>();
			results.addAll(orphanNames);
			results.addAll(indexByName.keySet());
			return results;
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#getOntologyIDsFromInChI(java.lang.String)
	 */
	public Set<String> getOntologyIDsFromInChI(String queryInchi) {
		try {
			rwLock.readLock().lock();
			if(indexByInchi.containsKey(queryInchi)) {
				return new HashSet<String>(indexByInchi.get(queryInchi).ontIDs);
			}
			return new HashSet<String>();
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#hasOntologyId(java.lang.String)
	 */
	public boolean hasOntologyId(String ontId) {
		try {
			rwLock.readLock().lock();
			return indexByOntID.containsKey(ontId);
		} finally {
			rwLock.readLock().unlock();			
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict#getInchisByOntologyId(java.lang.String)
	 */
	public Set<String> getInchisByOntologyId(String ontId) {
		try {
			rwLock.readLock().lock();
			Set<String> inchis = new HashSet<String>();
			if(indexByOntID.containsKey(ontId)) {
				for(ChemRecord record : indexByOntID.get(ontId)) {
					inchis.add(record.inchi);
				}
			}
			return inchis;
		} finally {
			rwLock.readLock().unlock();			
		}
	}

	private Document toXML() throws Exception {
		try {
			rwLock.readLock().lock();
			Element cnde = new Element("newchemnamedict");
						
			Element stops = new Element("stops");
			for(String s : stopWords) {
				Element stop = new Element("stop");
				stop.appendChild(s);
				stops.appendChild(stop);
			}
			cnde.appendChild(stops);
			
			Element orphans = new Element("orphanNames");
			for(String n : orphanNames) {
				Element name = new Element("name");
				name.appendChild(n);
				orphans.appendChild(name);
			}
			cnde.appendChild(orphans);
			
			Element records = new Element("records");
			for(ChemRecord r : chemRecords) {
				records.appendChild(r.toXML());
			}
			cnde.appendChild(records);
			
			return new Document(cnde);
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	public void readXML(Document doc) throws Exception {
		Element root = doc.getRootElement();
		if(root.getLocalName().equals("newchemnamedict")) {
			for(int i=0;i<root.getChildCount();i++) {
				if(root.getChild(i) instanceof Element) {
					Element elem = (Element)root.getChild(i);
					if(elem.getLocalName().equals("stops")) {
						for(int j=0;j<elem.getChildCount();j++) {
							if(elem.getChild(j) instanceof Element) addStopWord(elem.getChild(j).getValue());
						}
					} else if(elem.getLocalName().equals("orphanNames")) {
						for(int j=0;j<elem.getChildCount();j++) {
							if(elem.getChild(j) instanceof Element) addName(elem.getChild(j).getValue());
						}					
					} else if(elem.getLocalName().equals("records")) {
						for(int j=0;j<elem.getChildCount();j++) {
							if(elem.getChild(j) instanceof Element) {
								addChemRecord(xmlToRecord((Element)elem.getChild(j)));
							}
						}										
					}
				}
			}			
		} else {
			throw new Exception();
		}
	}
	
	private ChemRecord xmlToRecord(Element elem) throws Exception {
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
	
	public void writeToFile(File f) throws Exception {
		writeToFile(new FileOutputStream(f));
	}
	
	public synchronized void writeToFile(OutputStream outStr) throws Exception {
		Serializer s = new Serializer(outStr);
		s.setIndent(2);
		s.write(toXML());
	}
	
	public void readFromFile(File f) throws Exception {
		Document doc = new Builder().build(f);
		readXML(doc);
	}
	
	public int makeHash() throws Exception {
		return XOMTools.documentHash(toXML());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		File f = new File(new File(OscarProperties.getInstance().workspace, "chemnamedict"), "chemnamedict.xml");
		Document doc = new Builder().build(f);
		ChemNameDict ncnd = new ChemNameDict();
		ncnd.readXML(doc);
		doc = ncnd.toXML();
		System.out.println(doc.toXML().length());
		ncnd = new ChemNameDict();
		ncnd.readXML(doc);
		doc = ncnd.toXML();
		System.out.println(doc.toXML().length());
		
		/*NewChemNameDict ncnd = new NewChemNameDict();
		ChemNameDict cnd = ChemNameDictSingleton.getChemNameDictInstance();
		System.out.println("Ready!");
		long time = System.currentTimeMillis();
		cnd.exportToNewChemNameDict(ncnd);
		System.out.println("Exported in " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();
		Document doc = ncnd.toXML();
		Serializer ser = new Serializer(System.out);
		ser.setIndent(2);		
		ser.write(doc);

		System.out.println("XML output in " + (System.currentTimeMillis() - time));
		
		time = System.currentTimeMillis();
		NewChemNameDict ncnd2 = new NewChemNameDict();
		ncnd2.readXML(doc);
		System.out.println("XML read in " + (System.currentTimeMillis() - time));
		Document doc2 = ncnd2.toXML();
		
		ser = new Serializer(System.out);
		ser.setIndent(2);		
		ser.write(doc2);
		System.out.println(doc.toXML().length());
		System.out.println(doc2.toXML().length());*/
	}

}
