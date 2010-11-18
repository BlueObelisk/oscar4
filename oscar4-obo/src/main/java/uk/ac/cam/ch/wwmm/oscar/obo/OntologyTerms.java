package uk.ac.cam.ch.wwmm.oscar.obo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar.obo.dso.DSOtoOBO;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;

/**Holds strings corresponding to ontology terms and their matching IDs, for
 * use during named entity recognition.
 * 
 * @author ptc24
 *
 */
public final class OntologyTerms {

	Map<String,String> termsWithIDs;
	Set<String> hyphTokable;
	
	static Pattern maybeHyphPattern = Pattern.compile("(\\S+)\\s+\\$\\(\\s+\\$HYPH\\s+\\$\\)\\s+\\$\\?\\s+(\\S+)");
	
	private static OntologyTerms myInstance;
	
	private static OntologyTerms getInstance() {
		if(myInstance == null) myInstance = new OntologyTerms();
		return myInstance;
	}
	
	private OntologyTerms() {
		termsWithIDs = new HashMap<String,String>();
		for(String term : TermMaps.getOntology().keySet()) {
			addTerm(term, TermMaps.getOntology().get(term));
		}
		if(OscarProperties.getData().useDSO) {
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
		if(termsWithIDs.containsKey(term)) {
			termsWithIDs.put(term, termsWithIDs.get(term) + " " + id);
		} else {
			termsWithIDs.put(term, id);
		}
	}
	
	/**Whether the ontology set contains a given term name or synonym.
	 * 
	 * @param term The term name to query.
	 * @return Whether the term exists.
	 */
	public static boolean hasTerm(String term) {
		return getInstance().termsWithIDs.containsKey(term);
	}
	
	/**Gets all IDs that apply to the term name or synonym , as a
	 * space-separated list.
	 * 
	 * @param term The term name or synonym to query.
	 * @return The IDs, or null.
	 */
	public static String idsForTerm(String term) {
		return getInstance().termsWithIDs.get(term);
	}
	
	/**Gets all of the term names and synonyms.
	 * 
	 * @return The term names an synonyms.
	 */
	public static Set<String> getAllTerms() {
		return getInstance().termsWithIDs.keySet();
	}
	
	/**Produces some data for the HyphenTokeniser.
	 * 
	 * @return Some data for the HyphenTokeniser.
	 */
	/*
	 * This method appears to be broken. getInstance().termsWithIDs.keySet()
	 * returns 31616 items but nothing matches maybeHyphPattern
	 */
	public static Set<String> getHyphTokable() {
		if(getInstance().hyphTokable == null) {
			Set<String> ht = new HashSet<String>();
			getInstance().hyphTokable = ht;
			for(String term : getInstance().termsWithIDs.keySet()) {
				Matcher m = maybeHyphPattern.matcher(term);
				while(m.find()) {
					ht.add(m.group(1) + " " + m.group(2));
				}
			}
		}
		return getInstance().hyphTokable;
	}
	
}
