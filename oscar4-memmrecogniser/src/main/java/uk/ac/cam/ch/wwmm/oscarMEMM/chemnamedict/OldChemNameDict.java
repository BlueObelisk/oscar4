package uk.ac.cam.ch.wwmm.oscarMEMM.chemnamedict;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Serializer;
import uk.ac.cam.ch.wwmm.oscarMEMM.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscarMEMM.xmltools.XOMTools;

/** Name to structure dictionary, holds active data in memory, now obsolete.
 * The data can be written out and read in as XML. At some point there will need
 * to be an SQL version of this.
 * 
 * @author ptc24
 * @deprecated Use ChemNameDict instead.
 *
 */
public class OldChemNameDict {
	
	private class IDMapping {
		int nameID;
		int chemID;
		
		IDMapping(int ni, int ci) {
			nameID = ni;
			chemID = ci;
		}		
	}
	
	private class OntologyMapping {
		int chemID;
		String ontologyID;
		
		OntologyMapping(int ci, String oi) {
			chemID = ci;
			ontologyID = oi;
		}		
		
	}
	
	private Map<String, Integer> nameToNameID;
	private Map<Integer, String> nameIDToName;
	private Set<IDMapping> idMap;
	private Set<OntologyMapping> ontMap;
	private Map<String, Integer> inchiToChemID;
	private Map<Integer, String> chemIDToInchi;
	private Map<Integer, String> chemIDToSmiles;
	private int currentNameID;
	private int currentChemID;
	
	private Set<String> stopWords;
	private Set<String> triedPubChem;
	private ReadWriteLock rwLock;
	
	public OldChemNameDict() {
		super();
		rwLock = new ReentrantReadWriteLock();
		currentNameID = 0;
		currentChemID = 0;
		nameToNameID = new HashMap<String, Integer>();
		nameIDToName = new HashMap<Integer, String>();
		idMap = new HashSet<IDMapping>();
		ontMap = new HashSet<OntologyMapping>();
		inchiToChemID = new HashMap<String, Integer>();
		chemIDToInchi = new HashMap<Integer, String>();		
		chemIDToSmiles = new HashMap<Integer, String>();		
		stopWords = new HashSet<String>();
		triedPubChem = new HashSet<String>();
	}
	
	public void addStopWord(String word) {
		try {
			rwLock.writeLock().lock();
			stopWords.add(StringTools.normaliseName(word));
		} finally {
			rwLock.writeLock().unlock();			
		}
	}
	
	public void addOntologyId(String ontId, String inchi) throws Exception {
		if(inchi == null) throw new Exception();
		try {
			rwLock.writeLock().lock();
			int chemID;
			if(!inchiToChemID.containsKey(inchi)) {
				chemID = ++currentChemID;
				inchiToChemID.put(inchi, chemID);
				chemIDToInchi.put(chemID, inchi);
			} else {
				chemID = inchiToChemID.get(inchi);
			}
			
			OntologyMapping om = new OntologyMapping(chemID, ontId);
			ontMap.add(om);
		} finally {
			rwLock.writeLock().unlock();
		}
	}
	
	public void addChemical(String name, String smiles, String inchi) throws Exception {
		try {
			if(inchi == null) throw new Exception();
			
			if(smiles != null) {
				smiles = smiles.replaceAll("\\s+", "");
			}
			
			// Checking smiles vs inchi would be *nice*, if the tools worked...
			
			/*//System.out.println(name + " " + smiles + " " + inchi);
			 // Eliminate stereochemical smiles until we can do something useful with them.
			  //if(smiles.contains("@") || smiles.contains("/") || smiles.contains("\\")) smiles = null;
			   if(inchiToChemID.containsKey(inchi)) {
			   String mySmiles = chemIDToSmiles.get(inchiToChemID.get(inchi));
			   if(mySmiles != null || mySmiles == null) {
			   smiles = mySmiles;
			   //System.out.println("Cache hit");
			    } else {
			    smiles = SMILESTools.validateAndDearomatiseSMILES(smiles, inchi);
			    }
			    } else {
			    smiles = SMILESTools.validateAndDearomatiseSMILES(smiles, inchi);			
			    }*/
			
			name = StringTools.normaliseName(name);
			
			/* Save some time */
			if(nameToNameID.containsKey(name) && inchiToChemID.containsKey(inchi)) {
				if(getInChI(name).contains(inchi)) return;
			}

			/* Now we're committed to writing */
			rwLock.writeLock().lock();
			
			int nameID;
			if(!nameToNameID.containsKey(name)) {
				nameID = ++currentNameID;
				nameToNameID.put(name, nameID);
				nameIDToName.put(nameID, name);
			} else {
				nameID = nameToNameID.get(name);
			}
			
			int chemID;
			if(!inchiToChemID.containsKey(inchi)) {
				chemID = ++currentChemID;
				inchiToChemID.put(inchi, chemID);
				chemIDToInchi.put(chemID, inchi);
			} else {
				chemID = inchiToChemID.get(inchi);
			}
			if(smiles != null) chemIDToSmiles.put(chemID, smiles);
			
			IDMapping idm = new IDMapping(nameID, chemID);
			idMap.add(idm);
		} finally {
			rwLock.writeLock().tryLock();
			rwLock.writeLock().unlock();
		}
	}
	
	public boolean hasName(String queryName) {
		try {
			rwLock.readLock().lock();
			queryName = StringTools.normaliseName(queryName);
			return nameToNameID.containsKey(queryName);
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	public boolean hasStopWord(String queryWord) {
		try {
			rwLock.readLock().lock();
			queryWord = StringTools.normaliseName(queryWord);
			return stopWords.contains(queryWord);
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	public Set<String> getSMILES(String queryName) {
		try {
			rwLock.readLock().lock();
			Set<Integer> chemIDs = getChemIDSFromName(queryName);
			if(chemIDs == null) return null;
			Set<String> results = new HashSet<String>();
			for(int i : chemIDs) {
				String s = chemIDToSmiles.get(i);
				if(s != null) results.add(s);
			}
			return results;			
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	public String getShortestSMILES(String queryName) {
		try {
			rwLock.readLock().lock();
			String s = null;
			Set<String> smiles = getSMILES(queryName);
			if(smiles == null) return null;
			for(String smile : smiles) {
				if(s == null || s.length() > smile.length()) s = smile;
			}
			return s;
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	public Set<String> getInChI(String queryName) {
		try {
			rwLock.readLock().lock();
			Set<Integer> chemIDs = getChemIDSFromName(queryName);
			if(chemIDs == null) return null;
			Set<String> results = new HashSet<String>();
			for(int i : chemIDs) {
				String s = chemIDToInchi.get(i);
				if(s != null) results.add(s);
			}
			return results;
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	public String getInChIforShortestSMILES(String queryName) {
		try {
			rwLock.readLock().lock();
			Set<Integer> chemIDs = getChemIDSFromName(queryName);
			if(chemIDs == null) return null;
			String s = null;
			int currentId = -1;
			for(int i : chemIDs) {
				String smile = chemIDToSmiles.get(i);
				if(smile == null) continue;
				if(s == null || s.length() > smile.length()) {
					s = smile;
					currentId = i;
				}
			}
			if(currentId == -1) {
				return null;
			} else {
				return chemIDToInchi.get(currentId);
			}
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	public Set<String> getNames() {
		try {
			rwLock.readLock().lock();
			return nameToNameID.keySet();
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	public Set<String> getNames(String queryInchi) {
		try {
			rwLock.readLock().lock();
			Set<Integer> nameIDs = getNameIDSFromInchi(queryInchi);
			if(nameIDs == null) return null;
			Set<String> results = new HashSet<String>();
			for(int i : nameIDs) {
				String s = nameIDToName.get(i);
				if(s != null) results.add(s);
			}
			return results;				
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	public Set<String> getOntologyIDsFromInChI(String queryInchi) {
		try {
			rwLock.readLock().lock();
			if(!inchiToChemID.containsKey(queryInchi)) return null;
			int chemID = inchiToChemID.get(queryInchi);
			Set<String> resultsSet = new HashSet<String>();
			for(OntologyMapping om : ontMap) {
				if(om.chemID == chemID) {
					resultsSet.add(om.ontologyID);
				}
			}
			return resultsSet;
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	public Set<Set<String>> getSynonyms(String queryName) {
		try {
			rwLock.readLock().lock();
			Set<Integer> chemIDs = getChemIDSFromName(queryName);
			if(chemIDs == null) return null;
			Set<Set<String>> results = new HashSet<Set<String>>();
			for(int chemID : chemIDs) {
				Set<String> resultsSet = new HashSet<String>();
				for(IDMapping idm : idMap) {
					if(idm.chemID == chemID && nameIDToName.containsKey(idm.nameID)) {
						resultsSet.add(nameIDToName.get(idm.nameID));
					}
				}
				if(resultsSet.size() > 0) results.add(resultsSet);
			}
			return results;
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	
	public boolean hasOntologyId(String ontId) {
		try {
			rwLock.readLock().lock();
			for(OntologyMapping om : ontMap) {
				//System.out.println(om.ontologyID);
				if(om.ontologyID.equals(ontId)) {
					return true;
				}
			}
			return false;
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	public Set<String> getInchisByOntologyId(String ontId) {
		try {
			rwLock.readLock().lock();
			Set<String> inchis = new HashSet<String>();
			for(OntologyMapping om : ontMap) {
				//System.out.println(om.ontologyID);
				if(om.ontologyID.equals(ontId)) {
					inchis.add(chemIDToInchi.get(om.chemID));
				}
			}
			return inchis;
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	public Set<String> getStopWords() {
		try {
			rwLock.readLock().lock();
			return new HashSet<String>(stopWords);
		} finally {
			rwLock.readLock().unlock();			
		}
	}
	
	public boolean triedInPubChem(String s) {
		try {
			rwLock.readLock().lock();
			if(triedPubChem.contains(s)) {
				return true;
			} else {
				triedPubChem.add(s);
				return false;
			}
		} finally {
			rwLock.readLock().unlock();			
		}
	}
		
	public Document getXML() throws Exception {
		try {
			rwLock.readLock().lock();
			Element cnde = new Element("chemnamedict");
			cnde.addAttribute(new Attribute("currentChemID", Integer.toString(currentChemID)));
			cnde.addAttribute(new Attribute("currentNameID", Integer.toString(currentNameID)));
			
			Element names = new Element("names");
			List<String> nameList = new ArrayList<String>(nameToNameID.keySet());
			StringTools.sortStringList(nameList, nameToNameID);
			Collections.reverse(nameList);
			for(String n : nameList) {
				Element name = new Element("name");
				name.appendChild(n);
				name.addAttribute(new Attribute("id", Integer.toString(nameToNameID.get(n))));
				names.appendChild(name);
			}
			cnde.appendChild(names);
			
			Element inchis = new Element("inchis");
			List<String> inchiList = new ArrayList<String>(inchiToChemID.keySet());
			StringTools.sortStringList(inchiList, inchiToChemID);
			Collections.reverse(inchiList);
			for(String i : inchiList) {
				Element inchi = new Element("inchi");
				inchi.appendChild(i);
				inchi.addAttribute(new Attribute("id", Integer.toString(inchiToChemID.get(i))));
				inchis.appendChild(inchi);
			}
			cnde.appendChild(inchis);
			
			Element smiles = new Element("smiles");
			List<Integer> cidList = new ArrayList<Integer>(chemIDToSmiles.keySet());
			Collections.sort(cidList);
			for(int chemID : cidList) {
				Element smile = new Element("smile");
				smile.appendChild(chemIDToSmiles.get(chemID));
				smile.addAttribute(new Attribute("id", Integer.toString(chemID)));
				smiles.appendChild(smile);
			}
			cnde.appendChild(smiles);
			
			Element mappings = new Element("mappings");
			for(IDMapping idm : idMap) {
				Element mapping = new Element("mapping");
				mapping.addAttribute(new Attribute("nameID", Integer.toString(idm.nameID)));
				mapping.addAttribute(new Attribute("chemID", Integer.toString(idm.chemID)));
				mappings.appendChild(mapping);
			}
			cnde.appendChild(mappings);
			
			Element stops = new Element("stops");
			for(String s : stopWords) {
				Element stop = new Element("stop");
				stop.appendChild(s);
				stops.appendChild(stop);
			}
			cnde.appendChild(stops);
			
			Element tpc = new Element("triedPubChem");
			for(String s : triedPubChem) {
				Element tpce = new Element("tpc");
				tpce.appendChild(s);
				tpc.appendChild(tpce);
			}
			cnde.appendChild(tpc);
			
			Element ont = new Element("ontology");
			for(OntologyMapping om : ontMap) {
				Element onte = new Element("ontEntry");
				onte.appendChild(om.ontologyID);
				onte.addAttribute(new Attribute("chemID", Integer.toString(om.chemID)));
				ont.appendChild(onte);
			}
			cnde.appendChild(ont);
			
			return new Document(cnde);
		} finally {
			rwLock.readLock().unlock();
		}
	}
	
	public void writeToFile(File f) throws Exception {
		writeToFile(new FileOutputStream(f));
	}
	
	public synchronized void writeToFile(OutputStream outStr) throws Exception {
		Serializer s = new Serializer(outStr);
		s.setIndent(2);
		s.write(getXML());
	}
	
	public void readFromFile(File f) throws Exception {
		Document doc = new Builder().build(f);
		readFromDocument(doc);
	}
	
	public void readFromDocument(Document doc) throws Exception {
		try {
			rwLock.writeLock().lock();
			Element cnde = doc.getRootElement();
			currentChemID = Integer.parseInt(cnde.getAttributeValue("currentChemID"));
			currentNameID = Integer.parseInt(cnde.getAttributeValue("currentNameID"));
			
			Element names = cnde.getFirstChildElement("names");
			Elements nameElems = names.getChildElements();
			for(int i=0;i<nameElems.size();i++) {
				Element nameElem = nameElems.get(i);
				String name = nameElem.getChild(0).getValue();
				int id = Integer.parseInt(nameElem.getAttributeValue("id"));
				nameToNameID.put(name, id);
				nameIDToName.put(id, name);
			}
			
			Element inchis = cnde.getFirstChildElement("inchis");
			Elements inchiElems = inchis.getChildElements();
			for(int i=0;i<inchiElems.size();i++) {
				Element inchiElem = inchiElems.get(i);
				String inchi = inchiElem.getChild(0).getValue();
				int id = Integer.parseInt(inchiElem.getAttributeValue("id"));
				inchiToChemID.put(inchi, id);
				chemIDToInchi.put(id, inchi);
			}
			
			Element smiles = cnde.getFirstChildElement("smiles");
			Elements smileElems = smiles.getChildElements();
			for(int i=0;i<smileElems.size();i++) {
				Element smileElem = smileElems.get(i);
				String smile = smileElem.getChild(0).getValue();
				int id = Integer.parseInt(smileElem.getAttributeValue("id"));
				chemIDToSmiles.put(id, smile);
			}
			
			Element mappings = cnde.getFirstChildElement("mappings");
			Elements mappingElems = mappings.getChildElements();
			for(int i=0;i<mappingElems.size();i++) {
				Element mapping = mappingElems.get(i);
				int chemID = Integer.parseInt(mapping.getAttributeValue("chemID"));
				int nameID = Integer.parseInt(mapping.getAttributeValue("nameID"));
				IDMapping idm = new IDMapping(nameID, chemID);
				idMap.add(idm);
			}
			
			Element stops = cnde.getFirstChildElement("stops");
			if(stops != null) {
				Elements stopElems = stops.getChildElements();
				for(int i=0;i<stopElems.size();i++) {
					Element stop = stopElems.get(i);
					stopWords.add(stop.getValue());
				}
			}	
			
			Element tpc = cnde.getFirstChildElement("triedPubChem");
			if(tpc != null) {
				Elements tpcElems = tpc.getChildElements();
				for(int i=0;i<tpcElems.size();i++) {
					Element tpce = tpcElems.get(i);
					triedPubChem.add(tpce.getValue());
				}
			}	
			
			Element ont = cnde.getFirstChildElement("ontology");
			if(ont != null) {
				Elements ontElems = ont.getChildElements();
				for(int i=0;i<ontElems.size();i++) {
					Element onte = ontElems.get(i);
					ontMap.add(new OntologyMapping(Integer.parseInt(onte.getAttributeValue("chemID")), onte.getValue()));
				}
			}	
		} finally {
			rwLock.writeLock().unlock();
		}
	}
	
	private Set<Integer> getChemIDSFromName(String queryName) {
		queryName = StringTools.normaliseName(queryName);
		if(!nameToNameID.containsKey(queryName)) return null;
		int nameID = nameToNameID.get(queryName);
		Set<Integer> results = new HashSet<Integer>();
		for(IDMapping idm : idMap) {
			if(idm.nameID == nameID) results.add(idm.chemID);
		}
		if(results.size() == 0) return null;
		return results;
	}
	
	private Set<Integer> getNameIDSFromInchi(String queryInchi) {
		if(!inchiToChemID.containsKey(queryInchi)) return null;
		int chemID = inchiToChemID.get(queryInchi);
		Set<Integer> results = new HashSet<Integer>();
		for(IDMapping idm : idMap) {
			if(idm.chemID == chemID) results.add(idm.nameID);
		}
		if(results.size() == 0) return null;
		return results;
		
	}

	public int makeHash() throws Exception {
		return XOMTools.documentHash(getXML());
	}

	void exportToNewChemNameDict(ChemNameDict ncnd) throws Exception {
		for(int i=0;i<=currentChemID;i++) {
			if(chemIDToInchi.containsKey(i)) {
				String inchi = chemIDToInchi.get(i);
				String smiles = chemIDToSmiles.get(i);
				Set<String> names = new HashSet<String>();
				for(IDMapping idm : idMap) {
					if(idm.chemID == i) names.add(nameIDToName.get(idm.nameID));
				}
				Set<String> ontIDs = new HashSet<String>();
				for(OntologyMapping om : ontMap) {
					if(om.chemID == i) {
						ontIDs.add(om.ontologyID);
					}
				}
				ncnd.addChemRecord(inchi, smiles, names, ontIDs);
			}
		}
		for(String stopword : stopWords) {
			ncnd.addStopWord(stopword);
		}
	}
	
	
	/*public static void main(String [] args) {
		System.out.println(ChemNameDictSingleton.hasOntId("CHEBI:30341"));
	}*/
	
}
