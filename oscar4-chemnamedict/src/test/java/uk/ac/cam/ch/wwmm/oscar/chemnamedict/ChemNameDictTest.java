package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import org.junit.Assert;
import org.junit.Test;

public class ChemNameDictTest {

	@Test
	public void testConstructor() {
		Assert.assertNotNull(new ChemNameDict());
	}
	
	@Test
	public void testStopWords() throws Exception {
		IChemNameDict dictionary = new ChemNameDict();
		Assert.assertFalse(dictionary.hasStopWord("Uppsala"));
		dictionary.addStopWord("Uppsala");
		Assert.assertTrue(dictionary.hasStopWord("Uppsala"));
	}
}
