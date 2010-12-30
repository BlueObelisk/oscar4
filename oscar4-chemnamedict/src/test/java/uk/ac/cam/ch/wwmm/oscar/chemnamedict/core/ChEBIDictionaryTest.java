package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IInChIProvider;

public class ChEBIDictionaryTest extends AbstractDictionaryTest {

	@Test
	public void testACompound() throws Exception {
		IInChIProvider dict = new ChEBIDictionary();
		Assert.assertNotNull(dict);
		Assert.assertEquals(
			"InChI=1/CH4/h1H4",
			dict.getInChI("methane").iterator().next()
		);
	}
	

	@Test
	public void testCompoundFromSecondFile() throws Exception {
		// from chemnamedict.xml
		IInChIProvider dict = new ChEBIDictionary();
		Assert.assertNotNull(dict);
		Assert.assertEquals(
			"InChI=1/H2O4S/c1-5(2,3)4/h(H2,1,2,3,4)/f/h1-2H",
			dict.getInChI("sulfuric acid").iterator().next()
		);
	}

	@Override
	public IChemNameDict getDictionary() {
		return new ChEBIDictionary();
	}
}
