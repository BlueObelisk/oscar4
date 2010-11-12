package uk.ac.cam.ch.wwmm.oscar.obo.dso;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.unibe.jexample.Given;
import ch.unibe.jexample.JExample;

import uk.ac.cam.ch.wwmm.oscar.obo.OBOOntology;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

@RunWith(JExample.class)
public class DSOtoOBOTest {

	@Test
	public void testLoadingDSOFile() throws Exception {
		ResourceGetter rg = new ResourceGetter(
			this.getClass().getClassLoader(),
			"uk/ac/cam/ch/wwmm/oscar/obo/terms/"
		);
		Assert.assertNotNull(rg.getStream("ptcontology.dso"));
	}
	
	@Given("testLoadingDSOFile")
	public void testReadDSO() throws Exception {
		OBOOntology obo = DSOtoOBO.readDSO();
		Assert.assertNotNull(obo);
		Assert.assertNotSame(0, obo.getTerms().size());
	}
}
