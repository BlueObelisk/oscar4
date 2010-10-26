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

	@Test
	public void testBenzene() throws URISyntaxException {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertNotNull(dict);
		Assert.assertEquals(
			"InChI=1/C6H6/c1-2-4-6-5-3-1/h1-6H",
			dict.getInChI("benzene").iterator().next()
		);
	}

}
