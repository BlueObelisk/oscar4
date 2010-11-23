package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictSingleton;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.terms.TermSets;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscarMEMM.FeatureSet;
import uk.ac.cam.ch.wwmm.oscarrecogniser.etd.ExtractedTrainingData;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGram;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.TokenTypes;

/**
 * Converts a list of tokens into features for the MEMM.
 * 
 * @author ptc24
 * 
 */
/*
 * @dmj30
 * There is another class called FeatureExtractor in the
 * oscarMEMM.memm.rescorer package with apparently different
 * functionality
 */
//TODO deal with name duplication
public final class FeatureExtractor {

	private static final Pattern DIGITS = Pattern.compile("[0-9]+");
	private static final Pattern LETTERS = Pattern.compile("[a-z][a-z]+");
	private static final Pattern SINGLE_LETTER = Pattern.compile("[a-z]");
	private static final Pattern CAPS = Pattern.compile("[A-Z][A-Z]+");
	private static final Pattern SINGLE_CAP = Pattern.compile("[A-Z]");
	private static final Pattern GREEKS = Pattern.compile("["+ StringTools.lowerGreek + "]+");

	private static final String ZERO = "0";
	private static final String ONE = "1";
	private static final String TWO = "2";
	private static final String THREE = "3";
	private static final String FOUR = "4";
	private static final String FIVE = "5";
	private static final char C_ONE = '1';
	private static final char C_TWO = '2';
	private static final char C_THREE = '3';
	private static final char C_FOUR = '4';


	private static final String STOPWORD_USER_DEFINED_FEATURE = "$STOP:UDW";
	private static final String STOPWORD_NONCHEMICALNONWORD_FEATURE = "$STOP:NCNW";
	private static final String STOPWORD_NON_CHEMICAL_WORD_FEATURE = "$STOP:NCW";
	private static final String STOPWORD_CLOSED_CLASS_FEATURE = "$STOP:CLOSEDCLASS";
	private static final String STOPWORD_FEATURE = "$STOP:STOPWORD";
	private static final String OXIDATION_STATE_FEATURE = "oxidationState";
	private static final String ENDS_IN_ELEMENT_FEATURE = "endsinem";
	private static final String ELEMENT_FEATURE = "element";
	private static final String IN_NAMEDICT_FEATURE = "inCND";
	private static final String NGRAM_DEC_FEATURE = "ngram-=";
	private static final String NGRAM_INC_FEATURE = "ngram+=";
	private static final String SUFFIX_CT_FEATURE = "ct=";
	private static final String NGRAMSCORE_DEC_FEATURE = "ngscore-=";
	private static final String NGRAMSCORE_INC_FEATURE = "ngscore+=";
	private static final double NGRAM_SCORE_UPPER_BOUND = 15.0;
	private static final int SUFFIX_HI_SCORE = 100;
	private static final int SUFFIX_LO_SCORE = -100;
	private static final double SUFFIX_SCORE_UPPER_BOUND = 15.0;
	private static final double SUFFIX_SCORE_LOWER_BOUND = -15.0;
	private static final String SUFFIX_SCORE_DEC_FEATURE = "sscore-=";
	private static final String SUFFIX_SCORE_INC_FEATURE = "sscore+=";
	private static final String NGRAM_FEATURE = "G=";
	private static final double NGRAM_SCORE_LOWER_BOUND = -15.0;
	private static final String RE_LINE_END = "$";
	private static final String RE_LINE_START = "^";
	private static final String SUFFIX_FEATURE = "s=";
	private static final String SHAPE_FEATURE = "ws=";
	private static final String SHAPE_COMPLEX_FEATURE = "complex";
	private static final String WITHOUT_TERMINAL_S_FEATURE = "wts=";
	private static final String RNMID_FEATURE = "$RNMID";
	private static final String RNEND_FEATURE = "$RNEND";
	private static final String WORD_FEATURE = "w=";
	private static final int WORD_FEATURE_LENGTH = WORD_FEATURE.length();

	private ITokenSequence tokSeq;
	private List<FeatureSet> tokenFeatureSets;

	private static final Pattern suffixPattern = Pattern
			.compile(".*?((yl|ide|ite|ate|ium|ane|yne|ene|ol|"
					+ "ase|ic|oxy|ino|at(ed|ion|ing)|lys(is|es|ed|ing|tic)|i[sz](ed|ations|ing)|)s?)");
	private static final Pattern wordPattern = Pattern
			.compile(".*[a-z][a-z].*");
	private static final Pattern oxPattern = Pattern
			.compile("\\(([oO]|[iI]{1,4}|[iI]{0,3}[xvXV]|[xvXV][iI]{0,4})\\)");
	private static final Pattern pnPattern = Pattern
			.compile("(Mc|Mac)?[A-Z]\\p{Ll}\\p{Ll}+(s'|'s)?");

	private boolean noPC = false;
	private boolean noC = false;

	private boolean newSuffixes = false;

    public static List<List<String>> extractFeatures(ITokenSequence tokSeq) {
        FeatureExtractor featureExtractor = new FeatureExtractor(tokSeq);
        return featureExtractor.getFeatureLists();
    }

    private List<List<String>> getFeatureLists() {
        List<List<String>> features = new ArrayList<List<String>>(tokenFeatureSets.size());
        for (FeatureSet fs : tokenFeatureSets) {
            features.add(fs.getFeatures());
        }
        return features;
    }

    private FeatureExtractor(ITokenSequence tokSeq) {
		this.tokSeq = tokSeq;
		makeFeatures();
	}

	private void makeFeatures() {
        initFeatureSets();
		for (int i = 0; i < tokSeq.size(); i++) {
			makeFeatures(i);
		}
		for (int i = 0; i < tokSeq.size(); i++) {
			mergeFeatures(i);
		}
	}

    private void initFeatureSets() {
        tokenFeatureSets = new ArrayList<FeatureSet>(tokSeq.size());
        for (int i = 0; i < tokSeq.size(); i++) {
            tokenFeatureSets.add(new FeatureSet());
        }
    }

    private String makeWordFeature(String word) {
		return new StringBuilder(word.length() + WORD_FEATURE_LENGTH).append(
				WORD_FEATURE).append(
				word).toString();
	}

	/**
	 * 
	 * TODO This method is performance critical, taking 34% of the overall
	 * operation of OSCAR in processing documents.
	 * 
	 * TODO check whether this is ever called redundantly
	 */
	private void makeFeatures(int position) {
		List<String> local = tokenFeatureSets.get(position).getFeatures();
		List<String> contextable = tokenFeatureSets.get(position).getContextableFeatures();
		List<String> bigramable = tokenFeatureSets.get(position).getBigramableFeatures();

		Token token = tokSeq.getToken(position);
		String word = token.getValue();
		contextable.add(makeWordFeature(word));

		String normWord = StringTools.normaliseName(word);
		if (!word.equals(normWord)) {
			contextable.add(makeWordFeature(normWord));
		}

		ExtractedTrainingData etd = ExtractedTrainingData.getInstance();
		makeWordFeatures(word, normWord, bigramable, etd);
		makeReactionFeatures(word, bigramable, contextable, etd);

		String wts = StringTools.removeTerminalS(normWord);
		contextable.add(WITHOUT_TERMINAL_S_FEATURE + wts);

		makeShapeFeatures(word, bigramable, contextable);
		makeSuffixFeature(word, contextable);
		makeNGramFeatures(word, local);

		if (wordPattern.matcher(word).matches()) {
			if (newSuffixes) {
				handleNewSuffices(word, normWord, bigramable, contextable,
						local, token);
			} else {
				handleNoNewSuffices(word, normWord, bigramable, contextable,
						local, token);
			}
		}

		if (ChemNameDictSingleton.hasName(word)) {
			local.add(IN_NAMEDICT_FEATURE);
		}

		if (TermSets.getDefaultInstance().getElements().contains(normWord)) {
			contextable.add(ELEMENT_FEATURE);
			bigramable.add(ELEMENT_FEATURE);
		}
		if (TermSets.getDefaultInstance().getEndingInElementNamePattern().matcher(word).matches()) {
			contextable.add(ENDS_IN_ELEMENT_FEATURE);
			bigramable.add(ENDS_IN_ELEMENT_FEATURE);
		}
		if (oxPattern.matcher(word).matches()) {
			contextable.add(OXIDATION_STATE_FEATURE);
			bigramable.add(OXIDATION_STATE_FEATURE);
		}

		if (TermSets.getDefaultInstance().getStopWords().contains(normWord)
				|| ChemNameDictSingleton.hasStopWord(normWord)) {
			local.add(STOPWORD_FEATURE);
		}
		if (TermSets.getDefaultInstance().getClosedClass().contains(normWord)) {
			local.add(STOPWORD_CLOSED_CLASS_FEATURE);
		}
		if (ExtractedTrainingData.getInstance().nonChemicalWords
				.contains(normWord)) {
			local.add(STOPWORD_NON_CHEMICAL_WORD_FEATURE);
		}
		if (ExtractedTrainingData.getInstance().nonChemicalNonWords
				.contains(normWord)
				&& !TermSets.getDefaultInstance().getElements().contains(normWord)) {
			local.add(STOPWORD_NONCHEMICALNONWORD_FEATURE);
		}
		if (TermSets.getDefaultInstance().getUsrDictWords().contains(normWord)
				&& !(ChemNameDictSingleton.hasName(normWord) || ExtractedTrainingData
						.getInstance().chemicalWords.contains(normWord))) {
			local.add(STOPWORD_USER_DEFINED_FEATURE);
		}
	}

	private void handleNoNewSuffices(String word, String normWord,
			List<String> bigramable, List<String> contextable,
			List<String> local, Token token) {
		double ngscore = NGram.getInstance().testWord(word);
		// Already seen
		String type = TokenTypes.getTypeForSuffix(token.getValue());
		ngscore = Math.max(ngscore, NGRAM_SCORE_LOWER_BOUND);
		ngscore = Math.min(ngscore, NGRAM_SCORE_UPPER_BOUND);
		for (int i = 0; i < ngscore; i++) {
			local.add((NGRAM_INC_FEATURE + type).intern());
		}
		for (int i = 0; i > ngscore; i--) {
			local.add((NGRAM_DEC_FEATURE + type).intern());
		}

		if (TermSets.getDefaultInstance().getUsrDictWords().contains(normWord)
				|| TermSets.getDefaultInstance().getUsrDictWords().contains(word)) {
			ngscore = SUFFIX_LO_SCORE;
		}
		if (ExtractedTrainingData.getInstance().chemicalWords.contains(normWord)) {
			ngscore = 100;
		}
		if (ChemNameDictSingleton.hasName(word)) {
			ngscore = 100;
		}

		if (ngscore > 0) {
			contextable.add(SUFFIX_CT_FEATURE + type);
			bigramable.add(SUFFIX_CT_FEATURE + type);
		}
	}

	private void handleNewSuffices(String word, String normWord,
			List<String> bigramable, List<String> contextable,
			List<String> local, Token token) {
		double suffixScore = NGram.getInstance().testWordSuffix(word);
		String type = TokenTypes.getTypeForSuffix(token.getValue());

		suffixScore = Math.max(suffixScore, SUFFIX_SCORE_LOWER_BOUND);
		suffixScore = Math.min(suffixScore, SUFFIX_SCORE_UPPER_BOUND);
		for (int i = 0; i < suffixScore; i++) {
			local.add((SUFFIX_SCORE_INC_FEATURE + type).intern());
		}
		for (int i = 0; i > suffixScore; i--) {
			local.add((SUFFIX_SCORE_DEC_FEATURE + type).intern());
		}

		if (TermSets.getDefaultInstance().getUsrDictWords().contains(normWord)
				|| TermSets.getDefaultInstance().getUsrDictWords().contains(word)) {
			suffixScore = SUFFIX_LO_SCORE;
		}
		if (ExtractedTrainingData.getInstance().chemicalWords.contains(normWord)) {
			suffixScore = SUFFIX_HI_SCORE;
		}
		if (ChemNameDictSingleton.hasName(word)) {
			suffixScore = SUFFIX_HI_SCORE;
		}
		double ngscore = NGram.getInstance().testWord(word);
		ngscore = Math.max(ngscore, NGRAM_SCORE_LOWER_BOUND);
		ngscore = Math.min(ngscore, NGRAM_SCORE_UPPER_BOUND);
		for (int i = 0; i < ngscore; i++) {
			local.add((NGRAMSCORE_INC_FEATURE + type).intern());
		}
		for (int i = 0; i > ngscore; i--) {
			local.add((NGRAMSCORE_DEC_FEATURE + type).intern());
		}

		if (suffixScore > 0) {
			contextable.add(SUFFIX_CT_FEATURE + type);
			bigramable.add(SUFFIX_CT_FEATURE + type);
		}
	}

	private void makeNGramFeatures(String word, List<String> local) {
		StringBuilder decWord = new StringBuilder(RE_LINE_START).append(word).append(RE_LINE_END);
		for (int j = 0; j < decWord.length() - 3; j++) {
			for (int k = 1; k <= 4; k++) {
				if (j < 4 - k) {
					continue;
                }
				local.add(makeNGramFeature(decWord, j, k));
			}
		}
	}
	
	/**
	 * makeNGramFeatures is called in a very tight loop. This approach avoids
	 * unnecessarily declaring strings
	 * 
	 * @author dmj30
	 */
	static String makeNGramFeature(StringBuilder decWord, int offset, int length) {
		StringBuilder builder = new StringBuilder();
		//using a switch here doesn't actually make the code faster
		if (length == 1) {
            builder.append(C_ONE);
        }
		else if (length == 2) {
            builder.append(C_TWO);
        }
		else if (length == 3) {
            builder.append(C_THREE);
        }
		else if (length == 4) {
            builder.append(C_FOUR);
        }
		//not expected that k > 4
		else {
            builder.append(length);
        }
		builder.append(NGRAM_FEATURE);
		int y = offset+length;
		for (int x = offset; x < y; x++) {
			builder.append(decWord.charAt(x));
		}
		return builder.toString().intern();
	}
	
	
	private void makeSuffixFeature(String word, List<String> contextable) {
		String suffix = getSuffix(word);
		String suffixFeature = SUFFIX_FEATURE + suffix;
		contextable.add(suffixFeature);
	}

	private void makeShapeFeatures(String word, List<String> bigramable,
			List<String> contextable) {
		String wordShape = wordShape(word);
		if (wordShape.length() > 3) {
			wordShape = SHAPE_COMPLEX_FEATURE;
        }
		if (!wordShape.equals(word)) {
			String wordShapeFeature = SHAPE_FEATURE + wordShape;
			bigramable.add(wordShapeFeature);
			contextable.add(wordShapeFeature);
		}
	}

	private void makeReactionFeatures(String word,
			List<String> bigramable, List<String> contextable, ExtractedTrainingData etd) {
		if (etd.rnEnd.contains(word)) {
			bigramable.add(RNEND_FEATURE);
			contextable.add(RNEND_FEATURE);
		}
		if (etd.rnMid.contains(word)) {
			bigramable.add(RNMID_FEATURE);
			contextable.add(RNMID_FEATURE);
		}
	}

	private void makeWordFeatures(String word, String normWord,
			List<String> bigramable, ExtractedTrainingData etd) {
		if (word.length() < 4 || etd.polysemous.contains(word)
				|| etd.rnEnd.contains(word) || etd.rnMid.contains(word)) {
			bigramable.add(makeWordFeature(word));
			if (!word.equals(normWord)) {
				bigramable.add(makeWordFeature(normWord));
            }
		}
	}

	private void mergeFeatures(int position) {
		List<String> mergedFeatures = tokenFeatureSets.get(position).getFeatures();

		int backwards = Math.min(1, position);
		int forwards = Math.min(1, tokSeq.size() - position - 1);

		if (!noC) {
			for (int i = -backwards; i <= forwards; i++) {
				for (String cf : tokenFeatureSets.get(position + i).getContextableFeatures()) {
					mergedFeatures.add(("c" + i + ":" + cf).intern());
				}
			}
		}

		// NB support left in incase bg:0:0: etc. become viable
		for (int i = -backwards; i <= forwards; i++) {
			for (int j = i + 1; j <= forwards; j++) {
				if (j - i == 1 || j == i) {
					String prefix = "bg:" + i + ":" + j + ":";
					for (String feature1 : tokenFeatureSets.get(position + i).getBigramableFeatures()) {
						for (String feature2 : tokenFeatureSets.get(position
								+ j).getBigramableFeatures()) {
							// feature1 != feature2 is not a bug, if j == i
							if (j != i || feature1 != feature2)
								mergedFeatures
										.add((prefix + feature1 + "__" + feature2)
												.intern());
						}
					}
				}
			}
		}

		String word = tokSeq.getToken(position).getValue();

		if (pnPattern.matcher(word).matches()) {
			boolean suspect = false;
			if (word.matches("[A-Z][a-z]+")
					&& TermSets.getDefaultInstance().getUsrDictWords().contains(word.toLowerCase())
					&& !TermSets.getDefaultInstance().getUsrDictWords().contains(word))
				suspect = true;
			if (!noPC
					&& ExtractedTrainingData.getInstance().pnStops.contains(word))
				suspect = true;
			int patternPosition = position + 1;
			while (patternPosition < (tokSeq.size() - 2)
					&& StringTools.hyphens.contains(tokSeq.getToken(
							patternPosition).getValue())
					&& pnPattern.matcher(
							tokSeq.getToken(patternPosition + 1).getValue())
							.matches()) {
				patternPosition += 2;
				suspect = false;
			}
			if (patternPosition < tokSeq.size()) {
				for (String feature : tokenFeatureSets.get(patternPosition).getBigramableFeatures()) {
					if (suspect) {
						mergedFeatures.add(("suspectpn->bg:" + feature).intern());
					} else {
						mergedFeatures.add(("pn->bg:" + feature).intern());
					}
				}
				if (!suspect) {
					for (String feature : tokenFeatureSets.get(patternPosition).getContextableFeatures()) {
						mergedFeatures.add(("pn->c:" + feature).intern());
					}
				}
				for (int i = position + 1; i <= patternPosition; i++) {
					if (suspect) {
						tokenFeatureSets.get(i).getFeatures().add("inSuspectPN");
					} else {
						tokenFeatureSets.get(i).getFeatures().add("inPN");
					}
				}
			}
		}
	}

	private String getSuffix(String word) {
		Matcher m = suffixPattern.matcher(word);
		if (m.matches()) {
			return m.group(1);
		} else {
			return "unknown";
		}
	}

	private static String wordShape(String word) {
		String ws = word;
		ws = DIGITS.matcher(ws).replaceAll(ZERO);
		ws = LETTERS.matcher(ws).replaceAll(ONE);
		ws = SINGLE_LETTER.matcher(ws).replaceAll(TWO);
		ws = CAPS.matcher(ws).replaceAll(THREE);
		ws = SINGLE_CAP.matcher(ws).replaceAll(FOUR);
		ws = GREEKS.matcher(ws).replaceAll(FIVE);
		return ws;
	}
}
