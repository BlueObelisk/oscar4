package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChEBIDictionary;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.DefaultDictionary;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ChemicalStructure;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.FormatType;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ResolvedNamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

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
        register(ChEBIDictionary.getInstance());
	}
	
	/**
	 * Creates a new ChemNameDictRegistry for the specified
	 * language with no registered dictionaries.
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
			if (dict.hasName(queryName)) {
				return true;
			}
		}
		return false;
	}

	public Set<String> getSMILES(String queryName) {
		Set<String> allsmileses = new HashSet<String>();
		for (IChemNameDict dict : dictionaries.values()) {
			if (dict instanceof ISMILESProvider) {
				ISMILESProvider smiDict = (ISMILESProvider)dict;
				Set<String> smileses = smiDict.getSMILES(queryName);
				if (smileses != null) {
					allsmileses.addAll(smiDict.getSMILES(queryName));
				}
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
			defaultInstance.register(ChEBIDictionary.getInstance());
			defaultInstance.register(new DefaultDictionary());
			Map<URI, IChemNameDict> dictionaries = defaultInstance.dictionaries;
			defaultInstance.dictionaries = Collections.unmodifiableMap(dictionaries);
		}
		return defaultInstance;
	}

	/**
	 * Resolves the given {@link NamedEntity} to a {@link ResolvedNamedEntity}.
	 * Only named entities of type {@link NamedEntityType#COMPOUND} can
	 * be resolved.
	 * 
	 * @return a {@link ResolvedNamedEntity}, or null if the given named
	 * entity could not be resolved.
	 */
	public ResolvedNamedEntity resolveNamedEntity(NamedEntity ne) {
		if (!ne.getType().isInstance(NamedEntityType.COMPOUND)) {
			return null;
		}
		List <ChemicalStructure> structures = new ArrayList<ChemicalStructure>();
		
		for (IChemNameDict dictionary : dictionaries.values()) {
			if (dictionary instanceof ISMILESProvider) {
				Set <String> smilesStrings = ((ISMILESProvider) dictionary).getSMILES(ne.getSurface());
				for (String smiles : smilesStrings) {
					if (smiles != null) {
						structures.add(new ChemicalStructure(smiles, FormatType.SMILES, dictionary.getURI()));	
					}
				}
			}
			if (dictionary instanceof IInChIProvider) {
				Set <String> inchis = ((IInChIProvider) dictionary).getInChI(ne.getSurface());
				for (String inchi : inchis) {
					if (inchi != null) {
						structures.add(new ChemicalStructure(inchi, FormatType.INCHI, dictionary.getURI()));
					}
				}
			}
			if (dictionary instanceof ICMLProvider) {
				Set <Element> cmls = ((ICMLProvider) dictionary).getCML(ne.getSurface());
				for (Element cml : cmls) {
					if (cml != null) {
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						try {
							new Serializer(out, "UTF-8").write(new Document(cml));
						} catch (UnsupportedEncodingException e) {
							throw new Error("UTF-8 is not supported on your machine", e);
						} catch (IOException e) {
							LOG.warn("failed to serialise CML", e);
							continue;
						}
						String cmlString;
						try {
							cmlString = out.toString("UTF-8");
						} catch (UnsupportedEncodingException e) {
							throw new Error("UTF-8 is not supported on your machine", e);
						}
						structures.add(new ChemicalStructure(cmlString, FormatType.CML, dictionary.getURI()));
					}
				}
			}
		}
		
		if (structures.size() == 0) {
			return null;
		}
		return new ResolvedNamedEntity(ne, structures);
	}
}
