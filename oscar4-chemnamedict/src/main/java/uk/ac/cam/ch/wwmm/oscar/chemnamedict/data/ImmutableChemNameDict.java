package uk.ac.cam.ch.wwmm.oscar.chemnamedict.data;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**
 * An immutable name-to-structure dictionary which holds active data in memory.
 * It stores chemical records ({@link ChemRecord}), orphan names (see below),
 * and stop words.  records must have an InChI identifier, may have a SMILES
 * string, and an unlimited number of names and ontology identifiers. The InChI
 * identifiers are unique; it is not possible to have two records with the same
 * identifier.
 * 
 * <p>Orphan Names are names which have no InChI; a name can only be an Orphan
 * Name if it does not appear as a name in any chemical record. Stopwords are
 * things that the system should not recognise as chemical names.
 * 
 * <p>Note that in chemical records, the aim is to associate names with InChIs,
 * and ontology identifiers with InChIs, rather than to directly associate names
 * with ontology identifiers. If you need to associate names directly with
 * ontology identifiers, list them as orphan names here.
 *
 * @author ptc24
 * @author egonw
 */
public class ImmutableChemNameDict implements IChemNameDict {

	protected Set<ChemRecord> chemRecords;
	protected Map<String,ChemRecord> indexByInchi;
	protected Map<String,Set<ChemRecord>> indexByName;
	protected Set<String> orphanNames;
	protected Set<String> stopWords;

	private URI uri;

	public ImmutableChemNameDict(URI uri) {
		this.uri = uri;
		chemRecords = new HashSet<ChemRecord>();
		indexByInchi = new HashMap<String,ChemRecord>();
		indexByName = new HashMap<String,Set<ChemRecord>>();
		orphanNames = new HashSet<String>();
		stopWords = new HashSet<String>();
	}
	
	public URI getURI() {
		return this.uri;
	}

	public boolean hasStopWord(String queryWord) {
		queryWord = StringTools.normaliseName(queryWord);
		return stopWords.contains(queryWord);
	}

	public Set<String> getStopWords() {
		return new HashSet<String>(stopWords);
	}

	public boolean hasName(String queryName) {
		queryName = StringTools.normaliseName(queryName);
		return orphanNames.contains(queryName) || indexByName.containsKey(queryName);
	}

	public Set<String> getSMILES(String queryName) {
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
	}

	public String getShortestSMILES(String queryName) {
		String s = null;
		Set<String> smiles = getSMILES(queryName);
		if(smiles == null) return null;
		for(String smile : smiles) {
			if(s == null || s.length() > smile.length()) s = smile;
		}
		return s;
	}

	public Set<String> getInChI(String queryName) {
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
	}

	public String getInChIforShortestSMILES(String queryName) {
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
	}

	public Set<String> getNames(String inchi) {
		if(!indexByInchi.containsKey(inchi)) return null;
		Set<String> names = new HashSet<String>(indexByInchi.get(inchi).names);
		if(names.size() == 0) return null;
		return names;
	}

	public Set<String> getNames() {
		Set<String> results = new HashSet<String>();
		results.addAll(orphanNames);
		results.addAll(indexByName.keySet());
		return results;
	}

	public Set<String> getOrphanNames() {
		Set<String> results = new HashSet<String>();
		results.addAll(orphanNames);
		return results;
	}

	public Set<ChemRecord> getChemRecords() {
		Set<ChemRecord> results = new HashSet<ChemRecord>();
		results.addAll(chemRecords);
		return results;
	}

}
