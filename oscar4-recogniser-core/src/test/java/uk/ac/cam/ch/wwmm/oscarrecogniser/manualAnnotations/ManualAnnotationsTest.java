package uk.ac.cam.ch.wwmm.oscarrecogniser.manualAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

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
		ManualAnnotations annotations1 = ManualAnnotations.reinitialise(ManualAnnotations.loadEtdElement("chempapers"));
		assertTrue(annotations1.getNonChemicalWords().contains("elongate"));
		assertFalse(annotations1.getNonChemicalWords().contains("leukaemic"));
		
		ManualAnnotations annotations2 = ManualAnnotations.reinitialise(ManualAnnotations.loadEtdElement("pubmed"));
		assertFalse(annotations2.getNonChemicalWords().contains("elongate"));
		assertTrue(annotations2.getNonChemicalWords().contains("leukaemic"));
	}
}
