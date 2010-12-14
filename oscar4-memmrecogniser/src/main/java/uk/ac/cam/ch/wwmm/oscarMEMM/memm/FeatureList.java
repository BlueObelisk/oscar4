package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Class provides a type-safe list of features (represented using strings)
 * @author Sam Adams
 */
public class FeatureList implements Iterable<String> {

    private List<String> features;

    public FeatureList() {
        this.features = new ArrayList<String>();
    }

    public FeatureList(FeatureList featureList) {
        this.features = new ArrayList<String>(featureList.features);
    }


    public int getFeatureCount() {
        return this.features.size();
    }

    public String getFeature(int i) {
        return this.features.get(i);
    }

    public void addFeature(String feature) {
        this.features.add(feature);
    }


    public void removeFeatures(Collection<String> features) {
        this.features.removeAll(features);
    }


    public Iterator<String> iterator() {
        return this.features.iterator();
    }

    public String[] toArray() {
        String[] a = new String[this.features.size()];
        return this.features.toArray(a);
    }

    public List<String> toList() {
        return new ArrayList<String>(features);
    }

}
