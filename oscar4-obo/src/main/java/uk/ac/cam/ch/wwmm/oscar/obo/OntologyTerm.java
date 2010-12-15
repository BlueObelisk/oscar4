package uk.ac.cam.ch.wwmm.oscar.obo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A term in an OBO ontology.
 * 
 * @author ptc24
 *
 */
public final class OntologyTerm {

	private String id;
	private String name;
	private String def;
	private String defSrc;
	private List<Synonym> synonyms;
	private Set<String> isA;
	private Set<String> isTypeOf;
	private Set<String> altID;
	private Map<String,Set<String>> relationships;
	
	private static Pattern tagValuePattern = Pattern.compile("([a-z_]+):\\s+(\\S.*)");
	private static Pattern quotValPattern = Pattern.compile("\"(.*?)(?<!\\\\)\"\\s+(.*)");
	private static Pattern commentPattern = Pattern.compile("(.*?)\\s*(?<!\\\\)!\\s*(.+)");
	private static Pattern synonymQualPattern = Pattern.compile("\\s*(.*?)\\s*\\[(.*)\\]");
	private static Pattern relationshipPattern = Pattern.compile("(\\S+)\\s+(\\S+)");

	private OntologyTerm() {
		altID = new HashSet<String>();
		isA = new HashSet<String>();
		isTypeOf = new HashSet<String>();
		synonyms = new ArrayList<Synonym>();
		relationships = new HashMap<String,Set<String>>();
	}
	
	public OntologyTerm(String id, String name) {
		this();
		this.name = name;
		this.id = id;
		
	}
	
	OntologyTerm(List<String> lines) {
		this();
		for(String line : lines) {
			if(!line.contains(":")) continue;
			Matcher m = tagValuePattern.matcher(line);
			boolean matches = m.matches();
			String tag = m.group(1);
			String val = m.group(2);
			Matcher c = commentPattern.matcher(val);
			//String comment = "";
			if(c.matches()) {
				val = c.group(1);
				//comment = c.group(2);
			}
			Matcher qv = quotValPattern.matcher(val);
			String qualifiers = "";
			if(qv.matches()) {
				val = qv.group(1);
				qualifiers = qv.group(2);
			}
			//System.out.println(tag + " -> " + val + " -> " + qualifiers);
			if(tag.equals("id")) {
				id = val;
			} else if(tag.equals("name")) {
				name = val.replaceAll("\\\\\\{", "{").replaceAll("\\\\\\}", "}");
			} else if(tag.equals("def")) {
				def = val;
				Matcher sq = synonymQualPattern.matcher(qualifiers);
				if(sq.matches() && sq.group(2) != null && sq.group(2).length() > 0) {
					defSrc = sq.group(2);
				}
			} else if(tag.equals("alt_id")) {
				altID.add(val);
			} else if(tag.equals("is_a")) {
				isA.add(val);
			} else if(tag.equals("synonym")) {
				Matcher sq = synonymQualPattern.matcher(qualifiers);
				if(sq.matches()) {
					synonyms.add(new Synonym(val.replaceAll("\\\\\\{", "{").replaceAll("\\\\\\}", "}"), sq.group(1), sq.group(2)));
				}
			} else if(tag.equals("relationship")) {
				Matcher r = relationshipPattern.matcher(val);
				if(r.matches()) {
					String rel = r.group(1);
					if(!relationships.containsKey(rel)) relationships.put(rel, new HashSet<String>());
					relationships.get(rel).add(r.group(2));
					if(rel.equals("has_role")) isA.add(r.group(2));
				}
			}
		}
	}
	
	/**Gets the ontology ID of the term.
	 * 
	 * @return The ontology ID of the term.
	 */
	public String getId() {
		return id;
	}
	
	/**Gets the name of the term.
	 * 
	 * @return The name of the term.
	 */
	public String getName() {
		return name;
	}
	
	/**Gets the synonyms for the term.
	 * 
	 * @return The synonyms for the term.
	 */
	public List<Synonym> getSynonyms() {
		return synonyms;
	}
	
	
	public void addSynonym(String synonym) {
		synonyms.add(new Synonym(synonym, null, null));
	}
	
	Set<String> getIsA() {
		return isA;
	}
	
	public void addIsA(String termId) {
		isA.add(termId);
	}
	
	Set<String> getIsTypeOf() {
		return isTypeOf;
	}
	
	public void addIsTypeOf(String termId) {
		isTypeOf.add(termId);
	}
	
	/**Gets the definition of the term.
	 * 
	 * @return The definition of the term.
	 */
	public String getDef() {
		return def;
	}

	Map<String, Set<String>> getRelationships() {
		return relationships;
	}
	
	/**A string representation of the term.
	 * 
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append("name=" + name);
		sb.append(", id=" + id);
		if(def != null) sb.append(", def=" + def);
		if(defSrc != null) sb.append(", defSrc=" + defSrc);
		for(String alt : altID) sb.append(", alt_id=" + alt);
		for(String is : isA) sb.append(", is_a=" + is);
		for(String relType : relationships.keySet()) {
			for(String rel : relationships.get(relType)) {
				sb.append(", " + relType + "=" + rel);
			}
		}
		for(Synonym syn : synonyms) sb.append(", syn=" + syn);
		sb.append("]");
		return sb.toString();
	}
	
	
	/*public String toOBOString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Term]\n");
		sb.append("id: " + id + "\n");
		sb.append("name: " + name + "\n");
		for(Synonym synonym : synonyms) {
			sb.append("synonym: \"" + synonym.getSyn() + "\" ");
			if(synonym.getType() != null) sb.append(synonym.getType() + " ");
			if(synonym.getSource() != null) {
				sb.append("[" + synonym.getSource() + "]\n");
			} else {
				sb.append("[]\n");
			}
		}
		for(String isa : isA) {
			sb.append("is_a: " + isa + "\n");
		}
		
		return sb.toString();
	}*/
	
}
