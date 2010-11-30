package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import opennlp.maxent.GISModel;
import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelReader;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelWriter;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer.MEMMOutputRescorer;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.Model;

/**The main class for generating and running MEMMs
 *
 * @author ptc24
 *
 */
public final class MEMM {

    private static MEMM currentInstance;
    private static MEMM defaultInstance;

    private Map<String, Double> zeroProbs;
    private Map<String, GISModel> gmByPrev;
    private GISModel ubermodel;

    private Set<String> tagSet;
    private Set<NamedEntityType> namedEntityTypes;

    private boolean useUber = false;
    private boolean removeBlocked = false;
    private boolean filtering=true;

    private static double confidenceThreshold;

    private MEMMOutputRescorer rescorer;

    public MEMM(Element elem) {
        zeroProbs = new HashMap<String, Double>();
        gmByPrev = new HashMap<String, GISModel>();
        tagSet = new HashSet<String>();

        confidenceThreshold = OscarProperties.getData().neThreshold / 5.0;
        rescorer = null;

        try {
            readModel(elem);
        } catch (Exception e) {
			throw new Error(e);
		}
    }

    Set<String> getTagSet() {
        return tagSet;
    }

    Set<NamedEntityType> getNamedEntityTypes() {
        return namedEntityTypes;
    }

    private void makeEntityTypesAndZeroProbs() {
        namedEntityTypes = new HashSet<NamedEntityType>();
        for (String tagType : tagSet) {
            if (tagType.startsWith("B-") || tagType.startsWith("W-")) {
                namedEntityTypes.add(NamedEntityType.valueOf(tagType.substring(2)));
            }
        }
        for(String tag : tagSet) {
            zeroProbs.put(tag, 0.0);
        }
    }

    private Map<String, Double> runGIS(GISModel gm, String [] context) {
        Map<String, Double> results = new HashMap<String, Double>();
        results.putAll(zeroProbs);
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
            for (String prevTag : tagSet) {
                List<String> newFeatures = new ArrayList<String>(features);
                newFeatures.add("$$prevTag=" + prevTag);
                results.put(prevTag, runGIS(ubermodel, newFeatures.toArray(new String[newFeatures.size()])));
            }
        } else {
            String [] featArray = features.toArray(new String[features.size()]);
            for (String tag : tagSet) {
                GISModel gm = gmByPrev.get(tag);
                if (gm != null) {
                    Map<String, Double> modelResults = runGIS(gm, featArray);
                    results.put(tag, modelResults);
                }
            }
        }
        return results;
    }

    /**
     * Reads in an XML document containing a MEMM model.
     *
     * @param doc The XML document.
     * @throws Exception
     */
    public void readModel(Document doc) throws Exception {
        readModel(doc.getRootElement());
    }

    /**
     * Reads in a MEMM model from an XML element.
     *
     * @param memmRoot The XML element.
     * @throws Exception
     */
    public void readModel(Element memmRoot) throws Exception {
        Elements maxents = memmRoot.getChildElements("maxent");
        gmByPrev = new HashMap<String,GISModel>();
        tagSet = new HashSet<String>();
        for (int i = 0; i < maxents.size(); i++) {
            Element maxent = maxents.get(i);
            String prev = maxent.getAttributeValue("prev");
            StringGISModelReader sgmr = new StringGISModelReader(maxent.getValue());
            GISModel gm = sgmr.getModel();
            gmByPrev.put(prev, gm);
            tagSet.add(prev);
            for (int j = 0; j < gm.getNumOutcomes(); j++) {
                tagSet.add(gm.getOutcome(j));
            }
        }
        Element rescorerElem = memmRoot.getFirstChildElement("rescorer");
        if(rescorerElem != null) {
            rescorer = new MEMMOutputRescorer();
            rescorer.readElement(rescorerElem);
        } else {
            rescorer = null;
        }
        makeEntityTypesAndZeroProbs();
    }


    /**
     * Produces an XML element containing the current MEMM model.
     *
     * @return The XML element.
     * @throws Exception
     */
    public Element writeModel() throws Exception {
        Element root = new Element("memm");
        for (String prev : gmByPrev.keySet()) {
            Element maxent = new Element("maxent");
            maxent.addAttribute(new Attribute("prev", prev));
            StringGISModelWriter sgmw = new StringGISModelWriter(gmByPrev.get(prev));
            sgmw.persist();
            maxent.appendChild(sgmw.toString());
            root.appendChild(maxent);
        }
        if(rescorer != null) {
            root.appendChild(rescorer.writeElement());
        }
        return root;
    }

    /**
     * Uses this MEMM's rescorer to rescore a list of named entities. This
     * updates the confidence values held within the NEs.
     *
     * @param entities The entities to rescore.
     */
    public void rescore(List<NamedEntity> entities) {
        rescorer.rescore(entities);
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

    public static void load(Element elem) {
        currentInstance = new MEMM(elem);
    }

}
