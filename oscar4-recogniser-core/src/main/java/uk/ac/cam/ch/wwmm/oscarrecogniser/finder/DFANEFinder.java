package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import org.apache.log4j.Logger;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictSingleton;
import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.obo.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar.obo.TermMaps;
import uk.ac.cam.ch.wwmm.oscar.terms.TermSets;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGram;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.PrefixFinder;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.TokenLevelRegexHolder;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.TokenLevelRegex;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.TokenTypes;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/** A subclass of DFAFinder, used to find named entities.
 *
 * @author ptc24
 *
 */
public class DFANEFinder extends DFAFinder {

    private final Logger logger = Logger.getLogger(DFANEFinder.class);

    private static final long serialVersionUID = -3307600610608772402L;
    private static DFANEFinder myInstance;

    /**Get the DFANEFinder singleton, initialising if necessary.
     *
     * @return The DFANEFinder singleton.
     */
    public static DFANEFinder getInstance() {
        if (myInstance == null) {
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
        if (myInstance == null) return;
        ITokenSequence ts = Tokeniser.getInstance().tokenise(word);
        if (ts.getTokens().size() > 1) myInstance = null;
    }

    private DFANEFinder() {
        logger.debug("Initialising DFA NE Finder...");
        super.init();
        logger.debug("Initialised DFA NE Finder");
    }

    @Override
    protected void loadTerms() {
        logger.debug("Adding terms to DFA finder...");
        for(String s : TermMaps.getNeTerms().keySet()){
            addNamedEntity(s, TermMaps.getNeTerms().get(s), true);
        }
        logger.debug("Adding ontology terms to DFA finder...");
        for(String s : OntologyTerms.getAllTerms()){
            addNamedEntity(s, NamedEntityType.ONTOLOGY, false);
        }
        logger.debug("Adding custom NEs ...");
        for(String s : TermMaps.getCustEnt().keySet()){
            addNamedEntity(s, NamedEntityType.CUSTOM, true);
        }
        logger.debug("Adding names from ChemNameDict to DFA finder...");
        try {
            for(String s : ChemNameDictSingleton.getAllNames()) {
                // System.out.println(s);
                addNamedEntity(s, NamedEntityType.COMPOUND, false);
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
    public List<NamedEntity> findNamedEntities(ITokenSequence t) {
        NECollector nec = new NECollector();
        List<List<String>> repsList = generateTokenRepresentations(t);
        findItems(t, repsList, nec);
        return nec.getNes();
    }

    private List<List<String>> generateTokenRepresentations(ITokenSequence t) {
        List<List<String>> repsList = new ArrayList<List<String>>();
        for(IToken token : t.getTokens()) {
            repsList.add(generateTokenRepresentations(token));
        }
        return repsList;
    }

    protected List<String> generateTokenRepresentations(IToken token) {
        List<String> tokenRepresentations = new ArrayList<String>();
        // Avoid complications with compound refs
        //SciXML dependent - removed 24/11/10 by dmj30
//		if (TokenTypes.isCompRef(t)) {
//			tokenReps.add("$COMPREF");
//			return tokenReps;
//		}
//		if (TokenTypes.isRef(t)) tokenReps.add("$CITREF");
        String value = token.getValue();
        tokenRepresentations.add(value);
        String normalisedValue = StringTools.normaliseName(value);

        if (!normalisedValue.equals(value)) {
            tokenRepresentations.add(normalisedValue);
        }
        tokenRepresentations.addAll(getSubReRepsForToken(value));
        if (value.length() == 1) {
            if (StringTools.isHyphen(value)) {
                tokenRepresentations.add("$HYPH");
            } else if (StringTools.isMidElipsis(value)) {
                tokenRepresentations.add("$DOTS");
            }
        }
        for (TokenLevelRegex tokenLevelRegex : TokenLevelRegexHolder.getInstance().parseToken(value)) {
            if (NamedEntityType.PROPERNOUN.equals(tokenLevelRegex.getType())) {
                if (value.matches("[A-Z][a-z]+") && TermSets.getDefaultInstance().getUsrDictWords().contains(value.toLowerCase()) && !TermSets.getDefaultInstance().getUsrDictWords().contains(value)) tokenLevelRegex = null;
//				if (ExtractTrainingData.getInstance().pnStops.contains(t.getValue())) tlr = null;
            }
            if (tokenLevelRegex != null) {
                tokenRepresentations.add("$"+ tokenLevelRegex.getType());
            }
        }
        boolean stopWord = false;
        Matcher m = PrefixFinder.prefixPattern.matcher(value);
        if (value.length() >= 2 && m.matches()) {
            String lastGroup = m.group(m.groupCount());
            String lastGroupNorm = StringTools.normaliseName(lastGroup);
            if (lastGroup == null || lastGroup.equals("")) {
                tokenRepresentations.add("$" + NamedEntityType.LOCANTPREFIX.getName());
            } else {
                if (TokenLevelRegexHolder.getInstance().macthesTlr(lastGroup, "formulaRegex")) {
                    tokenRepresentations.add("$CPR_FORMULA");
                }
                if (TermSets.getDefaultInstance().getStopWords().contains(lastGroupNorm) ||
                        TermSets.getDefaultInstance().getClosedClass().contains(lastGroupNorm) ||
                        ChemNameDictSingleton.hasStopWord(lastGroupNorm)) {//||
//						ExtractTrainingData.getInstance().nonChemicalWords.contains(lastGroupNorm) ||
//						ExtractTrainingData.getInstance().nonChemicalNonWords.contains(lastGroupNorm)) {
                    if (!isElement(lastGroupNorm)) {
                        stopWord = true;
                    }
                }
//				boolean isModifiedCompRef = false;
//				for (int i = m.start(m.groupCount())+t.getStart(); i < t.getEnd(); i++) {
//					if (!XMLStrings.getInstance().isCompoundReferenceUnderStyle(t.getDoc().getStandoffTable().getElemAtOffset(i))) {
//						isModifiedCompRef = false;
//						break;
//					}
//				}
//				if (isModifiedCompRef) tokenReps.add("$CPR_COMPREF");
            }
        }


        if (isPrefixBody(value)) {
            tokenRepresentations.add("$PREFIXBODY");
        }
        if (isElement(normalisedValue)) {
            tokenRepresentations.add("$EM");
        }
        if (isEndingWithElementName(value)) {
            tokenRepresentations.add("$ENDSINEM");
        }

        try {
//			if (t.getValue().matches(".*[a-z][a-z].*") && !scoreAsStop && !ExtractTrainingData.getInstance().nonChemicalWords.contains(normValue)) {
            if (!stopWord && value.length() > 3 && value.matches(".*[a-z][a-z].*") ) {
                double score;
//				if (ExtractTrainingData.getInstance().chemicalWords.contains(normValue)) score = 100;
                if (ChemNameDictSingleton.hasName(value)) {
                    score = 100;
                }
                else if (TermSets.getDefaultInstance().getUsrDictWords().contains(normalisedValue)
                        || TermSets.getDefaultInstance().getUsrDictWords().contains(value)) {
                    score = -100;
                }
                else {
                    score = NGram.getInstance().testWord(value);
                }

                if (score > OscarProperties.getData().ngramThreshold) {
                    tokenRepresentations.add("$" + TokenTypes.getTypeForSuffix(value).getName());
                    if (value.startsWith("-")) {
                        tokenRepresentations.add("$-" + TokenTypes.getTypeForSuffix(value).getName());
                    }
                    if (value.endsWith("-")) {
                        tokenRepresentations.add("$" + TokenTypes.getTypeForSuffix(value).getName() + "-");
                    }

                    String withoutLastBracket = value;
                    while(withoutLastBracket.endsWith(")") || withoutLastBracket.endsWith("]")) {
                        withoutLastBracket = withoutLastBracket.substring(0, withoutLastBracket.length()-1);
                    }
                    for (int i = 1; i < withoutLastBracket.length(); i++) {
                        if (TermMaps.getSuffixes().contains(withoutLastBracket.substring(i))) {
                            tokenRepresentations.add("$-" + withoutLastBracket.substring(i));
                        }
                    }
                    
                    if (value.contains("(") && !value.contains(")")) {
                        tokenRepresentations.add("$-(-");
                    }
                    if (value.matches("[Pp]oly.+")) {
                        tokenRepresentations.add("$poly-");
                    }
                    if (value.matches("[Pp]oly[\\(\\[\\{].+")) {
                        tokenRepresentations.add("$polybracket-");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ChemNameDictSingleton.hasName(value)) {
            tokenRepresentations.add("$INCND");
        }
        if (OntologyTerms.hasTerm(normalisedValue)) {
            tokenRepresentations.add("$ONTWORD");
        }
        if (OscarProperties.getData().useWordShapeHeuristic) {
//			if (ExtractTrainingData.getInstance().chemicalNonWords.contains(t.getValue())) tokenReps.add("$CMNONWORD");
            if (!TokenTypes.twoLowerPattern.matcher(value).find()
                    && TokenTypes.oneCapitalPattern.matcher(value).find()) {
                 tokenRepresentations.add("$CMNONWORD");
            }
        }
        //SciXML dependent - removed 24/11/10 by dmj30
//		if (t.getDoc() != null) {
//			if (XMLStrings.getInstance().isCompoundReferenceUnderStyle(t.getDoc().getStandoffTable().getElemAtOffset(t.getEnd()-1)) 
//				&& !(XMLStrings.getInstance().isCompoundReferenceUnderStyle(t.getDoc().getStandoffTable().getElemAtOffset(t.getStart())))) {
//				tokenReps.add("$MODIFIEDCOMPREF");
//			}
//			if (!XMLStrings.getInstance().isCompoundReferenceUnderStyle(t.getDoc().getStandoffTable().getElemAtOffset(t.getEnd()-1)) 
//				&& (XMLStrings.getInstance().isCompoundReferenceUnderStyle(t.getDoc().getStandoffTable().getElemAtOffset(t.getStart())))) {
//				tokenReps.add("$MODIFIEDCOMPREF");
//			}			
//		}
        if (TermSets.getDefaultInstance().getStopWords().contains(normalisedValue) ||
                TermSets.getDefaultInstance().getClosedClass().contains(normalisedValue) ||
                ChemNameDictSingleton.hasStopWord(normalisedValue)){// ||
//				ExtractTrainingData.getInstance().nonChemicalWords.contains(normValue) ||
//				ExtractTrainingData.getInstance().nonChemicalNonWords.contains(normValue)) {
            if (!isElement(normalisedValue)) {
                tokenRepresentations.add("$STOP");
            }
        }

        return tokenRepresentations;
    }

    private boolean isEndingWithElementName(String value) {
        return TermSets.getDefaultInstance().getEndingInElementNamePattern().matcher(value).matches();
    }

    private boolean isElement(String normValue) {
        return TermSets.getDefaultInstance().getElements().contains(normValue);
    }

    private boolean isPrefixBody(String s) {
        Matcher m = PrefixFinder.prefixBody.matcher(s);
        return m.matches();
    }

}
