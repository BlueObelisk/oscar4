package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.model.MaxentModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.data.MEMMModel;

/**The main class for generating and running MEMMs
 *
 * @author ptc24
 *
 */
public final class MEMM {

    private MEMMModel model;

    //TODO something about these manual code switches
    private boolean useUber = false;
    private boolean removeBlocked = false;
    private boolean filtering=true;

    private double confidenceThreshold;

    private static final Logger LOG = LoggerFactory.getLogger(MEMM.class);

    public MEMM(MEMMModel model, double confidenceThreshold) {
    	this.model = model;
        this.confidenceThreshold = confidenceThreshold;
    }

    Set<BioType> getTagSet() {
        return model.getTagSet();
    }

    Set<NamedEntityType> getNamedEntityTypes() {
        return model.getNamedEntityTypes();
    }

    private Map<BioType, Double> runGIS(MaxentModel gm, FeatureList featureList) {
        Map<BioType, Double> results = new HashMap<BioType, Double>();
        results.putAll(model.getZeroProbs());
        String[] features = featureList.toArray();
        double [] gisResults = gm.eval(features);
        for (int i = 0; i < gisResults.length; i++) {
            results.put(
            	BioType.fromString(gm.getOutcome(i)),
            	gisResults[i]
            );
        }
        return results;
    }


    /**
     * Finds the named entities in a token sequence.
     *
     * @param tokSeq The token sequence.
     * @return Named entities, with confidences.
     */
    public List<NamedEntity> findNEs(TokenSequence tokSeq) {
        List<FeatureList> featureLists = FeatureExtractor.extractFeatures(tokSeq, model);
        List<Token> tokens = tokSeq.getTokens();
        if (tokens.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<BioType,Map<BioType,Double>>> classifierResults = new ArrayList<Map<BioType,Map<BioType,Double>>>();
        for (int i = 0; i < tokens.size(); i++) {
            FeatureList featuresForToken = featureLists.get(i);
            classifierResults.add(classifyToken(featuresForToken));
        }

        EntityTokeniser lattice = new EntityTokeniser(model, tokSeq, classifierResults);
        List<NamedEntity> namedEntities = lattice.getEntities(confidenceThreshold);
        PostProcessor pp = new PostProcessor(tokSeq, namedEntities, model.getExtractedTrainingData());
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

    private Map<BioType,Map<BioType,Double>> classifyToken(FeatureList features) {
        Map<BioType,Map<BioType,Double>> results = new HashMap<BioType,Map<BioType,Double>>();
        if (useUber) {
            for (BioType prevTag : model.getTagSet()) {
                FeatureList newFeatures = new FeatureList(features);
                newFeatures.addFeature("$$prevTag=" + prevTag);
                results.put(prevTag, runGIS(model.getUberModel(), newFeatures));
            }
        } else {
            for (BioType tag : model.getTagSet()) {
                MaxentModel gm = model.getMaxentModelByPrev(tag);
                if (gm != null) {
                    Map<BioType, Double> modelResults = runGIS(gm, features);
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
    	if (model.getRescorer() != null) {
    		model.getRescorer().rescore(entities, model.getChemNameDictNames());	
    	} 
    	else {
    		LOG.info("Model does not contain a rescorer");
    	}
    }

    public MEMMModel getModel() {
    	return model;
    }
    
    public void setModel(MEMMModel model) {
    	this.model = model;
    }

}
