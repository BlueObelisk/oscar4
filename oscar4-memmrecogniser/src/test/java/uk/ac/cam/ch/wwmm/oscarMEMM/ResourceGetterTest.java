package uk.ac.cam.ch.wwmm.oscarMEMM;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

/**
 * This test is here to test that we can properly load
 * resources from this module, using the {@link ResourceGetter}
 * in the oscar4-core module.
 */
public class ResourceGetterTest {

	@Test
	public void testLoadFromResourceGetterClasspath() throws Exception {
		InputStream stream = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscarMEMM/tools/"
			).getStream("DefaultProperties.dat");
		Assert.assertNotNull(stream);
	}

	@Test
	public void testLoadFromLocalClasspath() throws Exception {
		InputStream stream = new ResourceGetter(
				this.getClass().getClassLoader(),
				"uk/ac/cam/ch/wwmm/oscarMEMM/models/"
			).getStream("chempapers.xml");
		Assert.assertNotNull(stream);
	}
}
