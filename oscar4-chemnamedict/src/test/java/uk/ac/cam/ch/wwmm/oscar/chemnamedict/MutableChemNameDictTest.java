package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.MutableChemNameDict;

public class MutableChemNameDictTest {

	@Test
	public void testConstructor() throws Exception {
		Assert.assertNotNull(
			new MutableChemNameDict(new URI("http://example.com/"))
		);
	}
	
	@Test
	public void testStopWords() throws Exception {
		IMutableChemNameDict dictionary = new MutableChemNameDict(new URI("http://example.com/"));
		Assert.assertFalse(dictionary.hasStopWord("Uppsala"));
		dictionary.addStopWord("Uppsala");
		Assert.assertTrue(dictionary.hasStopWord("Uppsala"));
	}
}
