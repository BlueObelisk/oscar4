package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.ImmutableChemNameDict;

public class ImmutableChemNameDictTest {

	@Test
	public void testConstructor() throws Exception {
		Assert.assertNotNull(
			new ImmutableChemNameDict(new URI("http://example.com/"))
		);
	}
	
	@Test
	public void testStopWords() throws Exception {
		IChemNameDict dictionary = new ImmutableChemNameDict(new URI("http://example.com/"));
		Assert.assertFalse(dictionary.hasStopWord("Uppsala"));
	}
}
