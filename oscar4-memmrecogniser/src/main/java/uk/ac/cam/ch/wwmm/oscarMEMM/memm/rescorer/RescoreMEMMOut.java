package uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer;

import java.io.File;
import java.io.FileWriter;
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
import nu.xom.Elements;
import nu.xom.Nodes;
import opennlp.maxent.DataIndexer;
import opennlp.maxent.Event;
import opennlp.maxent.EventCollectorAsStream;
import opennlp.maxent.GIS;
import opennlp.maxent.GISModel;
import opennlp.maxent.TwoPassDataIndexer;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMM;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMSingleton;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.SimpleEventCollector;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelReader;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelWriter;
import uk.ac.cam.ch.wwmm.oscarMEMM.tools.Oscar3Props;
import uk.ac.cam.ch.wwmm.oscarMEMM.xmltools.XOMTools;

/**Handles rescoring of MEMM output.
 * 
 * @author ptc24
 *
 */
public final class RescoreMEMMOut {

	Map<String,List<Event>> eventsByType;
	Map<String,GISModel> modelsByType;
	List<Double> goodProbsBefore;
	List<Double> goodProbsAfter;
	List<Double> badProbsBefore;
	List<Double> badProbsAfter;
	int totalRecall;
	
	int trainingCycles = 200;
	double grandTotalGain;
	
	static String experName = "rescoreHalf";
	
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
			//System.out.println(goodIndex + badIndex + "\t" + goodIndex);
			pw.println(rec + "\t" + prec + "\t" + prob);
		}
		pw.close();
	}

	
	/**Initialises an empty rescorer. This rescorer must be given data or a
	 * model for it to work.
	 */
	public RescoreMEMMOut() {
		eventsByType = new HashMap<String,List<Event>>();
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
	 * @param domain Experimental: the domain of the training data 
	 * (may be null).
	 * @throws Exception
	 */
	public void trainOnFile(File f, String domain) throws Exception {
		trainOnFile(f, domain, MEMMSingleton.getInstance());
	}
	
	/**Take a file of training data, and analyse it. The data produced in this
	 * analysis will not be used to generate a new model straight away - the
	 * finishTraining() method must be called to do that.
	 * 
	 * @param f The file to train on.
	 * @param domain Experimental: the domain of the training data
	 * (may be null).
	 * @param memm The MEMM to be used to to generate the input potential NEs.
	 * @throws Exception
	 */
	public void trainOnFile(File f, String domain, MEMM memm) throws Exception {
		Document doc = new Builder().build(f);
		String name = f.getParentFile().getName();
		if(Oscar3Props.getInstance().verbose) System.out.println(name);
		Nodes n = doc.query("//cmlPile");
		for(int i=0;i<n.size();i++) n.get(i).detach();
		n = doc.query("//ne[@type='CPR']");
		for(int i=0;i<n.size();i++) XOMTools.removeElementPreservingText((Element)n.get(i));
		
		/*NameRecogniser nr = new NameRecogniser();
		nr.halfProcess(doc);

		nr.makeTokenisers(false);*/
		
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(doc, true, false, false);
		
		List<NamedEntity> entities = new ArrayList<NamedEntity>();

		Set<String> testNEs = new HashSet<String>();
		
		for(TokenSequence tokSeq : procDoc.getTokenSequences()) {
			Nodes neNodes = tokSeq.getElem().query(".//ne");
			for(int k=0;k<neNodes.size();k++) {
				Element neElem = (Element)neNodes.get(k);
				String neStr = "[NE:" + neElem.getAttributeValue("type") + ":" + neElem.getAttributeValue("xtspanstart") + ":" + neElem.getAttributeValue("xtspanend") + ":" + neElem.getValue() + "]";
				testNEs.add(neStr);
			}
			entities.addAll(memm.findNEs(tokSeq, domain).keySet());
		}

		FeatureExtractor fe = new FeatureExtractor(entities);
		
		for(NamedEntity entity : entities) {
			String isEntity;
			if(testNEs.contains(entity.toString())) {
				isEntity = "T";
			} else {
				isEntity = "F";
			}
			List<String> features = fe.getFeatures(entity);
			String type = entity.getType();
			if(!eventsByType.containsKey(type)) eventsByType.put(type, new ArrayList<Event>());
			eventsByType.get(type).add(new Event(isEntity, features.toArray(new String[0])));
		}
	}
	
	/**Generate a new rescoring model. This must be run after calling
	 * trainOnFile several times.
	 * 
	 * @throws Exception
	 */
	public void finishTraining() throws Exception {
		modelsByType = new HashMap<String,GISModel>();
		for(String type : eventsByType.keySet()) {
			DataIndexer di = null;
			List<Event> evs = eventsByType.get(type);
			if(evs.size() == 1) evs.add(evs.get(0));
			di = new TwoPassDataIndexer(new EventCollectorAsStream(new SimpleEventCollector(evs)), 1);
			modelsByType.put(type, GIS.trainModel(trainingCycles, di));
		}
	}
	
	/**Adjust the confidence scores of a list of named entities.
	 * 
	 * @param entities The named entities to rescore.
	 */
	public void rescore(List<NamedEntity> entities) {

		FeatureExtractor fe = new FeatureExtractor(entities);

		for(NamedEntity entity : entities) {
			List<String> features = fe.getFeatures(entity);
			String type = entity.getType();
			if(modelsByType.containsKey(type)) {
				GISModel model = modelsByType.get(type);
				if(model.getNumOutcomes() == 2) {
					double prob = model.eval(features.toArray(new String[0]))[model.getIndex("T")];
					entity.setConfidence(prob);

				}
			}
		}
	}
	
	/**For testing purposes.
	 * 
	 * @param f
	 * @throws Exception
	 */
	private void runOnFile(File f) throws Exception {
		//System.out.println(f);
		Document doc = new Builder().build(f);
		String name = f.getParentFile().getName();
		//System.out.println(name);
		Nodes n = doc.query("//cmlPile");
		for(int i=0;i<n.size();i++) n.get(i).detach();
		n = doc.query("//ne[@type='CPR']");
		for(int i=0;i<n.size();i++) XOMTools.removeElementPreservingText((Element)n.get(i));
		
		/*NameRecogniser nr = new NameRecogniser();
		nr.halfProcess(doc);

		nr.makeTokenisers(false);*/
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(doc, true, false, false);
		
		List<NamedEntity> entities = new ArrayList<NamedEntity>();

		Set<String> testNEs = new HashSet<String>();
		
		for(TokenSequence tokSeq : procDoc.getTokenSequences()) {
			Nodes neNodes = tokSeq.getElem().query(".//ne");
			for(int k=0;k<neNodes.size();k++) {
				Element neElem = (Element)neNodes.get(k);
				String neStr = "[NE:" + neElem.getAttributeValue("type") + ":" + neElem.getAttributeValue("xtspanstart") + ":" + neElem.getAttributeValue("xtspanend") + ":" + neElem.getValue() + "]";
				testNEs.add(neStr);
			}
			entities.addAll(MEMMSingleton.getInstance().findNEs(tokSeq, null).keySet());
		}
		totalRecall += testNEs.size();

		double totalGain = 0.0;
		
		FeatureExtractor fe = new FeatureExtractor(entities);
		
		for(NamedEntity entity : entities) {
			//System.out.print(entity);
			/*if(testNEs.contains(entity.toString())) {
				System.out.println("\tGOOD");
			} else {
				System.out.println("\tBAD");
			}*/
			List<String> features = fe.getFeatures(entity);
			String type = entity.getType();
			if(modelsByType.containsKey(type)) {
				GISModel model = modelsByType.get(type);
				if(model.getNumOutcomes() == 2) {
					double prob = model.eval(features.toArray(new String[0]))[model.getIndex("T")];
					//System.out.println(entity.getConfidence() + "\t->\t" + prob);
					
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
					
					/*if(gain > 0) {
						if((conf - 0.5) * (prob - 0.5) < 0) {
							System.out.println("CROSSOVER");
						} else if(confCaution < probCaution)  {
							System.out.println("DIMINISH");
						} else {
							System.out.println("BOOST");
						}
					}
					System.out.println(gain);*/
					totalGain += gain;
				}
			}
		}
		
		//System.out.println("Total gain: " + totalGain);
		grandTotalGain += totalGain;
	}

	/**Produce an XML Element that contains the trained rescorer model.
	 * 
	 * @return An XML Element that contains the trained rescorer model.
 	 * @throws Exception
	 */
	public Element writeElement() throws Exception {
		Element root = new Element("rescorer");
		for(String type : modelsByType.keySet()) {
			Element maxent = new Element("maxent");
			maxent.addAttribute(new Attribute("type", type));
			StringGISModelWriter sgmw = new StringGISModelWriter(modelsByType.get(type));
			sgmw.persist();
			maxent.appendChild(sgmw.toString());
			root.appendChild(maxent);

		}
		return root;
	}
	
	/**Take an XML Element that contains a trained rescorer model, and prepare
	 * to use it.
	 * 
	 * @param elem The XML Element that contains the trained rescorer model.
	 * @throws Exception
	 */
	public void readElement(Element elem) throws Exception {
		Elements maxents = elem.getChildElements("maxent");
		modelsByType = new HashMap<String,GISModel>();
		for(int i=0;i<maxents.size();i++) {
			Element maxent = maxents.get(i);
			String type = maxent.getAttributeValue("type");
			StringGISModelReader sgmr = new StringGISModelReader(maxent.getValue());
			GISModel gm = sgmr.getModel();
			modelsByType.put(type, gm);
		}		
	}
	
	
	/**
	 * @param args
	 */
	/*public static void main(String[] args) throws Exception {
		RescoreMEMMOut rmo = new RescoreMEMMOut();
		
		List<File> files = new ArrayList<File>();
		files.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/miscrsc/"), "scrapbook.xml"));
		
		Random r = new Random("foo".hashCode());
		Collections.shuffle(files, r);
		
		List<File> trainFiles = files.subList(0, files.size() / 2);
		System.out.print("Reduce " + trainFiles.size() + " train files to ");
		trainFiles = trainFiles.subList(trainFiles.size() / 2, trainFiles.size());
		System.out.println(trainFiles.size());
		List<File> testFiles = files.subList(files.size() / 2, files.size());
		
		for(File f : trainFiles) rmo.trainOnFile(f, null);
		//rmo.trainOnFile(new File("/home/ptc24/newows/miscrsc/b503019f/scrapbook.xml"));
		rmo.finishTraining();
		for(File f : testFiles) {
			long time = System.currentTimeMillis();
			rmo.runOnFile(f);
			System.out.println(System.currentTimeMillis() - time);
		}
		System.out.println("Grand total gain: " + rmo.grandTotalGain);
		recPrec(rmo.goodProbsBefore, rmo.badProbsBefore, rmo.totalRecall, "before");
		recPrec(rmo.goodProbsAfter, rmo.badProbsAfter, rmo.totalRecall, "after");
	}*/

}
