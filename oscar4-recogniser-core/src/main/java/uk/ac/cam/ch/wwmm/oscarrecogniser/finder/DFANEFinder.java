package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictSingleton;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.obo.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar.obo.TermMaps;
import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;
import uk.ac.cam.ch.wwmm.oscar.terms.TermSets;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityTypes;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGram;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.PrefixFinder;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.TLRHolder;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.TokenLevelRegex;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.TokenTypes;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/** A subclass of DFAFinder, used to find named entities.
 * 
 * @author ptc24
 *
 */
public class DFANEFinder extends DFAFinder {

	private final Logger logger = Logger.getLogger(DFANEFinder.class);

	private static final long serialVersionUID = -3307600610608772402L;
	private static DFANEFinder myInstance;
	
	/**Reads the current state of the DFANEFinder singleton from the workspace.
	 * 
	 */
	public static void readFromWorkspace() {
		try {
			//long time = System.currentTimeMillis();
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(OscarProperties.getData().workspace, "dfas.dat")));
			myInstance = (DFANEFinder)ois.readObject();
			ois.close();
			//System.out.println("DFAs loaded in " + (System.currentTimeMillis() - time) + " milliseconds");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error(e);
		}	
	}
	
	/**Writes the current state of the DFANEFinder singleton to the workspace.
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
	
	/**Get the DFANEFinder singleton, initialising if necessary.
	 * 
	 * @return The DFANEFinder singleton.
	 */
	public static DFANEFinder getInstance() {
		if(myInstance == null) {
			myInstance = new DFANEFinder();
		}
		return myInstance;
	}
	
	/**Re-initialise the DFANEFinder singleton.
	 * 
	 */
	public static void reinitialise() {
		myInstance = null;
		getInstance();
	}
	
	/**Destroy the DFANEFinder singleton.
	 * 
	 */
	public static void destroyInstance() {
		myInstance = null;
	}
	
	/**Checks to see if a string can be tokenised into multiple tokens; if
	 * so, deletes the DFANEFinder singleton.
	 * 
	 * @param word The string to test.
	 */
	public static void destroyInstanceIfWordTokenises(String word) {
		if(myInstance == null) return;
		TokenSequence ts = Tokeniser.getInstance().tokenise(word);
		if(ts.getTokens().size() > 1) myInstance = null;
	}

	private DFANEFinder() {
		logger.debug("Initialising DFA NE Finder...");
		super.init();
		logger.debug("Initialised DFA NE Finder");
	}
	
	@Override
	protected void addTerms() {
		logger.debug("Adding terms to DFA finder...");
		for(String s : TermMaps.getNeTerms().keySet()){
			addNE(s, TermMaps.getNeTerms().get(s), true);
		}
		logger.debug("Adding ontology terms to DFA finder...");
		for(String s : OntologyTerms.getAllTerms()){
			addNE(s, "ONT", false);
		}
		logger.debug("Adding custom NEs ...");
		for(String s : TermMaps.getCustEnt().keySet()){
			addNE(s, "CUST", true);
		}
		logger.debug("Adding names from ChemNameDict to DFA finder...");
		try {
			for(String s : ChemNameDictSingleton.getAllNames()) {
				// System.out.println(s);
				addNE(s, NamedEntityTypes.COMPOUND, false);
			}
		} catch (Exception e) {
			System.err.println("Couldn't add names from ChemNameDict!");
		}
	}
	
	//public List<NamedEntity> getNEs(TokenSequence t) {
	//	NECollector nec = new NECollector();
	//	findItems(t, nec);
	//	return nec.getNes();
	//}
	
	/**Finds the NEs from a token sequence.
	 * 
	 * @param t The token sequence
	 * @return The NEs.
	 */
	public List<NamedEntity> getNEs(TokenSequence t) {
		NECollector nec = new NECollector();
		List<List<String>> repsList = makeReps(t);
		findItems(t, repsList, nec);
		return nec.getNes();
	}
	
	private List<List<String>> makeReps(TokenSequence t) {
		List<List<String>> repsList = new ArrayList<List<String>>();
		for(Token token : t.getTokens()) {
			repsList.add(repsForToken(token));
		}
		return repsList;
	}
	
	protected List<String> repsForToken(Token t) {
		List<String> tokenReps = new ArrayList<String>();
		// Avoid complications with compound refs
		if(TokenTypes.isCompRef(t)) {
			tokenReps.add("$COMPREF");
			return tokenReps;
		}
		if(TokenTypes.isRef(t)) tokenReps.add("$CITREF");
		tokenReps.add(t.getValue());
		String normValue = StringTools.normaliseName(t.getValue());
		String withoutLastBracket = t.getValue();
		while(withoutLastBracket.endsWith(")") || withoutLastBracket.endsWith("]")) {
			withoutLastBracket = withoutLastBracket.substring(0, withoutLastBracket.length()-1);
		}
		if(!normValue.equals(t.getValue())) {
			tokenReps.add(normValue);
		}
		tokenReps.addAll(getSubReRepsForToken(t.getValue()));
		if(t.getValue().length() == 1) {
			if(StringTools.hyphens.contains(t.getValue())) {
				tokenReps.add("$HYPH");
			} else if(StringTools.midElipsis.contains(t.getValue())) {
				tokenReps.add("$DOTS");
			}
		}
		for(TokenLevelRegex tlr : TLRHolder.getInstance().parseToken(t.getValue())) {
			if(tlr.getType().equals(NamedEntityTypes.PROPERNOUN)) {
				if(t.getValue().matches("[A-Z][a-z]+") && TermSets.getDefaultInstance().getUsrDictWords().contains(t.getValue().toLowerCase()) && !TermSets.getDefaultInstance().getUsrDictWords().contains(t.getValue())) tlr = null;
//				if(ExtractTrainingData.getInstance().pnStops.contains(t.getValue())) tlr = null;
			} 
			if(tlr != null) tokenReps.add("$"+tlr.getType());
		}
		boolean scoreAsStop = false;
//		boolean scoreAsSeen = true;
		Matcher m = PrefixFinder.prefixPattern.matcher(t.getValue());
		if(t.getValue().length() >= 2 && m.matches()) {
			String lastGroup = m.group(m.groupCount());
			String lastGroupNorm = StringTools.normaliseName(lastGroup);
			if(lastGroup == null || lastGroup.equals("")) {
				tokenReps.add("$" + NamedEntityTypes.LOCANTPREFIX.toUpperCase());				
			} else {
				if(TLRHolder.getInstance().macthesTlr(lastGroup, "formulaRegex")) {
					tokenReps.add("$CPR_FORMULA");
				}
				if(TermSets.getDefaultInstance().getStopWords().contains(lastGroupNorm) ||
						TermSets.getDefaultInstance().getClosedClass().contains(lastGroupNorm) ||
						ChemNameDictSingleton.hasStopWord(lastGroupNorm)) {//|| 
//						ExtractTrainingData.getInstance().nonChemicalWords.contains(lastGroupNorm) ||
//						ExtractTrainingData.getInstance().nonChemicalNonWords.contains(lastGroupNorm)) {
					if(!TermSets.getDefaultInstance().getElements().contains(lastGroupNorm)) scoreAsStop = true;
				}
				boolean isModifiedCompRef = false;
//				for(int i=m.start(m.groupCount())+t.getStart();i<t.getEnd();i++) {
//					if(!XMLStrings.getInstance().isCompoundReferenceUnderStyle(t.getDoc().getStandoffTable().getElemAtOffset(i))) {
//						isModifiedCompRef = false;
//						break;
//					}
//				}
				if(isModifiedCompRef) tokenReps.add("$CPR_COMPREF");
				
			}
		}
		
		m = PrefixFinder.prefixBody.matcher(t.getValue());
		if(m.matches()) tokenReps.add("$PREFIXBODY");
		
		if(TermSets.getDefaultInstance().getElements().contains(normValue)) tokenReps.add("$EM");
		if(TermSets.getDefaultInstance().getEndingInElementNamePattern().matcher(t.getValue()).matches()) {
			tokenReps.add("$ENDSINEM");
		}

		try {
//			if(t.getValue().matches(".*[a-z][a-z].*") && !scoreAsStop && !ExtractTrainingData.getInstance().nonChemicalWords.contains(normValue)) {
			if(t.getValue().matches(".*[a-z][a-z].*") && !scoreAsStop ) {
    			double score = NGram.getInstance().testWord(t.getValue());
				if(TermSets.getDefaultInstance().getUsrDictWords().contains(normValue) ||
						TermSets.getDefaultInstance().getUsrDictWords().contains(t.getValue())) score = -100;
//				if(ExtractTrainingData.getInstance().chemicalWords.contains(normValue)) score = 100;
				if(ChemNameDictSingleton.hasName(t.getValue())) score = 100;
				if(t.getValue().length() > 3 && score > OscarProperties.getData().ngramThreshold) {
					tokenReps.add("$" + TokenTypes.getTypeForSuffix(t.getValue()).toUpperCase());
					if(t.getValue().startsWith("-")) {
						tokenReps.add("$-" + TokenTypes.getTypeForSuffix(t.getValue()).toUpperCase());
					}
					if(t.getValue().endsWith("-")) {
						tokenReps.add("$" + TokenTypes.getTypeForSuffix(t.getValue()).toUpperCase() + "-");
					}
					for(int i=1;i<withoutLastBracket.length();i++) {
						if(TermMaps.getSuffixes().contains(withoutLastBracket.substring(i))) {
							tokenReps.add("$-" + withoutLastBracket.substring(i));
						}
					}
					if(t.getValue().contains("(") && !t.getValue().contains(")")) {
						tokenReps.add("$-(-");
					}
					if(t.getValue().matches("[Pp]oly.+")) {
						tokenReps.add("$poly-");
					}
					if(t.getValue().matches("[Pp]oly[\\(\\[\\{].+")) {
						tokenReps.add("$polybracket-");
					}

					
				}

			}
		} catch (Exception e) {
			
		}
				
		if(ChemNameDictSingleton.hasName(t.getValue())) tokenReps.add("$INCND");
		if(OntologyTerms.hasTerm(normValue)) tokenReps.add("$ONTWORD");
		if(!TokenTypes.twoLowerPattern.matcher(t.getValue()).find() && TokenTypes.oneCapitalPattern.matcher(t.getValue()).find()) {
			//System.out.println("Yay!");
			if(OscarProperties.getData().useWordShapeHeuristic) tokenReps.add("$CMNONWORD");
//			if(ExtractTrainingData.getInstance().chemicalNonWords.contains(t.getValue())) tokenReps.add("$CMNONWORD");
		}
		if(t.getDoc() != null) {
			if(XMLStrings.getInstance().isCompoundReferenceUnderStyle(t.getDoc().getStandoffTable().getElemAtOffset(t.getEnd()-1)) 
				&& !(XMLStrings.getInstance().isCompoundReferenceUnderStyle(t.getDoc().getStandoffTable().getElemAtOffset(t.getStart())))) {
				tokenReps.add("$MODIFIEDCOMPREF");
			}
			if(!XMLStrings.getInstance().isCompoundReferenceUnderStyle(t.getDoc().getStandoffTable().getElemAtOffset(t.getEnd()-1)) 
				&& (XMLStrings.getInstance().isCompoundReferenceUnderStyle(t.getDoc().getStandoffTable().getElemAtOffset(t.getStart())))) {
				tokenReps.add("$MODIFIEDCOMPREF");
			}			
		}
		if(TermSets.getDefaultInstance().getStopWords().contains(normValue) ||
				TermSets.getDefaultInstance().getClosedClass().contains(normValue) ||
				ChemNameDictSingleton.hasStopWord(normValue)){// || 
//				ExtractTrainingData.getInstance().nonChemicalWords.contains(normValue) ||
//				ExtractTrainingData.getInstance().nonChemicalNonWords.contains(normValue)) {
			if(!TermSets.getDefaultInstance().getElements().contains(normValue)) tokenReps.add("$STOP");
		}

		return tokenReps;
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
			String s = OntologyTerms.idsForTerm(surface);
			if(s != null && s.length() > 0) {
				if(ontIds == null) ontIds = new HashSet<String>();
				ontIds.addAll(StringTools.arrayToList(s.split("\\s+")));				
			}
			ne.addOntIds(ontIds);
			//System.out.println(surface + "\t" + ontIds);
		}
		if(a.type.startsWith("CUST")) {
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
