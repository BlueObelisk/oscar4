package uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IInChIProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries.ChEBIDictionary;

public class ChEBIDictionaryTest extends AbstractDictionaryTest {

	@Test
	public void testACompound() throws Exception {
		IInChIProvider dict = ChEBIDictionary.getInstance();
		assertNotNull(dict);
		assertEquals(
			"InChI=1/CH4/h1H4",
			dict.getInchis("methane").iterator().next()
		);
	}
	

	@Test
	public void testCompoundFromSecondFile() throws Exception {
		// from chemnamedict.xml
		IInChIProvider dict = ChEBIDictionary.getInstance();
		assertNotNull(dict);
		assertEquals(
			"InChI=1/H2O4S/c1-5(2,3)4/h(H2,1,2,3,4)/f/h1-2H",
			dict.getInchis("sulfuric acid").iterator().next()
		);
	}

	@Override
	public IChemNameDict getDictionary() {
		return ChEBIDictionary.getInstance();
	}
}
