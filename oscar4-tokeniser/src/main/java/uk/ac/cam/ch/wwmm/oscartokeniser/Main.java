package uk.ac.cam.ch.wwmm.oscartokeniser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;


public class Main {
	
	public static void main(String args[]) throws ValidityException, FileNotFoundException, ParsingException, IOException{
		IProcessingDocument procDoc = null;
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscartokeniser/input/");
		String resourceName = "source.xml";
		Document sourceDoc = rg.getXMLDocument(resourceName);

		try {
			procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), sourceDoc);
		} catch (Exception e) {
            System.err.println("Can't find file, please check your path");
			e.printStackTrace();
		}
		List<TokenSequence> tokenSequences = procDoc.getTokenSequences();
		for (int j = 0; j < tokenSequences.size(); j++) 
		{
			TokenSequence tokenSequence = tokenSequences.get(j);
            				
			List<Token> tokens = tokenSequence.getTokens();
			
			System.out.println("There are "+tokens.size()+" tokens in the string");
			for (Token token : tokens) {
				System.out.println("Token:: "+token.getSurface());
			}
			
		}
	
	}

}
