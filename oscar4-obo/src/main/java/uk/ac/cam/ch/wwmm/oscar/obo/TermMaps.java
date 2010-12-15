package uk.ac.cam.ch.wwmm.oscar.obo;

import org.apache.log4j.Logger;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**A class to hold several mappings between terms and their identifiers.
 *
 * @author ptc24
 *
 */
public final class TermMaps {

	private static final Logger logger = Logger.getLogger(TermMaps.class);

    private static final ResourceGetter RESOURCE_GETTER = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar/obo/terms/");

    private static final String ONTOLOGY_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscar/obo/terms/ontology.txt";

	private Map<String, NamedEntityType> neTerms;
	private Map<String, String> iePatterns;
	private Map<String, String> custEnt;
	private Map<String, String> structureTypes;
	private Set<String> suffixes;

	private static TermMaps myInstance;

	/**Initialise the TermMaps singleton, deleting the old one if one already
	 * exists.
	 *
	 * @throws Exception
	 */
	public static void reinitialise() throws Exception {
		myInstance = null;
		getInstance();
	}

	/**Initialise the TermMaps singleton, if this has not already been done.
	 *
	 */
	public static void init() {
		try {
			if(myInstance == null) {
				myInstance = new TermMaps();
			}
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	private static TermMaps getInstance() {
		try {
		if(myInstance == null) {
			myInstance = new TermMaps();
		}
		return myInstance;
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	

	private void digestSuffixes() {
		suffixes = new HashSet<String>();
		for(String s : neTerms.keySet()) {
			String [] ss = s.split("\\s+");
			for (int i = 0; i < ss.length; i++) {
				if(ss[i].startsWith("$-")) {
					suffixes.add(ss[i].substring(2));
				}
			}
		}
    }

    private Map<String, String> loadTerms(String path, boolean concatenateTypes) throws IOException {
        InputStream is = RESOURCE_GETTER.getStream(path);
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            return TermsFileReader.loadTermMap(in, concatenateTypes);
        } finally {
            is.close();
        }
    }

    private Map<String, NamedEntityType> getNeTermMap(String filename, boolean concatenateTypes) throws Exception {
        Map<String,String> termMap = loadTerms(filename, concatenateTypes);
        Map<String,NamedEntityType> neTermMap = new HashMap<String, NamedEntityType>();
        for (Map.Entry<String,String> e : termMap.entrySet()) {
            neTermMap.put(e.getKey(), NamedEntityType.valueOf(e.getValue()));
        }
        return neTermMap;
    }

	private TermMaps() throws Exception {
		logger.debug("Initialising term maps... ");
		neTerms = getNeTermMap("neTerms.txt", false);
		//add additional neTerms for polymers if set to polymer mode
		if (OscarProperties.getData().polymerMode) {
			Map <String, NamedEntityType> polyNeTerms = getNeTermMap("polyNeTerms.txt", false);
			neTerms.putAll(polyNeTerms);
		}
		iePatterns = loadTerms("iePatterns.txt", false);
		structureTypes = loadTerms("structureTypes.txt", false);
		custEnt = loadTerms("custEnt.txt", true);
		digestSuffixes();
		logger.debug("term maps initialised");
	}



    /**Gets the term map for neTerms.txt.
	 *
	 * @return The term map.
	 */
	public static Map<String, NamedEntityType> getNeTerms() {
		return getInstance().neTerms;
	}

	/**Gets the term map for iePatterns.txt.
	 *
	 * @return The term map.
	 */
	public static Map<String, String> getIePatterns() {
		return getInstance().iePatterns;
	}

	/**Gets the term map for custEnt.txt.
	 *
	 * @return The term map.
	 */
	public static Map<String, String> getCustEnt() {
		return getInstance().custEnt;
	}

	/**Gets the term map for structureTypes.txt.
	 *
	 * @return The term map.
	 */
	public static Map<String, String> getStructureTypes() {
		return getInstance().structureTypes;
	}

	/**Gets a collection of suffixes harvested from neTerms.txt.
	 *
	 * @return A collection of suffixes harvested from neTerms.txt
	 */
	public static Set<String> getSuffixes() {
		return getInstance().suffixes;
	}
}
