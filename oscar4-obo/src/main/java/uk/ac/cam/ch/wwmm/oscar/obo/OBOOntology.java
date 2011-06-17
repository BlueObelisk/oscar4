package uk.ac.cam.ch.wwmm.oscar.obo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.io.IOUtils;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries.ChEBIDictionary;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries.DefaultDictionary;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.obo.dso.DSOtoOBO;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.util.CacheMap;

/**
 * A class to hold and process OBO ontologies, such as ChEBI.
 * 
 * @author ptc24
 */
public class OBOOntology {

	public Map<String,OntologyTerm> terms;
	Map<String,Set<String>> indexByName;
	static ResourceGetter rg = new ResourceGetter(OBOOntology.class.getClassLoader(),"uk/ac/cam/ch/wwmm/oscar/obo/terms/");
	CacheMap<String,Set<String>> queryCache;
	
	private static OBOOntology myInstance;
	
	boolean isTypeOfIsBuilt;
	private ChemNameDictRegistry registry;
	
	/**Gets the OBOOntology singleton, initialising (by loading ChEBI, FIX and REX) if
	 * necessary.
	 * 
	 * @return The OBOOntology singleton
	 */
	public static OBOOntology getInstance(boolean useDSO) {
		if(myInstance == null) {
			myInstance = new OBOOntology();
			try {
				myInstance.read("chebi.obo");
				myInstance.read("fix.obo");
				myInstance.read("rex.obo");
			}
			catch (IOException e) {
				throw new OscarInitialisationException("failed to load OBO Ontology", e);
			}
			if(useDSO)
				myInstance.addOntology(DSOtoOBO.readDSO());
		}
		return myInstance;
	}
	
	/**
	 * Initialise a new, empty, ontology.
	 */
	public OBOOntology() {
		terms = new HashMap<String,OntologyTerm>();
		indexByName = new HashMap<String,Set<String>>();
		queryCache = new CacheMap<String,Set<String>>(10000);
		isTypeOfIsBuilt = false;
		registry = new ChemNameDictRegistry(Locale.ENGLISH);
		registry.register(ChEBIDictionary.getInstance());
		registry.register(new DefaultDictionary());
	}
	
	void read(String s) throws IOException {
		InputStream is = rg.getStream(s);
		try {
			read(new BufferedReader(new InputStreamReader(is, "UTF-8")));	
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	/**Read a .obo file
	 * 
	 * @param f The .obo file to read.
	 * @throws Exception
	 */
	@Deprecated
	//TODO this isn't called - do we need it?
	public void read(File f) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		try {
			read(new BufferedReader(new InputStreamReader(fis, "UTF-8")));
		}
		finally {
			IOUtils.closeQuietly(fis);
		}
	}
	
	void read(BufferedReader br) throws IOException {
		List<String> lines = new ArrayList<String>();
		boolean inTerm = false;
		String line = br.readLine();
		while(line != null) {
			if(line.matches("\\[.*\\]")) {
				if(inTerm) {
					handleTerm(lines);
					inTerm = false;
					lines = new ArrayList<String>();
				}
				if(line.equals("[Term]")) inTerm = true;
			} else {
				if(inTerm) lines.add(line);
			}
			line = br.readLine();
		}
	}
	
	private void handleTerm(List<String> lines) {
		OntologyTerm term = new OntologyTerm(lines);
		terms.put(term.getId(),term);
		indexTerm(term);
	}
	
	/**Add a single OntologyTerm to the ontology.
	 * 
	 * @param term The OntologyTerm.
	 */
	public void addTerm(OntologyTerm term) {
		terms.put(term.getId(),term);
		indexTerm(term);
		isTypeOfIsBuilt = false;
	}
	
	/**Merge a whole ontology into the current one.
	 * 
	 * @param ont The ontology to merge in.
	 */
	public void addOntology(OBOOntology ont) {
		for(String id : ont.terms.keySet()) {
			addTerm(ont.terms.get(id));
		}
	}
	
	private void indexTerm(OntologyTerm term) {
		Set<String> names = new HashSet<String>();
		names.add(term.getName());
		for(Synonym s : term.getSynonyms()) {
			String st = s.getType();
			if(st != null && !st.matches(".*(InChI|SMILES|FORMULA).*")) {
				names.add(st);
			} 
		}
		for(String termName : names) {
			Set<String> termIds = indexByName.get(termName);
			if(termIds == null) {
				termIds = new HashSet<String>();
				indexByName.put(termName, termIds);
			}	
			termIds.add(term.getId());
		}
	}
	
	private void buildIsTypeOf() {
		if(isTypeOfIsBuilt) return;
		for(String termId : terms.keySet()) {
			OntologyTerm term = terms.get(termId);
			for(String isA : term.getIsA()) {
				terms.get(isA).addIsTypeOf(termId);
			}
		}
		isTypeOfIsBuilt = true;
	}

	/**Writes a file suitable for use as onotology.txt.
	 * 
	 * @param pw The PrintWriter to write to.
	 */
	public void writeOntTxt(PrintWriter pw) {
		for(String id : terms.keySet()) {
			OntologyTerm term = terms.get(id);
			Set<String> synSet = new HashSet<String>();
			synSet.add(term.getName());
			for(Synonym s : term.getSynonyms()) {
				String st = s.getType();
				if(id.startsWith("PTCO") || (st != null && !st.matches(".*(InChI|SMILES|FORMULA).*"))) {
					String synonym = s.getSyn();
					if (!synonym.matches("\\S")){//reject ontology terms consisting of a single letter as these produce too many false positives
						synSet.add(synonym);
					}
				} 
			}
			boolean inCND = false;
			for(String syn : synSet) {
				if(registry.hasName(syn)) {
					inCND = true;
					break;
				}
			}
			if(inCND) continue;
			
			if(id.startsWith("CHEBI")) {
				for(String syn : new ArrayList<String>(synSet)) {
					synSet.add(syn.replaceAll("oid$", "oids"));
					synSet.add(syn.replaceAll("oids$", "oid"));
					synSet.add(syn.replaceAll(" compound$", " compounds"));
					synSet.add(syn.replaceAll(" compounds$", " compound"));
					synSet.add(syn.replaceAll("alkane$", "alkanes"));
					synSet.add(syn.replaceAll("alkanes$", "alkane"));
					synSet.add(syn.replaceAll("alkene$", "alkenes"));
					synSet.add(syn.replaceAll("alkenes$", "alkene"));
					synSet.add(syn.replaceAll("alkyne$", "alkynes"));
					synSet.add(syn.replaceAll("alkynes$", "alkyne"));
					synSet.add(syn.replaceAll("entity$", "entities"));
					synSet.add(syn.replaceAll("entities$", "entity"));
					synSet.add(syn.replaceAll("agent$", "agents"));
					synSet.add(syn.replaceAll("agents$", "agent"));
					synSet.add(syn.replaceAll("^elemental ", ""));
					synSet.add(syn.replaceAll("group$", "groups"));
					synSet.add(syn.replaceAll("groups$", "group"));
					synSet.add(syn.replaceAll("derivative$", "derivatives"));
					synSet.add(syn.replaceAll("derivatives$", "derivative"));
					synSet.add(syn.replaceAll("inhibitor$", "inhibitors"));
					synSet.add(syn.replaceAll("inhibitors$", "inhibitor"));
					synSet.add(syn.replaceAll("element$", "elements"));
					synSet.add(syn.replaceAll("hydrocarbons$", "hydrocarbon"));
					synSet.add(syn.replaceAll("hydrocarbon$", "hydrocarbons"));
					synSet.add(syn.replaceAll("residues$", "residue"));
					synSet.add(syn.replaceAll("residue$", "residues"));
					synSet.add(syn.replaceAll("salts$", "salt"));
					synSet.add(syn.replaceAll("salt$", "salts"));
					synSet.add(syn.replaceAll("metabolites$", "metabolite"));
					synSet.add(syn.replaceAll("metabolite$", "metabolites"));
					synSet.add(syn.replaceAll("ions$", "ion"));
					synSet.add(syn.replaceAll("ion$", "ions"));
					synSet.add(syn.replaceAll("drugs$", "drug"));
					synSet.add(syn.replaceAll("drug$", "drugs"));
					synSet.add(syn.replaceAll("agents$", "agent"));
					synSet.add(syn.replaceAll("agent$", "agents"));
					synSet.add(syn.replaceAll("radicals$", "radical"));
					synSet.add(syn.replaceAll("radical$", "radicals"));
					synSet.add(syn.replaceAll("clusters$", "cluster"));
					synSet.add(syn.replaceAll("cluster$", "clusters"));
					synSet.add(syn.replaceAll("fuels$", "fuel"));
					synSet.add(syn.replaceAll("fuel$", "fuels"));
					synSet.add(syn.replaceAll("antibiotics$", "antibiotic"));
					synSet.add(syn.replaceAll("antibiotic$", "antibiotics"));
					synSet.add(syn.replaceAll("leptics$", "leptic"));
					synSet.add(syn.replaceAll("leptic$", "leptics"));
					synSet.add(syn.replaceAll("foods$", "food"));
					synSet.add(syn.replaceAll("food$", "foods"));
					synSet.add(syn.replaceAll("proteins$", "protein"));
					synSet.add(syn.replaceAll("protein$", "proteins"));
					synSet.add(syn.replaceAll("esters$", "ester"));
					synSet.add(syn.replaceAll("ester$", "esters"));
					synSet.add(syn.replaceAll("ketones$", "ketone"));
					synSet.add(syn.replaceAll("ketone$", "ketones"));
					synSet.add(syn.replaceAll("lactones$", "lactone"));
					synSet.add(syn.replaceAll("lactone$", "lactones"));
					synSet.add(syn.replaceAll("amines$", "amine"));
					synSet.add(syn.replaceAll("amine$", "amines"));
					synSet.add(syn.replaceAll("bases$", "base"));
					synSet.add(syn.replaceAll("base$", "bases"));
					synSet.add(syn.replaceAll("anaesthetics$", "anaesthetic"));
					synSet.add(syn.replaceAll("anaesthetics$", "anesthetic"));
					synSet.add(syn.replaceAll("anaesthetic$", "anaesthetics"));
					synSet.add(syn.replaceAll("anaesthetic$", "anesthetics"));
					synSet.add(syn.replaceAll("anesthetics$", "anesthetic"));
					synSet.add(syn.replaceAll("anesthetics$", "anaesthetic"));
					synSet.add(syn.replaceAll("anesthetic$", "anesthetics"));
					synSet.add(syn.replaceAll("anesthetic$", "anaesthetics"));
					synSet.add(syn.replaceAll("anaesthetics$", "anesthetics"));
					synSet.add(syn.replaceAll("anaesthetic$", "anesthetic"));
					synSet.add(syn.replaceAll("anesthetics$", "anaesthetics"));
					synSet.add(syn.replaceAll("anesthetic$", "anaesthetic"));
					/*synSet.add(syn.replaceAll("s$", ""));
					synSet.add(syn.replaceAll("$", "s"));
					synSet.add(syn.replaceAll("s$", ""));
					synSet.add(syn.replaceAll("$", "s"));
					synSet.add(syn.replaceAll("s$", ""));
					synSet.add(syn.replaceAll("$", "s"));*/
					
				}
			}
			
			pw.println("[" + id + "]");
			for(String syn : synSet) pw.println(syn);
			pw.println();
		}
		pw.flush();
	}
	
	/**Look up a term by name (or synonym), and return the IDs.
	 * 
	 * @param s The term name to look up. 
	 * @return The IDs for the name, or null.
	 */
	public Set<String> getIdsForTerm(String s) {
		return indexByName.get(s);
	}
	
	/**Given a set of IDs, return a set that contains all of the IDs, the
	 * parents of those IDs, the grandparents, etc.
	 * 
	 * @param termIds The initial "seed" IDs.
	 * @return The full set of IDs.
	 */
	public Set<String> getIdsForIdsWithAncestors(Collection<String> termIds) {
		Stack<String> idsToConsider = new Stack<String>();
		idsToConsider.addAll(termIds);
		Set<String> resultIds = new HashSet<String>();
		while(!idsToConsider.isEmpty()) {
			String id = idsToConsider.pop();
			if(!resultIds.contains(id)) {
				resultIds.add(id);
				if(terms.containsKey(id)) idsToConsider.addAll(terms.get(id).getIsA()); 
			}
		}
		return resultIds;		
	}
	
	/**Given a single ID, return that ID, its parents, grandparents etc.
	 * 
	 * @param termId The initial "seed" ID.
	 * @return The full set of IDs.
	 */
	public Set<String> getIdsForIdWithAncestors(String termId) {
		if(!terms.containsKey(termId)) return new HashSet<String>();
		Stack<String> idsToConsider = new Stack<String>();
		idsToConsider.add(termId);
		Set<String> resultIds = new HashSet<String>();
		while(!idsToConsider.isEmpty()) {
			String id = idsToConsider.pop();
			if(!resultIds.contains(id)) {
				resultIds.add(id);
				idsToConsider.addAll(terms.get(id).getIsA()); 
			}
		}
		return resultIds;		
	}
	
	/**Look up a term by name, and return its ID and the IDs of all
	 * of its ancestors.
	 * 
	 * @param s The term name to look up.
	 * @return The full set of IDs, empty if the term was not found.
	 */
	public Set<String> getIdsForTermWithAncestors(String s) {
		if(!indexByName.containsKey(s)) return new HashSet<String>();
		Stack<String> idsToConsider = new Stack<String>();
		idsToConsider.addAll(getIdsForTerm(s));
		Set<String> resultIds = new HashSet<String>();
		while(!idsToConsider.isEmpty()) {
			String id = idsToConsider.pop();
			if(!resultIds.contains(id)) {
				resultIds.add(id);
				idsToConsider.addAll(terms.get(id).getIsA()); 
			}
		}
		return resultIds;
	}

	/**Given a set of IDs, return a set that contains all of the IDs, the
	 * children of those IDs, the grandchildren, etc.
	 * 
	 * @param s The initial "seed" ID.
	 * @return The full set of IDs.
	 */
	public Set<String> getIdsForIdWithDescendants(String s) {
		buildIsTypeOf();
		if(queryCache.containsKey(s)) return queryCache.get(s);
		Stack<String> idsToConsider = new Stack<String>();
		idsToConsider.add(s);
		Set<String> resultIds = new HashSet<String>();
		while(!idsToConsider.isEmpty()) {
			String id = idsToConsider.pop();
			if(!resultIds.contains(id)) {
				resultIds.add(id);
				if(terms.containsKey(id)) idsToConsider.addAll(terms.get(id).getIsTypeOf()); 
			}
		}
		queryCache.put(s, resultIds);
		return resultIds;		
	}

	/**Look up a term by name, and return its ID and the IDs of all
	 * of its descendants.
	 * 
	 * @param s The term name to look up.
	 * @return The full set of IDs, empty if the term was not found.
	 */
	public Set<String> getIdsForTermWithDescendants(String s) {
		buildIsTypeOf();
		if(!indexByName.containsKey(s)) return new HashSet<String>();
		Stack<String> idsToConsider = new Stack<String>();
		idsToConsider.addAll(getIdsForTerm(s));
		Set<String> resultIds = new HashSet<String>();
		while(!idsToConsider.isEmpty()) {
			String id = idsToConsider.pop();
			if(!resultIds.contains(id)) {
				resultIds.add(id);
				idsToConsider.addAll(terms.get(id).getIsTypeOf()); 
			}
		}
		return resultIds;		
	}
	
	/**Given a set of seed IDs, expand that set of IDs to include all ancestor
	 * IDs, then return a map from each ID in the set to the descendant IDs 
	 * (including the ID itself).
	 * 
	 * @param ids The seed IDs.
	 * @return The mapping.
	 */
	public Map<String,Set<String>> queriesForIds(Collection<String> ids) {
		Set<String> idsWithParents = getIdsForIdsWithAncestors(ids);
		Map<String,Set<String>> queries = new HashMap<String,Set<String>>();
		for(String id : idsWithParents) {
			queries.put(id, getIdsForIdWithDescendants(id));
		}
		return queries;
	}

	/**Tests whether there is a direct is_a (or has_role) relationship
	 * between two IDs.
	 * 
	 * @param hypoID The potential hyponym (child term).
	 * @param hyperID The potential hypernym (parent term).
	 * @return Whether that direct relationship exists.
	 */
	public boolean directIsA(String hypoID, String hyperID) {
		if(!terms.containsKey(hypoID)) return false;
		OntologyTerm term = terms.get(hypoID);
		if(term.getIsA().contains(hyperID)) return true;
		return false;
	}
	
	/**Tests whether there is a direct or indirect is_a (or has_role)
	 * relationship between two IDs.
	 * 
	 * @param hypoID The potential hyponym (descendant term).
	 * @param hyperID The potential hypernym (ancestor term).
	 * @return Whether that direct relationship exists.
	 */
	public boolean isA(String hypoID, String hyperID) {
		if(hypoID.equals(hyperID)) return false;
		return getIdsForIdWithAncestors(hypoID).contains(hyperID);
	}
	
	/**Looks up the name for an ontology ID.
	 * 
	 * @param id The ontology ID.
	 * @return The name, or null.
	 */
	public String getNameForID(String id) {
		if(!terms.containsKey(id)) return null;
		return terms.get(id).getName();
	}

	/**Looks up the definition for an ontology ID.
	 * 
	 * @param id The ontology ID.
	 * @return The definition, or null.
	 */
	public String getDefinitionForID(String id) {
		if(!terms.containsKey(id)) return null;
		return terms.get(id).getDef();
	}
	
	/**Applies heuristics to see if an ontology ID corresponds to a class
	 * of chemical compounds.
	 * 
	 * @param id The ontology ID.
	 * @return Whether it corresponds to a class of chemical compounds.
	 */
	public boolean isCMType(String id) {
		buildIsTypeOf();
		OntologyTerm term = terms.get(id);
		if(term == null) return false;
		if(!id.startsWith("CHEBI:")) {
			return false;
		}
		if(term.getIsTypeOf().size() == 0) {
			return false;
		}
		if(registry.hasOntologyIdentifier(id)) {
			return false;
		}
		for(Synonym synonym : term.getSynonyms()) {
			if("EXACT FORMULA".equals(synonym.getType())) {
				return false;
			}
		}
		for(String relType : term.getRelationships().keySet()) {
			if("is_conjugate_base_of".equals(relType)) {
				return false;
			}
			if("is_conjugate_acid_of".equals(relType)) {
				return false;
			}
			
		}
		boolean isOK = false;
		for(String parentID : getIdsForIdWithAncestors(id)) {
			if(parentID.equals("CHEBI:24433")) {
				return false; // That means it's a group
			}
			if(parentID.equals("CHEBI:36342")) {
				return false; // That means it's a particle		
			}
		}
		return true;
	}
	
	/**Outputs the term dictionary.
	 * 
	 * @return The term dictionary.
	 */
	public Map<String, OntologyTerm> getTerms() {
		return terms;
	}
	

	/**
	 * Reads and combines the chebi, fix and rex.obo files before writing
	 * ontology.txt 
	 */
	public static void main(String[] args) throws Exception {
		OBOOntology o = getInstance(false);

		FileOutputStream fso = new FileOutputStream(new File("ontology.txt"));
		o.writeOntTxt(new PrintWriter(fso));
	}

}
