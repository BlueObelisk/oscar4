package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMModel;

public class PubMedModelTest {

	@Test
	public void testLoadChemPapers() {
		MEMMModel model = new PubMedModel();
		assertFalse(
			model.getExtractedTrainingData().nonChemicalWords.contains(
				"elongate"
			)
		);
		assertTrue(
			model.getExtractedTrainingData().nonChemicalWords.contains(
				"leukaemic"
			)
		);
	}

}
