package uk.ac.cam.ch.wwmm.oscar.opsin;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

public class OpsinDictionaryTest {

	@Test
	public void testMethane() throws URISyntaxException {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertNotNull(dict);
		Assert.assertEquals(
			"InChI=1/CH4/h1H4",
			dict.getInChI("methane").iterator().next()
		);
	}

}
