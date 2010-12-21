package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscarMEMM.memm.data.MEMMModel;

public class PubMedModelTest {

	@Test
	public void testConstructor() {
		PubMedModel model = new PubMedModel();
		assertNotNull(model.getZeroProbs());
		assertNull(model.getUberModel());
		assertNotNull(model.getRescorer());
		assertNotNull(model.getTagSet());
		assertNotNull(model.getNamedEntityTypes());
		assertNotNull(model.getManualAnnotations());
		assertNotNull(model.getGISModelPrevs());
		assertNotNull(model.getNGram());
	}
	
	
	@Test
	public void testLoadChemPapers() {
		MEMMModel model = new PubMedModel();
		assertFalse(
			model.getManualAnnotations().getNonChemicalWords().contains(
				"elongate"
			)
		);
		assertTrue(
			model.getManualAnnotations().getNonChemicalWords().contains(
				"leukaemic"
			)
		);
	}

}
