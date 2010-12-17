package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.maxent.MaxentModel;
import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.data.MEMMModel;

/**The main class for generating and running MEMMs
 *
 * @author ptc24
 *
 */
public final class MEMM {

    private MEMMModel model;

    private boolean useUber = false;
    private boolean removeBlocked = false;
    private boolean filtering=true;

    private static double confidenceThreshold;

    Logger LOG = Logger.getLogger(MEMM.class);
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

    private Map<String, Double> runGIS(MaxentModel gm, FeatureList featureList) {
        Map<String, Double> results = new HashMap<String, Double>();
        results.putAll(model.getZeroProbs());
        String[] features = featureList.toArray();
        double [] gisResults = gm.eval(features);
        for (int i = 0; i < gisResults.length; i++) {
            results.put(gm.getOutcome(i), gisResults[i]);
        }
        return results;
    }


    /**
     * Finds the named entities in a token sequence.
     *
     * @param tokSeq The token sequence.
     * @return Named entities, with confidences.
     */
    public List<NamedEntity> findNEs(ITokenSequence tokSeq) {
        List<FeatureList> featureLists = FeatureExtractor.extractFeatures(tokSeq, model.getNGram(), model.getManualAnnotations());
        List<IToken> tokens = tokSeq.getTokens();
        if (tokens.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String,Map<String,Double>>> classifierResults = new ArrayList<Map<String,Map<String,Double>>>();
        for (int i = 0; i < tokens.size(); i++) {
            FeatureList featuresForToken = featureLists.get(i);
            classifierResults.add(classifyToken(featuresForToken));
        }

        EntityTokeniser lattice = new EntityTokeniser(model, tokSeq, classifierResults);
        List<NamedEntity> namedEntities = lattice.getEntities(confidenceThreshold);
        PostProcessor pp = new PostProcessor(tokSeq, namedEntities, model.getManualAnnotations());
        if (filtering) {
            pp.filterEntities();
        }
        pp.getBlocked();
        if (removeBlocked) {
            pp.removeBlocked();
        }
        namedEntities = pp.getEntities();

        return namedEntities;
    }

    private Map<String,Map<String,Double>> classifyToken(FeatureList features) {
        Map<String,Map<String,Double>> results = new HashMap<String,Map<String,Double>>();
        if (useUber) {
            for (String prevTag : model.getTagSet()) {
                FeatureList newFeatures = new FeatureList(features);
                newFeatures.addFeature("$$prevTag=" + prevTag);
                results.put(prevTag, runGIS(model.getUberModel(), newFeatures));
            }
        } else {
            for (String tag : model.getTagSet()) {
                MaxentModel gm = model.getMaxentModelByPrev(tag);
                if (gm != null) {
                    Map<String, Double> modelResults = runGIS(gm, features);
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
    	if (model.getRescorer()!=null)
        model.getRescorer().rescore(entities);
    	else LOG.info("Model does not contain a rescorer");
    }

    public MEMMModel getModel() {
    	return model;
    }
    
    public void setModel(MEMMModel model) {
    	this.model = model;
    }

}
