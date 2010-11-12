package uk.ac.cam.ch.wwmm.oscarMEMM.memm.finder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.obo.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar.obo.TermMaps;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscarMEMM.tokenAnalysis.PrefixFinder;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/** A subclass of DFAFinder, used to find named entities that are not found 
 * by the MEMM. Currently handles CUST as well as ONT and CPR.
 * 
 * @author ptc24
 *
 */
@SuppressWarnings("serial")
public class DFAONTCPRFinder extends DFAFinder {

	private final Logger logger = Logger.getLogger(DFAONTCPRFinder.class);

	private static DFAONTCPRFinder myInstance;
	
	/**Reads the current state of the DFAONTCPRFinder singleton from the workspace.
	 * 
	 */
	public static void readFromWorkspace() {
		try {
			//long time = System.currentTimeMillis();
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(OscarProperties.getData().workspace, "dfas.dat")));
			myInstance = (DFAONTCPRFinder)ois.readObject();
			ois.close();
			//System.out.println("DFAs loaded in " + (System.currentTimeMillis() - time) + " milliseconds");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error(e);
		}	
	}
	
	/**Writes the current state of the DFAONTCPRFinder singleton to the workspace.
	 * 
	 */
	public static void writeToWorkspace() {
		try {
			//long time = System.currentTimeMillis();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(OscarProperties.getData().workspace, "dfas.dat")));
			oos.writeObject(getInstance());
			oos.close();
			//System.out.println("DFAs loaded in " + (System.currentTimeMillis() - time) + " milliseconds");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error(e);
		}	
		
	}
	
	/**Get the DFAONTCPRFinder singleton, initialising if necessary.
	 * 
	 * @return The DFAONTCPRFinder singleton.
	 */
	public static DFAONTCPRFinder getInstance() {
		if(myInstance == null) {
			myInstance = new DFAONTCPRFinder();
		}
		return myInstance;
	}
	
	/**Re-initialise the DFAONTCPRFinder singleton.
	 * 
	 */
	public static void reinitialise() {
		myInstance = null;
		getInstance();
	}
	
	/**Destroy the DFAONTCPRFinder singleton.
	 * 
	 */
	public static void destroyInstance() {
		myInstance = null;
	}
	
	/**Checks to see if a string can be tokenised into multiple tokens; if
	 * so, deletes the DFAONTCPRFinder singleton.
	 * 
	 * @param word The string to test.
	 */
	public static void destroyInstanceIfWordTokenises(String word) {
		if(myInstance == null) return;
		TokenSequence ts = Tokeniser.getInstance().tokenise(word);
		if(ts.getTokens().size() > 1) myInstance = null;
	}
	
	private DFAONTCPRFinder() {
		logger.debug("Initialising DFA ONT Finder...");
		super.init();
		logger.debug("Initialised DFA ONT Finder");
	}
	
	@Override
	protected void addTerms() {
		logger.debug("Adding ontology terms to DFA finder...");
		for(String s : OntologyTerms.getAllTerms()) addNE(s, "ONT", false);
		for(String s : TermMaps.getCustEnt().keySet()) addNE(s, "CUST", true);
		addNE("$ONTWORD", "ONT", false);
	}
	
	/**Finds the ONT/CPR/CUST NEs from a token sequence.
	 * 
	 * @param t The token sequence
	 * @return The NEs.
	 */
	public List<NamedEntity> getNEs(TokenSequence t) {
		NECollector nec = new NECollector();
		List<List<String>> repsList = makeReps(t);
		findItems(t, repsList, nec);
		//System.out.println("NEC SIZE "+nec.getNes().size());
		return nec.getNes();
	}
	
	private List<List<String>> makeReps(TokenSequence t) {
		List<List<String>> repsList = new ArrayList<List<String>>();
		for(Token token : t.getTokens()) {
			
			repsList.add(repsForToken(token));
			//System.out.println("repList size is "+repsList.size());
		}
		return repsList;
	}
	
	protected List<String> repsForToken(Token t) {
		List<String> reps = new ArrayList<String>();
		reps.add(t.getValue());
		String normValue = StringTools.normaliseName(t.getValue());
		if(!normValue.equals(t.getValue())) reps.add(normValue);
		if(OntologyTerms.hasTerm(normValue)) 
		{   
			reps.add("$ONTWORD");

		}
		if(t.getValue().length() == 1) {
			if(StringTools.hyphens.contains(t.getValue())) {
				reps.add("$HYPH");
			} else if(StringTools.midElipsis.contains(t.getValue())) {
				reps.add("$DOTS");
			}
		}
		reps.addAll(getSubReRepsForToken(t.getValue()));
		
		return reps;
	}
	
	@Override
	protected void handleNe(AutomatonState a, int endToken, TokenSequence t, ResultsCollector collector) {
		String surface = t.getSubstring(a.startToken, endToken);
		String type = a.type;
		//System.out.println(surface + " " + a.type);
		if(type.contains("_")) {
			type = type.split("_")[0];
		}
		NamedEntity ne = new NamedEntity(t.getTokens(a.startToken, endToken), surface, type);
		assert(collector instanceof NECollector);
		((NECollector)collector).collect(ne);
		//System.out.println(surface + ": " + a.reps);
		if(a.type.startsWith("ONT")) {
			Set<String> ontIds = runAutToStateToOntIds.get(a.type).get(a.state);
			String s = OntologyTerms.idsForTerm(StringTools.normaliseName(surface));
			if(s != null && s.length() > 0) {
				if(ontIds == null) ontIds = new HashSet<String>();
				ontIds.addAll(StringTools.arrayToList(s.split("\\s+")));				
			}
			ne.addOntIds(ontIds);
			//System.out.println(surface + "\t" + ontIds);
		}
		if(a.type.startsWith("CUST")) {
			//System.out.println(runAutToStateToOntIds.get(a.type));
			Set<String> custTypes = runAutToStateToOntIds.get(a.type).get(a.state);
			ne.addCustTypes(custTypes);
			//System.out.println(surface + "\t" + ontIds);
		}

		//ne.setPattern(StringTools.collectionToString(a.getReps(), "_"));
	}
	
	@Override
	protected void handleTokenForPrefix(Token t, ResultsCollector collector) {
		String prefix = PrefixFinder.getPrefix(t.getValue());
		if(prefix != null) {
			assert(collector instanceof NECollector);
			((NECollector)collector).collect(NamedEntity.forPrefix(t, prefix));
		}
	}
	
}
