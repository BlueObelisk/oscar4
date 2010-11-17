package uk.ac.cam.ch.wwmm.oscarrecogniser.etd;

import static org.junit.Assert.*;
import nu.xom.Document;
import nu.xom.Element;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

public class ExtractedTrainingDataTest {

	@Test
	public void testLoadDefaultModelData() {
		ExtractedTrainingData etd = ExtractedTrainingData.getInstance();
		assertEquals(OscarProperties.getData().model, etd.getModelName());
	}
	
	@Test
	public void testReadXML() {
		ExtractedTrainingData etd = new ExtractedTrainingData();
		assertFalse(etd.chemicalWords.contains("ammonia"));
		
		ResourceGetter rg = new ResourceGetter("/uk/ac/cam/ch/wwmm/oscarrecogniser/models/");
		Document modelDoc = rg.getXMLDocument("chempapers.xml");
		Element etdElement = modelDoc.getRootElement().getFirstChildElement("etd");

        etd = new ExtractedTrainingData(etdElement);
		assertTrue(etd.chemicalWords.contains("ammonia"));
	}

//  XXX Becoming immutable
//	@Test
//	public void testClear() {
//		ExtractedTrainingData etd = ExtractedTrainingData.getInstance();
//		assertFalse(etd.chemicalWords.size() == 0);
//
//		etd.clear();
//		ExtractedTrainingData etd2 = ExtractedTrainingData.getInstance();
//		assertTrue(etd == etd2);
//		assertTrue(etd.chemicalWords.size() == 0);
//	}
	
	@Test
	public void testReinitialise() {
		ExtractedTrainingData.reinitialise(ExtractedTrainingData.loadEtdElement("chempapers"));
		assertTrue(ExtractedTrainingData.getInstance().nonChemicalWords.contains("elongate"));
		assertFalse(ExtractedTrainingData.getInstance().nonChemicalWords.contains("leukaemic"));
		
		ExtractedTrainingData.reinitialise(ExtractedTrainingData.loadEtdElement("pubmed"));
		assertFalse(ExtractedTrainingData.getInstance().nonChemicalWords.contains("elongate"));
		assertTrue(ExtractedTrainingData.getInstance().nonChemicalWords.contains("leukaemic"));
	}
}
