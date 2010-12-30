package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;
import uk.ac.cam.ch.wwmm.oscar.tools.StandoffTable;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.BioTag;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XMLSpanTagger;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

/**
 * Converts SciXML documents into ProcessingDocuments.
 * 
 * @author ptc24
 */
public class XOMBasedProcessingDocumentFactory {

	private static XOMBasedProcessingDocumentFactory myInstance;
	

	/**Gets the singleton instance of the ProcessingDocumentFactory.
	 * 
	 * @return The singleton instance of the ProcessingDocumentFactory.
	 */
	public static XOMBasedProcessingDocumentFactory getInstance() {
		if(myInstance == null) myInstance = new XOMBasedProcessingDocumentFactory();
		return myInstance;
	}
	
	public XOMBasedProcessingDocumentFactory() {

	}
	
	/**Makes a minimal ProcessingDocument.
	 * 
	 * @param sourceDoc The source SciXML document. This document is not
	 * modified, and is not stored; instead, a copy of the document is stored.
	 * @return The processingDocument.
	 * @throws Exception
	 */
	XOMBasedProcessingDocument makeDocument(Document sourceDoc) throws Exception {
		XOMBasedProcessingDocument procDoc = new XOMBasedProcessingDocument();
		
		procDoc.doc = new Document((Element)XOMTools.safeCopy(sourceDoc.getRootElement()));
		XMLSpanTagger.tagUpDocument(procDoc.doc.getRootElement(), "a");
		procDoc.standoffTable = new StandoffTable(procDoc.doc.getRootElement());

		return procDoc;
	}

	/**Makes a ProcessingDocument from a source SciXML document. The SciXML
	 * document is not stored by the ProcessingDocument - instead, a copy is
	 * made. This document may contain inline named entity annotation.
	 * 
	 * @param sourceDoc The source SciXML document.
	 * @param tokeniseForNEs Whether named entity boundaries should always
	 * result in token boundaries.
	 * @param mergeNEs Whether to merge the tokens of named entities, to create
	 * a state where all named entities are single-token. If this is set to
	 * true, tokeniseForNEs should be true too.
	 * @param runGenia Whether to (attempt) to run the Genia tagger once
	 * tokenisation has taken place.
	 * @return The ProcessingDocument for the source document.
	 * @throws Exception
	 * 
	 * 
	 */
	public IXOMBasedProcessingDocument makeTokenisedDocument(Tokeniser tokeniser, Document sourceDoc, boolean tokeniseForNEs, boolean mergeNEs, boolean runGenia) throws Exception {
		/****************************
		 * @lh359 Tokenisation Walkthrough:
		 * This is the function used to call the tokeniser and tokensequence
		 * and it calls makeTokenisedDocument with a null safdoc.
		 */
		return makeTokenisedDocument(tokeniser, sourceDoc, tokeniseForNEs, mergeNEs, runGenia, null);
	}
	
	/**Makes a ProcessingDocument from a source SciXML document. The SciXML
	 * document is not stored by the ProcessingDocument - instead, a copy is
	 * made. A SAF XML document, containing named entity information, may be
	 * included.
	 * 
	 * @param sourceDoc The source SciXML document.
	 * @param tokeniseForNEs Whether named entity boundaries should always
	 * result in token boundaries.
	 * @param mergeNEs Whether to merge the tokens of named entities, to create
	 * a state where all named entities are single-token. If this is set to
	 * true, tokeniseForNEs should be true too.
	 * @param runGenia Whether to (attempt) to run the Genia tagger once
	 * tokenisation has taken place.
	 * @param safDoc A SAF document, containing named entity information.
	 * @return The ProcessingDocument for the source document.
	 * @throws Exception
	 * 
	 * 
	 */
	public IXOMBasedProcessingDocument makeTokenisedDocument(Tokeniser tokeniser, Document sourceDoc, boolean tokeniseForNEs, boolean mergeNEs, boolean runGenia, Document safDoc) throws Exception {
		XOMBasedProcessingDocument procDoc = makeDocument(sourceDoc);
		procDoc.tokensByStart = new HashMap<Integer,IToken>();
		procDoc.tokensByEnd = new HashMap<Integer,IToken>();
		
		procDoc.tokenSequences = new ArrayList<ITokenSequence>();
		/******************************************* 
		 *  @lh359: This is the function that zones 
		 *  in on the sections that contain 
		 *  experimental information,
		 *  should be replaced
		 */
		   
		Nodes placesForChemicals = XMLStrings.getInstance().getChemicalPlaces(procDoc.doc);
		/***************************
		 * @lh359: Iterates through 
		 * the chemical sections
		 */
		for (int i = 0; i < placesForChemicals.size(); i++) {
			Element e = (Element)placesForChemicals.get(i);
			/******************************************
			 * @lh359: e.getValue() is sometimes faulty 
			 * because it misses nested tags 
			 */
			
			String text = e.getValue();
			/*********************************
			 * @lh359: This needs to be replaced
			 * because we won't be having xtspanstart
			 */
			int offset = Integer.parseInt(e.getAttributeValue("xtspanstart"));
			
			/**************************************
			 * @lh359: This calls the tokeniser and
			 * returns a TokenSequence
			 */
			ITokenSequence ts = makeTokenSequence(tokeniser, tokeniseForNEs,
					mergeNEs, safDoc, procDoc, e, text, offset);
			/********************************************
			 * @lh359: Once it's done it adds the tokensequence
			 * to the processingdocument
			 */
			procDoc.tokenSequences.add(ts);
		}
		
		/****************************
		 * @lh359: runGenia is rarely
		 * set to true, from what I have seen
		 * only accessed from oscarServer
		 * as an option for the user.
		 * 
		 * @dmj30: the Genia POS tags are used by the "patterns" flow command
		 * in OSCAR3 but the user has to install Genia and configure OSCAR to
		 * use it. See iePatterns.txt
		 * 
		 * 
		 */
		if(runGenia) {
			procDoc.sentences = new ArrayList<Sentence>();
			for(ITokenSequence ts : procDoc.tokenSequences) {
				List<IToken> tokens = ts.getTokens();
				List<Sentence> sentences = SentenceFinder.makeSentences(tokens);
				procDoc.sentences.addAll(sentences);
//				for(List<Token> sentence : sentences) {
//					NewGeniaRunner.runGenia(sentence);
//					NewGeniaRunner.assignChunks(sentence);
//				}
			}			
		}
		return procDoc;
	}

	ITokenSequence makeTokenSequence(Tokeniser tokeniser,
			boolean tokeniseForNEs, boolean mergeNEs, Document safDoc,
			XOMBasedProcessingDocument procDoc, Element e, String text,
			int offset) {
		
		Element annotations = safDoc != null ? safDoc.getRootElement() : e;

		ITokenSequence ts = tokeniser.tokenise(text, procDoc, offset, annotations);

		if (annotations != null && tokeniseForNEs) {
            /**************
             * @lh359 corrected modifyTokenisationForTraining to pass annotations instead of
             *  'e'
             */
            modifyTokenisationForTraining(tokeniser, text, procDoc, offset, annotations, mergeNEs, ts.getTokens());
			if (procDoc.getTokensByStart() != null) {
				procDoc.getTokensByStart().clear();
				procDoc.getTokensByEnd().clear();
			}
			return tokeniser.indexTokensAndMakeTokenSequence(text, procDoc, offset, e, ts.getTokens());
		}
		
		return ts;
	}

	
	private void modifyTokenisationForTraining(Tokeniser tokeniser, String s,
			IProcessingDocument procDoc, int offset, Element annotations,
			boolean mergeNEs, List<IToken> tokens) {


		try {
			/*
			 * @lh359: This function is called
			 * when we know the tag of the word
			 * This is what was editing the results
			 * in oscarCRF
			 * 
			 ***************************/

			tokeniseOnAnnotationBoundaries(tokeniser, s, procDoc, offset, annotations, tokens);
		} catch (Exception e) {
			e.printStackTrace();
		}
		tidyHyphensAfterNEs(tokeniser, tokens);
		if (mergeNEs) {
			mergeNeTokens(tokens, s, offset);
		}
	}
	
	void tokeniseOnAnnotationBoundaries(Tokeniser tokeniser, String sourceString, IProcessingDocument doc,
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
			/*@lh359
			 * Now retrieves the neType
			 */
			Nodes nodes = currentElem.query(".//slot[@name='type']");
			neType = currentElem.getAttributeValue("type");
			if (nodes.size() > 0)
				neType = nodes.get(0).getValue();


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
					tokens.get(i).setBioTag(new BioType(
						BioTag.B,
						NamedEntityType.valueOf(neType)
					));
					((Token)tokens.get(i)).setNeElem(currentElem);
					inElem = true;
					i++;
				}
				else if (tokens.get(i).getStart() < elemStart) {
					//token straddles beginning of annotation
					splits++;
					List<IToken> splitResults = tokeniser.splitAt(tokens.get(i), elemStart);
					tokens.remove(i);
					tokens.addAll(i, splitResults);
				}
				else if (tokens.get(i).getEnd() > elemEnd) {
					//token straddles end of annotation
					splits++;
					List<IToken> splitResults = tokeniser.splitAt(tokens.get(i), elemEnd);
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
					tokens.get(i).setBioTag(new BioType(
						BioTag.I,
						NamedEntityType.valueOf(neType)
					));
					((Token)tokens.get(i)).setNeElem(currentElem);
					i++;
				} else {
					//token straddles the end of the annotation
					splits++;
					List<IToken> splitResults = tokeniser.splitAt(tokens.get(i), elemEnd);
					tokens.remove(i);
					tokens.addAll(i, splitResults);
				}
			}
		}
	}
	
	
	void tidyHyphensAfterNEs(Tokeniser tokeniser, List<IToken> tokens) {
		int i = 0;
		BioType prevTokType = new BioType(BioTag.O);
		while (i < tokens.size()) {
			if (BioTag.O == prevTokType.getBio()
					|| BioTag.O != tokens.get(i).getBioTag().getBio()
					|| tokens.get(i).getValue().length() < 2
					|| !StringTools.isHyphen(tokens.get(i).getValue()
							.substring(0, 1))
					|| !(tokens.get(i).getStart() == tokens.get(i - 1).getEnd())) {
				i++;
				prevTokType = tokens.get(i - 1).getBioTag();
			} else {
				List<IToken> splitResults = tokeniser.splitAt(tokens.get(i),
						tokens.get(i).getStart() + 1);
				tokens.remove(i);
				tokens.addAll(i, splitResults);
				i += 2;
			}
		}
	}
	
	
	//FIXME I'm not sure that this method does what one would expect...
	void mergeNeTokens(List<IToken> tokens, String sourceString,
			int offset) {
		List<IToken> newTokens = new ArrayList<IToken>();
		IToken currentToken = null;
		NamedEntityType neClass = null;
		for (IToken t : tokens) {
			if (currentToken == null || neClass == null
					|| BioTag.O == t.getBioTag().getBio()
					|| BioTag.B == t.getBioTag().getBio()) {
				currentToken = t;
				newTokens.add(t);
				if (BioTag.O == currentToken.getBioTag().getBio()) {
					neClass = null;
				} else {
					neClass = currentToken.getBioTag().getType();
				}
				// inside
			} else {
				currentToken.setEnd(t.getEnd());
				currentToken.setValue(sourceString.substring(currentToken.getStart()
						- offset, currentToken.getEnd() - offset));
			}

		}
		tokens.clear();
		tokens.addAll(newTokens);
	}
}
