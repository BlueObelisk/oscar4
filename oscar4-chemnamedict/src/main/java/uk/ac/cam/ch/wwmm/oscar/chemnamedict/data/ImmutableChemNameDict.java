package uk.ac.cam.ch.wwmm.oscar.chemnamedict.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictIO;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IInChIProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ISMILESProvider;
import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
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
public class ImmutableChemNameDict implements IChemNameDict, IInChIProvider, ISMILESProvider {

	protected Set<IChemRecord> chemRecords;
	protected Map<String,IChemRecord> indexByInchi;
	protected Map<String,Set<IChemRecord>> indexByName;
	protected Map<String,Set<IChemRecord>> indexByOntID;
	protected Set<String> orphanNames;
	protected Set<String> stopWords;

	private URI uri;
	private Locale language;

	public ImmutableChemNameDict(URI uri, Locale language) {
		this.uri = uri;
		this.language = language;
		chemRecords = new HashSet<IChemRecord>();
		indexByInchi = new HashMap<String,IChemRecord>();
		indexByName = new HashMap<String,Set<IChemRecord>>();
		indexByOntID = new HashMap<String,Set<IChemRecord>>();
		orphanNames = new HashSet<String>();
		stopWords = new HashSet<String>();
	}

	public ImmutableChemNameDict(URI uri, Locale language, InputStream in) throws DataFormatException {
		MutableChemNameDict mutableDict = new MutableChemNameDict(uri, language);
		Document doc;
		try {
			doc = new Builder().build(in);
		} catch (ParsingException e) {
			throw new DataFormatException("unreadable formatting in source dictionary", e);
		} catch (IOException e) {
			throw new OscarInitialisationException("failed to load chemical name dictionary", e);
		}
		ChemNameDictIO.readXML(doc, mutableDict);
		
		this.uri = uri;
		this.language = language;
		chemRecords = mutableDict.chemRecords;
		indexByInchi = mutableDict.indexByInchi;
		indexByName = mutableDict.indexByName;
		indexByOntID = mutableDict.indexByOntID;
		orphanNames = mutableDict.orphanNames;
		stopWords = mutableDict.stopWords;
	}

	public Locale getLanguage() {
		return language;
	}

	public URI getURI() {
		return this.uri;
	}

	public boolean hasStopWord(String queryWord) {
		queryWord = StringTools.normaliseName(queryWord);
		return stopWords.contains(queryWord);
	}

	
	//TODO would this not be better as Collections.unmodifiable?
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
			for(IChemRecord record : indexByName.get(queryName)) {
				if (record instanceof ISMILESChemRecord) {
					String smiles = ((ISMILESChemRecord) record).getSMILES();
					if (smiles != null) 
						results.add(smiles);
				}
			}
			return results;
		}
		return Collections.EMPTY_SET;
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
			for(IChemRecord record : indexByName.get(queryName)) {
				if (record instanceof IInChIChemRecord) {
					String inchi = ((IInChIChemRecord) record).getInChI();
					if (inchi != null) {
						results.add(inchi);	
					}
				}
			}
			return results;	
		}
		return Collections.EMPTY_SET;
	}

//	public String getInChIforShortestSMILES(String queryName) {
//		queryName = StringTools.normaliseName(queryName);
//		if(indexByName.containsKey(queryName)) {
//			String currentInchi = null;
//			String currentSmiles = null;
//			for(IChemRecord record : indexByName.get(queryName)) {
//				assert(record.getInChI() != null); 
//				if(currentInchi == null) {
//					currentInchi = record.getInChI();
//					currentSmiles = record.getSMILES();
//				} else if(record.getSMILES() == null && currentSmiles == null) {
//					if(currentInchi.compareTo(record.getInChI()) > 0) {
//						currentInchi = record.getInChI();
//						currentSmiles = record.getSMILES();															
//					}						
//				} else if(record.getSMILES() == null) {
//					// Do nothing, we prefer InChIs with associated smiles
//				} else if(currentSmiles == null) {
//					currentInchi = record.getInChI();
//					currentSmiles = record.getSMILES();																					
//				} else if(currentSmiles.length() == record.getSMILES().length()) {
//					if(currentSmiles.equals(record.getSMILES())) {
//						if(currentInchi.compareTo(record.getInChI()) > 0) {
//							currentInchi = record.getInChI();
//							currentSmiles = record.getSMILES();															
//						}
//					} else if(currentSmiles.compareTo(record.getSMILES()) > 0) {
//						currentInchi = record.getInChI();
//						currentSmiles = record.getSMILES();							
//					}
//				} else if(currentSmiles.length() > record.getSMILES().length()) {
//					currentInchi = record.getInChI();
//					currentSmiles = record.getSMILES();
//				} //Otherwise do nothing
//			}
//			return currentInchi;
//		} else {
//			return null;
//		}
//	}

	public Set<String> getNames(String inchi) {
		if(!indexByInchi.containsKey(inchi)) return null;
		Set<String> names = new HashSet<String>(indexByInchi.get(inchi).getNames());
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

	public Set<IChemRecord> getChemRecords() {
		Set<IChemRecord> results = new HashSet<IChemRecord>();
		results.addAll(chemRecords);
		return results;
	}

	public boolean hasOntologyIdentifier(String identifier) {
		return indexByOntID.containsKey(identifier);
	}
}
