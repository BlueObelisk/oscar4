package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.net.URI;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.MutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**
 * Provides static methods for accessing ChemNameDict.
 * 
 * @author ptc24
 */
public final class ChemNameDictSingleton {

	private static MutableChemNameDict myChemNameDict = null;
	private static ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar/chemnamedict/");
	
	/**Re-initialise the ChemNameDict singleton.
	 * 
	 * @throws Exception
	 */
	public static void reinitialise() throws Exception {
		myChemNameDict = null;
		getChemNameDictInstance();
	}

	/**Make a new ChemNameDict, and set it as the singleton.
	 * 
	 * @throws Exception
	 */
	public static void makeFromScratch() throws Exception {
		myChemNameDict = null;
		getChemNameDictInstance(true);		
	}
	
	private static MutableChemNameDict getChemNameDictInstance() throws Exception {
		return getChemNameDictInstance(false);
	}
	
	private static MutableChemNameDict getChemNameDictInstance(boolean forceFromScratch) throws Exception {
		if(myChemNameDict == null) {
			Logger logger = Logger.getLogger(ChemNameDictSingleton.class);
			logger.debug("Initialising ChemNameDict... ");
			myChemNameDict = new MutableChemNameDict(new URI("http://wwmm.ch.cam.ac.uk/dictionary/old/"));
			try {
				ChemNameDictIO.readXML(
					rg.getXMLDocument(OscarProperties.getData().chemNameDict),
					myChemNameDict
				);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Could not initialise ChemNameDict!");
			}
		}
		return myChemNameDict;
	}
		
	/**Private construction to enforce singelton pattern.
	 * 
	 */
	private ChemNameDictSingleton() {
		
	}

	/**Adds a chemical.
	 * 
	 * @param name The chemical name.
	 * @param smiles The SMILES string.
	 * @param inchi The InChI string.
	 * @throws Exception
	 */
	public static void addChemical(String name, String smiles, String inchi) throws Exception {
		getChemNameDictInstance().addChemical(name, smiles, inchi);
		// This causes the recogniser to need to be updated.
		
		//DFANEFinder.destroyInstanceIfWordTokenises(name);
	}

	/**Adds a chemical.
	 * 
	 * @param inchi The InChI string.
	 * @param smiles The SMILES string.
	 * @param names The set of names.
	 * @param ontIDs The set of ontology identifiers.
	 * @throws Exception
	 */
	public static void addChemRecord(String inchi, String smiles, Set<String> names, Set<String> ontIDs) throws Exception {
		getChemNameDictInstance().addChemRecord(inchi, smiles, names, ontIDs);
		// This causes the recogniser to need to be updated.
		
		if(false) {
			for(String name : names) {
				//DFANEFinder.destroyInstanceIfWordTokenises(name);			
			}
		}
	}

	/**Adds a stopword.
	 * 
	 * @param word The stopword to add.
	 * @throws Exception
	 */
	public static void addStopWord(String word) throws Exception {
		getChemNameDictInstance().addStopWord(word);
	}
	
	/**Checks to see whether the dictionary has the given name. 
	 * 
	 * @param name The name to check.
	 * @return Whether it is present in the ChemNameDict.
	 */
	public static boolean hasName(String name) {
		try {
			return getChemNameDictInstance().hasName(name);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO better exception handling here
			throw new RuntimeException("ChemNameDict not initialised yet!");
		}
	}

	/**Checks to see whether the dictionary has the given ontology ID.
	 * 
	 * @param ontId The ontology ID.
	 * @return Whether it is present in the chemnamedict.
	 */
	public static boolean hasOntId(String ontId) {
		try {
			return getChemNameDictInstance().hasOntologyIdentifier(ontId);
		} catch (Exception e) {
			// TODO better exception handling here
			throw new RuntimeException("ChemNameDict not initialised yet!");
		}
	}

	/**Checks to see whether a given word is present in the ChemNameDict as a 
	 * stop word.
	 * 
	 * @param word The word to query.
	 * @return Whether it is present as a stopword.
	 */
	public static boolean hasStopWord(String word) {
		try {
			return getChemNameDictInstance().hasStopWord(word);
		} catch (Exception e) {
			// TODO better exception handling here
			e.printStackTrace();
			throw new RuntimeException("ChemNameDict not initialised yet!");
		}
	}
	
	/**Gets all of the stopwords in the ChemNameDict.
	 * 
	 * @return The stopwords.
	 */
	public static Set<String> getStopWords() {
		try {
			return getChemNameDictInstance().getStopWords();
		} catch (Exception e) {
			// TODO better exception handling here
			throw new RuntimeException("ChemNameDict not initialised yet!");
		}		
	}
	
	/**Looks up a name, and gets the various SMILES strings that match, as
	 * a space-separated list.
	 * 
	 * @param name The name to look up.
	 * @return The SMILES strings.
	 * @throws Exception
	 */
	public static String getSpaceSeparatedSmiles(String name) throws Exception {
		Set<String> smilesSet = getChemNameDictInstance().getSMILES(name);
		return StringTools.collectionToString(smilesSet, " ");
	}

	/**Looks up a name, and gets the shortest SMILES string that matches.
	 * 
	 * @param name The name to look up.
	 * @return The SMILES string.
	 * @throws Exception
	 */
	public static String getShortestSmiles(String name) throws Exception {
		return getChemNameDictInstance().getShortestSMILES(name);
	}

	/**Looks up a name, and gets the various InChI strings that match, as
	 * a space-separated list.
	 * 
	 * @param name The name to look up.
	 * @return The InChI strings.
	 * @throws Exception
	 */
	public static String getSpaceSeparatedInchis(String name) throws Exception {
		Set<String> smilesSet = getChemNameDictInstance().getInChI(name);
		return StringTools.collectionToString(smilesSet, " ");
	}

	/**Looks up a name, finds the entry with the shortest SMILES string, and
	 * returns the InChI.
	 * 
	 * @param name The name to look up.
	 * @return The InChI string.
	 * @throws Exception
	 */
	public static String getInChIForShortestSmiles(String name) throws Exception {
		return getChemNameDictInstance().getInChIforShortestSMILES(name);
	}
	
	/**Looks up an InChI, and returns all of the names that match the InChI.
	 * 
	 * @param inchi The InChI to look up.
	 * @return The names.
	 * @throws Exception
	 */
	public static Set<String> getNamesFromInChI(String inchi) throws Exception {
		return getChemNameDictInstance().getNames(inchi);
	}

	/**Returns all of the names in the ChemNameDict.
	 * 
	 * @return The names.
	 * @throws Exception
	 */
	public static Set<String> getAllNames() throws Exception {
		// TODO stopwords, orphan names
		return getChemNameDictInstance().getNames();
	}
	
	/**Clears all of the entries in the ChemNameDict.
	 * 
	 * @throws Exception
	 */
	public static void purge() throws Exception {
		myChemNameDict = new MutableChemNameDict(new URI("http://wwmm.ch.cam.ac.uk/dictionary/old/"));
	}

	/**Imports all of the entries in a ChemNameDict into the singleton.
	 * 
	 * @param dict The dictionary to import.
	 * @throws Exception
	 */
	public static void importDict(IChemNameDict dict) throws Exception {
		myChemNameDict.importChemNameDict(dict);
	}
	
	/**Gets a hash value for the singleton.
	 * 
	 * @return The hash value.
	 * @throws Exception
	 */
	public static int getCNDHash() throws Exception {
		return ChemNameDictIO.makeHash(getChemNameDictInstance());
	}
	
	/*public static void main(String [] args) throws Exception {
		Serializer ser = new Serializer(System.out);
		ser.setIndent(2);
		ser.write(getChemNameDictInstance().getXML());
	}*/
}
