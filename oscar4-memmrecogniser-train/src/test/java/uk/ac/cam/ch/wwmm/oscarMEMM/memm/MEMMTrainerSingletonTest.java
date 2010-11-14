package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import org.junit.Assert;
import org.junit.Test;

public class MEMMTrainerSingletonTest {

	@Test
	public void testGetInstance() throws Exception {
		MEMMTrainer trainer = MEMMTrainerSingleton.getInstance();
		Assert.assertNotNull("MEMMTrainer was null", trainer);
		MEMMTrainerSingleton.clear();
		trainer = MEMMTrainerSingleton.getInstance();
		Assert.assertNotNull("MEMMTrainer was null", trainer);
	}
}
