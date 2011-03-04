package uk.ac.cam.ch.wwmm.oscarMEMM;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

public class Main {

	
	public static void main (String[] args) throws ValidityException, FileNotFoundException, ParsingException, IOException
	{
		MEMMRecogniser MER = new MEMMRecogniser();
		ResourceGetter rg = new ResourceGetter(MER.getClass().getClassLoader(),"uk/ac/cam/ch/wwmm/oscarMEMM/input/");
		String resourceName = "source.xml";
		Document sourceDoc = rg.getXMLDocument(resourceName);
		IProcessingDocument procDoc = null;
		
		try 
		{
			procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), sourceDoc);
		    
		 } 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			
			List<NamedEntity> Entities = MER.findNamedEntities(procDoc.getTokenSequences());
			for (NamedEntity namedEntity : Entities) {
				System.out.println("********************");
				System.out.println("NamedEntity= "+namedEntity.getSurface());
				System.out.println("Type="+namedEntity.getType());
				System.out.println("Confidence="+namedEntity.getConfidence());
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
