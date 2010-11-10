package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictSingleton;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.ExtractedTrainingData;
import uk.ac.cam.ch.wwmm.oscarMEMM.terms.TermSets;
import uk.ac.cam.ch.wwmm.oscarMEMM.tokenAnalysis.NGram;
import uk.ac.cam.ch.wwmm.oscarMEMM.tokenAnalysis.TokenTypes;

/**
 * Converts a list of tokens into features for the MEMM.
 * 
 * @author ptc24
 * 
 */
final class FeatureExtractor {

	private static final Pattern DIGITS = Pattern.compile("[0-9]+");
	private static final Pattern LETTERS = Pattern.compile("[a-z][a-z]+");
	private static final Pattern SINGLE_LETTER = Pattern.compile("[a-z]");
	private static final Pattern CAPS = Pattern.compile("[A-Z][A-Z]+");
	private static final Pattern SINGLE_CAP = Pattern.compile("[A-Z]");
	private static final Pattern GREEKS = Pattern.compile("["
			+ StringTools.lowerGreek + "]+");

	private static final String ZERO = "0";
	private static final String ONE = "1";
	private static final String TWO = "2";
	private static final String THREE = "3";
	private static final String FOUR = "4";
	private static final String FIVE = "5";

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
	private List<List<String>> features;
	private List<List<String>> contextableFeatures;
	private List<List<String>> bigramableFeatures;

	private Set<String> stretchable;

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

	public FeatureExtractor(TokenSequence tokSeq, String domain) {
		stretchable = new HashSet<String>();
		stretchable.add("and");
		for (int i = 0; i < StringTools.hyphens.length(); i++)
			stretchable.add(StringTools.hyphens.substring(i, i + 1));
		this.tokSeq = tokSeq;
		makeFeatures(domain);
	}

	public List<String> getFeatures(int pos) {
		return features.get(pos);
	}

	public void printFeatures() {
		for (List<String> f : features)
			System.out.println(f);
	}

	private void makeFeatures(String domain) {
		contextableFeatures = new ArrayList<List<String>>(tokSeq.size());
		bigramableFeatures = new ArrayList<List<String>>(tokSeq.size());
		features = new ArrayList<List<String>>(tokSeq.size());
		for (int i = 0; i < tokSeq.size(); i++) {
			contextableFeatures.add(new LinkedList<String>());
			bigramableFeatures.add(new LinkedList<String>());
			features.add(new LinkedList<String>());
		}
		for (int i = 0; i < tokSeq.size(); i++) {
			makeFeatures(i);
		}
		for (int i = 0; i < tokSeq.size(); i++) {
			mergeFeatures(i);
		}
		if (domain != null) {
			for (int i = 0; i < tokSeq.size(); i++) {
				List<String> ff = new ArrayList<String>(features.get(i));
				// features.get(i).clear();
				for (String f : ff) {
					features.get(i).add("D{" + domain + "}::" + f);
				}
			}
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
		List<String> local = features.get(position);
		List<String> contextable = contextableFeatures.get(position);
		List<String> bigramable = bigramableFeatures.get(position);

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

		if (TermSets.getElements().contains(normWord)) {
			contextable.add(ELEMENT_FEATURE);
			bigramable.add(ELEMENT_FEATURE);
		}
		if (TermSets.getEndingInElementPattern().matcher(word).matches()) {
			contextable.add(ENDS_IN_ELEMENT_FEATURE);
			bigramable.add(ENDS_IN_ELEMENT_FEATURE);
		}
		if (oxPattern.matcher(word).matches()) {
			contextable.add(OXIDATION_STATE_FEATURE);
			bigramable.add(OXIDATION_STATE_FEATURE);
		}

		if (TermSets.getStopWords().contains(normWord)
				|| ChemNameDictSingleton.hasStopWord(normWord)) {
			local.add(STOPWORD_FEATURE);
		}
		if (TermSets.getClosedClass().contains(normWord)) {
			local.add(STOPWORD_CLOSED_CLASS_FEATURE);
		}
		if (ExtractedTrainingData.getInstance().nonChemicalWords
				.contains(normWord)) {
			local.add(STOPWORD_NON_CHEMICAL_WORD_FEATURE);
		}
		if (ExtractedTrainingData.getInstance().nonChemicalNonWords
				.contains(normWord)
				&& !TermSets.getElements().contains(normWord)) {
			local.add(STOPWORD_NONCHEMICALNONWORD_FEATURE);
		}
		if (TermSets.getUsrDictWords().contains(normWord)
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

		if (TermSets.getUsrDictWords().contains(normWord)
				|| TermSets.getUsrDictWords().contains(word)) {
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

		if (TermSets.getUsrDictWords().contains(normWord)
				|| TermSets.getUsrDictWords().contains(word)) {
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
		StringBuilder decWord = new StringBuilder(RE_LINE_START).append(word)
				.append(
				RE_LINE_END);
		for (int j = 0; j < decWord.length() - 3; j++) {
			for (int k = 1; k <= 4; k++) {
				if (j < 4 - k)
					continue;
				local.add((k + NGRAM_FEATURE + decWord.substring(j, j + k)).intern());
			}
		}
	}

	private void makeSuffixFeature(String word, List<String> contextable) {
		String suffix = getSuffix(word);
		String suffixFeature = SUFFIX_FEATURE + suffix;
		contextable.add(suffixFeature);
	}

	private void makeShapeFeatures(String word, List<String> bigramable,
			List<String> contextable) {
		String wordShape = wordShape(word);
		if (wordShape.length() > 3)
			wordShape = SHAPE_COMPLEX_FEATURE;
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
			if (!word.equals(normWord))
				bigramable.add(makeWordFeature(normWord));
		}
	}

	private void mergeFeatures(int position) {
		List<String> mergedFeatures = features.get(position);

		int backwards = Math.min(1, position);
		int forwards = 1;
		/*
		 * while((position + forwards) < tokSeq.size()) { String fv =
		 * tokSeq.getToken(position + forwards).getValue(); if(fv == null)
		 * break; if(stretchable.contains(fv)) { forwards++; } else { break; } }
		 */
		forwards = Math.min(forwards, tokSeq.size() - position - 1);
		// boolean expanded = false;
		/*
		 * String word = tokSeq.getToken(position).getValue();
		 * if(word.equals("lead") || (word.length() < 3 &&
		 * TermSets.getElements().contains(word))) { backwards = Math.min(2,
		 * position); forwards = Math.min(2, tokSeq.size() - position - 1);
		 * expanded = true; }
		 */

		if (!noC) {
			for (int i = -backwards; i <= forwards; i++) {
				for (String cf : contextableFeatures.get(position + i)) {
					mergedFeatures.add(("c" + i + ":" + cf).intern());
				}
			}
		}

		// NB support left in incase bg:0:0: etc. become viable
		for (int i = -backwards; i <= forwards; i++) {
			for (int j = i + 1; j <= forwards; j++) {
				if (j - i == 1 || j == i) {
					String prefix = "bg:" + i + ":" + j + ":";
					for (String feature1 : bigramableFeatures.get(position + i)) {
						for (String feature2 : bigramableFeatures.get(position
								+ j)) {
							// feature1 != feature2 is not a bug, if j == i
							if (j != i || feature1 != feature2)
								mergedFeatures
										.add((prefix + feature1 + "__" + feature2)
												.intern());
						}
					}
				}
				// String prefix = "bg:" + i + ":" + j + ":";
				// for(String bg :
				// StringTools.makeNGrams(bigramableFeatures.subList(i +
				// position, j+1 + position))) {
				// mergedFeatures.add((prefix + bg).intern());
				// }
			}
		}

		String word = tokSeq.getToken(position).getValue();

		if (pnPattern.matcher(word).matches()) {
			boolean suspect = false;
			if (word.matches("[A-Z][a-z]+")
					&& TermSets.getUsrDictWords().contains(word.toLowerCase())
					&& !TermSets.getUsrDictWords().contains(word))
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
				for (String feature : bigramableFeatures.get(patternPosition)) {
					if (suspect) {
						mergedFeatures.add(("suspectpn->bg:" + feature)
								.intern());
					} else {
						mergedFeatures.add(("pn->bg:" + feature).intern());
					}
				}
				if (!suspect) {
					for (String feature : contextableFeatures
							.get(patternPosition)) {
						mergedFeatures.add(("pn->c:" + feature).intern());
					}
				}
				for (int i = position + 1; i <= patternPosition; i++) {
					if (suspect) {
						features.get(i).add("inSuspectPN");
					} else {
						features.get(i).add("inPN");
					}
				}
			}
			/*
			 * if(suspect) { System.out.println("Suspect DGC: " +
			 * tokSeq.getSubstring(position, patternPosition)); } else {
			 * System.out.println("Non-Suspect DGC: " +
			 * tokSeq.getSubstring(position, patternPosition)); }
			 */
		}

		// for(int i=Math.max(0,
		// position-5);i<Math.min(position+5+1,tokSeq.size());i++) {
		// if(i == position) continue;
		// mergedFeatures.add(("ww=" + tokSeq.getToken(i).getValue()).intern());
		// }

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
