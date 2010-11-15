package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscarrecogniser.etd.ExtractedTrainingData;

public class ModelTest {

	@Test
	public void testGetInstance() {
		Model.loadModel();
	}

	
	@Test
	public void testLoadModel() {
		Model.loadModel("chempapers");
		assertTrue(ExtractedTrainingData.getInstance().nonChemicalWords.contains("elongate"));
		assertFalse(ExtractedTrainingData.getInstance().nonChemicalWords.contains("leukaemic"));
		
		Model.loadModel("pubmed");
		assertFalse(ExtractedTrainingData.getInstance().nonChemicalWords.contains("elongate"));
		assertTrue(ExtractedTrainingData.getInstance().nonChemicalWords.contains("leukaemic"));
		
		Model.loadModel("model");
		assertEquals(0, ExtractedTrainingData.getInstance().nonChemicalWords.size());
	}
}
