package uk.ac.cam.ch.wwmm.oscartokeniser;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscartokeniser.TokenClassifier;


public class TokenClassifierTest {

	@Test
	public void testConstructor() {
		Assert.assertNotNull(TokenClassifier.getInstance());
	}

}
