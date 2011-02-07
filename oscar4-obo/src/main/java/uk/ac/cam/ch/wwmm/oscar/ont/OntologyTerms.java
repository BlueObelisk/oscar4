package uk.ac.cam.ch.wwmm.oscar.ont;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;
import uk.ac.cam.ch.wwmm.oscar.exceptions.ResourceInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.obo.OntologyTerm;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

/**
 * @author Sam Adams
 */
public class OntologyTerms {

    private static final Logger LOG = LoggerFactory.getLogger(OntologyTerm.class);

    private static final ResourceGetter RESOURCE_GETTER = new ResourceGetter(OntologyTerm.class.getClassLoader(),"/");

    private static final String ONTOLOGY_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscar/obo/terms/ontology.txt";
    private static final String POLYMER_ONTOLOGY_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscarrecogniser/finder/polyOntology.txt";

    private static OntologyTerms defaultInstance;

    private final ListMultimap<String, String> ontology;


    public static OntologyTerms getDefaultInstance() throws ResourceInitialisationException {
        if (defaultInstance == null) {
            defaultInstance = loadDefaultInstance();
        }
        return defaultInstance;
    }

    private static synchronized OntologyTerms loadDefaultInstance() throws ResourceInitialisationException {
        if (defaultInstance == null) {
            defaultInstance = new OntologyTerms();
        }
        return defaultInstance;
    }

    public OntologyTerms(ListMultimap<String,String> terms) {
        ListMultimap<String,String> copy = ArrayListMultimap.create(terms);
        this.ontology = Multimaps.unmodifiableListMultimap(copy);
    }

    private OntologyTerms() throws ResourceInitialisationException {

        if (OscarProperties.getData().useONT) {
        	ListMultimap<String,String> terms;
        	try {
        		 terms = loadTerms(ONTOLOGY_TERMS_FILE);
                //add polymer ontology if set to polymer mode
                if (OscarProperties.getData().polymerMode) {
                    ListMultimap<String,String> polyOntology = loadTerms(POLYMER_ONTOLOGY_TERMS_FILE);
                    terms.putAll(polyOntology);
                }	
        	} catch (IOException e) {
            	throw new ResourceInitialisationException("failed to load OntologyTerms", e);
            } catch (DataFormatException e) {
            	throw new ResourceInitialisationException("failed to load OntologyTerms", e);
			}
            this.ontology = Multimaps.unmodifiableListMultimap(terms);
        } else {
            this.ontology = ArrayListMultimap.create();
        }

    }

    private ListMultimap<String,String> loadTerms(String path) throws IOException, DataFormatException {
        LOG.info("Loading ontology terms: "+path);
        InputStream is = RESOURCE_GETTER.getStream(path);
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            return TermsFileReader.loadConcatenatedTermMap(in);
        } finally {
            is.close();
        }
    }

    /**
     * @return a Map of ontology terms to space-separated ontology ids
     */
    public ListMultimap<String,String> getOntology() {
        return ontology;
    }
    
}
