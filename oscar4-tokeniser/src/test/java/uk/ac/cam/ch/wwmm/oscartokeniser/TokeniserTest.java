package uk.ac.cam.ch.wwmm.oscartokeniser;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author egonw
 */
public final class TokeniserTest {

	@Test public void testConstructor() {
		Assert.assertNotNull(new Tokeniser());
	}

	@Test public void testGettingInstance()
	throws InstantiationException, IllegalAccessException,
	       ClassNotFoundException {
		Assert.assertNotNull(
			getClass().getClassLoader().loadClass(
				"uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser"
			).newInstance()
		);
	}
}
