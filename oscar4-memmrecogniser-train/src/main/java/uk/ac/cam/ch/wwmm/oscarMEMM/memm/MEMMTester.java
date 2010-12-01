package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.IXOMBasedProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.XOMBasedProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.obo.OBOOntology;
import uk.ac.cam.ch.wwmm.oscar.obo.TermMaps;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.ExtractTrainingData;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.ExtractedTrainingData;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGram;
import uk.ac.cam.ch.wwmm.oscartokeniser.HyphenTokeniser;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**A standalone class for experimentation with MEMMs. This class is only usable
 * by altering the source code.
 * 
 * @author ptc24
 *
 */
public final class MEMMTester {

	private static List<List<Integer>> getFolds(int items, int folds) {
		int foldSize = items / folds;
		int remainder = items % folds;
		List<List<Integer>> foldList = new ArrayList<List<Integer>>();
		List<Integer> fold = new ArrayList<Integer>();
		foldList.add(fold);
		for (int i = 0; i < items; i++) {
			if(fold.size() == foldSize+1) {
				remainder--;
				fold = new ArrayList<Integer>();
				foldList.add(fold);
			} else if(remainder == 0 && fold.size() == foldSize) {
				fold = new ArrayList<Integer>();
				foldList.add(fold);
			}
			fold.add(i);
		}
		return foldList;
	}
	
	private static boolean cheatTokenisation=false;
	
	private static String experName = "test";
	
	private static Pattern nePattern = Pattern.compile("\\[NE:([A-Z]+):\\d+:\\d+:(.+)\\]", Pattern.DOTALL);

	private static String filterType = null;
	private static String antiFilterType = null;
	//public static String antiFilterType = "CLASS";
	
	private static void recPrec(List<Double> goodProbs, List<Double> badProbs, int maxRecall, String name) throws Exception {
		File f = new File("/home/ptc24/tmp/rpres/" + name + "_" + experName + ".csv");
		PrintWriter pw = new PrintWriter(new FileWriter(f));
		Collections.sort(goodProbs, Collections.reverseOrder());
		Collections.sort(badProbs, Collections.reverseOrder());
		int goodIndex = 0;
		int badIndex = 0;
		while(goodIndex < goodProbs.size() || badIndex < badProbs.size()) {
			double prob;
			if(goodIndex == goodProbs.size()) {
				prob = badProbs.get(badIndex);
				badIndex++;				
			} else if(badIndex == badProbs.size()) {
				prob = goodProbs.get(goodIndex);
				goodIndex++;				
			} else if(goodProbs.get(goodIndex) > badProbs.get(badIndex)) {
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
	
	/**The main method. This is experimental code, and the UI is Eclipse.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		TermMaps.init();
		OBOOntology.getInstance();
		ExtractedTrainingData.clear();
		NGram.getInstance();

		List<File> sbFiles = new ArrayList<File>();
		//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/corpora/bc2sciXML1"), "markedup.xml"));
		//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/corpora/bc2sciXML"), "markedup.xml"));
		//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/goodrsc"), "scrapbook.xml"));
		//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/newpubmed"), "scrapbook.xml"));
		//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/cleanOscar/oscar3-chem/scrapbookfold1_pruned"), "scrapbook.xml"));
		//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/cleanOscar/oscar3-chem/scrapbookfold2_pruned"), "scrapbook.xml"));
		//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/cleanOscar/oscar3-chem/scrapbookfold3_pruned"), "scrapbook.xml"));
		//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/scrapbookfold1"), "scrapbook.xml"));
		//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/tmp/chemscrap"), "scrapbook.xml"));
//		if(true) {
//			sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/cleanOscar/oscar3-chem/scrapbookfold1_pruned"), "scrapbook.xml"));
//			sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/cleanOscar/oscar3-chem/scrapbookfold2_pruned"), "scrapbook.xml"));
//			sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/cleanOscar/oscar3-chem/scrapbookfold3_pruned"), "scrapbook.xml"));			
//		} else if(false) {
//			sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/corpora/NewSciXMLPatents2"), "markedup.xml"));
//			//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/corpora/SciXMLPatents"), "markedup.xml"));
//			//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/corpora/MESciXMLPatents"), "markedup.xml"));
//		} else {
//			sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/newows/newpubmed"), "scrapbook.xml"));			
//		}
		//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/scrapbookfold2"), "scrapbook.xml"));
		//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/scrapbookfold3"), "scrapbook.xml"));
		//sbFiles.addAll(FileTools.getFilesFromDirectoryByName(new File("/home/ptc24/tmp/pubmedscrap"), "scrapbook.xml"));

		Random random = new Random("seed3".hashCode());
		//Collections.shuffle(sbFiles, random);
		
		//sbFiles = sbFiles.subList(0, 20);
		
		List<File> tmpFiles = new ArrayList<File>(sbFiles);
		for(File f : tmpFiles) {
			if(f.getPath().contains("default")) sbFiles.remove(f);
		}

		int foldNumber = 3;
		List<List<Integer>> folds = getFolds(sbFiles.size(), foldNumber);
		
		int totalNEs = 0;

		List<Double> goodProbs = new ArrayList<Double>();
		List<Double> badProbs = new ArrayList<Double>();
		
		int foldNo = 0;
		for(List<Integer> fold : folds) {
			foldNo++;
			if(foldNo != 1) continue;

			System.out.println(fold);
			Set<File> trainFiles = new HashSet<File>(sbFiles);
			for(Integer i : fold) {
				trainFiles.remove(sbFiles.get(i));
			}

			List<File> trainFilesList = new ArrayList<File>(trainFiles);
			MEMMTrainer memm = new MEMMTrainer();
			if(true) {
				//System.out.println("Reducing " + trainFilesList.size() + " training files to " + 10);
				//trainFilesList = trainFilesList.subList(0,10);
				memm.trainOnSbFiles(trainFilesList);
				//memm.trainOnSbFilesWithCVFS(trainFilesList);
				//memm.trainOnSbFilesWithRescore(trainFilesList);
				
				//Serializer ser = new Serializer(new FileOutputStream(new File("modeltest.xml")));
				//ser.write(nmt.writeModel());
				
				//if(true) return;				
			} else{
				HyphenTokeniser.reinitialise();
				new ExtractTrainingData(trainFilesList);
				HyphenTokeniser.reinitialise();				

				memm.getModel().readModel(new Builder().build("modeltest.xml"));
				//nmt.examineModel("O");
				//return;
			}
						
			for(Integer j : fold) {
				File f = sbFiles.get(j);
				Document doc = new Builder().build(f);
				String name = f.getParentFile().getName();
				System.out.println(name);
				Nodes n = doc.query("//cmlPile");
				for (int i = 0; i < n.size(); i++) n.get(i).detach();
				n = doc.query("//ne[@type='CPR']");
				for (int i = 0; i < n.size(); i++) XOMTools.removeElementPreservingText((Element)n.get(i));
				//n = doc.query("//ne[@type='CLASS']");
				//for (int i = 0; i < n.size(); i++) XOMTools.removeElementPreservingText((Element)n.get(i));
				if(false) {
					n = doc.query("//ne");
					for (int i = 0; i < n.size(); i++) {
						Element e = (Element)n.get(i);
						e.addAttribute(new Attribute("type", "CHEMICAL"));
						//XOMTools.removeElementPreservingText((Element)n.get(i));
					}					
				}
				IXOMBasedProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeTokenisedDocument(
					Tokeniser.getInstance(), doc, true, false, false);

				//NameRecogniser nr = new NameRecogniser();
				//nr.halfProcess(doc);

				//if(memm.patternFeatures) {
				//	nr.findForReps(cheatTokenisation);					
				//} else {
					//nr.makeTokenisers(cheatTokenisation);
				//}
				
				int paperTestNEs = 0;
				
				List<Double> paperGoodProbs = new ArrayList<Double>();
				List<Double> paperBadProbs = new ArrayList<Double>();

				Map<NamedEntity,Double> confidences = new HashMap<NamedEntity,Double>();
				
				List<NamedEntity> entities;
				
				Set<String> testNEs = new LinkedHashSet<String>();

				for(ITokenSequence tokSeq : procDoc.getTokenSequences()) {
					TokenSequence seq = (TokenSequence)tokSeq;
					Nodes neNodes = seq.getElem().query(".//ne");
					for (int k = 0; k < neNodes.size(); k++) {
						Element neElem = (Element)neNodes.get(k);
						if(filterType != null && !neElem.getAttributeValue("type").equals(filterType)) continue;
						if(antiFilterType != null && neElem.getAttributeValue("type").equals(antiFilterType)) continue;
						String neStr = "[NE:" + neElem.getAttributeValue("type") + ":" + neElem.getAttributeValue("xtspanstart") + ":" + neElem.getAttributeValue("xtspanend") + ":" + neElem.getValue() + "]";
						testNEs.add(neStr);
					}
					confidences.putAll(memm.findNEs(seq));
				}

				paperTestNEs += testNEs.size();
				totalNEs += testNEs.size();

				Set<String> remainNEs = new HashSet<String>(testNEs);				

				List<NamedEntity> neList = new ArrayList<NamedEntity>(confidences.keySet());
				memm.rescore(neList);
				/*if(memm.getRescorer() != null) {
					System.out.println("Rescore!");
					memm.getRescorer().rescore(neList);
				}*/
				
				/*final Map<NamedEntity,Double> conf = confidences;
				Collections.sort(neList, Collections.reverseOrder(new Comparator<NamedEntity>() {
					@SuppressWarnings("unchecked")
					public int compare(NamedEntity o1, NamedEntity o2) {
						return conf.get(o1).compareTo(conf.get(o2));
					}
				}));*/

				for(NamedEntity ne : neList) {
					String neStr = ne.toString();
					if(filterType != null && !ne.getType().equals(filterType)) continue;
					if(antiFilterType != null && ne.getType().equals(antiFilterType)) continue;
					if(testNEs.contains(neStr)) {
						if(NamedEntityType.LOCANTPREFIX.equals(ne.getType())) System.out.println("Yay:" + neStr + "\t" + confidences.get(ne));
						//System.out.println("Good: " + ne + "\t" + confidences.get(ne));						
						paperGoodProbs.add(ne.getConfidence());
						remainNEs.remove(neStr);
					} else {
						if(ne.getConfidence() > 0.5) System.out.println("Bad:  " + ne + "\t" + confidences.get(ne));

						//System.out.println("Bad:  " + ne + "\t" + confidences.get(ne));
						paperBadProbs.add(ne.getConfidence());
					}
				}
				
				if(remainNEs.size() > 0) System.out.println("Couldn't tag");
				for(String s : remainNEs) {
					System.out.println(s);
					Matcher m = nePattern.matcher(s);
					if(m.matches()) {
						System.out.println(PostProcessor.filterEntity(m.group(2), NamedEntityType.valueOf(m.group(1))));
					}
				}
				
				recPrec(paperGoodProbs, paperBadProbs, paperTestNEs, name);

				goodProbs.addAll(paperGoodProbs);
				badProbs.addAll(paperBadProbs);
				
			}
			recPrec(goodProbs, badProbs, totalNEs, "overall");			
		}
	}

}
