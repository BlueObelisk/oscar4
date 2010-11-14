package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import org.junit.Assert;
import org.junit.Test;

public class MEMMTrainerTest {

	@Test
	public void testConstructor() throws Exception {
		MEMMTrainer trainer = new MEMMTrainer();
		Assert.assertNotNull(trainer);
	}
}
