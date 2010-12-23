package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.obo.TermsFileReader;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**A class to hold several mappings between terms and their identifiers.
 *
 * @author ptc24
 *
 */
public final class TermMaps {

    private static final Logger LOG = LoggerFactory.getLogger(TermMaps.class);

    private static final ResourceGetter RESOURCE_GETTER = new ResourceGetter("/");

    private static final String NE_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscarrecogniser/finder/terms/neTerms.txt";
    private static final String POLY_NE_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscar/obo/terms/polyNeTerms.txt";
    private static final String IE_PATTERNS_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscarrecogniser/finder/terms/iePatterns.txt";
    private static final String STRUCTURE_TYPES_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscarrecogniser/finder/terms/structureTypes.txt";
    private static final String CUST_ENT_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscarrecogniser/finder/terms/custEnt.txt";

    private final Map<String, NamedEntityType> neTerms;
    private final Map<String, String> iePatterns;
    private final Map<String, String> custEnt;
    private final Map<String, String> structureTypes;
    private final Set<String> suffixes;

    private static TermMaps defaultInstance;


    /**Initialise the TermMaps singleton, deleting the old one if one already
     * exists.
     *
     * @throws Exception
     */
    public static void reinitialise() {
        defaultInstance = null;
        getInstance();
    }

    public static TermMaps getInstance() {
        TermMaps instance = defaultInstance;
        if (instance == null) {
            instance = loadDefaultInstance();
        }
        return instance;
    }

    private static synchronized TermMaps loadDefaultInstance() {
        if (defaultInstance == null) {
            try {
                defaultInstance = new TermMaps();
            } catch (IOException e) {
                throw new RuntimeException("Error loading term maps", e);
            }
        }
        return defaultInstance;
    }



    private Set<String> digestSuffixes(Map<String, NamedEntityType> neTerms) {
        Set<String> suffixes = new HashSet<String>();
        for(String s : neTerms.keySet()) {
            String[] ss = s.split("\\s+");
            for (int i = 0; i < ss.length; i++) {
                if(ss[i].startsWith("$-")) {
                    suffixes.add(ss[i].substring(2));
                }
            }
        }
        return suffixes;
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

    private Map<String, NamedEntityType> getNeTermMap(String filename, boolean concatenateTypes) throws IOException {
        Map<String,String> termMap = loadTerms(filename, concatenateTypes);
        Map<String,NamedEntityType> neTermMap = new HashMap<String, NamedEntityType>();
        for (Map.Entry<String,String> e : termMap.entrySet()) {
            neTermMap.put(e.getKey(), NamedEntityType.valueOf(e.getValue()));
        }
        return neTermMap;
    }

    private TermMaps() throws IOException {
        LOG.debug("Initialising term maps... ");
        Map<String,NamedEntityType> neTerms = getNeTermMap(NE_TERMS_FILE, false);
        //add additional neTerms for polymers if set to polymer mode
        if (OscarProperties.getData().polymerMode) {
            Map <String, NamedEntityType> polyNeTerms = getNeTermMap(POLY_NE_TERMS_FILE, false);
            neTerms.putAll(polyNeTerms);
        }
        this.neTerms = Collections.unmodifiableMap(neTerms);
        this.iePatterns = Collections.unmodifiableMap(loadTerms(IE_PATTERNS_TERMS_FILE, false));
        this.structureTypes = Collections.unmodifiableMap(loadTerms(STRUCTURE_TYPES_TERMS_FILE, false));
        this.custEnt = Collections.unmodifiableMap(loadTerms(CUST_ENT_TERMS_FILE, true));
        this.suffixes = Collections.unmodifiableSet(digestSuffixes(neTerms));
        LOG.debug("term maps initialised");
    }



    /**Gets the term map for neTerms.txt.
     *
     * @return The term map.
     */
    public Map<String, NamedEntityType> getNeTerms() {
        return neTerms;
    }

    /**Gets the term map for iePatterns.txt.
     *
     * @return The term map.
     */
    public Map<String, String> getIePatterns() {
        return iePatterns;
    }

    /**Gets the term map for custEnt.txt.
     *
     * @return The term map.
     */
    public Map<String, String> getCustEnt() {
        return custEnt;
    }

    /**Gets the term map for structureTypes.txt.
     *
     * @return The term map.
     */
    public Map<String, String> getStructureTypes() {
        return structureTypes;
    }

    /**Gets a collection of suffixes harvested from neTerms.txt.
     *
     * @return A collection of suffixes harvested from neTerms.txt
     */
    public Set<String> getSuffixes() {
        return suffixes;
    }

}
