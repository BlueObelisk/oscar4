package uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata;

import static org.junit.Assert.*;
import nu.xom.Document;
import nu.xom.Element;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata.ExtractedTrainingData;

public class ExtractedTrainingDataTest {

	@Test
	public void testLoadDefaultModelData() {
		assertNotNull(ExtractedTrainingData.getDefaultInstance());
	}
	
	@Test
	public void testReadXML() throws Exception {
		ExtractedTrainingData etd = new ExtractedTrainingData();
		assertFalse(etd.getChemicalWords().contains("ammonia"));
		
		ResourceGetter rg = new ResourceGetter("/uk/ac/cam/ch/wwmm/oscarrecogniser/models/");
		Document modelDoc = rg.getXMLDocument("chempapers.xml");
		Element etdElement = modelDoc.getRootElement().getFirstChildElement("etd");

        etd = new ExtractedTrainingData(etdElement);
		assertTrue(etd.getChemicalWords().contains("ammonia"));
	}

	
	@Test
	public void testReinitialise() {
		ExtractedTrainingData annotations1 = new ExtractedTrainingData(ExtractedTrainingData.loadEtdElement("chempapers"));
		assertTrue(annotations1.getNonChemicalWords().contains("elongate"));
		assertFalse(annotations1.getNonChemicalWords().contains("leukaemic"));
		
		ExtractedTrainingData annotations2 = new ExtractedTrainingData(ExtractedTrainingData.loadEtdElement("pubmed"));
		assertFalse(annotations2.getNonChemicalWords().contains("elongate"));
		assertTrue(annotations2.getNonChemicalWords().contains("leukaemic"));
	}
}
