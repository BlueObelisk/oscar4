package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;
import uk.ac.cam.ch.wwmm.oscar.exceptions.ResourceInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.ont.TermsFileReader;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

/**A class to hold several mappings between terms and their identifiers.
 *
 * @author ptc24
 *
 */
public final class TermMaps {

    private static final Logger LOG = LoggerFactory.getLogger(TermMaps.class);

    private static final ResourceGetter RESOURCE_GETTER = new ResourceGetter(TermMaps.class.getClassLoader(),"/");

    private static final String NE_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscarrecogniser/finder/terms/neTerms.txt";
    private static final String POLY_NE_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscar/obo/terms/polyNeTerms.txt";
    private static final String CUST_ENT_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscarrecogniser/finder/terms/custEnt.txt";

    private final Map<String, NamedEntityType> neTerms;
    private final Map<String, String> custEnt;
    private final Set<String> suffixes;

    private static TermMaps defaultInstance;


    /**Initialise the TermMaps singleton, deleting the old one if one already
     * exists.
     * @throws ResourceInitialisationException 
     *
     * @throws Exception
     */
    //TODO this isn't called anywhere - do we need to keep it?
    @Deprecated
    public static void reinitialise() throws ResourceInitialisationException {
        defaultInstance = null;
        getInstance();
    }

    public static TermMaps getInstance() throws ResourceInitialisationException {
        TermMaps instance = defaultInstance;
        if (instance == null) {
            instance = loadDefaultInstance();
        }
        return instance;
    }

    private static synchronized TermMaps loadDefaultInstance() throws ResourceInitialisationException {
        if (defaultInstance == null) {
            defaultInstance = new TermMaps();
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

    private Map<String, String> loadTerms(String path, boolean concatenateTypes) throws IOException, DataFormatException {
        InputStream is = RESOURCE_GETTER.getStream(path);
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            return TermsFileReader.loadTermMap(in, concatenateTypes);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private Map<String, NamedEntityType> getNeTermMap(String filename, boolean concatenateTypes) throws IOException, DataFormatException {
        Map<String,String> termMap = loadTerms(filename, concatenateTypes);
        Map<String,NamedEntityType> neTermMap = new HashMap<String, NamedEntityType>();
        for (Map.Entry<String,String> e : termMap.entrySet()) {
            neTermMap.put(e.getKey(), NamedEntityType.valueOf(e.getValue()));
        }
        return neTermMap;
    }

    private TermMaps() throws ResourceInitialisationException {
        LOG.debug("Initialising term maps... ");
        try {
        	Map<String,NamedEntityType> neTerms = getNeTermMap(NE_TERMS_FILE, false);
            //add additional neTerms for polymers if set to polymer mode
            if (OscarProperties.getData().polymerMode) {
                Map <String, NamedEntityType> polyNeTerms = getNeTermMap(POLY_NE_TERMS_FILE, false);
                neTerms.putAll(polyNeTerms);
            }
            this.neTerms = Collections.unmodifiableMap(neTerms);
            this.custEnt = Collections.unmodifiableMap(loadTerms(CUST_ENT_TERMS_FILE, true));
            this.suffixes = Collections.unmodifiableSet(digestSuffixes(neTerms));	
        } catch (IOException e) {
        	throw new ResourceInitialisationException("failed to load TermMaps", e);
        } catch (DataFormatException e) {
        	throw new ResourceInitialisationException("failed to load TermMaps", e);
		}
        
        LOG.debug("term maps initialised");
    }



    /**Gets the term map for neTerms.txt.
     *
     * @return The term map.
     */
    public Map<String, NamedEntityType> getNeTerms() {
        return neTerms;
    }

    /**Gets the term map for custEnt.txt.
     *
     * @return The term map.
     */
    public Map<String, String> getCustEnt() {
        return custEnt;
    }

    /**Gets a collection of suffixes harvested from neTerms.txt.
     *
     * @return A collection of suffixes harvested from neTerms.txt
     */
    public Set<String> getSuffixes() {
        return suffixes;
    }

}
