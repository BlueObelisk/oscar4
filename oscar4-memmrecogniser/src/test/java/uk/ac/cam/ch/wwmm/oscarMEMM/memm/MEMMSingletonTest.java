package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import org.junit.Assert;
import org.junit.Test;

public class MEMMSingletonTest {

	@Test
	public void testGetInstance() throws Exception {
		MEMM memm = MEMMSingleton.getInstance();
		Assert.assertNotNull("MEMM was null", memm);
		MEMMSingleton.clear();
		memm = MEMMSingleton.getInstance();
		Assert.assertNotNull("MEMM was null", memm);
	}
}
