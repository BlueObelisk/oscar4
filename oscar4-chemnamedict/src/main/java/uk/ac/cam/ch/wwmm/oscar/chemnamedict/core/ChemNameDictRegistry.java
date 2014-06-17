package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries.ChEBIDictionary;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries.DefaultDictionary;
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
	private List <IChemNameDict> dictionaries = new ArrayList<IChemNameDict>();

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
	 * @throws IllegalArgumentException if a dictionary with
	 * the same URI is already registered
	 */
	public void register(IChemNameDict dictionary) {
		if (!language.getLanguage().equals(dictionary.getLanguage().getLanguage())){
			LOG.warn("Registry has different language than dictionary");
		}
		for (IChemNameDict dict : dictionaries) {
			if (dict.getURI().equals(dictionary.getURI())) {
				throw new IllegalArgumentException(
						"ChemNameDictRegistry already contains a dictionary" +
						"with URI: " + dictionary.getURI());
			}
		}
		dictionaries.add(dictionary);
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
		for (IChemNameDict dict : dictionaries) {
			uris.add(dict.getURI());
		}
		return uris;
	}

	/**
	 * Returns the {@link ChemNameDict} identified by the given {@link URI},
	 * or null if there is no such dictionary registered.
	 *
	 * @param  uri the unique {@link URI} of the dictionary
	 * @return     a {@link ChemNameDict}
	 */
	public IChemNameDict getDictionary(URI uri) {
		for (IChemNameDict dict : dictionaries) {
			if(dict.getURI().equals(uri)) {
				return dict;
			}
		}
		return null;
	}

	/**
	 * Tests to see if one of the currently-registered dictionaries contains
	 * the given name.
	 * 
	 */
	public boolean hasName(String queryName) {
		for (IChemNameDict dict : dictionaries) {
			if (dict.hasName(queryName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a set containing all the SMILES strings for the given
	 * query name contained by the currently-registered dictionaries.
	 * 
	 */
	public Set<String> getAllSmiles(String queryName) {
		Set<String> allSmiles = new HashSet<String>();
		for (IChemNameDict dict : dictionaries) {
			if (dict instanceof ISMILESProvider) {
				allSmiles.addAll(((ISMILESProvider)dict).getAllSmiles(queryName));
			}
		}
		return allSmiles;
	}

	/**
	 * Returns the shortest SMILES string for the given query
	 * name contained by the currently-registered dictionaries.
	 * 
	 * @return the shortest SMILES string or null if no SMILES
	 * string for the given query name are contained in any of
	 * the currently-registered dictionaries.
	 */
	public String getShortestSmiles(String queryName) {
		String shortestSmiles = null;
		for (IChemNameDict dict : dictionaries) {
			if (dict instanceof ISMILESProvider) {
				String smiles = ((ISMILESProvider)dict).getShortestSmiles(queryName);
				if (smiles != null) {
					if (shortestSmiles == null || smiles.length() < shortestSmiles.length()) {
						shortestSmiles = smiles;	
					}
				}
			}
		}
		return shortestSmiles;
	}

	/**
	 * Returns a set containing all the InChI strings for the given
	 * query name contained by the currently-registered dictionaries.
	 * 
	 * @deprecated Please use {@link #getStdInchis} instead.
     *
	 */
	@Deprecated
	public Set<String> getInchis(String queryName) {
		Set<String> allInchis = new HashSet<String>();
		for (IChemNameDict dict : dictionaries) {
			if (dict instanceof IInChIProvider) {
				Set<String> inchis = ((IInChIProvider)dict).getInchis(queryName);
				allInchis.addAll(inchis);
			}
		}
		return allInchis;
	}
	
	/**
	 * Returns a set containing all the Standard InChI strings for the given
	 * query name contained by the currently-registered dictionaries.
	 * 
	 */
	public Set<String> getStdInchis(String queryName) {
		Set<String> allStdInchis = new HashSet<String>();
		for (IChemNameDict dict : dictionaries) {
			if (dict instanceof IStdInChIProvider) {
				Set<String> stdInchis = ((IStdInChIProvider)dict).getStdInchis(queryName);
				allStdInchis.addAll(stdInchis);
			}
		}
		return allStdInchis;
	}
	
	/**
	 * Returns a set containing all the Standard InChI Key strings for the given
	 * query name contained by the currently-registered dictionaries.
	 * 
	 */
	public Set<String> getStdInchiKeys(String queryName) {
		Set<String> allStdInchiKeys = new HashSet<String>();
		for (IChemNameDict dict : dictionaries) {
			if (dict instanceof IStdInChIKeyProvider) {
				Set<String> stdInchiKeys = ((IStdInChIKeyProvider)dict).getStdInchiKeys(queryName);
				allStdInchiKeys.addAll(stdInchiKeys);
			}
		}
		return allStdInchiKeys;
	}

	/**
	 * Returns a set containing all the chemical names that correspond
	 * to the given Standard InChI in the currently-registered dictionaries. 
	 */
	public Set<String> getNames(String stdInchi) {
		Set<String> allNames = new HashSet<String>();
		for (IChemNameDict dict : dictionaries) {
			allNames.addAll(dict.getNames(stdInchi));
		}
		return allNames;
	}

	/**
	 * Checks if the given ontology identifier is contained within any
	 * of the currently-registered dictionaries.
	 * @param identifier
	 * @return
	 */
	public boolean hasOntologyIdentifier(String identifier) {
		for (IChemNameDict dict : dictionaries) {
			if (dict.hasOntologyIdentifier(identifier)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a set of all names contained by the currently-registered
	 * dictionaries. Don't do this too often. The returned set is a copy
     * of the stored names, so any changes will not affect this object.
	 */
	public Set<String> getAllNames() {
		Set<String> allNames = new HashSet<String>();
		for (IChemNameDict dict : dictionaries) {
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
			List <IChemNameDict> dictionaries = defaultInstance.dictionaries;
			defaultInstance.dictionaries = Collections.unmodifiableList(dictionaries);
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
		
		for (IChemNameDict dictionary : dictionaries) {
			if (dictionary instanceof ISMILESProvider) {
				Set <String> smilesStrings = ((ISMILESProvider) dictionary).getAllSmiles(ne.getSurface());
				for (String smiles : smilesStrings) {
					if (smiles != null) {
						structures.add(new ChemicalStructure(smiles, FormatType.SMILES, dictionary.getURI()));	
					}
				}
			}
			if (dictionary instanceof IInChIProvider) {
				Set <String> inchis = ((IInChIProvider) dictionary).getInchis(ne.getSurface());
				for (String inchi : inchis) {
					if (inchi != null) {
						structures.add(new ChemicalStructure(inchi, FormatType.INCHI, dictionary.getURI()));
					}
				}
			}
			if (dictionary instanceof IStdInChIProvider) {
				Set <String> stdInchis = ((IStdInChIProvider) dictionary).getStdInchis(ne.getSurface());
				for (String stdInchi : stdInchis) {
					if (stdInchi != null) {
						structures.add(new ChemicalStructure(stdInchi, FormatType.STD_INCHI, dictionary.getURI()));
					}
				}
			}
			if (dictionary instanceof IStdInChIKeyProvider) {
				Set <String> stdInchiKeys = ((IStdInChIKeyProvider) dictionary).getStdInchiKeys(ne.getSurface());
				for (String stdInchiKey : stdInchiKeys) {
					if (stdInchiKey != null) {
						structures.add(new ChemicalStructure(stdInchiKey, FormatType.STD_INCHI_KEY, dictionary.getURI()));
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
