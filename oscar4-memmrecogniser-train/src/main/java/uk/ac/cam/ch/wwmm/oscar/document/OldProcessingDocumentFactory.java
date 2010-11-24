package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;
import uk.ac.cam.ch.wwmm.oscar.tools.StandoffTable;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XMLSpanTagger;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

/**
 * Converts SciXML documents into ProcessingDocuments.
 * 
 * @author ptc24
 */
public class OldProcessingDocumentFactory {

	private static OldProcessingDocumentFactory myInstance;
	

	/**Gets the singleton instance of the ProcessingDocumentFactory.
	 * 
	 * @return The singleton instance of the ProcessingDocumentFactory.
	 */
	public static OldProcessingDocumentFactory getInstance() {
		if(myInstance == null) myInstance = new OldProcessingDocumentFactory();
		return myInstance;
	}
	
	public OldProcessingDocumentFactory() {

	}
	
	/**Makes a minimal ProcessingDocument.
	 * 
	 * @param sourceDoc The source SciXML document. This document is not
	 * modified, and is not stored; instead, a copy of the document is stored.
	 * @return The processingDocument.
	 * @throws Exception
	 */
	private OldProcessingDocument makeDocument(Document sourceDoc) throws Exception {
		OldProcessingDocument procDoc = new OldProcessingDocument();
		
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
	public IOldProcessingDocument makeTokenisedDocument(ITokeniser tokeniser, Document sourceDoc, boolean tokeniseForNEs, boolean mergeNEs, boolean runGenia) throws Exception {
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
	public IOldProcessingDocument makeTokenisedDocument(ITokeniser tokeniser, Document sourceDoc, boolean tokeniseForNEs, boolean mergeNEs, boolean runGenia, Document safDoc) throws Exception {
		OldProcessingDocument procDoc = makeDocument(sourceDoc);
		procDoc.tokensByStart = new HashMap<Integer,Token>();
		procDoc.tokensByEnd = new HashMap<Integer,Token>();
		
		procDoc.tokenSequences = new ArrayList<TokenSequence>();
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
		for(int i=0;i<placesForChemicals.size();i++) {
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
			TokenSequence ts = tokeniser.tokenise(text, procDoc, offset, safDoc != null ? safDoc.getRootElement() : e, tokeniseForNEs, mergeNEs);
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
			procDoc.sentences = new ArrayList<List<Token>>();
			for(ITokenSequence ts : procDoc.tokenSequences) {
				List<Token> tokens = ts.getTokens();
				List<List<Token>> sentences = SentenceSplitter.makeSentences(tokens);
				procDoc.sentences.addAll(sentences);
//				for(List<Token> sentence : sentences) {
//					NewGeniaRunner.runGenia(sentence);
//					NewGeniaRunner.assignChunks(sentence);
//				}
			}			
		}
		return procDoc;
	}

}
