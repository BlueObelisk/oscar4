package uk.ac.cam.ch.wwmm.oscar.document;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;

/**
 * 
 * Allows the creation of a ProcessingDocument for use in OSCAR processing
 * 
 * @author dmj30
 *
 */
public class ProcessingDocumentFactory {

	private static ProcessingDocumentFactory myInstance;
	
	@Deprecated
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
		throw new RuntimeException("method shouldn't have been called");
	}

	/**
	 * Creates a tokenised ProcessingDocument from a SciXML document, using the supplied tokeniser
	 */
	public IProcessingDocument makeTokenisedDocument(ITokeniser tokeniser,
			Document sciXmlDoc, boolean tokeniseForNEs, boolean mergeNEs, boolean runGenia) {

		ProcessingDocument procDoc = new ProcessingDocument();
		Nodes placesForChemicals = XMLStrings.getInstance().getChemicalPlaces(sciXmlDoc);
		for (int i = 0; i < placesForChemicals.size(); i++) {
			Element e = (Element) placesForChemicals.get(i);
			String source = e.getValue();
			ITokenSequence ts = tokeniser.tokenise(source, procDoc, 0, null, false, false);
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
		ITokenSequence ts = tokeniser.tokenise(source, procDoc, 0, null, false, false);
		procDoc.addTokenSequence(ts);
		return procDoc;
	}

}
