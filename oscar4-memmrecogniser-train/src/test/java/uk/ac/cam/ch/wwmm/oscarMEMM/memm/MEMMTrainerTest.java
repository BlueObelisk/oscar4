package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

public class MEMMTrainerTest {

	@Test
	public void testConstructor() throws Exception {
		MEMMTrainer trainer = new MEMMTrainer();
		Assert.assertNotNull(trainer);
	}

	@Test
	public void testLearning() throws Exception {
		MEMMTrainer trainer = new MEMMTrainer();
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream(
			"uk/ac/cam/ch/wwmm/oscarMEMM/memm/paper.xml"
		);
		Assert.assertNotNull(stream);
		trainer.trainOnStream(stream, "what to enter here??");
		// FIXME: how to test that it learned something??
	}
}
