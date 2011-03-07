package uk.ac.cam.ch.wwmm.oscar.obo;

import org.junit.Assert;
import org.junit.Test;

public class OBOOntologyTest {

	@Test
	public void testGetInstance() {
		OBOOntology obo = OBOOntology.getInstance(false);
		Assert.assertNotNull(obo);
		Assert.assertNotSame(0, obo.getTerms().size());
	}

}
