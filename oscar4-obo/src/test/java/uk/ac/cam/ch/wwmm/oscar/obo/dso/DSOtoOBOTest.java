package uk.ac.cam.ch.wwmm.oscar.obo.dso;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.obo.OBOOntology;

public class DSOtoOBOTest {

	@Test
	public void testReadDSO() throws Exception {
		OBOOntology obo = DSOtoOBO.readDSO();
		Assert.assertNotNull(obo);
		Assert.assertNotSame(0, obo.getTerms().size());
	}
}
