package uk.ac.cam.ch.wwmm.oscartokeniser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.ITokeniser;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

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
	private static Tokeniser myInstance;

	/**
	 * Gets the Tokeniser singleton.
	 * 
	 * @return The Tokeniser singleton.
	 */
	public static Tokeniser getInstance() {
		if (myInstance == null)
			myInstance = new Tokeniser();
		return myInstance;
	}

	Tokeniser() {

	}

	/**
	 * Tokenises a string.
	 * 
	 * @param s
	 *            The string to tokenise.
	 * @return The TokenSequence for the string.
	 */
	public ITokenSequence tokenise(String s) {
		return tokenise(s, null, 0, null, false, false);
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
	public ITokenSequence tokenise(String s, IProcessingDocument doc, int offset,
			Element annotations, boolean tokeniseForNEs, boolean mergeNEs) {
		/*
		 * @dmj30: The arguments tokeniseForNEs and mergeNEs confuse me.
		 * Under what circumstances are we calling the tokeniser after we've
		 * already identified the named entities? I suspect the method is
		 * trying to do too many things.
		 */
		List<IToken> tokens = new LinkedList<IToken>();
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
			tokens.add(new Token(value, start, end, doc, "O", null));
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
			// System.out.println("***Before Split tokens = "+tokens.get(i).value);
			List<IToken> results = splitToken(tokens.get(i));

			
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
		// System.out.println(System.nanoTime() - nt);
		/* Discard empty tokens */
		
		/*
		 * @lh359: Remove empty tokens
		 */
		List<IToken> tmpTokens = new ArrayList<IToken>();
		for (IToken t : tokens) {
			if (t.getValue() != null && !"".equals(t.getValue())) {
				tmpTokens.add(t);
			}
		}
		tokens = tmpTokens;
		
		if (annotations != null && tokeniseForNEs) {
			try {
				/*
				 * @lh359: This function is called
				 * when we know the tag of the word
				 * This is what was editing the results
				 * in oscarCRF
				 * 
				 ***************************/
				tokeniseOnAnnotationBoundaries(s, doc, offset, annotations, tokens);
			} catch (Exception e) {
				e.printStackTrace();
			}
			tidyHyphensAfterNEs(tokens);
			if (mergeNEs) {
				mergeNeTokens(tokens, s, offset);
			}
		}
		/* Number tokens */
		int id = 0;
		for (IToken t : tokens) {
			t.setId(id);
			id++;
		}
		
		/* Make an index of the tokens in the ProcessingDocument */
		if (doc != null && doc.getTokensByStart() != null) {
			for (IToken t : tokens) {
				doc.getTokensByStart().put((Integer) t.getStart(), t);
				doc.getTokensByEnd().put(t.getEnd(), t);
			}
		}
		
		/* Create a TokenSequence from the tokens */
		TokenSequence tokenSequence = new TokenSequence(s, offset, doc, tokens);
		for (IToken t : tokens) {
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
	private List<IToken> splitToken(IToken token) {
		List<IToken> tokenList = rawSplitToken(token);
		if (tokenList == null)
			return null;
		int goodTokens = 0;
		for (IToken t : tokenList) {
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
	private List<IToken> rawSplitToken(IToken token) {
		/*
		 * @lh359: Added temporarily by me
		 *  so that it doesn't
		 *  tokenise on abbreviations
		 *  
		 *  @dmj30: Using String.split for every token in the document, are we?
		 */
		//TODO optimise this operation if it gets left in
		//TODO unit tests to check the extent of the tokenising on
		//     abbreviations problem.
		String abbreviations = "et. al. etc. e.g. i.e. vol. ca. wt. aq. ea-";
		List<String> abvList = new ArrayList<String>();
		for (String item : abbreviations.split(" ")) {
			abvList.add(item);
		}
		
		if (abvList.contains(token.getValue().toLowerCase())) return null;
		
		/*
		* End of lh359 code
		*****************************************/			
		String middleValue = "";
		if (token.getValue().length() > 2)
			middleValue = token.getValue().substring(0, token.getValue().length() - 1);
		/* Don't split special lexicon entries */
		if (token.getValue().startsWith("$")) {
			return null;
		}
		/* One-character tokens don't split! */
		if (token.getEnd() - token.getStart() < 2) {
			return null;
		}
		if (token.getValue().equals("--")) {
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
		if (oxidationStatePattern.matcher(token.getValue()).matches()) {
			return null;
		}
		/* Split unmatched brackets off the front */
		//TODO unit tests for the various StringTools methods
		if ("([{".indexOf(token.getValue().codePointAt(0)) != -1
				&& (StringTools.isBracketed(token.getValue()) || StringTools
						.isLackingCloseBracket(token.getValue()))) {
			return splitAt(token, token.getStart() + 1);
		}
		/* Split unmatched brackets off the end */
		if (")]}".indexOf(token.getValue().codePointAt(token.getValue().length() - 1)) != -1
				&& (StringTools.isBracketed(token.getValue()) || StringTools
						.isLackingOpenBracket(token.getValue()))) {
			return splitAt(token, token.getEnd() - 1);
		}
		/* Split oxidation state off the end */
		if (oxidationStateEndPattern.matcher(token.getValue()).matches()) {
			return splitAt(token, token.getStart() + token.getValue().lastIndexOf('('));
		}
		/* Split some characters off the front of tokens */
		if ((StringTools.relations + StringTools.quoteMarks)
				.indexOf(token.getValue().codePointAt(0)) != -1) {
			return splitAt(token, token.getStart() + 1);
		}
		/* Split some characters off the back of tokens */
		
		//This is probably the source of the abbreviation problem.
		if ((".,;:!?\u2122\u00ae-" + StringTools.quoteMarks)
				.indexOf(token.getValue().codePointAt(token.getValue().length() - 1)) != -1) {
			// Careful with Jones' reagent
			if (!(token.getValue().substring(token.getValue().length() - 1).equals("'") && token.getValue()
					.matches("[A-Z][a-z]+s'"))) {
				return splitAt(token, token.getEnd() - 1);
			}
		}
		/* Split trademark symbols off the back of tokens */
		Matcher m = trademarkPattern.matcher(token.getValue());
		if (m.matches() && m.start(1) > 0) {
			return splitAt(token, token.getStart() + m.start(1));
		}
		/* characters to split on */
		if (middleValue.contains("<")) {
			return splitAt(token, token.getStart() + token.getValue().indexOf("<"),
					token.getStart() + token.getValue().indexOf("<") + 1);
		}
		if (middleValue.contains(">")) {
			return splitAt(token, token.getStart() + token.getValue().indexOf(">"),
					token.getStart() + token.getValue().indexOf(">") + 1);
		}
		if (middleValue.contains("/")) {
			return splitAt(token, token.getStart() + token.getValue().indexOf("/"),
					token.getStart() + token.getValue().indexOf("/") + 1);
		}
		if (middleValue.contains(":")) {
			// Check to see if : is nestled in brackets, such as in ring systems
			if (StringTools.bracketsAreBalanced(token.getValue())
					&& StringTools.bracketsAreBalanced(token.getValue()
							.substring(token.getValue().indexOf(":") + 1))) {
				return splitAt(token, token.getStart() + token.getValue().indexOf(":"),
						token.getStart() + token.getValue().indexOf(":") + 1);
			}
		}
		if (middleValue.contains("+")) {
			int index = token.getValue().indexOf("+");
			if (index < (token.getValue().length() - 2)
					&& StringTools.isHyphen(token.getValue().substring(
							index + 1, index + 2))) {
				return splitAt(token, token.getStart() + index + 1, token.getStart()
						+ index + 2);
			}
			if (index > 0 && index < token.getValue().length() - 1) {
				if (token.getValue().endsWith("-")) {

				} else if (StringTools.bracketsAreBalanced(token.getValue())
						&& StringTools.bracketsAreBalanced(token.getValue()
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
					+ token.getValue().indexOf(StringTools.midElipsis), token.getStart()
					+ token.getValue().indexOf(StringTools.midElipsis) + 1);
		}
		/* Hyphens */
		if (middleValue.contains("--")) {
			return splitAt(token, token.getStart() + token.getValue().indexOf("--"),
					token.getStart() + token.getValue().indexOf("--") + 2);
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
		int splittableHyphenIndex = -1;
		//int splittableHyphenIndex = HyphenTokeniser.indexOfSplittableHyphen(token.getValue());
		
		
		/* Split on appropriate hyphens */
		if (splittableHyphenIndex != -1
				&& !token.getValue().matches(".*[a-z][a-z].*")
				&& token.getValue().matches(".*[A-Z].*")) {
//			if (TLRHolder.getInstance().macthesTlr(token.getValue(), "bondRegex")) {
//				splittableHyphenIndex = -1;
//			}
		}
		if (splittableHyphenIndex != -1) {
			if (token.getValue().endsWith("NMR")) {
				return splitAt(token, token.getStart() + splittableHyphenIndex,
						token.getStart() + splittableHyphenIndex + 1);
			} else if (prefixPattern.matcher(token.getValue()).matches()) {
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

	private List<IToken> splitAt(IToken token, int splitOffset) {
		int internalOffset = splitOffset - token.getStart();
		List<IToken> tokens = new LinkedList<IToken>();
		tokens
				.add(new Token(token.getValue().substring(0, internalOffset),
						token.getStart(), splitOffset, token.getDoc(), token.getBioTag(),
						((Token)token).getNeElem()));
		tokens.add(new Token(token.getValue().substring(internalOffset),
				splitOffset, token.getEnd(), token.getDoc(), token.getBioTag(),
				((Token)token).getNeElem()));
		// System.out.printf("Split: %s %d", value, splitOffset);
		return tokens;
	}

	private List<IToken> splitAt(IToken token, int splitOffset0, int splitOffset1) {
		int internalOffset0 = splitOffset0 - token.getStart();
		int internalOffset1 = splitOffset1 - token.getStart();
		List<IToken> tokens = new LinkedList<IToken>();
		tokens.add(new Token(token.getValue().substring(0, internalOffset0),
					token.getStart(), splitOffset0, token.getDoc(), token.getBioTag(),
					((Token)token).getNeElem()));
		tokens.add(new Token(token.getValue().substring(internalOffset0,
					internalOffset1), splitOffset0, splitOffset1, token.getDoc(),
					token.getBioTag(), ((Token)token).getNeElem()));
		tokens.add(new Token(token.getValue().substring(internalOffset1),
					splitOffset1, token.getEnd(), token.getDoc(), token.getBioTag(),
					((Token)token).getNeElem()));

		return tokens;
	}

	
	public void tokeniseOnAnnotationBoundaries(String sourceString, IProcessingDocument doc,
			int offset, Element safOrInlineAnnotations, List<IToken> tokens) throws Exception {
		Nodes annotationNodes;
		int currentNodeId = 0;
		Element currentElem = null;
		int elemStart = -1;
		int elemEnd = -1;
		String neType = null;
		boolean sourceIsInline;

		//prime variables, depending on whether we are working from saf or inline
		if (safOrInlineAnnotations.getLocalName().equals("saf")) {
			sourceIsInline = false;
			Nodes safAnnotationNodes = safOrInlineAnnotations.query("annot");
			annotationNodes = new Nodes();
			int endOffset = sourceString.length() + offset;
			for (int i = 0; i < safAnnotationNodes.size(); i++) {
				Element annot = (Element) safAnnotationNodes.get(i);
				String startX = annot.getAttributeValue("from");
				int start = doc.getStandoffTable().getOffsetAtXPoint(startX);
				if (start < offset)
					continue;
				String endX = annot.getAttributeValue("to");
				int end = doc.getStandoffTable().getOffsetAtXPoint(endX);
				if (end > endOffset)
					continue;
				annotationNodes.append(annot);
			}
			if (annotationNodes.size() == 0)
				return;
			currentElem = (Element) annotationNodes.get(currentNodeId);
			elemStart = doc.getStandoffTable().getOffsetAtXPoint(currentElem
					.getAttributeValue("from"));
			elemEnd = doc.getStandoffTable().getOffsetAtXPoint(currentElem
					.getAttributeValue("to"));
		}
		else {
			sourceIsInline = true;
			annotationNodes = safOrInlineAnnotations.query(".//ne");
			if (annotationNodes.size() == 0)
				return;
			currentElem = (Element) annotationNodes.get(currentNodeId);
			elemStart = Integer.parseInt(currentElem
					.getAttributeValue("xtspanstart"));
			elemEnd = Integer.parseInt(currentElem
					.getAttributeValue("xtspanend"));
			neType = currentElem.getAttributeValue("type");
		}
		
		
		//find start and end offsets the first annotation that fits the condition elemEnd > elemStart 
		while (!(elemEnd > elemStart)) {
			currentNodeId++;
			if (currentNodeId >= annotationNodes.size()) {
				return;
			}
			else {
				currentElem = (Element) annotationNodes.get(currentNodeId);
				if (sourceIsInline) {
					elemStart = Integer.parseInt(currentElem
							.getAttributeValue("xtspanstart"));
					elemEnd = Integer.parseInt(currentElem
							.getAttributeValue("xtspanend"));
					neType = currentElem.getAttributeValue("type");
				} else {
					elemStart = doc.getStandoffTable().getOffsetAtXPoint(currentElem
							.getAttributeValue("from"));
					elemEnd = doc.getStandoffTable().getOffsetAtXPoint(currentElem
							.getAttributeValue("to"));
				}
			}
		}
		
		
		//split tokens that span annotation boundaries on those annotation boundaries
		int splits = 0;
		int i = 0;
		boolean inElem = false;
		while (i < tokens.size()) {
			//note that when we split tokens we don't increment i, so we're
			//doing that recursive processing without recursion thing again
			if (!inElem) {
				if (tokens.get(i).getEnd() <= elemStart) {
					//token precedes annotation element - skip
					i++;
				}
				else if (tokens.get(i).getStart() >= elemStart
						&& tokens.get(i).getEnd() <= elemEnd) {
					//token is fully contained within annotation
					((Token)tokens.get(i)).setNeElem(currentElem);
					inElem = true;
					i++;
				}
				else if (tokens.get(i).getStart() < elemStart) {
					//token straddles beginning of annotation
					splits++;
					List<IToken> splitResults = splitAt(tokens.get(i), elemStart);
					tokens.remove(i);
					tokens.addAll(i, splitResults);
				}
				else if (tokens.get(i).getEnd() > elemEnd) {
					//token straddles end of annotation
					splits++;
					List<IToken> splitResults = splitAt(tokens.get(i), elemEnd);
					tokens.remove(i);
					tokens.addAll(i, splitResults);
				}
				else {
					//something has probably gone wrong
					i++;
				}
			}
			else {
				if (tokens.get(i).getStart() >= elemEnd) {
					//token starts after the annotation - find next annotation
					inElem = false;
					int oldElemEnd = elemEnd;
					while (!(elemEnd > oldElemEnd && elemEnd > elemStart)) {
						currentNodeId++;
						if (currentNodeId >= annotationNodes.size()) {
							return;
						} else {
							currentElem = (Element) annotationNodes.get(currentNodeId);
							if (sourceIsInline) {
								elemStart = Integer.parseInt(currentElem
										.getAttributeValue("xtspanstart"));
								elemEnd = Integer.parseInt(currentElem
										.getAttributeValue("xtspanend"));
								neType = currentElem.getAttributeValue("type");
							} else {
								elemStart = doc.getStandoffTable()
										.getOffsetAtXPoint(currentElem
												.getAttributeValue("from"));
								elemEnd = doc.getStandoffTable()
										.getOffsetAtXPoint(currentElem
												.getAttributeValue("to"));
							}
						}
					}
				}
				else if (tokens.get(i).getEnd() <= elemEnd) {
					//token is contained in the annotation
					tokens.get(i).setBioTag("I-" + neType);
					((Token)tokens.get(i)).setNeElem(currentElem);
					i++;
				} else {
					//token straddles the end of the annotation
					splits++;
					List<IToken> splitResults = splitAt(tokens.get(i), elemEnd);
					tokens.remove(i);
					tokens.addAll(i, splitResults);
				}
			}
		}
	}

	public void tidyHyphensAfterNEs(List<IToken> tokens) {
		int i = 0;
		String prevTokType = "O";
		while (i < tokens.size()) {
			if (prevTokType.equals("O")
					|| !tokens.get(i).getBioTag().equals("O")
					|| tokens.get(i).getValue().length() < 2
					|| !StringTools.isHyphen(tokens.get(i).getValue()
							.substring(0, 1))
					|| !(tokens.get(i).getStart() == tokens.get(i - 1).getEnd())) {
				i++;
				prevTokType = tokens.get(i - 1).getBioTag();
			} else {
				List<IToken> splitResults = splitAt(tokens.get(i),
						tokens.get(i).getStart() + 1);
				tokens.remove(i);
				tokens.addAll(i, splitResults);
				i += 2;
			}
		}
	}

	
	public List<IToken> mergeNeTokens(List<IToken> tokens, String sourceString,
			int offset) {
		List<IToken> newTokens = new ArrayList<IToken>();
		IToken currentToken = null;
		String neClass = null;
		for (IToken t : tokens) {
			if (currentToken == null || neClass == null
					|| t.getBioTag().startsWith("O") || t.getBioTag().startsWith("B")) {
				currentToken = t;
				newTokens.add(t);
				if (currentToken.getBioTag().equals("O")) {
					neClass = null;
				} else {
					neClass = currentToken.getBioTag().substring(2);
				}
				// inside
			} else {
				currentToken.setEnd(t.getEnd());
				currentToken.setValue(sourceString.substring(currentToken.getStart()
						- offset, currentToken.getEnd() - offset));
			}

		}

		return newTokens;
	}
}
