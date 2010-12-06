package uk.ac.cam.ch.wwmm.oscarpattern;

import nu.xom.Document;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

public class Main {

	public static void main(String[] args) throws Exception {
		PatternRecogniser PER = new PatternRecogniser();
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscarpattern/input/");
		String resourceName = "source.xml";
		Document sourceDoc = rg.getXMLDocument(resourceName);
		
		
		IProcessingDocument procDoc = null;
		try {
			procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), sourceDoc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			java.util.List<NamedEntity> Entities = PER
					.findNamedEntities(procDoc);
			for (NamedEntity namedEntity : Entities) {
				System.out.println("********************");
				System.out.println("NamedEntity= " + namedEntity.getSurface());
				System.out.println("Type=" + namedEntity.getType());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
