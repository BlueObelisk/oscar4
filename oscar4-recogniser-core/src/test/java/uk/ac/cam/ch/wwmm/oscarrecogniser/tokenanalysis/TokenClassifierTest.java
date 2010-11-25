package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;

import org.junit.Assert;
import org.junit.Test;


public class TokenClassifierTest {

	@Test
	public void testConstructor() {
		Assert.assertNotNull(TokenClassifier.getInstance());
	}

}
