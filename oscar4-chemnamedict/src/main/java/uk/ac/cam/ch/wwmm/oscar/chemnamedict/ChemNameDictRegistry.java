package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central point of access to query dictionaries.
 *
 * @author egonw
 */
public class ChemNameDictRegistry {

	private static ChemNameDictRegistry instance = null;

	Map<URI,IChemNameDict> dictionaries = null;

	private ChemNameDictRegistry() {
		dictionaries = new HashMap<URI,IChemNameDict>();
	}

	/**
	 * Returns an instance of the {@link ChemNameDict} registry.
	 *
	 * @return a {@link ChemNameDictRegistry}.
	 */
	public static ChemNameDictRegistry getInstance() {
		if (instance == null)
			instance = new ChemNameDictRegistry();
		return instance;
	}

	/**
	 * Registers a new dictionary, uniquely identified by its {@link URI}.
	 *
	 * @param dictionary
	 */
	public void register(IChemNameDict dictionary) {
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

	public boolean hasStopWord(String queryWord) {
		for (IChemNameDict dict : dictionaries.values()) {
			if (dict.hasStopWord(queryWord)) return true;
		}
		return false;
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
			Set<String> smileses = dict.getSMILES(queryName);
			if (smileses != null)
				allsmileses.addAll(dict.getSMILES(queryName));
		}
		return allsmileses;
	}

	public String getShortestSMILES(String queryName) {
		String shortestSMILES = null;
		for (IChemNameDict dict : dictionaries.values()) {
			String smiles = dict.getShortestSMILES(queryName);
			if (shortestSMILES == null ||
				smiles.length() < shortestSMILES.length()) {
				shortestSMILES = smiles;
			}
		}
		return shortestSMILES;
	}

	public Set<String> getInChI(String queryName) {
		Set<String> allInchis = new HashSet<String>();
		for (IChemNameDict dict : dictionaries.values()) {
			Set<String> inchis = dict.getInChI(queryName);
			if (inchis != null)
				allInchis.addAll(inchis);
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

}
