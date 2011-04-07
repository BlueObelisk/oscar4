package uk.ac.cam.ch.wwmm.oscar.ont;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.obo.OntologyTerm;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

/**Holds strings corresponding to ontology terms and their matching IDs, for
 * use during named entity recognition.
 * 
 * @author ptc24
 * @author dmj30
 *
 */
public final class OntologyTerms {

	private static final Logger LOG = LoggerFactory.getLogger(OntologyTerm.class);
    private static final ResourceGetter RESOURCE_GETTER = new ResourceGetter(OntologyTerm.class.getClassLoader(),"/");
	
	private static final String ONTOLOGY_TERMS_FILE = "uk/ac/cam/ch/wwmm/oscar/obo/terms/ontology.txt";
	
	private ListMultimap<String,String> terms;
	private Set<String> hyphTokable;
	
	private static Pattern maybeHyphPattern = Pattern.compile("(\\S+)(?:\\s+|-)(\\S+)");
	
	private static OntologyTerms defaultInstance;

	/**
	 * Gets the default instance of OntologyTerms, containing the
	 * ontology terms defined in ontology.txt and derived from
	 * ChEBI, FIX and REX.
	 */
	public static OntologyTerms getDefaultInstance() {
		if (defaultInstance == null) {
            return createInstance();
        }
		return defaultInstance;
	}

    private static synchronized OntologyTerms createInstance() {
        if (defaultInstance == null) {
            defaultInstance = new OntologyTerms();
        }
        return defaultInstance;    
    }

    private OntologyTerms() {
    	ListMultimap<String,String> terms;
    	try {
    		 terms = loadTerms(ONTOLOGY_TERMS_FILE);
    	} catch (IOException e) {
        	throw new OscarInitialisationException("failed to load OntologyTerms", e);
        } catch (DataFormatException e) {
        	throw new OscarInitialisationException("failed to load OntologyTerms", e);
		}
        this.terms = Multimaps.unmodifiableListMultimap(terms);
	}
	
	
    /**
     * Constructor for using custom ontologies
     * 
     * @param terms a ListMultimap of ontology terms to corresponding ids
     */
	public OntologyTerms(ListMultimap<String, String> terms) {
		this.terms = Multimaps.unmodifiableListMultimap(terms);
	}

	/**Checks if the ontology set contains a given term name or synonym.
	 * 
	 * @param term The term name to query.
	 * @return Whether the term exists.
	 */
	public boolean containsTerm(String term) {
		return terms.containsKey(term);
	}
	
	/**Gets all IDs that apply to the term name or synonym , as a
	 * space-separated list. The returned list is read-only.
	 * 
	 * @param term The term name or synonym to query.
	 * @return The IDs, or null.
	 */
	public List<String> getIdsForTerm(String term) {
		return terms.get(term);
	}
	
	/**Gets all of the term names and synonyms. The returned set
	 * is read-only.
	 * 
	 * @return The term names an synonyms.
	 */
	public Set<String> getAllTerms() {
		return terms.keySet();
	}
	
	/**
	 * Returns the {@link ListMultimap} defining the term-id relations.
	 * The returned ListMultimap is read-only. 
	 */
	public ListMultimap<String, String> getOntology() {
		return terms;
	}
	
	/**Produces some data for the HyphenTokeniser.
	 * 
	 * @return Some data for the HyphenTokeniser, as
	 * an unmodifiable Set.
	 */
	public Set<String> getHyphTokable() {
		if (hyphTokable == null) {
			Set<String> ht = makeHyphTokable(terms.keySet());
            hyphTokable = Collections.unmodifiableSet(ht);
		}
		return hyphTokable;
	}

	Set<String> makeHyphTokable(Set<String> ontologyTerms) {
		Set<String> ht = new HashSet<String>();
		for (String term : ontologyTerms) {
			Matcher m = maybeHyphPattern.matcher(term);
			while(m.find()) {
				ht.add(m.group(1) + " " + m.group(2));
			}
		}
		return ht;
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
}
