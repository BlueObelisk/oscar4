package uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IInChIProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries.DefaultDictionary;

public class DefaultDictionaryTest extends AbstractDictionaryTest {

	@Test
	public void testACompound() throws Exception {
		IInChIProvider dict = new DefaultDictionary();
		assertNotNull(dict);
		// from defaultCompounds.xml
		assertEquals(
			"InChI=1/C4H6O3/c1-3(5)7-4(2)6/h1-2H3",
			dict.getInchis("Ac2O").iterator().next()
		);
	}

	@Override
	public IChemNameDict getDictionary() {
		return new DefaultDictionary();
	}
}
