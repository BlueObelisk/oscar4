package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import opennlp.maxent.GIS;
import opennlp.model.DataIndexer;
import opennlp.model.Event;
import opennlp.model.EventCollectorAsStream;
import opennlp.model.MaxentModel;
import opennlp.model.TwoPassDataIndexer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.XOMBasedProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.XOMBasedProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.BioTag;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.data.MutableMEMMModel;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.SimpleEventCollector;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer.MEMMOutputRescorer;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer.MEMMOutputRescorerTrainer;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.TrainingDataExtractor;
import uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata.ExtractedTrainingData;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGramBuilder;
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

	private final Logger logger = LoggerFactory.getLogger(MEMMTrainer.class);

	private MutableMEMMModel model;

	//TODO candidate for ArrayListMultimap
	Map<BioType, List<Event>> evsByPrev;
	
	//number of training cycles to be passed to GIS.trainModel
	private int trainingCycles;
	
	/* "The minimum number of times a predicate must have been
	observed in order to be included in the model" */
	private int featureCutOff;

	// looks to be intended to control setting the extracted training
	// data in trainOnSbFilesNosplit
	private boolean retrain=true;
	
	private boolean splitTrain=true;
	
	// whether or not to use the FeatureSelector to reduce
	// the number of events used to train the memm models
	private boolean featureSel=true;
	
	private boolean simpleRescore=true;
	
	//if true, uses regexes to recognise identify which 
	//CM and RN are named reactions / named chemicals
	private boolean nameTypes=false;
		
	private Map<BioType,Map<String,Double>> featureCVScores;
	
	private Map<BioType,Set<String>> perniciousFeatures;
	

	
	public MEMMTrainer(ChemNameDictRegistry registry) {
		Set<String> chemNameDictNames = Collections.unmodifiableSet(registry.getAllNames());
		model = new MutableMEMMModel(chemNameDictNames);
		evsByPrev = new HashMap<BioType, List<Event>>();
		perniciousFeatures = null;
		
		trainingCycles = 100;
		featureCutOff = 1;
	}

	/**
	 * Adds some contextual data to the evsByPrev field
	 * 
	 * @param features
	 * @param thisTag
	 * @param prevTag
	 */
	private void train(FeatureList features, BioType thisTag, BioType prevTag) {
		if(perniciousFeatures != null && perniciousFeatures.containsKey(prevTag)) {
			features.removeFeatures(perniciousFeatures.get(prevTag));
		}
		if(features.getFeatureCount() == 0) {
			features.addFeature("EMPTY");
		}
		model.getTagSet().add(thisTag);
		String [] context = features.toArray();
		Event ev = new Event(thisTag.toString(), context);
		List<Event> evs = evsByPrev.get(prevTag);
		if(evs == null) {
			evs = new ArrayList<Event>();
			evsByPrev.put(prevTag, evs);
		}
		evs.add(ev);
	}
	
	/**
	 * Populates the evsByPrev field with data derived from
	 * the given {@link TokenSequence}
	 */
	private void trainOnSentence(TokenSequence tokSeq) {
        List<FeatureList> featureLists = FeatureExtractor.extractFeatures(tokSeq, model.getNGram(), model.getChemNameDictNames());
		List<Token> tokens = tokSeq.getTokens();
		BioType prevTag = new BioType(BioTag.O);
		for (int i = 0; i < tokens.size(); i++) {
			train(featureLists.get(i), tokens.get(i).getBioType(), prevTag);
			prevTag = tokens.get(i).getBioType();
		}
	}
	
	//dmj30 was public
	private void trainOnFile(File file) throws DataFormatException, IOException {
		logger.debug("Train on: " + file + "... ");
		FileInputStream fis = new FileInputStream(file); 
		try {
			trainOnStream(fis);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}
	
	//all the trainOn methods eventually end up here...
	//dmj30 was public
	private void trainOnStream(InputStream stream) throws DataFormatException, IOException {
		long time = System.currentTimeMillis();
		Document doc;
		try {
			doc = new Builder().build(stream);
		} catch (ParsingException e) {
			throw new DataFormatException("incorrect formatting of training resource");
		}
		trainOnDoc(doc);
		logger.debug("Time: {}", System.currentTimeMillis() - time);
	}

	private void trainOnDoc(Document doc) {
		Nodes n = doc.query("//cmlPile");
		for (int i = 0; i < n.size(); i++) {
			n.get(i).detach();
		}
		n = doc.query("//ne[@type='CPR']");
		
		
		for (int i = 0; i < n.size(); i++) {
			XOMTools.removeElementPreservingText((Element)n.get(i));
		}

		
		//FIXME probably a mistake - this method is called per file by trainOnSbFiles
//		TrainingDataExtractor extractor = new TrainingDataExtractor(doc);
//		model.setExtractedTrainingData(
//			new ExtractedTrainingData(extractor.toXML())
//		);


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
		
		XOMBasedProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeTokenisedDocument(
			Tokeniser.getDefaultInstance(), doc, true, false);

		for(TokenSequence ts : procDoc.getTokenSequences()) {
			trainOnSentence(ts);
		}
	}

	//dmj30 was public
	private void trainOnSbFilesNosplit(List<File> files) throws DataFormatException, IOException {
		if(retrain) {
			//likely overriden by bug in trainOnStream
			HyphenTokeniser.reinitialise();
			TrainingDataExtractor extractor = new TrainingDataExtractor(filesToDocs(files));
			model.setExtractedTrainingData(
				new ExtractedTrainingData(extractor.toXML())
			);
			HyphenTokeniser.reinitialise();					
		}
		for(File f : files) {
			trainOnFile(f);
		}				
		finishTraining();
	}
	
	@Deprecated
	/*
	 * Temporary adapter method
	 */
	private Collection<Document> filesToDocs(Collection <File> files) {
		List <Document> docs = new ArrayList<Document>();
		for (File file : files) {
			try {
				docs.add(new Builder().build(file));
			} catch (ValidityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return docs;
	}

	//dmj30 was public
	private void trainOnSbFiles(List<File> files) throws DataFormatException, IOException {
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
				//does this code change anything?
				HyphenTokeniser.reinitialise();
				new TrainingDataExtractor(filesToDocs(splitTrainAntiFiles.get(split)));
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
			//does this code change anything?
			HyphenTokeniser.reinitialise();
			new TrainingDataExtractor(filesToDocs(files));
			HyphenTokeniser.reinitialise();				
		}
	}

	//dmj30 was public
	private void trainOnSbFilesWithCVFS(List<File> files) throws DataFormatException, IOException {
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
	}

	//dmj30 was public
	private void trainOnSbFilesWithRescore(List<File> files, MEMMModel memm,
			double confidenceThreshold) throws Exception {
		
		MEMMOutputRescorerTrainer rescorerTrainer =
			new MEMMOutputRescorerTrainer(memm, confidenceThreshold);
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

	
	/**
	 * Builds and stores in MEMMModel.gmByPrev the memm models
	 * for each BioTag based on the current data stored in evsByPrev
	 * 
	 * @throws IOException
	 */
	//dmj30 was public
	private void finishTraining() throws IOException {
		model.makeEntityTypesAndZeroProbs();
		
		for(BioType prevTagg : evsByPrev.keySet()) {
			logger.debug("tag: {}", prevTagg);
			List<Event> evs = evsByPrev.get(prevTagg);
			if(featureSel) {
				evs = new FeatureSelector().selectFeatures(evs);						
			}		
			if(evs.size() == 1) {
				evs.add(evs.get(0));
			}
			DataIndexer di = null;
			try {
				//I don't understand what would cause an exception to throw,
				//but this seems like a bad way to control the logic
				di = new TwoPassDataIndexer(new EventCollectorAsStream(new SimpleEventCollector(evs)), featureCutOff);
				model.putGISModel(prevTagg, GIS.trainModel(trainingCycles, di));
			} catch (Exception e) {
				di = new TwoPassDataIndexer(new EventCollectorAsStream(new SimpleEventCollector(evs)), 1);				
				model.putGISModel(prevTagg, GIS.trainModel(trainingCycles, di));
			}	
		}
	}
	
	//unreachable method
	private Map<BioType, Double> runGIS(MaxentModel gm, String [] context) {
		Map<BioType, Double> results = new HashMap<BioType, Double>();
		results.putAll(model.getZeroProbs());
		double [] gisResults = gm.eval(context);
		for (int i = 0; i < gisResults.length; i++) {
			results.put(BioType.fromString(gm.getOutcome(i)), gisResults[i]);
		}
		return results;
	}
	
	private Map<BioType,Map<BioType,Double>> calcResults(FeatureList features) {
		Map<BioType,Map<BioType,Double>> results = new HashMap<BioType,Map<BioType,Double>>();
		String [] featArray = features.toArray();
		for(BioType tag : model.getTagSet()) {
			MaxentModel gm = model.getMaxentModelByPrev(tag);
			if(gm == null) continue;
			Map<BioType, Double> modelResults = runGIS(gm, featArray);
			results.put(tag, modelResults);
		}
		return results;
	}
	
	
	private void cvFeatures(File file) throws IOException, DataFormatException {
		long time = System.currentTimeMillis();
		logger.debug("Cross-Validate features on: " + file + "... ");
		Document doc;
		try {
			doc = new Builder().build(file);
		} catch (ParsingException e) {
			throw new DataFormatException("malformed scrapbook file: " + file.getName(), e);
		}
		Nodes n = doc.query("//cmlPile");
		for (int i = 0; i < n.size(); i++) n.get(i).detach();
		n = doc.query("//ne[@type='CPR']");
		for (int i = 0; i < n.size(); i++) XOMTools.removeElementPreservingText((Element)n.get(i));
		
		
		XOMBasedProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeTokenisedDocument(
			Tokeniser.getDefaultInstance(), doc, true, false);
		for(TokenSequence ts : procDoc.getTokenSequences()) {
			cvFeatures(ts);
		}
		logger.debug("time: {}", System.currentTimeMillis() - time);
	}

	
	private double infoLoss(double [] probs, int index) {
		return -Math.log(probs[index])/Math.log(2);
	}
	
	private void cvFeatures(TokenSequence tokSeq) {
		if(featureCVScores == null) {
			featureCVScores = new HashMap<BioType,Map<String,Double>>();
		}
		List<FeatureList> featureLists = FeatureExtractor.extractFeatures(tokSeq, model.getNGram(), model.getChemNameDictNames());
		List<Token> tokens = tokSeq.getTokens();
		BioType prevTag = new BioType(BioTag.O);
		for (int i = 0; i < tokens.size(); i++) {
			BioType tag = tokens.get(i).getBioType();
			MaxentModel gm = model.getMaxentModelByPrev(prevTag);
			if(gm == null) continue;
			Map<String,Double> scoresForPrev = featureCVScores.get(prevTag);
			if(scoresForPrev == null) {
				scoresForPrev = new HashMap<String,Double>();
				featureCVScores.put(prevTag, scoresForPrev);
			}
			
			prevTag = tag;
			int outcomeIndex = gm.getIndex(tag.toString());
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
		perniciousFeatures = new HashMap<BioType,Set<String>>();
		for(BioType prev : featureCVScores.keySet()) {
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
		model.getRescorer().rescore(entities, model.getChemNameDictNames());
	}

	public MEMMModel getModel() {
		return model;
	}

	
	public void trainOnDocs(List<Document> sourceDocs) throws IOException {
		
		TrainingDataExtractor extractor = new TrainingDataExtractor(sourceDocs);
		model.setExtractedTrainingData(
			new ExtractedTrainingData(extractor.toXML())
		);
		//FIXME model.nGram was already set once in MutableMemmModel constructor
		model.nGram = NGramBuilder.buildOrDeserialiseModel(
				model.etd, model.chemNameDictNames);
		
		for (Document doc : sourceDocs) {
			trainOnDoc((Document) doc.copy());
		}
		finishTraining();
		
	}
}
