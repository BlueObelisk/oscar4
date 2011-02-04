package uk.ac.cam.ch.wwmm.oscar.obo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import uk.ac.cam.ch.wwmm.oscar.exceptions.ResourceInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.obo.dso.DSOtoOBO;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;

/**Holds strings corresponding to ontology terms and their matching IDs, for
 * use during named entity recognition.
 * 
 * @author ptc24
 *
 */
public final class OntologyTermIdIndex {

	private ListMultimap<String,String> termIdMap;
	private Set<String> hyphTokable;
	
//	private static Pattern maybeHyphPattern = Pattern.compile("(\\S+)\\s+\\$\\(\\s+\\$HYPH\\s+\\$\\)\\s+\\$\\?\\s+(\\S+)");
//																foo $( $HYPH $) $? bar
	private static Pattern maybeHyphPattern = Pattern.compile("(\\S+)(?:\\s+|-)(\\S+)");
	
	private static OntologyTermIdIndex defaultInstance;
	
	public static OntologyTermIdIndex getInstance() throws ResourceInitialisationException {
		if (defaultInstance == null) {
            return createInstance();
        }
		return defaultInstance;
	}

    private static synchronized OntologyTermIdIndex createInstance() throws ResourceInitialisationException {
        if (defaultInstance == null) {
            defaultInstance = new OntologyTermIdIndex();
        }
        return defaultInstance;    
    }

    private OntologyTermIdIndex() throws ResourceInitialisationException {
		termIdMap = ArrayListMultimap.create();
        Map<String,String> ontology = OntologyTerms.getDefaultInstance().getOntology();
		for (String term : ontology.keySet()) {
			addTerm(term, ontology.get(term));
		}
		if (OscarProperties.getData().useDSO) {
			try {
				OBOOntology dso = DSOtoOBO.readDSO();
				for(OntologyTerm term : dso.terms.values()) {
					addTerm(term.getName(), term.getId());
					for(Synonym s : term.getSynonyms()) {
						addTerm(s.getSyn(), term.getId());
					}
				}
			} catch (Exception e) {
				throw new Error(e);
			}
		}
	}
	
	private void addTerm(String term, String id) {
		termIdMap.put(term, id);
	}
	
	/**Whether the ontology set contains a given term name or synonym.
	 * 
	 * @param term The term name to query.
	 * @return Whether the term exists.
	 */
	public boolean containsTerm(String term) {
		return termIdMap.containsKey(term);
	}
	
	/**Gets all IDs that apply to the term name or synonym , as a
	 * space-separated list.
	 * 
	 * @param term The term name or synonym to query.
	 * @return The IDs, or null.
	 */
	public List<String> getIdsForTerm(String term) {
		return termIdMap.get(term);
	}
	
	/**Gets all of the term names and synonyms.
	 * 
	 * @return The term names an synonyms.
	 */
	public Set<String> getAllTerms() {
		return termIdMap.keySet();
	}
	
	/**Produces some data for the HyphenTokeniser.
	 * 
	 * @return Some data for the HyphenTokeniser.
	 */
	public Set<String> getHyphTokable() {
		if (hyphTokable == null) {
			Set<String> ht = makeHyphTokable(termIdMap.keySet());
            hyphTokable = ht;
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
	
}
