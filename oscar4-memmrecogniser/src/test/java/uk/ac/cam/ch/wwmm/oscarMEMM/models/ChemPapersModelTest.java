package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscarMEMM.memm.data.MEMMModel;

public class ChemPapersModelTest {

	@Test
	public void testLoadChemPapers() {
		MEMMModel model = new ChemPapersModel();
		assertTrue(
			model.getManualAnnotations().nonChemicalWords.contains(
				"elongate"
			)
		);
		assertFalse(
			model.getManualAnnotations().nonChemicalWords.contains(
				"leukaemic"
			)
		);
	}

}
