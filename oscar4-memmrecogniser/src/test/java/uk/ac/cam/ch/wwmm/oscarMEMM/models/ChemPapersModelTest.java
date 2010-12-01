package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMModel;

public class ChemPapersModelTest {

	@Test
	public void testLoadChemPapers() {
		MEMMModel model = new ChemPapersModel();
		assertTrue(
			model.getExtractedTrainingData().nonChemicalWords.contains(
				"elongate"
			)
		);
		assertFalse(
			model.getExtractedTrainingData().nonChemicalWords.contains(
				"leukaemic"
			)
		);
	}

}
