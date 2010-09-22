package uk.ac.cam.ch.wwmm.oscarpattern;

import nu.xom.Document;
import uk.ac.cam.ch.wwmm.oscarpattern.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscarpattern.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscarpattern.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscarpattern.tools.ResourceGetter;

public class Main {

	public static void main(String[] args) throws Exception {
		PatternRecogniser PER = new PatternRecogniser();
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscarpattern/input/");
		String resourceName = "source.xml";
		Document sourceDoc = rg.getXMLDocument(resourceName);
		
		
		ProcessingDocument procDoc = null;
		try {
			procDoc = new ProcessingDocumentFactory().makeTokenisedDocument(
					sourceDoc, true, false, false);
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
