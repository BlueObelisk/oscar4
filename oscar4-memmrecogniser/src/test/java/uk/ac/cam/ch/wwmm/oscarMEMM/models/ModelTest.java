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
		Model model = Model.loadModel("chempapers");
		assertTrue(model.getExtractedTrainingData().nonChemicalWords.contains("elongate"));
		assertFalse(model.getExtractedTrainingData().nonChemicalWords.contains("leukaemic"));
		
		model = Model.loadModel("pubmed");
		assertFalse(model.getExtractedTrainingData().nonChemicalWords.contains("elongate"));
		assertTrue(model.getExtractedTrainingData().nonChemicalWords.contains("leukaemic"));

		model = Model.loadModel("model");
		assertEquals(0, model.getExtractedTrainingData().nonChemicalWords.size());
	}
}
