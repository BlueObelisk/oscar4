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
		String xml = trainer.writeModel().toXML();
		trainer.trainOnStream(stream, "what to enter here??");
		trainer.finishTraining();
		Assert.assertNotSame(xml, trainer.writeModel().toXML());
		// it must have learned something
	}
}
