package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IInChIProvider;

public class ChEBIDictionaryTest {

	@Test
	public void testACompound() throws Exception {
		IInChIProvider dict = new ChEBIDictionary();
		Assert.assertNotNull(dict);
		Assert.assertEquals(
			"InChI=1/CH4/h1H4",
			dict.getInChI("methane").iterator().next()
		);
	}
	
}
