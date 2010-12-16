package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import opennlp.maxent.*;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.IXOMBasedProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.XOMBasedProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.data.MEMMModel;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.data.MutableMEMMModel;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.SimpleEventCollector;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer.MEMMOutputRescorer;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer.MEMMOutputRescorerTrainer;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.ExtractManualAnnotations;
import uk.ac.cam.ch.wwmm.oscarrecogniser.manualAnnotations.ManualAnnotations;
import uk.ac.cam.ch.wwmm.oscartokeniser.HyphenTokeniser;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * The main class for generating and running MEMMs.
 * 
 * The flow of methods calls is something like:
 * <pre>
 *   MEMMTrainer trainer = new MEMMTrainer();
 *   trainer.trainOnFile(new File("someFile.xml"));
 *   trainer.finishTraining();
 * </pre>
 * 
 * @author ptc24
 *
 */
public final class MEMMTrainer {

	private final Logger logger = Logger.getLogger(MEMMTrainer.class);

	private MutableMEMMModel model;

	private Map<String, List<Event>> evsByPrev;
	private int trainingCycles;
	private int featureCutOff;

	private boolean useUber = false;
	private boolean removeBlocked = false;
	private boolean retrain=true;
	private boolean splitTrain=true;
	private boolean featureSel=true;
	private boolean simpleRescore=true;
	private boolean filtering=true;
	private boolean nameTypes=false;
		
	private Map<String,Map<String,Double>> featureCVScores;
	
	private Map<String,Set<String>> perniciousFeatures;
	
	private static double confidenceThreshold;

	public MEMMTrainer() throws Exception {
		model = new MutableMEMMModel();
		evsByPrev = new HashMap<String, List<Event>>();
		perniciousFeatures = null;
		
		trainingCycles = 100;
		featureCutOff = 1;
		confidenceThreshold = OscarProperties.getData().neThreshold / 5.0;
	}

	private void train(FeatureList features, String thisTag, String prevTag) {
		if(perniciousFeatures != null && perniciousFeatures.containsKey(prevTag) /*&& !tampering*/) {
			features.removeFeatures(perniciousFeatures.get(prevTag));
		}
		if(features.getFeatureCount() == 0) features.addFeature("EMPTY");
		model.getTagSet().add(thisTag);
		if(useUber) {
			features.addFeature("$$prevTag=" + prevTag);
		}
		String [] c = features.toArray();
		Event ev = new Event(thisTag, c);
		List<Event> evs = evsByPrev.get(prevTag);
		if(evs == null) {
			evs = new ArrayList<Event>();
			evsByPrev.put(prevTag, evs);
		}
		evs.add(ev);
	}
	
	private void trainOnSentence(ITokenSequence tokSeq) {
        List<FeatureList> featureLists = FeatureExtractor.extractFeatures(tokSeq, model.getNGram());
		//extractor.printFeatures();
		List<IToken> tokens = tokSeq.getTokens();
		String prevTag = "O";
		for (int i = 0; i < tokens.size(); i++) {
			train(featureLists.get(i), tokens.get(i).getBioTag(), prevTag);
			prevTag = tokens.get(i).getBioTag();
		}
	}
	
	public void trainOnFile(File file) throws Exception {
		logger.debug("Train on: " + file + "... ");
		trainOnStream(new FileInputStream(file));
	}
	
	public void trainOnStream(InputStream stream) throws Exception {
		long time = System.currentTimeMillis();
		Document doc = new Builder().build(stream);
		Nodes n = doc.query("//cmlPile");
		for (int i = 0; i < n.size(); i++) n.get(i).detach();
		n = doc.query("//ne[@type='CPR']");
		
		
		for (int i = 0; i < n.size(); i++) {
			XOMTools.removeElementPreservingText((Element)n.get(i));
		}

		
		
		ExtractManualAnnotations extractManualAnnotations = new ExtractManualAnnotations(doc);
		model.setExtractedTrainingData(
			new ManualAnnotations(extractManualAnnotations.toXML())
		);


		//another manual code switch?
		if(nameTypes) {
			n = doc.query("//ne");
			for (int i = 0; i < n.size(); i++) {
				Element ne = (Element)n.get(i);
				if(ne.getAttributeValue("type").equals(NamedEntityType.REACTION.getName()) && ne.getValue().matches("[A-Z]\\p{Ll}\\p{Ll}.*\\s.*")) {
					ne.addAttribute(new Attribute("type", "NRN"));
					logger.debug("NRN: " + ne.getValue());
				} else if(ne.getAttributeValue("type").equals(NamedEntityType.COMPOUND.getName()) && ne.getValue().matches("[A-Z]\\p{Ll}\\p{Ll}.*\\s.*")) {
					ne.addAttribute(new Attribute("type", "NCM"));
					logger.debug("NCM: " + ne.getValue());
				}
				else if (ne.getAttributeValue("type").equals(NamedEntityType.COMPOUND.getName()))  {
					ne.addAttribute(new Attribute("type", "CM"));
					logger.debug("CM: " + ne.getValue());
				}
			}
		}
		
		IXOMBasedProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeTokenisedDocument(
			Tokeniser.getInstance(), doc, true, false, false);

		for(ITokenSequence ts : procDoc.getTokenSequences()) {
			trainOnSentence(ts);
		}
		logger.debug(System.currentTimeMillis() - time);
	}

	public void trainOnSbFilesNosplit(List<File> files) throws Exception {
		if(retrain) {
			HyphenTokeniser.reinitialise();
			ExtractManualAnnotations extractManualAnnotations = new ExtractManualAnnotations(files);
			model.setExtractedTrainingData(
				new ManualAnnotations(extractManualAnnotations.toXML())
			);
			HyphenTokeniser.reinitialise();					
		}
		for(File f : files) {
			trainOnFile(f);
		}				
		finishTraining();
	}
	
	public void trainOnSbFiles(List<File> files) throws Exception {
		if(!splitTrain) {
			trainOnSbFilesNosplit(files);
			return;
		}
		List<Set<File>> splitTrainFiles = new ArrayList<Set<File>>();
		List<Set<File>> splitTrainAntiFiles = new ArrayList<Set<File>>();
		int splitNo = 2;
		
		for (int i = 0; i < splitNo; i++) {
			splitTrainFiles.add(new HashSet<File>());
			splitTrainAntiFiles.add(new HashSet<File>());
		}
		
		for (int i = 0; i < files.size(); i++) {
			for (int j = 0; j < splitNo; j++) {
				if(j == i % splitNo) {
					splitTrainFiles.get(j).add(files.get(i));
				} else {
					splitTrainAntiFiles.get(j).add(files.get(i));
				}
			}
		}
		
		for (int split = 0; split < splitNo; split++) {
			if(retrain) {
				HyphenTokeniser.reinitialise();
				new ExtractManualAnnotations(splitTrainAntiFiles.get(split));
				HyphenTokeniser.reinitialise();					
			}
			
			int fileno = 0;
			for(File f : splitTrainFiles.get(split)) {
				fileno++;
				trainOnFile(f);
			}				
		}

		finishTraining();
		if(retrain) {
			HyphenTokeniser.reinitialise();
			new ExtractManualAnnotations(files);
			HyphenTokeniser.reinitialise();				
		}
	}

	public void trainOnSbFilesWithCVFS(List<File> files) throws Exception {
		List<List<File>> splitTrainFiles = new ArrayList<List<File>>();
		List<List<File>> splitTrainAntiFiles = new ArrayList<List<File>>();
		int splitNo = 3;
		
		for (int i = 0; i < splitNo; i++) {
			splitTrainFiles.add(new ArrayList<File>());
			splitTrainAntiFiles.add(new ArrayList<File>());
		}
		
		for (int i = 0; i < files.size(); i++) {
			for (int j = 0; j < splitNo; j++) {
				if(j == i % splitNo) {
					splitTrainFiles.get(j).add(files.get(i));
				} else {
					splitTrainAntiFiles.get(j).add(files.get(i));
				}
			}
		}
		
		for (int split = 0; split < splitNo; split++) {
			trainOnSbFiles(splitTrainAntiFiles.get(split));
			evsByPrev.clear();
			for(File f : splitTrainFiles.get(split)) {
				cvFeatures(f);
			}				
		}
		
		findPerniciousFeatures();
		trainOnSbFiles(files);
		/*if(tampering) {
			List<String> prefixesToRemove = new ArrayList<String>();
			prefixesToRemove.add("anchor=");
			for(String tag : gmByPrev.keySet()) {
				gmByPrev.put(tag, Tamperer.tamperModel(gmByPrev.get(tag), perniciousFeatures.get(tag), prefixesToRemove));
			}			
		}*/
	}

	public void trainOnSbFilesWithRescore(List<File> files, MEMM memm) throws Exception {
		MEMMOutputRescorerTrainer rescorerTrainer =
			new MEMMOutputRescorerTrainer(memm);
		List<List<File>> splitTrainFiles = new ArrayList<List<File>>();
		List<List<File>> splitTrainAntiFiles = new ArrayList<List<File>>();
		int splitNo = 3;
		
		for (int i = 0; i < splitNo; i++) {
			splitTrainFiles.add(new ArrayList<File>());
			splitTrainAntiFiles.add(new ArrayList<File>());
		}
		
		for (int i = 0; i < files.size(); i++) {
			for (int j = 0; j < splitNo; j++) {
				if(j == i % splitNo) {
					splitTrainFiles.get(j).add(files.get(i));
				} else {
					splitTrainAntiFiles.get(j).add(files.get(i));
				}
			}
		}
		
		for (int split = 0; split < splitNo; split++) {
			if(simpleRescore) {
				trainOnSbFiles(splitTrainAntiFiles.get(split));
			} else {
				trainOnSbFilesWithCVFS(splitTrainAntiFiles.get(split));
			}
			for(File f : splitTrainFiles.get(split)) {
				String domain = null;
				rescorerTrainer.trainOnFile(f, memm);
			}				
			evsByPrev.clear();
			if(!simpleRescore) {
				featureCVScores.clear();
				perniciousFeatures.clear();
			}
		}
		rescorerTrainer.finishTraining();
		MEMMOutputRescorer rescorer = new MEMMOutputRescorer();
		rescorer.readElement(rescorerTrainer.writeElement());
		model.setRescorer(rescorer);

		if(simpleRescore) {
			trainOnSbFiles(files);
		} else {
			trainOnSbFilesWithCVFS(files);
		}
	}

	
	
	public void finishTraining() throws Exception {
		model.makeEntityTypesAndZeroProbs();
		
		if(useUber) {
			List<Event> evs = new ArrayList<Event>();
			for(String prevTagg : evsByPrev.keySet()) {
				evs.addAll(evsByPrev.get(prevTagg));
			}
			DataIndexer di = new TwoPassDataIndexer(new EventCollectorAsStream(new SimpleEventCollector(evs)), featureCutOff);
			model.setUberModel(GIS.trainModel(trainingCycles, di));
		} else {
			for(String prevTagg : evsByPrev.keySet()) {
				logger.debug(prevTagg);
				List<Event> evs = evsByPrev.get(prevTagg);
				if(featureSel) {
					evs = new FeatureSelector().selectFeatures(evs);						
				}		
				if(evs.size() == 1) {
					evs.add(evs.get(0));
				}
				DataIndexer di = null;
				try {
					di = new TwoPassDataIndexer(new EventCollectorAsStream(new SimpleEventCollector(evs)), featureCutOff);
					model.putGISModel(prevTagg, GIS.trainModel(trainingCycles, di));
				} catch (Exception e) {
					di = new TwoPassDataIndexer(new EventCollectorAsStream(new SimpleEventCollector(evs)), 1);				
					model.putGISModel(prevTagg, GIS.trainModel(trainingCycles, di));
				}	
			}
		}
	}
	
	private Map<String, Double> runGIS(MaxentModel gm, String [] context) {
		Map<String, Double> results = new HashMap<String, Double>();
		results.putAll(model.getZeroProbs());
		double [] gisResults = gm.eval(context);
		for (int i = 0; i < gisResults.length; i++) {
			results.put(gm.getOutcome(i), gisResults[i]);
		}
		return results;
	}
	
	private Map<String,Map<String,Double>> calcResults(FeatureList features) {
		Map<String,Map<String,Double>> results = new HashMap<String,Map<String,Double>>();
		if(useUber) {
			for(String prevTag : model.getTagSet()) {
				FeatureList newFeatures = new FeatureList(features);
				newFeatures.addFeature("$$prevTag=" + prevTag);
				results.put(prevTag, runGIS(model.getUberModel(), newFeatures.toArray()));
			}
		} else {
			String [] featArray = features.toArray();
			for(String tag : model.getTagSet()) {
				MaxentModel gm = model.getMaxentModelByPrev(tag);
				if(gm == null) continue;
				Map<String, Double> modelResults = runGIS(gm, featArray);
				results.put(tag, modelResults);
			}
		}
		return results;
	}
	
	/**Finds the named entities in a token sequence.
	 * 
	 * @param tokSeq The token sequence.
	 * @return Named entities, with confidences.
	 */
	public List<NamedEntity> findNEs(ITokenSequence tokSeq) {
		List<FeatureList> featureLists = FeatureExtractor.extractFeatures(tokSeq, model.getNGram());
		List<IToken> tokens = tokSeq.getTokens();
		if(tokens.size() == 0) return new ArrayList<NamedEntity>();

		List<Map<String,Map<String,Double>>> classifierResults = new ArrayList<Map<String,Map<String,Double>>>();	
		for (int i = 0; i < tokens.size(); i++) {
			classifierResults.add(calcResults(featureLists.get(i)));
		}
		
		EntityTokeniser lattice = new EntityTokeniser(
			model, tokSeq, classifierResults
		);
		List<NamedEntity> neConfidences = lattice.getEntities(confidenceThreshold);
		PostProcessor pp = new PostProcessor(tokSeq, neConfidences);
		if(filtering) pp.filterEntities();
		pp.getBlocked();
		if(removeBlocked) pp.removeBlocked();
		neConfidences = pp.getEntities();
		
		return neConfidences;
	}
	
	private void cvFeatures(File file) throws Exception {
		long time = System.currentTimeMillis();
		logger.debug("Cross-Validate features on: " + file + "... ");
		Document doc = new Builder().build(file);
		Nodes n = doc.query("//cmlPile");
		for (int i = 0; i < n.size(); i++) n.get(i).detach();
		n = doc.query("//ne[@type='CPR']");
		for (int i = 0; i < n.size(); i++) XOMTools.removeElementPreservingText((Element)n.get(i));
		
		
		IXOMBasedProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeTokenisedDocument(
			Tokeniser.getInstance(), doc, true, false, false);
		//NameRecogniser nr = new NameRecogniser();
		//nr.halfProcess(doc);
		//if(patternFeatures) {
		//	nr.findForReps(true);
		//} else {
			//nr.makeTokenisers(true);
		//}
		for(ITokenSequence ts : procDoc.getTokenSequences()) {
			cvFeatures(ts);
		}
		logger.debug(System.currentTimeMillis() - time);
	}

	
	private double infoLoss(double [] probs, int index) {
		return -Math.log(probs[index])/Math.log(2);
	}
	
	private void cvFeatures(ITokenSequence tokSeq) {
		if(featureCVScores == null) {
			featureCVScores = new HashMap<String,Map<String,Double>>();
		}
		List<FeatureList> featureLists = FeatureExtractor.extractFeatures(tokSeq, model.getNGram());
		List<IToken> tokens = tokSeq.getTokens();
		String prevTag = "O";
		for (int i = 0; i < tokens.size(); i++) {
			String tag = tokens.get(i).getBioTag();
			MaxentModel gm = model.getMaxentModelByPrev(prevTag);
			if(gm == null) continue;
			Map<String,Double> scoresForPrev = featureCVScores.get(prevTag);
			if(scoresForPrev == null) {
				scoresForPrev = new HashMap<String,Double>();
				featureCVScores.put(prevTag, scoresForPrev);
			}
			
			prevTag = tag;
			int outcomeIndex = gm.getIndex(tag);
			if(outcomeIndex == -1) continue;
			FeatureList features = featureLists.get(i);
			if(features.getFeatureCount() == 0) continue;
			String [] featuresArray = features.toArray();
			String [] newFeaturesArray = features.toArray();
			double [] baseProbs = gm.eval(featuresArray);
			for (int j = 0; j < features.getFeatureCount(); j++) {
				newFeaturesArray[j] = "IGNORETHIS";
				double [] newProbs = gm.eval(newFeaturesArray);
				double gain = infoLoss(newProbs, outcomeIndex) - infoLoss(baseProbs, outcomeIndex);
				if(Double.isNaN(gain)) gain = 0.0;
				String feature = features.getFeature(j);
				double oldScore = 0.0;
				if(scoresForPrev.containsKey(feature)) oldScore = scoresForPrev.get(feature);
				scoresForPrev.put(feature, gain + oldScore);
				newFeaturesArray[j] = featuresArray[j];
			}
		}
	}
	
	private void findPerniciousFeatures() {
		perniciousFeatures = new HashMap<String,Set<String>>();
		for(String prev : featureCVScores.keySet()) {
			Set<String> pffp = new HashSet<String>();
			perniciousFeatures.put(prev, pffp);
			
			List<String> features = StringTools.getSortedKeyList(featureCVScores.get(prev));
			for(String feature : features) {
				double score = featureCVScores.get(prev).get(feature);
				if(score < 0.0) {
					logger.debug("Removing:\t" + prev + "\t" + feature + "\t" + score);
					pffp.add(feature);
				}
			}
		}
	}
	
	/**Uses this MEMM's rescorer to rescore a list of named entities. This
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
}
