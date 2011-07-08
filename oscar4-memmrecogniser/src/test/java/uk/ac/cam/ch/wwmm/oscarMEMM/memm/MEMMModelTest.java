package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import static org.junit.Assert.*;

import java.util.Set;

import nu.xom.Document;
import nu.xom.Element;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMModel;

public class MEMMModelTest {

	@Test
	public void testConstructor() {
		MEMMModel model = new MEMMModel();
		// this test ensures that no Exceptions and Errors are thrown
		assertNotNull(model);
		// and that the fields are initialised correctly
		assertNotNull(model.getZeroProbs());
		assertNull(model.getRescorer());
		assertNotNull(model.getTagSet());
		assertNotNull(model.getNamedEntityTypes());
		assertNull(model.getExtractedTrainingData());
		assertNotNull(model.getGISModelPrevs());
		assertNull(model.getNGram());
	}

	@Test
	public void testGetTagSet() {
		Set<BioType> set = new MEMMModel().getTagSet();
		assertNotNull(set);
		// a new instance should have no data
		assertEquals(0, set.size());
	}

	@Test
	public void testGetNamedEntityTypes() {
		Set<NamedEntityType> set = new MEMMModel().getNamedEntityTypes();
		assertNotNull(set);
		// a new instance should have no data
		assertEquals(0, set.size());
	}

	@Test
	public void testReadModel() throws Exception {
		Document modelDoc = new ResourceGetter(
			MEMMModel.class.getClassLoader(),
			"uk/ac/cam/ch/wwmm/oscarMEMM/models/"
		).getXMLDocument("chempapers.xml");
		Element modelRoot = modelDoc.getRootElement();
		assertNotNull(modelRoot);
		MEMMModel model = new MEMMModel();
		model.readModel(modelRoot);
		assertNotSame(0, model.getNamedEntityTypes().size());
		assertNotNull(model.getRescorer());
		assertTrue(
			model.getExtractedTrainingData().getNonChemicalWords().contains(
				"elongate"
			)
		);
		assertFalse(
			model.getExtractedTrainingData().getNonChemicalWords().contains(
				"leukaemic"
			)
		);
	}

	@Test
	public void testWriteModel() throws Exception {
		Document modelDoc = new ResourceGetter(
			MEMMModel.class.getClassLoader(),
			"uk/ac/cam/ch/wwmm/oscarMEMM/models/"
		).getXMLDocument("chempapers.xml");
		MEMMModel model = new MEMMModel();
		model.readModel(modelDoc.getRootElement());

		Element writtenModel = model.writeModel();
		assertEquals("model", writtenModel.getLocalName());
		assertEquals(1, writtenModel.getChildElements("memm").size());
		assertEquals(1, writtenModel.getChildElements("etd").size());
	}
}
