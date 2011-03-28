package uk.ac.cam.ch.wwmm.oscarrecogniser.manualAnnotations;

import static org.junit.Assert.*;
import nu.xom.Document;
import nu.xom.Element;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

public class ManualAnnotationsTest {

	@Test
	public void testLoadDefaultModelData() {
		assertNotNull(ManualAnnotations.getDefaultInstance());
	}
	
	@Test
	public void testReadXML() throws Exception {
		ManualAnnotations manualAnnotations = new ManualAnnotations();
		assertFalse(manualAnnotations.getChemicalWords().contains("ammonia"));
		
		ResourceGetter rg = new ResourceGetter("/uk/ac/cam/ch/wwmm/oscarrecogniser/models/");
		Document modelDoc = rg.getXMLDocument("chempapers.xml");
		Element etdElement = modelDoc.getRootElement().getFirstChildElement("etd");

        manualAnnotations = new ManualAnnotations(etdElement);
		assertTrue(manualAnnotations.getChemicalWords().contains("ammonia"));
	}

	
	@Test
	public void testReinitialise() {
		ManualAnnotations annotations1 = new ManualAnnotations(ManualAnnotations.loadEtdElement("chempapers"));
		assertTrue(annotations1.getNonChemicalWords().contains("elongate"));
		assertFalse(annotations1.getNonChemicalWords().contains("leukaemic"));
		
		ManualAnnotations annotations2 = new ManualAnnotations(ManualAnnotations.loadEtdElement("pubmed"));
		assertFalse(annotations2.getNonChemicalWords().contains("elongate"));
		assertTrue(annotations2.getNonChemicalWords().contains("leukaemic"));
	}
}
