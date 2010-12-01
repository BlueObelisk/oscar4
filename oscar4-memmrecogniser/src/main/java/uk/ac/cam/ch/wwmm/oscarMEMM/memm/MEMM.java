package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.maxent.GISModel;
import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.Model;

/**The main class for generating and running MEMMs
 *
 * @author ptc24
 *
 */
public final class MEMM {

    private static MEMM currentInstance;
    private static MEMM defaultInstance;

    private MEMMModel model;

    private boolean useUber = false;
    private boolean removeBlocked = false;
    private boolean filtering=true;

    private static double confidenceThreshold;

    public MEMM(MEMMModel model) {
    	this.model = model;
        confidenceThreshold = OscarProperties.getData().neThreshold / 5.0;
    }

    Set<String> getTagSet() {
        return model.getTagSet();
    }

    Set<NamedEntityType> getNamedEntityTypes() {
        return model.getNamedEntityTypes();
    }

    private Map<String, Double> runGIS(GISModel gm, String [] context) {
        Map<String, Double> results = new HashMap<String, Double>();
        results.putAll(model.getZeroProbs());
        double [] gisResults = gm.eval(context);
        for (int i = 0; i < gisResults.length; i++) {
            results.put(gm.getOutcome(i), gisResults[i]);
        }
        return results;
    }


    /**
     * Finds the named entities in a token sequence.
     *
     * @param tokSeq The token sequence.
     * @param domain A string to represent the domain (experimental, should
     * usually be null).
     * @return Named entities, with confidences.
     */
    public Map<NamedEntity,Double> findNEs(ITokenSequence tokSeq, String domain) {
        List<List<String>> featureLists = FeatureExtractor.extractFeatures(tokSeq);
        List<IToken> tokens = tokSeq.getTokens();
        if (tokens.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Map<String,Map<String,Double>>> classifierResults = new ArrayList<Map<String,Map<String,Double>>>();
        for (int i = 0; i < tokens.size(); i++) {
            List<String> featuresForToken = featureLists.get(i);
            classifierResults.add(calcResults(featuresForToken));
        }

        EntityTokeniser lattice = new EntityTokeniser(this, tokSeq, classifierResults);
        Map<NamedEntity,Double> neConfidences = lattice.getEntities(confidenceThreshold);
        PostProcessor pp = new PostProcessor(tokSeq, neConfidences);
        if (filtering) {
            pp.filterEntities();
        }
        pp.getBlocked();
        if (removeBlocked) {
            pp.removeBlocked();
        }
        neConfidences = pp.getEntities();

        return neConfidences;
    }

    private Map<String,Map<String,Double>> calcResults(List<String> features) {
        Map<String,Map<String,Double>> results = new HashMap<String,Map<String,Double>>();
        if (useUber) {
            for (String prevTag : model.getTagSet()) {
                List<String> newFeatures = new ArrayList<String>(features);
                newFeatures.add("$$prevTag=" + prevTag);
                results.put(prevTag, runGIS(
                	model.getUberModel(),
                	newFeatures.toArray(new String[newFeatures.size()])
                ));
            }
        } else {
            String [] featArray = features.toArray(new String[features.size()]);
            for (String tag : model.getTagSet()) {
                GISModel gm = model.getGISModelByPrev(tag);
                if (gm != null) {
                    Map<String, Double> modelResults = runGIS(gm, featArray);
                    results.put(tag, modelResults);
                }
            }
        }
        return results;
    }

    /**
     * Uses this MEMM's rescorer to rescore a list of named entities. This
     * updates the confidence values held within the NEs.
     *
     * @param entities The entities to rescore.
     */
    public void rescore(List<NamedEntity> entities) {
        model.getRescorer().rescore(entities);
    }

    public MEMMModel getModel() {
    	return model;
    }
    
    public void setModel(MEMMModel model) {
    	this.model = model;
    }

    public static MEMM getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = Model.getDefaultInstance().getMemm();
        }
        return defaultInstance;
    }

    public static MEMM getInstance() {
        if (currentInstance == null) {
            currentInstance = getDefaultInstance();
        }
        return currentInstance;
    }

}
