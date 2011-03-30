package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChEBIDictionary;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.DefaultDictionary;

/**
 * Central point of access to query dictionaries.
 *
 * @author egonw
 * @author dmj30
 */
public class ChemNameDictRegistry {

	private static final Logger LOG = LoggerFactory.getLogger(ChemNameDictRegistry.class);

	private static ChemNameDictRegistry defaultInstance;

	private Locale language;
	private Map<URI,IChemNameDict> dictionaries = new HashMap<URI,IChemNameDict>();

	/**
	 * Creates a new ChemNameDictRegistry for the English language.
	 * The {@link ChEBIDictionary} and {@link DefaultDictionary} are
	 * registered by default.
	 * 
	 */
	public ChemNameDictRegistry() {
		this.language = Locale.ENGLISH;
		register(new DefaultDictionary());
        register(new ChEBIDictionary());
	}
	
	/**
	 * Creates a new ChemNameDictRegistry for the specified
	 * language.
	 */
	public ChemNameDictRegistry(Locale locale) {
		this.language = locale;
	}
	
	
	/**
	 * Registers a new dictionary, uniquely identified by its {@link URI}.
	 *
	 * @param dictionary
	 */
	public void register(IChemNameDict dictionary) {
		if (!language.getLanguage().equals(dictionary.getLanguage().getLanguage())){
			LOG.warn("Registry has different language than dictionary");
		}
		dictionaries.put(dictionary.getURI(), dictionary);
	}

	/**
	 * Removes all dictionaries from the registry.
	 */
	public void clear() {
		dictionaries.clear();
	}

	/**
	 * Returns all unique {@link URI}s for the registered dictionaries.
	 *
	 * @return a {@link List} of {@link URI}s.
	 */
	public List<URI> listDictionaries() {
		List<URI> uris = new ArrayList<URI>();
		uris.addAll(dictionaries.keySet());
		return uris;
	}

	/**
	 * Returns the {@link ChemNameDict} identified by the given {@link URI}.
	 *
	 * @param  uri the unique {@link URI} of the dictionary
	 * @return     a {@link ChemNameDict}
	 */
	public IChemNameDict getDictionary(URI uri) {
		return dictionaries.get(uri);
	}

	public boolean hasName(String queryName) {
		for (IChemNameDict dict : dictionaries.values()) {
			if (dict.hasName(queryName)) return true;
		}
		return false;
	}

	public Set<String> getSMILES(String queryName) {
		Set<String> allsmileses = new HashSet<String>();
		for (IChemNameDict dict : dictionaries.values()) {
			if (dict instanceof ISMILESProvider) {
				ISMILESProvider smiDict = (ISMILESProvider)dict;
				Set<String> smileses = smiDict.getSMILES(queryName);
				if (smileses != null)
					allsmileses.addAll(smiDict.getSMILES(queryName));
			}
		}
		return allsmileses;
	}

	public String getShortestSMILES(String queryName) {
		String shortestSMILES = null;
		for (IChemNameDict dict : dictionaries.values()) {
			if (dict instanceof ISMILESProvider) {
				ISMILESProvider smiDict = (ISMILESProvider)dict;
				String smiles = smiDict.getShortestSMILES(queryName);
				if (smiles !=null && (shortestSMILES == null ||
					smiles.length() < shortestSMILES.length())) {
					shortestSMILES = smiles;
				}
			}
		}
		return shortestSMILES;
	}

	public Set<String> getInChI(String queryName) {
		Set<String> allInchis = new HashSet<String>();
		for (IChemNameDict dict : dictionaries.values()) {
			if (dict instanceof IInChIProvider) {
				IInChIProvider inchiDict = (IInChIProvider)dict;
				Set<String> inchis = inchiDict.getInChI(queryName);
				if (inchis != null)
					allInchis.addAll(inchis);
			}
		}
		return allInchis;
	}

	public Set<String> getNames(String inchi) {
		Set<String> allNames = new HashSet<String>();
		for (IChemNameDict dict : dictionaries.values()) {
			Set<String> names = dict.getNames(inchi);
			if (names != null)
				allNames.addAll(dict.getNames(inchi));
		}
		return allNames;
	}

	public boolean hasOntologyIdentifier(String identifier) {
		for (IChemNameDict dict : dictionaries.values()) {
			if (dict.hasOntologyIdentifier(identifier)) return true;
		}
		return false;
	}

	/**
	 * Returns a set of all names of all registered dictionaries. Don't do this too often.
	 */
	public Set<String> getAllNames() {
		Set<String> allNames = new HashSet<String>();
		for (IChemNameDict dict : dictionaries.values()) {
			allNames.addAll(dict.getNames());
		}
		return allNames;
	}

	public Locale getLanguage() {
		return language;
	}

	/**
	 * Returns an immutable ChemNameDictRegistry with the
	 * {@link ChEBIDictionary} and {@link DefaultDictionary}
	 * registered. 
	 */
	public static synchronized ChemNameDictRegistry getDefaultInstance() {
		if (defaultInstance == null) {
			defaultInstance = new ChemNameDictRegistry(Locale.ENGLISH);
			//having the ChEBI & default dictionaries and only these dictionaries
			//registered is a requirement in order pre-generated models are supplied
			//with the correct data - DO NOT CHANGE THIS.
			defaultInstance.register(new ChEBIDictionary());
			defaultInstance.register(new DefaultDictionary());
			Map<URI, IChemNameDict> dictionaries = defaultInstance.dictionaries;
			defaultInstance.dictionaries = Collections.unmodifiableMap(dictionaries);
		}
		return defaultInstance;
	}
}
