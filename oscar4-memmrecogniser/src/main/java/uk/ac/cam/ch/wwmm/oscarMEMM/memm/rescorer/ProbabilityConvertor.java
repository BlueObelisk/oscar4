package uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer;

/**
 * Interconverts probabilities between various units.
 *
 * @author egonw
 */
public class ProbabilityConvertor {

	public static double probToLogit(double p) {
		return Math.log(p) - Math.log(1-p);
	}
	
	public static double logitToProb(double l) {
		return Math.exp(l) / (Math.exp(l) + 1);
	}
}
