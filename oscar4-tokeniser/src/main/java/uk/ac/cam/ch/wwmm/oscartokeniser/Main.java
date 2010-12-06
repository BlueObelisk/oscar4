package uk.ac.cam.ch.wwmm.oscartokeniser;

import java.util.List;

import nu.xom.Document;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;


public class Main {
	
	public static void main(String args[]){
		IProcessingDocument procDoc = null;
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscartokeniser/input/");
		String resourceName = "source.xml";
		Document sourceDoc = rg.getXMLDocument(resourceName);

		try {
			procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), sourceDoc);
		} catch (Exception e) {
            System.err.println("Can't find file, please check your path");
			e.printStackTrace();
		}
		List<ITokenSequence> tokenSequences = procDoc.getTokenSequences();
		for (int j = 0; j < tokenSequences.size(); j++) 
		{
			ITokenSequence tokenSequence = tokenSequences.get(j);
            				
			List<IToken> tokens = tokenSequence.getTokens();
			
			System.out.println("There are "+tokens.size()+" tokens in the string");
			for (IToken token : tokens) {
				System.out.println("Token:: "+token.getValue());
			}
			
		}
	
	}

}
