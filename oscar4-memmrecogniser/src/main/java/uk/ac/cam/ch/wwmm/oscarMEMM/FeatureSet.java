package uk.ac.cam.ch.wwmm.oscarMEMM;

import uk.ac.cam.ch.wwmm.oscarMEMM.memm.FeatureList;

/**
 * @author sea36
 */
public class FeatureSet {

    private FeatureList features = new FeatureList();
	private FeatureList contextableFeatures = new FeatureList();
	private FeatureList bigramableFeatures = new FeatureList();

    public FeatureList getFeatures() {
        return features;
    }

    public FeatureList getContextableFeatures() {
        return contextableFeatures;
    }

    public FeatureList getBigramableFeatures() {
        return bigramableFeatures;
    }
    
}
