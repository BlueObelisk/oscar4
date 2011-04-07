package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.net.URI;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IMutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.MutableChemNameDict;

public class MutableChemNameDictTest {

	@Test
	public void testConstructor() throws Exception {
		Assert.assertNotNull(
			new MutableChemNameDict(
				new URI("http://example.com/"),
				Locale.ENGLISH
			)
		);
	}

	@Test
	public void testHasName() throws Exception {
		IMutableChemNameDict dict = new MutableChemNameDict(
			new URI("http://example.com/"),
			Locale.ENGLISH
		);
		Assert.assertFalse(dict.hasName("acetic acid"));
		dict.addName("acetic acid");
		Assert.assertTrue(dict.hasName("acetic acid"));
	}

	@Test
	public void testAddWithoutInChIOrSMILES() throws Exception {
		IMutableChemNameDict dict = new MutableChemNameDict(
			new URI("http://example.com/"),
			Locale.ENGLISH
		);
		Assert.assertFalse(dict.hasName("acetic acid"));
		dict.addChemical("acetic acid", null, null);
		Assert.assertTrue(dict.hasName("acetic acid"));
	}
}
