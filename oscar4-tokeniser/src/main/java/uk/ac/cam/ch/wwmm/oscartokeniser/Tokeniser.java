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
	
	private static Pattern arrowUsage = Pattern.compile("[^>]*->.*");
	
	private static Pattern chemicalNameColonUsage = Pattern.compile("[^:]*\\d+[a-g]?'*(alpha|beta)?,\\d+[a-g]?'*(alpha|beta)?(-([a-zA-Z]'*)+)?:\\d+[a-g]?'*(alpha|beta)?,\\d+.*");

	private static Pattern chemicalNameEqualsUsage = Pattern.compile("[^=]*[CNHOP]+[0-9]*=[CNOP].*");
	
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
	 * @param string The string to tokenise.
	 * @return A {@link TokenSequence} representing the tokenised string.
	 */
	public TokenSequence tokenise(String string) {
		return tokenise(string, null, 0, null);
	}

	/**
	 * Tokenises a string.
	 * 
	 * @param string
	 *            The string to tokenise.
	 * @param doc
	 *            The ProcessingDocument into which to insert the
	 *            tokenised string.
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
	public TokenSequence tokenise(String string, IProcessingDocument doc, int offset,
			Element annotations) {
		List<Token> tokens = new LinkedList<Token>();
		Matcher m = tokenPattern.matcher(string);
		/* Initial tokenisation */
		
		/*
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
	
		/* Remove empty tokens */
		List<Token> tmpTokens = new ArrayList<Token>();
		for (Token t : tokens) {
			if (t.getSurface() != null && !"".equals(t.getSurface())) {
				tmpTokens.add(t);
			}
		}
		tokens = tmpTokens;
		
		TokenSequence tokenSequence = indexTokensAndMakeTokenSequence(string, doc,
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
	 * @dl387: I cannot see why all tokens wouldn't be good tokens and hence why a bad token wouldn't be exceptional behaviour
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
	 * @dmj30:
	 * Localising citation bit is redundant
	 * 
	 * @return Subtokenised List or null if no subtokenisation has occurred
	 ***********************************/
	private List<Token> rawSplitToken(Token token) {
		String tokenSurface = token.getSurface();

		if (TermSets.getDefaultInstance().getAbbreviations().contains(tokenSurface.toLowerCase())) {
			return null;
		}

		/* Don't split special lexicon entries */
		if (tokenSurface.startsWith("$")) {
			return null;
		}
		/* One-character tokens don't split! */
		if (token.getEnd() - token.getStart() < 2) {
			return null;
		}
		if (tokenSurface.equals("--")) {
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
		if (oxidationStatePattern.matcher(tokenSurface).matches()) {
			return null;
		}
		/* Split unmatched brackets off the front */
		// NB: this isn't just unmatched brackets - read the code!
		//TODO unit tests for the various StringTools methods
		if ("([{".indexOf(tokenSurface.codePointAt(0)) != -1
				&& (StringTools.isBracketed(tokenSurface) || StringTools
						.isLackingCloseBracket(tokenSurface))) {
			return splitAt(token, token.getStart() + 1);
		}
		/* Split unmatched brackets off the end */
		// NB: this isn't just unmatched brackets - read the code!
		if (")]}".indexOf(tokenSurface.codePointAt(tokenSurface.length() - 1)) != -1
				&& (StringTools.isBracketed(tokenSurface) || StringTools
						.isLackingOpenBracket(tokenSurface))) {
			return splitAt(token, token.getEnd() - 1);
		}
//		/* Split oxidation state off the end */
		//TODO disabled, was probably a mistake anyway. But might we want to tokenise non-chemical terms in brackets?
//		if (oxidationStateEndPattern.matcher(tokenSurface).matches()) {
//			return splitAt(token, token.getStart() + tokenSurface.lastIndexOf('('));
//		}
		/* Split some characters off the front of tokens */
		if ((StringTools.relations + StringTools.quoteMarks)
				.indexOf(tokenSurface.codePointAt(0)) != -1) {
			return splitAt(token, token.getStart() + 1);
		}
		/* Split some characters off the back of tokens */
		
		//This is probably the source of the abbreviation problem.
		if ((".,;:!?=\u00D7\u00F7\u2122\u00ae-" + StringTools.quoteMarks)//unicode chars are times, divide, (R) and (TM)
				.indexOf(tokenSurface.codePointAt(tokenSurface.length() - 1)) != -1) {
			// Careful with Jones' reagent
			if (!(tokenSurface.substring(tokenSurface.length() - 1).equals("'") && tokenSurface
					.matches("[A-Z][a-z]+s'"))) {
				return splitAt(token, token.getEnd() - 1);
			}
		}
		/* Split trademark symbols off the back of tokens */
		int indiceOfTrademark = findIndiceOfTrademarkSymbol(tokenSurface);
		if (indiceOfTrademark > 0){
			return splitAt(token, token.getStart() + indiceOfTrademark);
		}
		/* Split physical state off the back of tokens */
		int indiceOfPhysicalState = findIndiceOfPhysicalState(tokenSurface);
		if (indiceOfPhysicalState > 0){
			return splitAt(token, token.getStart() + indiceOfPhysicalState);
		}
		/* characters to split on */
		if (tokenSurface.contains("<")) {
			return splitAt(token, token.getStart() + tokenSurface.indexOf("<"),
					token.getStart() + tokenSurface.indexOf("<") + 1);
		}
		if (tokenSurface.contains(">") && !arrowUsage.matcher(tokenSurface).matches()) {
			return splitAt(token, token.getStart() + tokenSurface.indexOf(">"),
					token.getStart() + tokenSurface.indexOf(">") + 1);
		}
		if (tokenSurface.contains("/")) {
			return splitAt(token, token.getStart() + tokenSurface.indexOf("/"),
					token.getStart() + tokenSurface.indexOf("/") + 1);
		}
		if (tokenSurface.contains(":") && !chemicalNameColonUsage.matcher(tokenSurface).matches()) {
			return splitAt(token, token.getStart() + tokenSurface.indexOf(":"),
				token.getStart() + tokenSurface.indexOf(":") + 1);
		}
		if (tokenSurface.contains("=") && !chemicalNameEqualsUsage.matcher(tokenSurface).matches()) {
			return splitAt(token, token.getStart() + tokenSurface.indexOf("="),
				token.getStart() + tokenSurface.indexOf("=") + 1);
		}
		if (tokenSurface.contains("\u00D7")) {
			return splitAt(token, token.getStart() + tokenSurface.indexOf("\u00D7"),
				token.getStart() + tokenSurface.indexOf("\u00D7") + 1);
		}
		if (tokenSurface.contains("\u00F7")) {
			return splitAt(token, token.getStart() + tokenSurface.indexOf("\u00F7"),
				token.getStart() + tokenSurface.indexOf("\u00F7") + 1);
		}
		if (tokenSurface.substring(0, tokenSurface.length() - 1).contains("+")) {
			int index = tokenSurface.indexOf("+");
			if (index < (tokenSurface.length() - 2)
					&& StringTools.isHyphen(tokenSurface.substring(
							index + 1, index + 2))) {
				if (index >0 && StringTools.isBracketed(tokenSurface.substring(index - 1, index + 3))) {
					//(+-), probably a description of light rotation
				}
				else {
					return splitAt(token, token.getStart() + index + 1, token.getStart() + index + 2);
				}
			}
			if (index > 0 && index < tokenSurface.length() - 1) {
				if (tokenSurface.endsWith("-")) {

				} else if (StringTools.bracketsAreBalanced(tokenSurface)
						&& StringTools.bracketsAreBalanced(tokenSurface
								.substring(index + 1))) {
					return splitAt(token, token.getStart() + index, token.getStart()
							+ index + 1);
				}
			} else {
				return splitAt(token, token.getStart() + index, token.getStart() + index
						+ 1);
			}
		}
		if (tokenSurface.contains(StringTools.midElipsis)) {
			return splitAt(token, token.getStart()
					+ tokenSurface.indexOf(StringTools.midElipsis), token.getStart()
					+ tokenSurface.indexOf(StringTools.midElipsis) + 1);
		}
		/* Hyphens */
		if (tokenSurface.contains("--")) {
			return splitAt(token, token.getStart() + tokenSurface.indexOf("--"),
					token.getStart() + tokenSurface.indexOf("--") + 2);
		}
		
		/* Attempts to find the index of a hyphen at which splitting is appropriate */
		int splittableHyphenIndex = HyphenTokeniser.indexOfSplittableHyphen(tokenSurface);
		
		/* Split on appropriate hyphens */
		if (splittableHyphenIndex != -1
				&& !tokenSurface.matches(".*[a-z][a-z].*")
				&& tokenSurface.matches(".*[A-Z].*")) {
			//FIXME dmj30 I don't see the point of the two String.matches calls above
			if (tokenClassifier.isTokenLevelRegexMatch(tokenSurface, "bondRegex")) {
				splittableHyphenIndex = -1;
			}
		}
		if (splittableHyphenIndex != -1) {
			if (tokenSurface.endsWith("NMR")) {
				return splitAt(token, token.getStart() + splittableHyphenIndex,
						token.getStart() + splittableHyphenIndex + 1);
			} else if (prefixPattern.matcher(tokenSurface).matches()) {
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

	/**
	 * Returns the indice of a trademark symbol which is at the end of the token
	 * @param tokenSurface
	 * @return
	 */
	private int findIndiceOfTrademarkSymbol(String tokenSurface) {
		int indice = -1;
		if (tokenSurface.endsWith("(TM)")){
			indice =  tokenSurface.length() -4;
		}
		else if (tokenSurface.endsWith("(R)")){
			indice =  tokenSurface.length() - 3;
		}
		else if (tokenSurface.endsWith("((TM))")){
			indice =  tokenSurface.length() - 6;
		}
		else if (tokenSurface.endsWith("((R))")){
			indice =  tokenSurface.length() - 5;
		}
		return indice;
	}

	/**
	 * Returns the indice of a physical state symbol which is at the end of the token
	 * The bracketted symbol MUST be preceded by a non digit to be recognised
	 * @param tokenSurface
	 * @return
	 */
	private int findIndiceOfPhysicalState(String tokenSurface) {
		int indice = -1;
		if (tokenSurface.endsWith("(aq)")){
			indice =  tokenSurface.length() - 4;
		}
		else if (tokenSurface.endsWith("(aq.)")){
			indice =  tokenSurface.length() - 5;
		}
		else if (tokenSurface.endsWith("(s)")){
			indice =  tokenSurface.length() - 3;
		}
		else if (tokenSurface.endsWith("(l)")){
			indice =  tokenSurface.length() - 3;
		}
		else if (tokenSurface.endsWith("(g)")){
			indice =  tokenSurface.length() - 3;
		}
		if (indice >= 0){
			String prefixStr = tokenSurface.substring(0, indice);
			for (char charac : prefixStr.toCharArray()) {
				if (!Character.isDigit(charac)){
					return indice;
				}
			}
			return -1;
		}
		return indice;
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
