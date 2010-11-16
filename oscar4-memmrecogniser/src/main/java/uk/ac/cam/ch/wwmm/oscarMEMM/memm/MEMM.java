package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import opennlp.maxent.Event;
import opennlp.maxent.GISModel;
import org.apache.log4j.Logger;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelReader;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelWriter;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer.RescoreMEMMOut;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**The main class for generating and running MEMMs
 *
 * @author ptc24
 *
 */
public final class MEMM {

    private final Logger logger = Logger.getLogger(MEMM.class);

    private Map<String, List<Event>> evsByPrev;
    private Map<String, Double> zeroProbs;
    private Map<String, GISModel> gmByPrev;
    private GISModel ubermodel;
    private int trainingCycles;
    private int featureCutOff;

    private Set<String> tagSet;
    private Set<String> entityTypes;

    private boolean useUber = false;
    private boolean removeBlocked = false;
    private boolean retrain=true;
    private boolean splitTrain=true;
    //public boolean patternFeatures=false;
    private boolean featureSel=true;
    //public boolean tampering=false;
    private boolean simpleRescore=true;
    private boolean filtering=true;
    private boolean nameTypes=false;

    private Map<String,Map<String,Double>> featureCVScores;

    private Map<String,Set<String>> perniciousFeatures;

    private static double confidenceThreshold;

    private RescoreMEMMOut rescorer;

    MEMM() throws Exception {
        evsByPrev = new HashMap<String, List<Event>>();
        zeroProbs = new HashMap<String, Double>();
        gmByPrev = new HashMap<String, GISModel>();
        tagSet = new HashSet<String>();
        perniciousFeatures = null;

        trainingCycles = 100;
        featureCutOff = 1;
        confidenceThreshold = OscarProperties.getData().neThreshold / 5.0;
        rescorer = null;
    }

    Set<String> getTagSet() {
        return tagSet;
    }

    Set<String> getEntityTypes() {
        return entityTypes;
    }

    private void train(List<String> features, String thisTag, String prevTag) {
        if (perniciousFeatures != null && perniciousFeatures.containsKey(prevTag)) {
            features.removeAll(perniciousFeatures.get(prevTag));
        }
        if (features.isEmpty()) {
            features.add("EMPTY");
        }
        tagSet.add(thisTag);
        if (useUber) {
            features.add("$$prevTag=" + prevTag);
        }
        String [] c = features.toArray(new String[0]);
        Event ev = new Event(thisTag, c);
        List<Event> evs = evsByPrev.get(prevTag);
        if(evs == null) {
            evs = new ArrayList<Event>();
            evsByPrev.put(prevTag, evs);
        }
        evs.add(ev);
    }

    private void trainOnSentence(TokenSequence tokSeq, String domain) {
        FeatureExtractor extractor = new FeatureExtractor(tokSeq, domain);
        List<Token> tokens = tokSeq.getTokens();
        String prevTag = "O";
        for (int i = 0; i < tokens.size(); i++) {
            train(extractor.getFeatures(i), tokens.get(i).getBioTag(), prevTag);
            prevTag = tokens.get(i).getBioTag();
        }
    }

    private void trainOnFile(File file, String domain) throws Exception {
        long time = System.currentTimeMillis();
        logger.debug("Train on: " + file + "... ");
        Document doc = new Builder().build(file);
        Nodes n = doc.query("//cmlPile");
        for (int i = 0; i < n.size(); i++) {
            n.get(i).detach();
        }
        n = doc.query("//ne[@type='CPR']");
        for (int i = 0; i < n.size(); i++) {
            XOMTools.removeElementPreservingText((Element)n.get(i));
        }

        if(nameTypes) {
            n = doc.query("//ne");
            for (int i = 0; i < n.size(); i++) {
                Element ne = (Element)n.get(i);
                if (ne.getAttributeValue("type").equals("RN") && ne.getValue().matches("[A-Z]\\p{Ll}\\p{Ll}.*\\s.*")) {
                    ne.addAttribute(new Attribute("type", "NRN"));
                    logger.debug("NRN: " + ne.getValue());
                } else if (ne.getAttributeValue("type").equals("CM") && ne.getValue().matches("[A-Z]\\p{Ll}\\p{Ll}.*\\s.*")) {
                    ne.addAttribute(new Attribute("type", "NCM"));
                    logger.debug("NCM: " + ne.getValue());
                }
            }
        }

        ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
                Tokeniser.getInstance(), doc, true, false, false);

        for(TokenSequence ts : procDoc.getTokenSequences()) {
            trainOnSentence(ts, domain);
        }
        logger.debug(System.currentTimeMillis() - time);
    }

    private void makeEntityTypesAndZeroProbs() {
        entityTypes = new HashSet<String>();
        for(String tagType : tagSet) {
            if(tagType.startsWith("B-") || tagType.startsWith("W-")) entityTypes.add(tagType.substring(2));
        }
        for(String tag : tagSet) {
            zeroProbs.put(tag, 0.0);
        }
    }

    private Map<String, Double> runGIS(GISModel gm, String [] context) {
        Map<String, Double> results = new HashMap<String, Double>();
        results.putAll(zeroProbs);
        double [] gisResults = gm.eval(context);
        for(int i=0;i<gisResults.length;i++) {
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
    public Map<NamedEntity,Double> findNEs(TokenSequence tokSeq, String domain) {
        FeatureExtractor extractor = new FeatureExtractor(tokSeq, domain);
        List<Token> tokens = tokSeq.getTokens();
        if (tokens.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Map<String,Map<String,Double>>> classifierResults = new ArrayList<Map<String,Map<String,Double>>>();
        for (int i = 0; i < tokens.size(); i++) {
            List<String> featuresForToken = extractor.getFeatures(i);
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




    private void cvFeatures(File file, String domain) throws Exception {
        long time = System.currentTimeMillis();
        logger.debug("Cross-Validate features on: " + file + "... ");
        Document doc = new Builder().build(file);
        Nodes n = doc.query("//cmlPile");
        for(int i=0;i<n.size();i++) n.get(i).detach();
        n = doc.query("//ne[@type='CPR']");
        for(int i=0;i<n.size();i++) XOMTools.removeElementPreservingText((Element)n.get(i));


        ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
                Tokeniser.getInstance(), doc, true, false, false);

        for(TokenSequence ts : procDoc.getTokenSequences()) {
            cvFeatures(ts, domain);
        }
        logger.debug(System.currentTimeMillis() - time);
    }


    private double infoLoss(double [] probs, int index) {
        return -Math.log(probs[index])/Math.log(2);
    }

    private void cvFeatures(TokenSequence tokSeq, String domain) {
        if(featureCVScores == null) {
            featureCVScores = new HashMap<String,Map<String,Double>>();
        }
        FeatureExtractor extractor = new FeatureExtractor(tokSeq, domain);
        List<Token> tokens = tokSeq.getTokens();
        String prevTag = "O";
        for(int i=0;i<tokens.size();i++) {
            String tag = tokens.get(i).getBioTag();
            GISModel gm = gmByPrev.get(prevTag);
            if(gm == null) continue;
            Map<String,Double> scoresForPrev = featureCVScores.get(prevTag);
            if(scoresForPrev == null) {
                scoresForPrev = new HashMap<String,Double>();
                featureCVScores.put(prevTag, scoresForPrev);
            }

            prevTag = tag;
            int outcomeIndex = gm.getIndex(tag);
            if(outcomeIndex == -1) continue;
            List<String> features = extractor.getFeatures(i);
            if(features.size() == 0) continue;
            String [] featuresArray = features.toArray(new String[0]);
            String [] newFeaturesArray = features.toArray(new String[0]);
            double [] baseProbs = gm.eval(featuresArray);
            for(int j=0;j<features.size();j++) {
                newFeaturesArray[j] = "IGNORETHIS";
                double [] newProbs = gm.eval(newFeaturesArray);
                double gain = infoLoss(newProbs, outcomeIndex) - infoLoss(baseProbs, outcomeIndex);
                if(Double.isNaN(gain)) gain = 0.0;
                String feature = features.get(j);
                double oldScore = 0.0;
                if(scoresForPrev.containsKey(feature)) oldScore = scoresForPrev.get(feature);
                scoresForPrev.put(feature, gain + oldScore);
                newFeaturesArray[j] = featuresArray[j];
            }
        }
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
        for(int i=0;i<maxents.size();i++) {
            Element maxent = maxents.get(i);
            String prev = maxent.getAttributeValue("prev");
            StringGISModelReader sgmr = new StringGISModelReader(maxent.getValue());
            GISModel gm = sgmr.getModel();
            gmByPrev.put(prev, gm);
            tagSet.add(prev);
            for(int j=0;j<gm.getNumOutcomes();j++) {
                tagSet.add(gm.getOutcome(j));
            }
        }
        Element rescorerElem = memmRoot.getFirstChildElement("rescorer");
        if(rescorerElem != null) {
            rescorer = new RescoreMEMMOut();
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

}
