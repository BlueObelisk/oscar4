package uk.ac.cam.ch.wwmm.oscar.tools;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

public class ResourceGetterTest {

	@Test
	public void testLoadFromResourceGetterClasspath() throws Exception {
		InputStream stream = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar/tools/"
			).getStream("DefaultProperties.dat");
		Assert.assertNotNull(stream);
	}

}
