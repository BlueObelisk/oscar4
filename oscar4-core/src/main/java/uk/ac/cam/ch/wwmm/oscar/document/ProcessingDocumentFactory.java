package uk.ac.cam.ch.wwmm.oscar.document;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XMLSpanTagger;

/**
 * 
 * Allows the creation of a ProcessingDocument for use in OSCAR processing
 * 
 * @author dmj30
 *
 */
public class ProcessingDocumentFactory {

	private static ProcessingDocumentFactory myInstance;
	
	private ProcessingDocumentFactory() {
		
	}

	public static ProcessingDocumentFactory getInstance() {
		if (myInstance == null) {
			myInstance = new ProcessingDocumentFactory();
		}
		return myInstance;
	}

	/**
	 * This method is (hopefully) only used when training memm models and will never be called here
	 */
	@Deprecated
	public IProcessingDocument makeTokenisedDocument(ITokeniser instance,
			Document doc, boolean b, boolean c, boolean d, Document safDoc) {
		throw new UnsupportedOperationException("method shouldn't have been called");
	}

	/**
	 * Creates a tokenised ProcessingDocument from a SciXML document, using the supplied tokeniser
	 * 
	 * @param tokeniser the tokeniser to be used
	 * @param sciXmlDoc the document to be tokenised
	 */
	public ProcessingDocument makeTokenisedDocument(ITokeniser tokeniser,
			Document sciXmlDoc) {
		return makeTokenisedDocument(tokeniser, sciXmlDoc, XMLStrings.getDefaultInstance());
	}
	
	/**
	 * Creates a tokenised ProcessingDocument from a SciXML document, using the supplied tokeniser
	 * 
	 * @param tokeniser the tokeniser to be used
	 * @param sciXmlDoc the document to be tokenised
	 * @param xmlStrings the {@link XMLStrings} for the sciXmlDoc's schema
	 */
	public ProcessingDocument makeTokenisedDocument(ITokeniser tokeniser,
			Document sciXmlDoc, XMLStrings xmlStrings) {

		ProcessingDocument procDoc = new ProcessingDocument();
		Document taggedDoc = (Document) sciXmlDoc.copy();
		XMLSpanTagger.tagUpDocument(taggedDoc.getRootElement(), "a"); //"a" was used in OSCAR3, but the prefix seems pretty unnecessary 
		Nodes placesForChemicals = xmlStrings.getChemicalPlaces(taggedDoc);
		for (int i = 0; i < placesForChemicals.size(); i++) {
			Element e = (Element) placesForChemicals.get(i);
			String source = e.getValue();
			int offset = Integer.parseInt(e.getAttributeValue("xtspanstart"));
			TokenSequence ts = tokeniser.tokenise(source, procDoc, offset, null);
			procDoc.addTokenSequence(ts);
		}
		
		return procDoc;
	}

	/**
	 * Creates a tokenised ProcessingDocument from a string, using the supplied tokeniser 
	 * 
	 */
	public ProcessingDocument makeTokenisedDocument(ITokeniser tokeniser,
			String source) {

		ProcessingDocument procDoc = new ProcessingDocument();
		TokenSequence ts = tokeniser.tokenise(source, procDoc, 0, null);
		procDoc.addTokenSequence(ts);
		return procDoc;
	}

}
