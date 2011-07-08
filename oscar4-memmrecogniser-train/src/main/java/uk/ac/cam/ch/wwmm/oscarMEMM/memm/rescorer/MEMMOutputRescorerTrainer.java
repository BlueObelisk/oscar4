package uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import opennlp.maxent.GIS;
import opennlp.maxent.GISModel;
import opennlp.model.DataIndexer;
import opennlp.model.Event;
import opennlp.model.EventCollectorAsStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.XOMBasedProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMModel;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.SimpleEventCollector;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelWriter;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * Trains the rescoring of MEMM output.
 * 
 * @author ptc24
 * @author egonw
 */
public final class MEMMOutputRescorerTrainer {

	private static final Logger LOG = LoggerFactory.getLogger(MEMMOutputRescorerTrainer.class);
	
	private MEMMModel memm;
	private double confidenceThreshold;
	
	Map<NamedEntityType,List<Event>> eventsByNamedEntityType;
	Map<NamedEntityType,GISModel> modelsByNamedEntityType;
	List<Double> goodProbsBefore;
	List<Double> goodProbsAfter;
	List<Double> badProbsBefore;
	List<Double> badProbsAfter;
	int totalRecall;
	
	int trainingCycles = 200;
	double grandTotalGain;
	
	static String experName = "rescoreHalf";
	
	@Deprecated
	//TODO this isn't called and has a hard-coded /home/ptc24 path - do we need it?
	private static void recPrec(List<Double> goodProbs, List<Double> badProbs, int maxRecall, String name) throws Exception {
		File f = new File("/home/ptc24/tmp/rpres/" + name + "_" + experName + ".csv");
		PrintWriter pw = new PrintWriter(new FileWriter(f));
		Collections.sort(goodProbs, Collections.reverseOrder());
		Collections.sort(badProbs, Collections.reverseOrder());
		int goodIndex = 0;
		int badIndex = 0;
		while(goodIndex < goodProbs.size() && badIndex < badProbs.size()) {
			double prob;
			if(goodProbs.get(goodIndex) > badProbs.get(badIndex)) {
				prob = goodProbs.get(goodIndex);
				goodIndex++;
			} else {
				prob = badProbs.get(badIndex);
				badIndex++;
			}
			double rec = (goodIndex * 1.0) / maxRecall;
			double prec = (goodIndex * 1.0) / (goodIndex + badIndex);
			pw.println(rec + "\t" + prec + "\t" + prob);
		}
		pw.close();
	}

	
	/**Initialises an empty rescorer. This rescorer must be given data or a
	 * model for it to work.
	 */
	public MEMMOutputRescorerTrainer(MEMMModel memm, double confidenceThreshold) {
		this.memm = memm;
		this.confidenceThreshold = confidenceThreshold;
		eventsByNamedEntityType = new HashMap<NamedEntityType,List<Event>>();
		grandTotalGain = 0.0;
		
		goodProbsBefore = new ArrayList<Double>();
		goodProbsAfter = new ArrayList<Double>();
		badProbsBefore = new ArrayList<Double>();
		badProbsAfter = new ArrayList<Double>();
		totalRecall = 0;
	}

	/**Take a file of training data, and analyse it, using the current MEMM
	 * singleton to generate the input potential NEs. The data produced in this
	 * analysis will not be used to generate a new model straight away - the
	 * finishTraining() method must be called to do that.
	 * 
	 * @param f The file to train on.
	 * @throws Exception
	 */
	@Deprecated
	//TODO this isn't called - do we need it?
	public void trainOnFile(File f) throws Exception {
		trainOnFile(f, memm);
	}
	
	/**Take a file of training data, and analyse it. The data produced in this
	 * analysis will not be used to generate a new model straight away - the
	 * finishTraining() method must be called to do that.
	 * 
	 * @param f The file to train on.
	 * @param memm The MEMM to be used to to generate the input potential NEs.
	 * @throws IOException 
	 * @throws DataFormatException 
	 */
	public void trainOnFile(File f, MEMMModel mexmm) throws IOException, DataFormatException {
		Document doc;
		try {
			doc = new Builder().build(f);
		} catch (ParsingException e) {
			throw new DataFormatException("incorrectly formatted training file: " + f.getName());
		}
		String name = f.getParentFile().getName();
		LOG.debug(name);
		Nodes n = doc.query("//cmlPile");
		for (int i = 0; i < n.size(); i++) n.get(i).detach();
		n = doc.query("//ne[@type='CPR']");
		for (int i = 0; i < n.size(); i++) XOMTools.removeElementPreservingText((Element)n.get(i));
		
		/*NameRecogniser nr = new NameRecogniser();
		nr.halfProcess(doc);

		nr.makeTokenisers(false);*/
		
		IProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), doc, true, false);
		/*
		 * previously, this was using the revised ProcessingDocumentFactory to create a revised ProcessingDocument,
		 * which was probably a mistake - dmj30
		 */
//		IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
//		Tokeniser.getInstance(), doc, true, false, false);

		
		List<NamedEntity> entities = new ArrayList<NamedEntity>();

		Set<String> testNEs = new HashSet<String>();
		
		for(TokenSequence tokSeq : procDoc.getTokenSequences()) {
			Nodes neNodes = ((TokenSequence)tokSeq).getElem().query(".//ne");
			for (int k = 0; k < neNodes.size(); k++) {
				Element neElem = (Element)neNodes.get(k);
				String neStr = "[NE:" + neElem.getAttributeValue("type") + ":" + neElem.getAttributeValue("xtspanstart") + ":" + neElem.getAttributeValue("xtspanend") + ":" + neElem.getValue() + "]";
				testNEs.add(neStr);
			}
			entities.addAll(memm.findNEs(tokSeq, confidenceThreshold));
		}

		FeatureExtractor fe = new FeatureExtractor(entities);
		
		for(NamedEntity entity : entities) {
			String isEntity;
			if(testNEs.contains(entity.toString())) {
				isEntity = "T";
			} else {
				isEntity = "F";
			}
			List<String> features = fe.getFeatures(entity, memm.getChemNameDictNames());
			NamedEntityType namedEntityType = entity.getType();
			if(!eventsByNamedEntityType.containsKey(namedEntityType)) eventsByNamedEntityType.put(namedEntityType, new ArrayList<Event>());
			eventsByNamedEntityType.get(namedEntityType).add(new Event(isEntity, features.toArray(new String[0])));
		}
	}
	
	/**Generate a new rescoring model. This must be run after calling
	 * trainOnFile several times.
	 * @throws IOException 
	 * 
	 */
	public void finishTraining() throws IOException {
		modelsByNamedEntityType = new HashMap<NamedEntityType,GISModel>();
		for(NamedEntityType type : eventsByNamedEntityType.keySet()) {
			DataIndexer di = null;
			List<Event> evs = eventsByNamedEntityType.get(type);
			if(evs.size() == 1) evs.add(evs.get(0));
			di = new opennlp.model.TwoPassDataIndexer(new EventCollectorAsStream(new SimpleEventCollector(evs)), 1);
			modelsByNamedEntityType.put(type, GIS.trainModel(trainingCycles, di));
		}
	}
	
	/**For testing purposes.
	 * 
	 * @param f
	 * @throws Exception
	 */
	private void runOnFile(File f) throws Exception {
		Document doc = new Builder().build(f);
		String name = f.getParentFile().getName();
		Nodes n = doc.query("//cmlPile");
		for (int i = 0; i < n.size(); i++) n.get(i).detach();
		n = doc.query("//ne[@type='CPR']");
		for (int i = 0; i < n.size(); i++) XOMTools.removeElementPreservingText((Element)n.get(i));
		
		/*NameRecogniser nr = new NameRecogniser();
		nr.halfProcess(doc);

		nr.makeTokenisers(false);*/
		IProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeTokenisedDocument(
			Tokeniser.getDefaultInstance(), doc, true, false);
		/*
		 * previously, this was using the revised ProcessingDocumentFactory to create a revised ProcessingDocument,
		 * which was probably a mistake - dmj30
		 */
//		IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
//				Tokeniser.getInstance(), doc, true, false, false);
		
		List<NamedEntity> entities = new ArrayList<NamedEntity>();

		Set<String> testNEs = new HashSet<String>();
		
		for(TokenSequence tokSeq : procDoc.getTokenSequences()) {
			Nodes neNodes = ((TokenSequence)tokSeq).getElem().query(".//ne");
			for (int k = 0; k < neNodes.size(); k++) {
				Element neElem = (Element)neNodes.get(k);
				String neStr = "[NE:" + neElem.getAttributeValue("type") + ":" + neElem.getAttributeValue("xtspanstart") + ":" + neElem.getAttributeValue("xtspanend") + ":" + neElem.getValue() + "]";
				testNEs.add(neStr);
			}
			entities.addAll(memm.findNEs(tokSeq, confidenceThreshold));
		}
		totalRecall += testNEs.size();

		double totalGain = 0.0;
		
		FeatureExtractor fe = new FeatureExtractor(entities);
		
		for(NamedEntity entity : entities) {
			List<String> features = fe.getFeatures(entity, memm.getChemNameDictNames());
			NamedEntityType namedEntityType = entity.getType();
			if(modelsByNamedEntityType.containsKey(namedEntityType)) {
				GISModel model = modelsByNamedEntityType.get(namedEntityType);
				if(model.getNumOutcomes() == 2) {
					double prob = model.eval(features.toArray(new String[0]))[model.getIndex("T")];
					
					double conf = entity.getConfidence();
					
					double prob2 = prob;
					double conf2 = conf;
					if(testNEs.contains(entity.toString())) {
						prob2 = 1-prob;
						conf2 = 1-conf;
						goodProbsBefore.add(conf);
						goodProbsAfter.add(prob);
					} else {
						badProbsBefore.add(conf);
						badProbsAfter.add(prob);						
					}
					double gain = (Math.log(conf2) - Math.log(prob2))/Math.log(2);
					
					double confCaution = conf * (1-conf);
					double probCaution = prob * (1-prob);
					
					totalGain += gain;
				}
			}
		}
		
		grandTotalGain += totalGain;
	}

	/**Produce an XML Element that contains the trained rescorer model.
	 * 
	 * @return An XML Element that contains the trained rescorer model.
 	 * @throws IOException
	 */
	public Element writeElement() throws IOException {
		Element root = new Element("rescorer");
		for(NamedEntityType namedEntityType : modelsByNamedEntityType.keySet()) {
			Element maxent = new Element("maxent");
			maxent.addAttribute(new Attribute("type", namedEntityType.getName()));
			StringGISModelWriter sgmw = new StringGISModelWriter(modelsByNamedEntityType.get(namedEntityType));
			sgmw.persist();
			maxent.appendChild(sgmw.toString());
			root.appendChild(maxent);

		}
		return root;
	}

	public MEMMOutputRescorer getMEMMOutputRescorer() {
		MEMMOutputRescorer rescorer = new MEMMOutputRescorer();
		try {
			rescorer.readElement(this.writeElement());
		} catch (Exception exception) {
			throw new Error(
				"Error while creating MEMM output rescorer: " + exception.getMessage(),
				exception
			);
		}
		return rescorer;
	}
	
}
