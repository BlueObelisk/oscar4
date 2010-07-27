package uk.ac.cam.ch.wwmm.oscartokeniser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.wwmm.oscartokeniser.scixml.XMLStrings;
import uk.ac.cam.ch.wwmm.oscartokeniser.xml.StandoffTable;
import uk.ac.cam.ch.wwmm.oscartokeniser.xml.XMLSpanTagger;
import uk.ac.cam.ch.wwmm.oscartokeniser.xml.XOMTools;

/**Converts SciXML documents into ProcessingDocuments.
 * 
 * @author ptc24
 *
 */

public final class ProcessingDocumentFactory {
	
	

	private static ProcessingDocumentFactory myInstance;
	

	/**Gets the singleton instance of the ProcessingDocumentFactory.
	 * 
	 * @return The singleton instance of the ProcessingDocumentFactory.
	 */
	public static ProcessingDocumentFactory getInstance() {
		if(myInstance == null) myInstance = new ProcessingDocumentFactory();
		return myInstance;
	}
	
	public ProcessingDocumentFactory() {

	}
	
	/**Makes a minimal ProcessingDocument.
	 * 
	 * @param sourceDoc The source SciXML document. This document is not
	 * modified, and is not stored; instead, a copy of the document is stored.
	 * @return The processingDocument.
	 * @throws Exception
	 */
	private ProcessingDocument makeDocument(Document sourceDoc) throws Exception {
		ProcessingDocument procDoc = new ProcessingDocument();
		
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
	 */
	public ProcessingDocument makeTokenisedDocument(Document sourceDoc, boolean tokeniseForNEs, boolean mergeNEs, boolean runGenia) throws Exception {
		return makeTokenisedDocument(sourceDoc, tokeniseForNEs, mergeNEs, runGenia, null);
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
	 */
	public ProcessingDocument makeTokenisedDocument(Document sourceDoc, boolean tokeniseForNEs, boolean mergeNEs, boolean runGenia, Document safDoc) throws Exception {
		ProcessingDocument procDoc = makeDocument(sourceDoc);
		procDoc.tokensByStart = new HashMap<Integer,Token>();
		procDoc.tokensByEnd = new HashMap<Integer,Token>();
		
		procDoc.tokenSequences = new ArrayList<TokenSequence>();
		Nodes placesForChemicals = XMLStrings.getInstance().getChemicalPlaces(procDoc.doc);
//		Nodes placesForChemicals = procDoc.getDoc().query("//DIV");
		System.out.println("Places for chemicals size: "+placesForChemicals.size());
		Tokeniser tokeniser = Tokeniser.getInstance();
		for(int i=0;i<placesForChemicals.size();i++) {
			Element e = (Element)placesForChemicals.get(i);
			String text = e.getValue();
			int offset = Integer.parseInt(e.getAttributeValue("xtspanstart"));
			TokenSequence ts = tokeniser.tokenise(text, procDoc, offset, safDoc != null ? safDoc.getRootElement() : e, tokeniseForNEs, mergeNEs);
			procDoc.tokenSequences.add(ts);
		}
		if(runGenia) {
			procDoc.sentences = new ArrayList<List<Token>>();
			for(TokenSequence ts : procDoc.tokenSequences) {
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
