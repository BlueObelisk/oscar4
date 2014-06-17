package uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IStdInChIProvider;

public class ChEBIDictionaryTest extends AbstractDictionaryTest {

	@Test
	public void testACompound() throws Exception {
		IStdInChIProvider dict = ChEBIDictionary.getInstance();
		assertNotNull(dict);
		assertEquals(
			"InChI=1S/CH4/h1H4",
			dict.getStdInchis("methane").iterator().next()
		);
	}
	

	@Test
	public void testCompoundFromSecondFile() throws Exception {
		// from chemnamedict.xml
		IStdInChIProvider dict = ChEBIDictionary.getInstance();
		assertNotNull(dict);
		assertEquals(
			"InChI=1S/H2O4S/c1-5(2,3)4/h(H2,1,2,3,4)",
			dict.getStdInchis("sulfuric acid").iterator().next()
		);
	}

	@Override
	public IChemNameDict getDictionary() {
		return ChEBIDictionary.getInstance();
	}
}
