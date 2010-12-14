package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.maxent.GISModel;

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
     * @return Named entities, with confidences.
     */
    public List<NamedEntity> findNEs(ITokenSequence tokSeq) {
        List<List<String>> featureLists = FeatureExtractor.extractFeatures(tokSeq);
        List<IToken> tokens = tokSeq.getTokens();
        if (tokens.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String,Map<String,Double>>> classifierResults = new ArrayList<Map<String,Map<String,Double>>>();
        for (int i = 0; i < tokens.size(); i++) {
            List<String> featuresForToken = featureLists.get(i);
            classifierResults.add(classifyToken(featuresForToken));
        }

        EntityTokeniser lattice = new EntityTokeniser(model, tokSeq, classifierResults);
        List<NamedEntity> namedEntities = lattice.getEntities(confidenceThreshold);
        PostProcessor pp = new PostProcessor(tokSeq, namedEntities);
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

    private Map<String,Map<String,Double>> classifyToken(List<String> features) {
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
