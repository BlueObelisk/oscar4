package uk.ac.cam.ch.wwmm.oscarMEMM.tokenAnalysis;

import org.junit.Assert;
import org.junit.Test;

public class TLRHolderTest {

	@Test
	public void testConstructor() {
		Assert.assertNotNull(TLRHolder.getInstance());
	}

}
