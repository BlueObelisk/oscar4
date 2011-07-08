package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMModel;

public class PubMedModelTest {

private static MEMMModel model;
	
	@BeforeClass
	public static void setUp() {
		model = new PubMedModel();
	}
	
	@AfterClass
	public static void cleanUp() {
		model = null;
	}
	
	@Test
	public void testConstructor() {
		assertNotNull(model.getZeroProbs());
		assertNotNull(model.getRescorer());
		assertNotNull(model.getTagSet());
		assertNotNull(model.getNamedEntityTypes());
		assertNotNull(model.getExtractedTrainingData());
		assertNotNull(model.getGISModelPrevs());
		assertNotNull(model.getNGram());
	}
	
	
	@Test
	public void testLoadChemPapers() {
		assertFalse(
			model.getExtractedTrainingData().getNonChemicalWords().contains(
				"elongate"
			)
		);
		assertTrue(
			model.getExtractedTrainingData().getNonChemicalWords().contains(
				"leukaemic"
			)
		);
	}

	@Test
	public void testLoadChemNameDictNames() {
		assertTrue(model.getChemNameDictNames().contains("3-(methylthio)propionate"));
		assertFalse(model.getChemNameDictNames().contains("cricket"));
	}
}
