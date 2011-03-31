package uk.ac.cam.ch.wwmm.oscarMEMM.memm.data;

import static org.junit.Assert.*;

import java.util.Collections;

import org.apache.commons.collections.set.UnmodifiableSet;
import org.junit.Test;

public class MutableMEMMModelTest {

    @Test
	public void testConstructor() {
		MutableMEMMModel model = new MutableMEMMModel(
				(UnmodifiableSet) UnmodifiableSet.decorate(Collections.emptySet()));
		assertNotNull(model.getZeroProbs());
		assertNull(model.getUberModel());
		assertNull(model.getRescorer());
		assertNotNull(model.getTagSet());
		assertNotNull(model.getNamedEntityTypes());
		assertNull(model.getExtractedTrainingData());
		assertNotNull(model.getGISModelPrevs());
		assertNotNull(model.getNGram());
		assertNotNull(model.getChemNameDictNames());
	}
}
