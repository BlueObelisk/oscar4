package uk.ac.cam.ch.wwmm.oscarMEMM;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sea36
 */
public class FeatureSet {

    private List<String> features = new ArrayList<String>();
	private List<String> contextableFeatures = new ArrayList<String>();
	private List<String> bigramableFeatures = new ArrayList<String>();

    public List<String> getFeatures() {
        return features;
    }

    public List<String> getContextableFeatures() {
        return contextableFeatures;
    }

    public List<String> getBigramableFeatures() {
        return bigramableFeatures;
    }
    
}
