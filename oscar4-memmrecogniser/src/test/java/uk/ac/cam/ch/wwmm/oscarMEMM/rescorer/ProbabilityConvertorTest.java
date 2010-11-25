package uk.ac.cam.ch.wwmm.oscarMEMM.rescorer;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer.ProbabilityConvertor;

/**
 * @author egonw
 */
public class ProbabilityConvertorTest {

	@Test
	public void testRoundTrip() {
		double prob = 0.1;
		Assert.assertEquals(
			prob,
			ProbabilityConvertor.logitToProb(
				ProbabilityConvertor.probToLogit(prob)
			),
			0.0000001
		);
	}

}
