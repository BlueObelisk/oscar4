package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.terms.TermSets;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarMEMM.FeatureSet;
import uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata.ExtractedTrainingData;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGram;

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

	private TokenSequence tokSeq;
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
	private NGram ngram;
	private ExtractedTrainingData etd;
	private Set<String> chemNameDictNames;
	
	
	public static List<FeatureList> extractFeatures(TokenSequence tokSeq, MEMMModel model) {
		return extractFeatures(tokSeq, model.getNGram(), model.getExtractedTrainingData(), model.getChemNameDictNames());
	}
	
	public static List<FeatureList> extractFeatures(TokenSequence tokSeq,
			NGram ngram, Set<String> chemNameDictNames) {
		return extractFeatures(tokSeq, ngram, new ExtractedTrainingData(), chemNameDictNames);
	}
	
    static List<FeatureList> extractFeatures(TokenSequence tokSeq, NGram ngram,
    		ExtractedTrainingData annotations, Set<String> chemNameDictNames) {
        FeatureExtractor featureExtractor = new FeatureExtractor(tokSeq, ngram, annotations, chemNameDictNames);
        return featureExtractor.getFeatureLists();
    }

    private List<FeatureList> getFeatureLists() {
        List<FeatureList> features = new ArrayList<FeatureList>(tokenFeatureSets.size());
        for (FeatureSet fs : tokenFeatureSets) {
            features.add(fs.getFeatures());
        }
        return features;
    }

    private FeatureExtractor(TokenSequence tokSeq, NGram ngram, ExtractedTrainingData annotations, Set<String> chemNameDictNames) {
		this.tokSeq = tokSeq;
		this.ngram = ngram;
		this.etd = annotations;
		this.chemNameDictNames = Collections.unmodifiableSet(chemNameDictNames);
		makeFeatures();
	}

	private void makeFeatures() {
        initFeatureSets();
		for (int i = 0; i < tokSeq.getSize(); i++) {
			makeFeatures(i);
		}
		for (int i = 0; i < tokSeq.getSize(); i++) {
			mergeFeatures(i);
		}
	}

    private void initFeatureSets() {
        tokenFeatureSets = new ArrayList<FeatureSet>(tokSeq.getSize());
        for (int i = 0; i < tokSeq.getSize(); i++) {
            tokenFeatureSets.add(new FeatureSet());
        }
    }

    private String makeWordFeature(String word) {
		return new StringBuilder(word.length() + WORD_FEATURE_LENGTH).append(
				WORD_FEATURE).append(
				word).toString();
	}

    
	private void makeFeatures(int position) {
		FeatureList local = tokenFeatureSets.get(position).getFeatures();
		FeatureList contextable = tokenFeatureSets.get(position).getContextableFeatures();
		FeatureList bigramable = tokenFeatureSets.get(position).getBigramableFeatures();

		Token token = tokSeq.getToken(position);
		String word = token.getSurface();
		contextable.addFeature(makeWordFeature(word));

		String normWord = StringTools.normaliseName(word);
		if (!word.equals(normWord)) {
			contextable.addFeature(makeWordFeature(normWord));
		}

		makeWordFeatures(word, normWord, bigramable, etd);
		makeReactionFeatures(word, bigramable, contextable, etd);

		String wts = StringTools.withoutTerminalS(normWord);
		contextable.addFeature(WITHOUT_TERMINAL_S_FEATURE + wts);

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

		if (chemNameDictNames.contains(word)) {
			local.addFeature(IN_NAMEDICT_FEATURE);
		}

		if (TermSets.getDefaultInstance().getElements().contains(normWord)) {
			contextable.addFeature(ELEMENT_FEATURE);
			bigramable.addFeature(ELEMENT_FEATURE);
		}
		if (TermSets.getDefaultInstance().getEndingInElementNamePattern().matcher(word).matches()) {
			contextable.addFeature(ENDS_IN_ELEMENT_FEATURE);
			bigramable.addFeature(ENDS_IN_ELEMENT_FEATURE);
		}
		if (oxPattern.matcher(word).matches()) {
			contextable.addFeature(OXIDATION_STATE_FEATURE);
			bigramable.addFeature(OXIDATION_STATE_FEATURE);
		}

		if (TermSets.getDefaultInstance().getStopWords().contains(normWord)) {
			local.addFeature(STOPWORD_FEATURE);
		}
		if (TermSets.getDefaultInstance().getClosedClass().contains(normWord)) {
			local.addFeature(STOPWORD_CLOSED_CLASS_FEATURE);
		}
		if (etd.getNonChemicalWords()
				.contains(normWord)) {
			local.addFeature(STOPWORD_NON_CHEMICAL_WORD_FEATURE);
		}
		if (etd.getNonChemicalNonWords()
				.contains(normWord)
				&& !TermSets.getDefaultInstance().getElements().contains(normWord)) {
			local.addFeature(STOPWORD_NONCHEMICALNONWORD_FEATURE);
		}
		if (TermSets.getDefaultInstance().getUsrDictWords().contains(normWord)
				&& !(chemNameDictNames.contains(normWord) || 
						etd.getChemicalWords().contains(normWord))) {
			local.addFeature(STOPWORD_USER_DEFINED_FEATURE);
		}
	}

	private void handleNoNewSuffices(String word, String normWord,
			FeatureList bigramable, FeatureList contextable,
			FeatureList local, Token token) {
		double ngscore = ngram.testWord(word);
		// Already seen
		NamedEntityType namedEntityType = uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.TokenSuffixClassifier.classifyBySuffix(token.getSurface());
		ngscore = Math.max(ngscore, NGRAM_SCORE_LOWER_BOUND);
		ngscore = Math.min(ngscore, NGRAM_SCORE_UPPER_BOUND);
		for (int i = 0; i < ngscore; i++) {
			local.addFeature((NGRAM_INC_FEATURE + namedEntityType.getName()).intern());
		}
		for (int i = 0; i > ngscore; i--) {
			local.addFeature((NGRAM_DEC_FEATURE + namedEntityType.getName()).intern());
		}

		if (TermSets.getDefaultInstance().getUsrDictWords().contains(normWord)
				|| TermSets.getDefaultInstance().getUsrDictWords().contains(word)) {
			ngscore = SUFFIX_LO_SCORE;
		}
		if (etd.getChemicalWords().contains(normWord)) {
			ngscore = 100;
		}
		if (chemNameDictNames.contains(word)) {
			ngscore = 100;
		}

		if (ngscore > 0) {
			contextable.addFeature(SUFFIX_CT_FEATURE + namedEntityType.getName());
			bigramable.addFeature(SUFFIX_CT_FEATURE + namedEntityType.getName());
		}
	}

	private void handleNewSuffices(String word, String normWord,
			FeatureList bigramable, FeatureList contextable,
			FeatureList local, Token token) {
		double suffixScore = ngram.testWordSuffix(word);
		NamedEntityType namedEntityType = uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.TokenSuffixClassifier.classifyBySuffix(token.getSurface());

		suffixScore = Math.max(suffixScore, SUFFIX_SCORE_LOWER_BOUND);
		suffixScore = Math.min(suffixScore, SUFFIX_SCORE_UPPER_BOUND);
		for (int i = 0; i < suffixScore; i++) {
			local.addFeature((SUFFIX_SCORE_INC_FEATURE + namedEntityType.getName()).intern());
		}
		for (int i = 0; i > suffixScore; i--) {
			local.addFeature((SUFFIX_SCORE_DEC_FEATURE + namedEntityType.getName()).intern());
		}

		if (TermSets.getDefaultInstance().getUsrDictWords().contains(normWord)
				|| TermSets.getDefaultInstance().getUsrDictWords().contains(word)) {
			suffixScore = SUFFIX_LO_SCORE;
		}
		if (etd.getChemicalWords().contains(normWord)) {
			suffixScore = SUFFIX_HI_SCORE;
		}
		if (chemNameDictNames.contains(word)) {
			suffixScore = SUFFIX_HI_SCORE;
		}
		double ngscore = ngram.testWord(word);
		ngscore = Math.max(ngscore, NGRAM_SCORE_LOWER_BOUND);
		ngscore = Math.min(ngscore, NGRAM_SCORE_UPPER_BOUND);
		for (int i = 0; i < ngscore; i++) {
			local.addFeature((NGRAMSCORE_INC_FEATURE + namedEntityType.getName()).intern());
		}
		for (int i = 0; i > ngscore; i--) {
			local.addFeature((NGRAMSCORE_DEC_FEATURE + namedEntityType.getName()).intern());
		}

		if (suffixScore > 0) {
			contextable.addFeature(SUFFIX_CT_FEATURE + namedEntityType.getName());
			bigramable.addFeature(SUFFIX_CT_FEATURE + namedEntityType.getName());
		}
	}

	private void makeNGramFeatures(String word, FeatureList local) {
		StringBuilder decWord = new StringBuilder(RE_LINE_START).append(word).append(RE_LINE_END);
		for (int j = 0; j < decWord.length() - 3; j++) {
			for (int k = 1; k <= 4; k++) {
				if (j < 4 - k) {
					continue;
                }
				local.addFeature(makeNGramFeature(decWord, j, k));
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
	
	
	private void makeSuffixFeature(String word, FeatureList contextable) {
		String suffix = getSuffix(word);
		String suffixFeature = SUFFIX_FEATURE + suffix;
		contextable.addFeature(suffixFeature);
	}

	private void makeShapeFeatures(String word, FeatureList bigramable,
			FeatureList contextable) {
		String wordShape = wordShape(word);
		if (wordShape.length() > 3) {
			wordShape = SHAPE_COMPLEX_FEATURE;
        }
		if (!wordShape.equals(word)) {
			String wordShapeFeature = SHAPE_FEATURE + wordShape;
			bigramable.addFeature(wordShapeFeature);
			contextable.addFeature(wordShapeFeature);
		}
	}

	private void makeReactionFeatures(String word,
			FeatureList bigramable, FeatureList contextable, ExtractedTrainingData etd) {
		if (etd.getRnEnd().contains(word)) {
			bigramable.addFeature(RNEND_FEATURE);
			contextable.addFeature(RNEND_FEATURE);
		}
		if (etd.getRnMid().contains(word)) {
			bigramable.addFeature(RNMID_FEATURE);
			contextable.addFeature(RNMID_FEATURE);
		}
	}

	private void makeWordFeatures(String word, String normWord,
			FeatureList bigramable, ExtractedTrainingData etd) {
		if (word.length() < 4 || etd.getPolysemous().contains(word)
				|| etd.getRnEnd().contains(word) || etd.getRnMid().contains(word)) {
			bigramable.addFeature(makeWordFeature(word));
			if (!word.equals(normWord)) {
				bigramable.addFeature(makeWordFeature(normWord));
            }
		}
	}

	private void mergeFeatures(int position) {
		FeatureList mergedFeatures = tokenFeatureSets.get(position).getFeatures();

		int backwards = Math.min(1, position);
		int forwards = Math.min(1, tokSeq.getSize() - position - 1);

		if (!noC) {
			for (int i = -backwards; i <= forwards; i++) {
				for (String cf : tokenFeatureSets.get(position + i).getContextableFeatures()) {
					mergedFeatures.addFeature(("c" + i + ":" + cf).intern());
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
										.addFeature((prefix + feature1 + "__" + feature2)
												.intern());
						}
					}
				}
			}
		}

		String word = tokSeq.getToken(position).getSurface();

		if (pnPattern.matcher(word).matches()) {
			boolean suspect = false;
			if (word.matches("[A-Z][a-z]+")
					&& TermSets.getDefaultInstance().getUsrDictWords().contains(word.toLowerCase())
					&& !TermSets.getDefaultInstance().getUsrDictWords().contains(word))
				suspect = true;
			if (!noPC
					&& etd.getPnStops().contains(word))
				suspect = true;
			int patternPosition = position + 1;
			while (patternPosition < (tokSeq.getSize() - 2)
					&& StringTools.isHyphen(tokSeq.getToken(
							patternPosition).getSurface())
					&& pnPattern.matcher(
							tokSeq.getToken(patternPosition + 1).getSurface())
							.matches()) {
				patternPosition += 2;
				suspect = false;
			}
			if (patternPosition < tokSeq.getSize()) {
				for (String feature : tokenFeatureSets.get(patternPosition).getBigramableFeatures()) {
					if (suspect) {
						mergedFeatures.addFeature(("suspectpn->bg:" + feature).intern());
					} else {
						mergedFeatures.addFeature(("pn->bg:" + feature).intern());
					}
				}
				if (!suspect) {
					for (String feature : tokenFeatureSets.get(patternPosition).getContextableFeatures()) {
						mergedFeatures.addFeature(("pn->c:" + feature).intern());
					}
				}
				for (int i = position + 1; i <= patternPosition; i++) {
					if (suspect) {
						tokenFeatureSets.get(i).getFeatures().addFeature("inSuspectPN");
					} else {
						tokenFeatureSets.get(i).getFeatures().addFeature("inPN");
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
