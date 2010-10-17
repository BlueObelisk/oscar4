package uk.ac.cam.ch.wwmm.oscarMEMM.subtypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import opennlp.maxent.DataIndexer;
import opennlp.maxent.Event;
import opennlp.maxent.EventCollectorAsStream;
import opennlp.maxent.GIS;
import opennlp.maxent.GISModel;
import opennlp.maxent.TwoPassDataIndexer;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.SimpleEventCollector;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelReader;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis.StringGISModelWriter;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.Model;
import uk.ac.cam.ch.wwmm.oscarMEMM.saf.SafTools;
import uk.ac.cam.ch.wwmm.oscarMEMM.tokenAnalysis.TokenTypes;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/** An experimental class to subclassify named entities.
 * 
 * @author ptc24
 *
 */
public final class NESubtypes {

	Map<String, Map<String, List<List<String>>>> trainingData;
	Map<String, GISModel> classifiers;
	
	boolean useTagger = false;
	boolean bestOnly = false;
	public boolean OK = false;
		
	class State {
		Document sourceDoc;
		Document safDoc;		
		ProcessingDocument procDoc;
		String domain = null;
	}
	
	private static NESubtypes myInstance;
	
	//private static Stemmer stemmerTools = new Stemmer(new EnglishStemmer());
	
	/**Destroys the current NESubstypes singleton.
	 * 
	 */
	public static void clear() {
		myInstance = new NESubtypes();
	}
	
	/**Re-initalises the NESubtypes singleton.
	 * 
	 * @param elem An XML element containing the NESubtypes model.
	 * @throws Exception
	 */
	public static void reinitialise(Element elem) throws Exception {
		myInstance = new NESubtypes();
		myInstance.load(elem);
		myInstance.OK = true;
	}
	
	/**Get the NESubtypes singleton, initialising if necessary.
	 * 
	 * @return The NESubtypes singleton.
	 * @throws Exception
	 */
	public static NESubtypes getInstance() throws Exception {
		if(myInstance == null) {
			Model.loadModel();
			if(myInstance == null) {
				myInstance = new NESubtypes();
			} else {
				myInstance.OK = true;
			}
		}
		return myInstance;
	}
	
	/**Builds a new NESubtypes singleton based on the files supplied, sets it
	 * as the NESubtypes singleton.
	 * 
	 * @param maybeFiles The files to train on.
	 * @return The NESubtypes singleton.
	 */
//	public static boolean trainOnFiles(List<File> maybeFiles) {
//		try {
//			NESubtypes nes = new NESubtypes();
//			List<File> files = new ArrayList<File>();
//			for(File f : maybeFiles) {
//				Document doc = new Builder().build(f);
//				if(doc.query("//ne[@subtype]").size() > 0) {
//					files.add(f.getParentFile());
//					if(Oscar3Props.getInstance().verbose) System.out.println("Adding: " + f.getParentFile().getName());
//				} else {
//					if(Oscar3Props.getInstance().verbose) System.out.println("Skipping: " + f.getParentFile().getName());					
//				}
//			}
//			if(files.size() == 0) return false;
//			for(File f : files) {
//				nes.trainOnPaper(f);
//			}
//			nes.finishTraining();
//			myInstance = nes;
//			nes.OK = true;
//			return true;
//		} catch (Exception e) {
//			throw new Error(e);
//		}
//	}
	
	/**Assigns subtypes to named entities.
	 * 
	 * @param sourceDoc The SciXML source document.
	 * @param safDoc The SAF XML document containing the named entities.
	 * @throws Exception
	 */
//	public static void doSubTypes(Document sourceDoc, Document safDoc) throws Exception {
//		NESubtypes nes = getInstance();
//		if(!nes.OK) return;
//		State state = nes.setPaper(sourceDoc, safDoc);
//		nes.applyToSaf(state);
//	}
	
	private NESubtypes() {
		trainingData = new HashMap<String, Map<String, List<List<String>>>>();
	}

	private String flattenSubtype(String subtype) {
		//if(subtype.equals("DESC")) subtype = "OTHER";
		
		if(subtype == null) return null;
		//if(subtype.equals("PROTEIN")) subtype = "EXACT";
		//if(subtype.equals("TECH")) subtype = "PART";
		//if(subtype.equals("LIKE")) subtype = "EXACT";
		//if(subtype.equals("ION")) subtype = "EXACT";
		//if(subtype.equals("BOND")) subtype = "PART";
		
		//if(subtype.equals("SPECIES")) subtype = "CLASS";
		//if(subtype.equals("LIKE")) subtype = "EXACT";
		//if(subtype.equals("ION")) subtype = "OTHER";
		//if(subtype.equals("TECH")) subtype = "OTHER";
		//if(subtype.equals("BOND")) subtype = "OTHER";
		//if(subtype.equals("PART")) subtype = "OTHER";
		return subtype;
	}
	
//	private State setPaper(File f) throws Exception {
//		State state = new State();
//		if(Oscar3Props.getInstance().verbose) System.out.println("Setting: " + f);
//		if(new File(f, "scrapbook.xml").exists()) {
//			if(new File(f, "source.xml").exists()) {
//				ScrapBook sb = new ScrapBook(f);
//				sb.makePaper();
//				state.safDoc = new Builder().build(new File(f, "saf.xml"));
//				state.sourceDoc = new Builder().build(new File(f, "source.xml"));							
//			} else {
//				ScrapBook sb = new ScrapBook(f);
//				Document doc = (Document)sb.getDoc().copy();
//				Nodes nodes = doc.query("//cmlPile");
//				for(int i=0;i<nodes.size();i++) nodes.get(i).detach();
//				state.sourceDoc = (Document)doc.copy();
//				nodes = state.sourceDoc.query("//ne");
//				for(int i=0;i<nodes.size();i++) {
//					XOMTools.removeElementPreservingText((Element)nodes.get(i));
//				}
//				state.safDoc = InlineToSAF.extractSAFs(doc, state.sourceDoc, "foo");
//			}
//		} else {
//			state.safDoc = new Builder().build(new File(f, "saf.xml"));
//			state.sourceDoc = new Builder().build(new File(f, "source.xml"));			
//		}
//		state.procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(state.sourceDoc, true, false, useTagger, state.safDoc);
//		setPaper(state.sourceDoc, state.safDoc);
//		
//		if(f.getPath().contains("reactnewpubmed")) {
//			state.domain = "pubmed";
//		} else if(f.getPath().contains("reactmiscrsceasy")) {
//			state.domain = "misc";
//		} else if(f.getPath().contains("reactgoodrsc")) {
//			state.domain = "perkin";
//		} else {
//			state.domain = null;
//		}
//		return state;
//	}
	
	private State setPaper(Document sourceDoc, Document safDoc) throws Exception {
		State state = new State();
		state.sourceDoc = sourceDoc;
		state.safDoc = safDoc;		
		state.procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
			Tokeniser.getInstance(), sourceDoc, true, false, useTagger, safDoc);
		return state;
	}
	
//	private void iterateOverEntities(State state, NESubtypeHandler handler) throws Exception {
//		Nodes n = state.safDoc.query("//annot[@type='oscar']");
//		for(int i=0;i<n.size();i++) {
//			Element e = (Element)n.get(i);
//			String type = SafTools.getSlotValue(e, "type");
//			String subtype = SafTools.getSlotValue(e, "subtype");
//			subtype = flattenSubtype(subtype);
//			//if("LIKE".equals(subtype)) continue;
//			if(type == null) continue;
//			List<String> features = getFeatures(state, e);
//			handler.handle(e, type, subtype, features);
//		}
//	}
//	
//	private void simpleEval(List<File> fl) throws Exception {
//		final Map<String,ClassificationEvaluator> evaluations = new HashMap<String,ClassificationEvaluator>();
//		for(File f : fl) {
//			State state = setPaper(f);
//			simpleEval(state, evaluations);
//		}
//		pprintEvaluations(evaluations);
//	}
//	
//	
//	private void simpleEval(State state) throws Exception {
//		final Map<String,ClassificationEvaluator> evaluations = new HashMap<String,ClassificationEvaluator>();
//		simpleEval(state, evaluations);
//		pprintEvaluations(evaluations);
//	}
//	
//	private void simpleEval(State state, final Map<String,ClassificationEvaluator> evaluations) throws Exception {
//		NESubtypeHandler h = new NESubtypeHandler() {
//			public void handle(Element annot, String type, String subtype, java.util.List<String> features) {
//				if(!classifiers.containsKey(type)) return;
//				if(!evaluations.containsKey(type)) evaluations.put(type, new ClassificationEvaluator());
//				double [] outcomes = classifiers.get(type).eval(features.toArray(new String[0]));				
//				String outcome = classifiers.get(type).getBestOutcome(outcomes);
//				//subtype = flattenSubtype(subtype);
//				//double prob = outcomes[classifiers.get(type).getIndex(outcome)];
//				if(!subtype.equals(outcome)) {
//					String surface = SafTools.getSlotValue(annot, "surface");
//					System.out.println(surface + "_" + type + ":" + subtype + "->" + outcome);
//				}
//				//if(prob < 0.42) return;
//				//if(prob > 0.48) return;
//				//if(type.equals("CM")) System.out.println(annot + ": " + features);
//				evaluations.get(type).logEvent(subtype, outcome);
//			}
//		};
//		iterateOverEntities(state, h);
//	}

//	private void pprintEvaluations(Map<String,ClassificationEvaluator> evaluations) {
//		for(String type : evaluations.keySet()) {
//			System.out.println(type);
//			System.out.println("acc:\t" + evaluations.get(type).getAccuracy());
//			System.out.println("kappa:\t" + evaluations.get(type).getKappa());
//			evaluations.get(type).pprintConfusionMatrix();
//			evaluations.get(type).pprintPrecisionRecallEval();
//		}
//	}
//	
//	private void trainOnPaper(File f) throws Exception {
//		State state = setPaper(f);
//		trainOnPaper(state);
//	}
	
//	private void trainOnPaper(State state) throws Exception {
//		NESubtypeHandler h = new NESubtypeHandler() {
//			public void handle(Element annot, String type, String subtype, java.util.List<String> features) {
//				if(subtype == null) return;
//				if(!trainingData.containsKey(type)) trainingData.put(type, new HashMap<String, List<List<String>>>());
//				if(!trainingData.get(type).containsKey(subtype)) trainingData.get(type).put(subtype, new ArrayList<List<String>>());
//				trainingData.get(type).get(subtype).add(features);
//			}
//		};
//		iterateOverEntities(state, h);
//	}
//
//	private void applyToSaf(State state) throws Exception {
//		NESubtypeHandler h = new NESubtypeHandler() {
//			public void handle(Element annot, String type, String subtype, java.util.List<String> features) {
//				if(!classifiers.containsKey(type)) return;
//				double [] outcomes = classifiers.get(type).eval(features.toArray(new String[0]));				
//				boolean forceBestOnly = false;
//				String confidenceStr = SafTools.getSlotValue(annot, "confidence");
//				double confidence = 1.0;
//				if(confidenceStr == null) {
//					forceBestOnly = true;
//				} else {
//					confidence = Double.parseDouble(confidenceStr);
//				}
//				String outcome = classifiers.get(type).getBestOutcome(outcomes);
//				SafTools.setSlot(annot, "subtype", outcome);					
//				if(!bestOnly && !forceBestOnly){
//					SafTools.setSlot(annot, "confidence", Double.toString(confidence * outcomes[classifiers.get(type).getIndex(outcome)]));
//					for(int i=0;i<outcomes.length;i++) {
//						if(i == classifiers.get(type).getIndex(outcome)) continue;
//						double newConf = confidence * outcomes[i];
//						if(newConf > Oscar3Props.getInstance().neThreshold) {
//							Element newAnnot = new Element(annot);
//							SafTools.setSlot(newAnnot, "subtype", classifiers.get(type).getOutcome(i));
//							SafTools.setSlot(newAnnot, "confidence", Double.toString(newConf));
//							SafTools.setSlot(newAnnot, "blocked", "true");
//							XOMTools.insertAfter(annot, newAnnot);
//						}
//					}
//				}
//			}
//		};
//		iterateOverEntities(state, h);		
//	}
//	
	private void finishTraining() throws Exception {
		classifiers = new HashMap<String,GISModel>();
		for(String type : trainingData.keySet()) {
			List<Event> events = new ArrayList<Event>();
			for(String subtype : trainingData.get(type).keySet()) {
				for(List<String> features : trainingData.get(type).get(subtype)) {
					Event ev = new Event(subtype, features.toArray(new String[0]));
					events.add(ev);
				}
			}
			if(events.size() < 2) continue;
			DataIndexer di = null;
			di = new TwoPassDataIndexer(new EventCollectorAsStream(new SimpleEventCollector(events)), 1);
			Logger.getLogger(NESubtypes.class).debug(di);
			GISModel gm = GIS.trainModel(100, di);
			classifiers.put(type, gm);
		}
	}

//	private List<String> getFeatures(State state, Element annot) throws Exception {
//		String type = SafTools.getSlotValue(annot, "type");
//		List<String> features;
//		if("PRW".equals(type)) {
//			features = getFeaturesPRW(state, annot);
//		} else {
//			features = getFeaturesCMRNCJASE(state, annot);
//		}
//		
//		if(state.domain != null) {
//			for(String feature : new ArrayList<String>(features)) {
//				features.add(state.domain + ":" + feature);
//			}
//			//features.add("domain=" + domain);
//		}
//		return features;
//	}
//	
//	private List<String> getFeaturesPRW(State state, Element annot) throws Exception {
//		boolean fName = true;
//		boolean fSuffix = true;
//		boolean fStem = true;
//		boolean fPlural = false;
//		boolean fPrevious = true;
//		boolean fNext = true;
//		
//		List<String> features = new ArrayList<String>();			
//		String surface = SafTools.getSlotValue(annot, "surface");
//		if(surface == null) surface = "";
//		if(fName) features.add("this=" + surface.replaceAll("\\s+", "_"));
//				
//		String suffix;
//		if(surface.length() > 4) {
//			suffix = surface.substring(surface.length() - 4);				
//		} else {
//			suffix = surface;
//		}
//		
//		String stem = stemmerTools.getStem(surface.replaceAll("\\s+", "_"));
//		
//		if(fStem) features.add(stem);
//		
//		if(fSuffix) features.add("suffix=" + suffix.replaceAll("\\s+", "_"));
//		if(fPlural) {
//			if(surface.endsWith("s")) {
//				features.add("MAYBEPLURAL");
//			} else {
//				features.add("MAYBENOTPLURAL");
//			}
//		}
//		
//		//if(surface.matches("([Pp]oly).+")) features.add("polymer");
//		//if(surface.matches(".+\\(\\d\\d\\d+\\)")) features.add("surfacenotation");
//		
//		Token t = state.procDoc.getTokenByStart(annot.getAttributeValue("from"));
//		if(fPrevious && t != null) {
//			Token tt = t.getNAfter(-1);
//			if(tt != null) {
//				for(int i=1;i<=1;i++) {
//					if(TokenTypes.isRef(tt) && tt.getNAfter(-1) != null) {
//						//	features.add(prefix + "skiprefprev");
//						tt = tt.getNAfter(-1);
//					}
//					String ttv = tt.getValue();
//					ttv = ttv.replaceAll("\\s+", "_");
//					//if(i == 1) features.add("prev" + 1 + "=" + ttv);
//					features.add("pbg" + i + "=" + ttv + "_" + surface.replaceAll("\\s+", "_"));
//					//features.add("pbg" + (i+1) + "=" + ttv + "_" + surface.replaceAll("\\s+", "_"));
//					//features.add("pbg" + 0 + "=" + ttv + "_" + surface.replaceAll("\\s+", "_"));
//					//features.add("uibg" + 0 + "=" + ttv + "_" + surface.replaceAll("\\s+", "_"));
//					
//					tt = tt.getNAfter(-1);
//					if(tt == null) break;
//				}				
//
//				
//				//if(ttv.length() > 4) features.add("prevs=" + ttv.substring(ttv.length()-4) + "_" + surface.replaceAll("\\s+", "_"));
//				//features.add("psbg=" + ttv + "_" + suffix.replaceAll("\\s+", "_"));
//				//features.add("pstbg=" + ttv + "_" + stem);
//				
//			}
//		}
//		
//		t = state.procDoc.getTokenByEnd(annot.getAttributeValue("to"));
//		if(fNext && t != null) {
//			Token tt = t.getNAfter(1);
//			//if(tt != null && tt.getValue().equals("-")) {
//			//	tt = t.getNAfter(2);
//			//}
//			if(tt != null) {
//				for(int i=1;i<=1;i++) {
//					if(TokenTypes.isRef(tt) && tt.getNAfter(1) != null) {
//						tt = tt.getNAfter(1);
//					}
//					
//					String ttv = tt.getValue();
//					ttv = ttv.replaceAll("\\s+", "_");
//					//if(i == 1) features.add("next=" + ttv);
//					features.add("nbg" + i + "=" + surface.replaceAll("\\s+", "_")  + "_" + ttv);
//					
//					//features.add("uibg" + 0 + "=" + ttv + "_" + surface.replaceAll("\\s+", "_"));
//
//					
//					tt = tt.getNAfter(1);
//					if(tt == null) break;
//				}
//				//if(ttv.length() > 4) features.add("nexts=" + surface.replaceAll("\\s+", "_")  + "_" + ttv.substring(ttv.length()-4));
//				//features.add("nsbg=" + suffix.replaceAll("\\s+", "_")  + "_" + ttv);
//				//features.add("nstbg=" + stem  + "_" + ttv);
//				
//			}
//		}
//		
//		features.add("defaultFeature");
//		//System.out.println(features);
//		return features;
//	}	
	
	private List<String> getFeaturesCMRNCJASE(State state, Element annot) throws Exception {
		boolean fName = true;
		boolean fSuffix = true;
		boolean fPlural = true;
		boolean fPrevious = true;
		boolean fNext = true;
		
		List<String> features = new ArrayList<String>();			
		String surface = SafTools.getSlotValue(annot, "surface");
		if(surface == null) surface = "";
		if(fName) features.add("this=" + surface.replaceAll("\\s+", "_"));
				
		String suffix;
		if(surface.length() > 4) {
			suffix = surface.substring(surface.length() - 4);				
		} else {
			suffix = surface;
		}
		if(fSuffix) features.add("suffix=" + suffix.replaceAll("\\s+", "_"));
		if(fPlural) {
			if(surface.endsWith("s")) {
				features.add("MAYBEPLURAL");
			} else {
				features.add("MAYBENOTPLURAL");
			}
		}
		
		//if(surface.matches("([Pp]oly).+")) features.add("polymer");
		//if(surface.matches(".+\\(\\d\\d\\d+\\)")) features.add("surfacenotation");
		
		Token t = state.procDoc.getTokenByStart(annot.getAttributeValue("from"));
		if(fPrevious && t != null) {
			Token tt = t.getNAfter(-1);
			if(tt != null) {
				if(TokenTypes.isRef(tt) && tt.getNAfter(-1) != null) {
					//	features.add(prefix + "skiprefprev");
					tt = tt.getNAfter(-1);
				}
				String ttv = tt.getValue();
				ttv = ttv.replaceAll("\\s+", "_");
				features.add("prev=" + ttv);
			}
		}
		
		t = state.procDoc.getTokenByEnd(annot.getAttributeValue("to"));
		if(fNext && t != null) {
			Token tt = t.getNAfter(1);
			if(tt != null && tt.getValue().equals("-")) {
				tt = t.getNAfter(2);
			}
			if(tt != null) {
				if(TokenTypes.isRef(tt) && tt.getNAfter(1) != null) {
					tt = tt.getNAfter(1);
				}
				
				String ttv = tt.getValue();
				ttv = ttv.replaceAll("\\s+", "_");
				features.add("next=" + ttv);
			}
		}
		
		features.add("defaultFeature");
		return features;
	}	

	/**Produces an XML serialisation of the current subtypes model.
	 * 
	 * @return An XML serialisation of the current subtypes model.
	 * @throws Exception
	 */
	public Element toXML() throws Exception {
		Element e = new Element("subtypes");
		for(String type : classifiers.keySet()) {
			Element c = new Element("classifier");
			e.appendChild(c);
			c.addAttribute(new Attribute("type", type));
			StringGISModelWriter sgmw = new StringGISModelWriter(classifiers.get(type));
			sgmw.persist();
			//System.out.println(sgmw.toString());
			c.appendChild(sgmw.toString());
		}
		return e;
	}
	
	private void load(Element rootElem) throws Exception {
		Elements ee = rootElem.getChildElements("classifier");
		classifiers = new HashMap<String,GISModel>();
		for(int i=0;i<ee.size();i++) {
			String type = ee.get(i).getAttributeValue("type");
			StringGISModelReader sgmr = new StringGISModelReader(ee.get(i).getValue());
			classifiers.put(type, sgmr.getModel());
		}
	}
	

	/*public static void main(String[] args) throws Exception {
		if(true) {
			//NESubtypes nes = getInstance();
			List<File> files = new ArrayList<File>();
			//files.add(new File("C:\\newows\\scrapbook\\"));
			//List<File> maybeFiles = FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/reactgoodrsc"), "scrapbook.xml");
			//List<File> maybeFiles = FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/reactmiscrsceasy"), "scrapbook.xml");
			//List<File> maybeFiles = FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/reactnewpubmed"), "scrapbook.xml");
			
			List<File> maybeFiles = new ArrayList<File>();
			maybeFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/reactgoodrsc"), "scrapbook.xml"));
			maybeFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/reactmiscrsceasy"), "scrapbook.xml"));
			maybeFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/reactnewpubmed"), "scrapbook.xml"));
			
			
			List<File> maybeExtraTrainFiles = new ArrayList<File>();
			
			//maybeExtraTrainFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/reactgoodrsc"), "scrapbook.xml"));
			//maybeExtraTrainFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/reactmiscrsceasy"), "scrapbook.xml"));
			//maybeExtraTrainFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/reactnewpubmed"), "scrapbook.xml"));
			List<File> extraTrainFiles = new ArrayList<File>();
			
			//System.out.println(maybeFiles);
			for(File f : maybeFiles) {
				Document doc = new Builder().build(f);
				if(doc.query("//ne[@subtype]").size() > 0) {
					files.add(f.getParentFile());
					System.out.println("Adding: " + f.getParentFile().getName());
				} else {
					System.out.println("Skipping: " + f.getParentFile().getName());					
				}
			}
			
			for(File f : maybeExtraTrainFiles) {
				Document doc = new Builder().build(f);
				if(doc.query("//ne[@subtype]").size() > 0) {
					extraTrainFiles.add(f.getParentFile());
				}
			}
			
			//new Random().
			if(false) Collections.reverse(files);
			if(true) Collections.shuffle(files, new Random(0));
			
			int halfLen = files.size() / 2;
			//int thirdLen = files.size() / 3;
			//int twoThirdLen = 2 * files.size() / 3;
			
			NESubtypes nes = new NESubtypes();
			int i=0;
			List<File> trainFiles = new ArrayList<File>(files.subList(0, halfLen));
			trainFiles.addAll(extraTrainFiles);
			for(File f : trainFiles) {
				//if(i % 3 != 2)
				nes.trainOnPaper(f);
				i++;
			}
			nes.finishTraining();
			//nes.bootstrap(files.subList(thirdLen, twoThirdLen), 0.9);
			//nes.finishTraining();
			nes.simpleEval(files.subList(halfLen, files.size()));
			//nes.simpleEval(files.subList(0, halfLen));
			//nes.simpleEval(files);
		}

	}*/

}
