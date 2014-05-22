package uk.ac.cam.ch.wwmm.oscar.chemnamedict.tools;


import nu.xom.Document;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

public class ResourceGetterTest {

	@Test
	public void testLoadFromResourceGetterClasspath() throws Exception {
		Document document = new ResourceGetter(
			"uk/ac/cam/ch/wwmm/oscar/chemnamedict/")
			.getXMLDocument("defaultCompounds.xml");
		Assert.assertNotNull(document);
	}

}
