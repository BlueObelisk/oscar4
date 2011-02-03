package uk.ac.cam.ch.wwmm.oscar.obo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

/**
 * @author Sam Adams
 */
public class OntologyTerms {

    private static final Logger LOG = LoggerFactory.getLogger(OntologyTerm.class);

    private static final ResourceGetter RESOURCE_GETTER = new ResourceGetter("/");

    private static final String ONTOLOGY_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscar/obo/terms/ontology.txt";
    private static final String POLYMER_ONTOLOGY_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscarrecogniser/finder/polyOntology.txt";

    private static OntologyTerms defaultInstance;

    private final Map<String, String> ontology;


    public static OntologyTerms getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = loadDefaultInstance();
        }
        return defaultInstance;
    }

    private static synchronized OntologyTerms loadDefaultInstance() {
        if (defaultInstance == null) {
            try {
                defaultInstance = new OntologyTerms();
            } catch (IOException e) {
                throw new RuntimeException("Error loading ontology terms", e);
            }
        }
        return defaultInstance;
    }

    public OntologyTerms(Map<String,String> terms) {
        Map<String,String> copy = new HashMap<String, String>(terms);
        this.ontology = Collections.unmodifiableMap(copy);
    }

    private OntologyTerms() throws IOException {

        if (OscarProperties.getData().useONT) {
            Map<String,String> terms = loadTerms(ONTOLOGY_TERMS_FILE);
            //add polymer ontology if set to polymer mode
            if (OscarProperties.getData().polymerMode) {
                Map<String, String> polyOntology = loadTerms(POLYMER_ONTOLOGY_TERMS_FILE);
                terms.putAll(polyOntology);
            }
            this.ontology = Collections.unmodifiableMap(terms);
        } else {
            this.ontology = Collections.emptyMap(); 
        }

    }

    private Map<String, String> loadTerms(String path) throws IOException {
        LOG.info("Loading ontology terms: "+path);
        InputStream is = RESOURCE_GETTER.getStream(path);
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            return TermsFileReader.loadTermMap(in, true);
        } finally {
            is.close();
        }
    }

    /**
     * @return a Map of ontology terms to space-separated ontology ids
     */
    public Map<String, String> getOntology() {
        return ontology;
    }
    
}
