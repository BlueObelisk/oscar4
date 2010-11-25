package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;

import org.junit.Assert;
import org.junit.Test;


public class TLRHolderTest {

	@Test
	public void testConstructor() {
		Assert.assertNotNull(TokenLevelRegexHolder.getInstance());
	}

}
