package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

public class ChemNameDictTest {

	@Test
	public void testConstructor() throws Exception {
		Assert.assertNotNull(
			new ChemNameDict(new URI("http://example.com/"))
		);
	}

}
