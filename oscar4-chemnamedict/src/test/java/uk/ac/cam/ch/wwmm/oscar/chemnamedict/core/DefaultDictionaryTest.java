package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IInChIProvider;

public class DefaultDictionaryTest {

	@Test
	public void testACompound() throws Exception {
		IInChIProvider dict = new DefaultDictionary();
		Assert.assertNotNull(dict);
		// from defaultCompounds.xml
		Assert.assertEquals(
			"InChI=1/C4H6O3/c1-3(5)7-4(2)6/h1-2H3",
			dict.getInChI("Ac2O").iterator().next()
		);
	}
}
