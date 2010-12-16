package uk.ac.cam.ch.wwmm.oscarMEMM.memm.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class MutableMEMMModelTest {

    @Test
	public void testConstructor() {
		MutableMEMMModel model = new MutableMEMMModel();
		assertNotNull(model.getZeroProbs());
		assertNull(model.getUberModel());
		assertNull(model.getRescorer());
		assertNotNull(model.getTagSet());
		assertNotNull(model.getNamedEntityTypes());
		assertNull(model.getManualAnnotations());
		assertNotNull(model.getGISModelPrevs());
		assertNotNull(model.getNGram());
	}
}
