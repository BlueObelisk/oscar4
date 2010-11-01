package uk.ac.cam.ch.wwmm.oscar.tools;

import java.io.InputStream;

import nu.xom.Document;

import org.junit.Assert;
import org.junit.Test;

public class ResourceGetterTest {

	@Test
	public void testLoadFromResourceGetterClasspath() throws Exception {
		Document document = new ResourceGetter(
			"uk/ac/cam/ch/wwmm/oscar/chemnamedict/")
			.getXMLDocument("defaultCompounds.xml");
		Assert.assertNotNull(document);
	}

}
