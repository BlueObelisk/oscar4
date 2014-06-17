package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.ChemRecord;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.IChemRecord;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.IInChIChemRecord;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.ISMILESChemRecord;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.IStdInChIChemRecord;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.IStdInChIKeyChemRecord;
import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**
 * An immutable name-to-structure dictionary which holds active data in memory.
 * It stores chemical records ({@link ChemRecord}), orphan names (see below),
 * and stop words.  Records must have an Standard InChI identifier, may have a SMILES
 * string, and an unlimited number of names and ontology identifiers. The Standard InChI
 * identifiers are unique; it is not possible to have two records with the same
 * identifier.
 * 
 * <p>Orphan Names are names which have no Standard InChI; a name can only be an Orphan
 * Name if it does not appear as a name in any chemical record. Stopwords are
 * things that the system should not recognise as chemical names.
 * 
 * <p>Note that in chemical records, the aim is to associate names with Standard InChIs,
 * and ontology identifiers with Standard InChIs, rather than to directly associate names
 * with ontology identifiers. If you need to associate names directly with
 * ontology identifiers, list them as orphan names here.
 *
 * @author ptc24
 * @author egonw
 * @author dmj30
 */
public class ImmutableChemNameDict implements IChemNameDict, IInChIProvider, IStdInChIProvider, IStdInChIKeyProvider, ISMILESProvider {

	protected Set<IChemRecord> chemRecords;
	/**
	 * @deprecated Please use {@link #indexByStdInchi} instead.
	 */
	@Deprecated
	protected Map<String,IChemRecord> indexByInchi;
	protected Map<String,IChemRecord> indexByStdInchi;
	protected Map<String,IChemRecord> indexByStdInchiKey;
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
		indexByStdInchi = new HashMap<String,IChemRecord>();
		indexByStdInchiKey = new HashMap<String,IChemRecord>();
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
		indexByStdInchi = mutableDict.indexByStdInchi;
		indexByStdInchiKey = mutableDict.indexByStdInchiKey;
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

	public Set<String> getAllSmiles(String queryName) {
		Set<String> results = new HashSet<String>();
		queryName = StringTools.normaliseName(queryName);
		if(indexByName.containsKey(queryName)) {
			for(IChemRecord record : indexByName.get(queryName)) {
				if (record instanceof ISMILESChemRecord) {
					String smiles = ((ISMILESChemRecord) record).getSMILES();
					if (smiles != null) 
						results.add(smiles);
				}
			}
		}
		return results;
	}

	public String getShortestSmiles(String queryName) {
		String s = null;
		Set<String> smiles = getAllSmiles(queryName);
		if(smiles == null) return null;
		for(String smile : smiles) {
			if(s == null || s.length() > smile.length()) s = smile;
		}
		return s;
	}

	/**
	 * Deprecated "Use {@link #getStdInchis} instead."
	 */
	@Deprecated
	public Set<String> getInchis(String queryName) {
		Set<String> results = new HashSet<String>();
		queryName = StringTools.normaliseName(queryName);
		if(indexByName.containsKey(queryName)) {
			for(IChemRecord record : indexByName.get(queryName)) {
				if (record instanceof IInChIChemRecord) {
					String inchi = ((IInChIChemRecord) record).getInChI();
					if (inchi != null) {
						results.add(inchi);	
					}
				}
			}
		}
		return results;
	}
	
	public Set<String> getStdInchis(String queryName) {
		Set<String> results = new HashSet<String>();
		queryName = StringTools.normaliseName(queryName);
		if(indexByName.containsKey(queryName)) {
			for(IChemRecord record : indexByName.get(queryName)) {
				if (record instanceof IStdInChIChemRecord) {
					String stdInchi = ((IStdInChIChemRecord) record).getStdInChI();
					if (stdInchi != null) {
						results.add(stdInchi);	
					}
				}
			}
		}
		return results;
	}
	
	public Set<String> getStdInchiKeys(String queryName) {
		Set<String> results = new HashSet<String>();
		queryName = StringTools.normaliseName(queryName);
		if (indexByName.containsKey(queryName)) {
			for (IChemRecord record : indexByName.get(queryName)) {
				if (record instanceof IStdInChIKeyChemRecord) {
					String stdInchiKey = ((IStdInChIKeyChemRecord) record)
							.getStdInChIKey();
					if (stdInchiKey != null) {
						results.add(stdInchiKey);
					}
				}
			}
		}
		return results;
	}


	public Set<String> getNames(String stdInchi) {
		IChemRecord record = indexByStdInchi.get(stdInchi);
		if (record == null) {
			return new HashSet<String>();
		}
		else {
			return new HashSet<String>(record.getNames());
		}
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
		Set<IChemRecord> results = new HashSet<IChemRecord>(chemRecords);
		return results;
	}

	public boolean hasOntologyIdentifier(String identifier) {
		return indexByOntID.containsKey(identifier);
	}


}
