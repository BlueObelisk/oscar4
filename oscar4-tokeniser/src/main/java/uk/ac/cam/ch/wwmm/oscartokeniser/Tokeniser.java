package uk.ac.cam.ch.wwmm.oscartokeniser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Element;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ITokeniser;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.terms.TermSets;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.BioTag;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;

/**
 * Tokenisation of text.
 * 
 * @author ptc24
 * 
 */
public final class Tokeniser implements ITokeniser {

	private static Pattern oxidationStatePattern = Pattern.compile(
			"\\((o|i{1,4}|i{0,3}[xv]|[xv]i{0,4})\\)", Pattern.CASE_INSENSITIVE);
	private static Pattern oxidationStateEndPattern = Pattern.compile(
			".*\\((o|i{1,4}|i{0,3}[xv]|[xv]i{0,4})\\)",
			Pattern.CASE_INSENSITIVE);
	private static Pattern trademarkPattern = Pattern
			.compile(".+?(\\((TM|R)\\)|\\(\\((TM|R)\\)\\))");

	private static Pattern tokenPattern = Pattern.compile("[^\\s"
			+ StringTools.whiteSpace + "]+");

	private static String primesRe = "[" + StringTools.primes + "]*";
	private static String locantRe = "(\\d+"
			+ primesRe
			+ "[RSEZDLH]?|"
			+ "\\(([RSEZDLH\u00b1]|\\+|"
			+ StringTools.hyphensRegex
			+ "[DLRSEZ]|"
			+ ")\\)|"
			+ "([CNOS]|Se)\\d*|"
			+ "\\d*["
			+ StringTools.lowerGreek
			+ "]|"
			+ "cis|trans|o(rtho)?|m(eta)?|p(ara)?|asym|sym|sec|tert|catena|closo|enantio|ent|endo|exo|"
			+ "fac|mer|gluco|nido|aci|erythro|threo|arachno|meso|syn|anti|tele|cine"
			+ ")" + primesRe;
	private static String prefixRe = "(" + locantRe + "(," + locantRe + ")*)";

	private static Pattern prefixPattern = Pattern.compile(prefixRe + "["
			+ StringTools.hyphens + "](\\S*)");
	private static Tokeniser defaultInstance;

	private TokenClassifier tokenClassifier;
	
	/**
	 * Gets the default instance of the OSCAR4 Tokeniser
	 * 
	 */
	public static synchronized Tokeniser getDefaultInstance() {
		if (defaultInstance == null)
			defaultInstance = new Tokeniser(TokenClassifier.getDefaultInstance());
		return defaultInstance;
	}

	/**
	 * Constructs a new Tokeniser using the given TokenClassifier to identify
	 * chemical bonds (e.g. C-H)
	 * @param tokenClassifier
	 */
	public Tokeniser(TokenClassifier tokenClassifier) {
		this.tokenClassifier = tokenClassifier;
	}

	/**
	 * Tokenises a string.
	 * 
	 * @param s
	 *            The string to tokenise.
	 * @return The TokenSequence for the string.
	 */
	public TokenSequence tokenise(String s) {
		return tokenise(s, null, 0, null);
	}

	/**
	 * Tokenises a string.
	 * 
	 * @param s
	 *            The string to tokenise.
	 * @param doc
	 *            The ProcessingDocument for the string.
	 * @param offset
	 *            The start offset of the string.
	 * @param annotations
	 *            An XML element, corresponding to either the inline-annotated
	 *            SciXML for the string, or to the root element of a SAF
	 *            document with the named entities.
	 * @param tokeniseForNEs
	 *            Whether to ensure that named entity boundaries always result
	 *            in token boundaries.
	 * @param mergeNEs
	 *            Whether to merge tokens to make sure that multi-token NEs
	 *            become single-token NEs.
	 * @return The TokenSequence for the string.
	 */
	public TokenSequence tokenise(String s, IProcessingDocument doc, int offset,
			Element annotations) {
		List<Token> tokens = new LinkedList<Token>();
		Matcher m = tokenPattern.matcher(s);
		/* Initial tokenisation */
		
		/*
		 * @lh359: The words in 
		 * "String s" match 
		 * the regex tokenPattern
		 * create a Token out of it
		 * 
		 * @dmj30: The tokenPattern is; "[^\\s" + StringTools.whiteSpace + "]+"
		 * creates tokens out of consecutive characters of non-whitespace
		 */
		while (m.find()) {
			int start = m.start() + offset;
			int end = m.end() + offset;
			String value = m.group();
			tokens.add(new Token(value, start, end, doc, new BioType(BioTag.O), null));
		}
		/* Split tokens */
		// This isn't theoretically optimal, as it involves traversing
		// a linked list a lot, but it seems not to make a difference in
		// practise.
		int i = 0;
		/*
		 * @lh359: Let the messiness begin
		 * 
		 * @dmj30: This loop iterates over the (whitespace separated) tokens we
		 * just created and subtokenises them using the splitToken(Token) method.
		 * Newly created tokens are recursively subtokenised.
		 */
		while (i < tokens.size()) {
			List<Token> results = splitToken(tokens.get(i));

			
			/* Returns null if no splitting occurs */
			if (results == null) {
				i++;
			} else {

				tokens.remove(i);
				
				tokens.addAll(i, results);
				/*
				 * Note: NO i++ here. This allows the resulting tokens to be
				 * recursively subtokenised without recursion. Neat huh?
				 */
			}
		}
		/* Discard empty tokens */
		
		/*
		 * @lh359: Remove empty tokens
		 */
		List<Token> tmpTokens = new ArrayList<Token>();
		for (Token t : tokens) {
			if (t.getSurface() != null && !"".equals(t.getSurface())) {
				tmpTokens.add(t);
			}
		}
		tokens = tmpTokens;
		
		TokenSequence tokenSequence = indexTokensAndMakeTokenSequence(s, doc,
				offset, annotations, tokens);
		
		return tokenSequence;
	}

	

	public TokenSequence indexTokensAndMakeTokenSequence(String s,
			IProcessingDocument doc, int offset, Element annotations,
			List<Token> tokens) {
		
		/* Number tokens */
		int id = 0;
		for (Token t : tokens) {
			t.setIndex(id);
			id++;
		}
		
		/* Make an index of the tokens in the ProcessingDocument */
		if (doc != null && doc.getTokensByStart() != null) {
			for (Token t : tokens) {
				doc.getTokensByStart().put((Integer) t.getStart(), t);
				doc.getTokensByEnd().put(t.getEnd(), t);
			}
		}
		
		/* Create a TokenSequence from the tokens */
		TokenSequence tokenSequence = new TokenSequence(s, offset, doc, tokens);
		for (Token t : tokens) {
			t.setTokenSequence(tokenSequence);
		}
		tokenSequence.setElem(annotations);
		return tokenSequence;
	}

	/*****************************
	 * @lh359: Had difficulty following
	 * the logic of this one and rawSplitToken .
	 *  But it seems like a set of cases 
	 * for splitting token
	 * @param token
	 * @return Subtokenised List or null if no subtokenisation has occurred
	 */
	private List<Token> splitToken(Token token) {
		List<Token> tokenList = rawSplitToken(token);
		if (tokenList == null)
			return null;
		int goodTokens = 0;
		for (Token t : tokenList) {
			if (t.getEnd() - t.getStart() > 0)
				goodTokens++;
		}
		if (goodTokens > 1)
			return tokenList;
		return null;
	}

	/************************************
	 * @lh359: Splits tokens based on different
	 * patterns
	 * 
	 * @dmj30: Subtokenisation of hyphen-containing tokens
	 * is currently disabled
	 * 
	 * Localising citation bit is redundant
	 * 
	 * @return Subtokenised List or null if no subtokenisation has occurred
	 ***********************************/
	private List<Token> rawSplitToken(Token token) {

		if (TermSets.getDefaultInstance().getAbbreviations().contains(token.getSurface().toLowerCase())) {
			return null;
		}

		String middleValue = "";
		if (token.getSurface().length() > 2)
			middleValue = token.getSurface().substring(0, token.getSurface().length() - 1);
		/* Don't split special lexicon entries */
		if (token.getSurface().startsWith("$")) {
			return null;
		}
		/* One-character tokens don't split! */
		if (token.getEnd() - token.getStart() < 2) {
			return null;
		}
		if (token.getSurface().equals("--")) {
			return null;
		}
		// /* Pull citation references off the end */
		// if(token.doc != null &&
		// XMLStrings.getInstance().isCitationReferenceUnderStyle(token.doc.standoffTable.getElemAtOffset(token.getEnd()-1))
		// &&
		// !(XMLStrings.getInstance().isCitationReferenceUnderStyle(token.doc.standoffTable.getElemAtOffset(token.start))))
		// {
		// int citrefOffset = token.getEnd()-1;
		// while(XMLStrings.getInstance().isCitationReferenceUnderStyle(token.doc.standoffTable.getElemAtOffset(citrefOffset-1)))
		// citrefOffset--;
		// return splitAt(token, citrefOffset);
		// }
		// /* Pull citation references off the start */
		// if(token.doc != null &&
		// !XMLStrings.getInstance().isCitationReferenceUnderStyle(token.doc.standoffTable.getElemAtOffset(token.getEnd()-1))
		// &&
		// (XMLStrings.getInstance().isCitationReferenceUnderStyle(token.doc.standoffTable.getElemAtOffset(token.start))))
		// {
		// int citrefOffset = token.start;
		// while(XMLStrings.getInstance().isCitationReferenceUnderStyle(token.doc.standoffTable.getElemAtOffset(citrefOffset)))
		// citrefOffset++;
		// return splitAt(token, citrefOffset);
		// }
		// /* Split equations off ends */
		// if(token.doc != null &&
		// XMLStrings.getInstance().isEquationUnderStyle(token.doc.standoffTable.getElemAtOffset(token.getEnd()-1))
		// &&
		// !(XMLStrings.getInstance().isEquationUnderStyle(token.doc.standoffTable.getElemAtOffset(token.start))))
		// {
		// //System.out.println("EQN! " + value);
		// int eqnOffset = token.getEnd()-1;
		// while(XMLStrings.getInstance().isEquationUnderStyle(token.doc.standoffTable.getElemAtOffset(eqnOffset-1)))
		// eqnOffset--;
		// //System.out.println(eqnOffset - start);
		// return splitAt(token, eqnOffset);
		// }
		// /* Split equations off starts */
		// if(token.doc != null &&
		// !XMLStrings.getInstance().isEquationUnderStyle(token.doc.standoffTable.getElemAtOffset(token.getEnd()-1))
		// &&
		// (XMLStrings.getInstance().isEquationUnderStyle(token.doc.standoffTable.getElemAtOffset(token.start))))
		// {
		// //System.out.println("EQN! " + value);
		// int eqnOffset = token.start;
		// while(XMLStrings.getInstance().isEquationUnderStyle(token.doc.standoffTable.getElemAtOffset(eqnOffset)))
		// eqnOffset++;
		// //System.out.println(eqnOffset - start);
		// return splitAt(token, eqnOffset);
		// }
		// / COMMENTED ON 27/1

		/* Preserve oxidation states whole - don't eat for brackets */
		if (oxidationStatePattern.matcher(token.getSurface()).matches()) {
			return null;
		}
		/* Split unmatched brackets off the front */
		// NB: this isn't just unmatched brackets - read the code!
		//TODO unit tests for the various StringTools methods
		if ("([{".indexOf(token.getSurface().codePointAt(0)) != -1
				&& (StringTools.isBracketed(token.getSurface()) || StringTools
						.isLackingCloseBracket(token.getSurface()))) {
			return splitAt(token, token.getStart() + 1);
		}
		/* Split unmatched brackets off the end */
		// NB: this isn't just unmatched brackets - read the code!
		if (")]}".indexOf(token.getSurface().codePointAt(token.getSurface().length() - 1)) != -1
				&& (StringTools.isBracketed(token.getSurface()) || StringTools
						.isLackingOpenBracket(token.getSurface()))) {
			return splitAt(token, token.getEnd() - 1);
		}
//		/* Split oxidation state off the end */
		//TODO disabled, was probably a mistake anyway. But might we want to tokenise non-chemical terms in brackets?
//		if (oxidationStateEndPattern.matcher(token.getSurface()).matches()) {
//			return splitAt(token, token.getStart() + token.getSurface().lastIndexOf('('));
//		}
		/* Split some characters off the front of tokens */
		if ((StringTools.relations + StringTools.quoteMarks)
				.indexOf(token.getSurface().codePointAt(0)) != -1) {
			return splitAt(token, token.getStart() + 1);
		}
		/* Split some characters off the back of tokens */
		
		//This is probably the source of the abbreviation problem.
		if ((".,;:!?\u2122\u00ae-" + StringTools.quoteMarks)
				.indexOf(token.getSurface().codePointAt(token.getSurface().length() - 1)) != -1) {
			// Careful with Jones' reagent
			if (!(token.getSurface().substring(token.getSurface().length() - 1).equals("'") && token.getSurface()
					.matches("[A-Z][a-z]+s'"))) {
				return splitAt(token, token.getEnd() - 1);
			}
		}
		/* Split trademark symbols off the back of tokens */
		Matcher m = trademarkPattern.matcher(token.getSurface());
		if (m.matches() && m.start(1) > 0) {
			return splitAt(token, token.getStart() + m.start(1));
		}
		/* characters to split on */
		if (middleValue.contains("<")) {
			return splitAt(token, token.getStart() + token.getSurface().indexOf("<"),
					token.getStart() + token.getSurface().indexOf("<") + 1);
		}
		if (middleValue.contains(">")) {
			return splitAt(token, token.getStart() + token.getSurface().indexOf(">"),
					token.getStart() + token.getSurface().indexOf(">") + 1);
		}
		if (middleValue.contains("/")) {
			return splitAt(token, token.getStart() + token.getSurface().indexOf("/"),
					token.getStart() + token.getSurface().indexOf("/") + 1);
		}
		if (middleValue.contains(":")) {
			// Check to see if : is nestled in brackets, such as in ring systems
			if (StringTools.bracketsAreBalanced(token.getSurface())
					&& StringTools.bracketsAreBalanced(token.getSurface()
							.substring(token.getSurface().indexOf(":") + 1))) {
				return splitAt(token, token.getStart() + token.getSurface().indexOf(":"),
						token.getStart() + token.getSurface().indexOf(":") + 1);
			}
		}
		if (middleValue.contains("+")) {
			int index = token.getSurface().indexOf("+");
			if (index < (token.getSurface().length() - 2)
					&& StringTools.isHyphen(token.getSurface().substring(
							index + 1, index + 2))) {
				return splitAt(token, token.getStart() + index + 1, token.getStart()
						+ index + 2);
			}
			if (index > 0 && index < token.getSurface().length() - 1) {
				if (token.getSurface().endsWith("-")) {

				} else if (StringTools.bracketsAreBalanced(token.getSurface())
						&& StringTools.bracketsAreBalanced(token.getSurface()
								.substring(index + 1))) {
					return splitAt(token, token.getStart() + index, token.getStart()
							+ index + 1);
				}
			} else {
				return splitAt(token, token.getStart() + index, token.getStart() + index
						+ 1);
			}
		}
		if (middleValue.contains(StringTools.midElipsis)) {
			return splitAt(token, token.getStart()
					+ token.getSurface().indexOf(StringTools.midElipsis), token.getStart()
					+ token.getSurface().indexOf(StringTools.midElipsis) + 1);
		}
		/* Hyphens */
		if (middleValue.contains("--")) {
			return splitAt(token, token.getStart() + token.getSurface().indexOf("--"),
					token.getStart() + token.getSurface().indexOf("--") + 2);
		}
		
		/*
		 * @lh359: This function used to call
		 * HyphenTokeniser but works better when set
		 * to -1 for somereason. Needs to be investigated
		 * further
		 * 
		 * @dmj30: it appears that subtokenisation of hyphenated words
		 * has been disabled. In phrases like "alcohol-consuming bacteria",
		 * this prevents OSCAR from recognising the ne "alcohol".
		 */
//		int splittableHyphenIndex = -1;
		int splittableHyphenIndex = HyphenTokeniser.indexOfSplittableHyphen(token.getSurface());
		
		
		/* Split on appropriate hyphens */
		if (splittableHyphenIndex != -1
				&& !token.getSurface().matches(".*[a-z][a-z].*")
				&& token.getSurface().matches(".*[A-Z].*")) {
			//FIXME dmj30 I don't see the point of the two String.matches calls above
			if (tokenClassifier.isTokenLevelRegexMatch(token.getSurface(), "bondRegex")) {
				splittableHyphenIndex = -1;
			}
		}
		if (splittableHyphenIndex != -1) {
			if (token.getSurface().endsWith("NMR")) {
				return splitAt(token, token.getStart() + splittableHyphenIndex,
						token.getStart() + splittableHyphenIndex + 1);
			} else if (prefixPattern.matcher(token.getSurface()).matches()) {
				return splitAt(token, token.getStart() + splittableHyphenIndex + 1);

			} else {
				///This is where tokenisation happens
				return splitAt(token, token.getStart() + splittableHyphenIndex,
						token.getStart() + splittableHyphenIndex + 1);
			}
		} else {
			return null;
		}
	}

	public List<Token> splitAt(Token token, int splitOffset) {
		int internalOffset = splitOffset - token.getStart();
		List<Token> tokens = new LinkedList<Token>();
		tokens
				.add(new Token(token.getSurface().substring(0, internalOffset),
						token.getStart(), splitOffset, token.getDoc(), token.getBioType(),
						((Token)token).getNeElem()));
		tokens.add(new Token(token.getSurface().substring(internalOffset),
				splitOffset, token.getEnd(), token.getDoc(), token.getBioType(),
				((Token)token).getNeElem()));
		return tokens;
	}

	private List<Token> splitAt(Token token, int splitOffset0, int splitOffset1) {
		int internalOffset0 = splitOffset0 - token.getStart();
		int internalOffset1 = splitOffset1 - token.getStart();
		List<Token> tokens = new LinkedList<Token>();
		tokens.add(new Token(token.getSurface().substring(0, internalOffset0),
					token.getStart(), splitOffset0, token.getDoc(), token.getBioType(),
					((Token)token).getNeElem()));
		tokens.add(new Token(token.getSurface().substring(internalOffset0,
					internalOffset1), splitOffset0, splitOffset1, token.getDoc(),
					token.getBioType(), ((Token)token).getNeElem()));
		tokens.add(new Token(token.getSurface().substring(internalOffset1),
					splitOffset1, token.getEnd(), token.getDoc(), token.getBioType(),
					((Token)token).getNeElem()));

		return tokens;
	}

	
}
