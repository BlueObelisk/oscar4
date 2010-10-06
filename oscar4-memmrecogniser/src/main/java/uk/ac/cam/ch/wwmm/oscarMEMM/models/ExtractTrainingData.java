package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.document.HyphenTokeniser;
import uk.ac.cam.ch.wwmm.oscarMEMM.ptcDataStruct.Bag;
import uk.ac.cam.ch.wwmm.oscarMEMM.tools.Oscar3Props;
import uk.ac.cam.ch.wwmm.oscarMEMM.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscarMEMM.types.NETypes;
import uk.ac.cam.ch.wwmm.oscarMEMM.xmltools.XOMTools;

/** Extracts and holds useful data from hand-annotated text.
 * 
 * @author ptc24
 *
 */
public final class ExtractTrainingData {
	
	private static ExtractTrainingData myInstance;
	/**Words only found in chemical named entities.*/
	public Collection<String> chemicalWords;
	/**Words never found in chemical named entities.*/
	public Collection<String> nonChemicalWords;
	/**Nonwords only found in chemical named entities.*/
	public Set<String> chemicalNonWords;
	/**Nonwords never found in chemical named entities.*/
	public Set<String> nonChemicalNonWords;
	/**Words that occur after a hyphen, with a chemical named entity before
	 * the hyphen. E.g. "based" from "acetone-based".
	 */
	public Set<String> afterHyphen;
	/**Words where a prefix like 3- should not be interpreted as a CPR*/
	public Set<String> notForPrefix;
	/**Words with initial capitalisation that are not likely to be 
	 * proper nouns.
	 */
	public Set<String> pnStops;
	/**Strings seen both in and not in chemical named entities.*/
	public Set<String> polysemous;
	/**Words found at the end of multi-word reaction names.*/
	public Set<String> rnEnd;
	/**Words found in the middle of multi-word reaction names.*/
	public Set<String> rnMid;
	
	private static Pattern notForPrefixPattern = Pattern.compile("[0-9]+-([a-z]+)");
		
	/**Get the current singleton. If this does not exist, initialise it using
	 * the current model file.
	 * 
	 * @return The singleton.
	 */
	public static ExtractTrainingData getInstance() {
		if(myInstance == null) {
			Model.loadModel();
		}
		return myInstance;
	}
	
	/**Re-initialise the current singleton, using the current model file.
	 * 
	 */
	public static void reinitialise() {
		myInstance = null;
		getInstance();
	}
	
	/**Re-initialise the current singleton, given a collection of scrapbook
	 * files.
	 * 
	 * @param files The ScrapBook files
	 */
	public static void reinitialise(Collection<File> files) {
		myInstance = new ExtractTrainingData(files);
	}

	/**Re-initialise the current singleton, given an XML serialization
	 * produced by toXML.
	 * 
	 * @param elem The XML serialized data.
	 */
	public static void reinitialise(Element elem) {
		myInstance = new ExtractTrainingData();
		myInstance.readXML(elem);
	}

	/**Destroy the current singleton.
	 * 
	 */
	public static void clear() {
		myInstance = new ExtractTrainingData();		
	}
	
	/**Looks in the workspace for scrapbook files, extracts the data therein,
	 * and initialises the singleton from it.
	 * 
	 * @return If the procedure succeeded.
	 * Commented out on 27th Jan
	 */
//	public static boolean trainFromScrapbook() {
//		if(Oscar3Props.getInstance().workspace.equals("none")) return false;
//		File scrapbookdir = new File(Oscar3Props.getInstance().workspace, "scrapbook");
//		if(!scrapbookdir.exists() || !scrapbookdir.isDirectory()) return false;
//		List<File> sbFiles = FileTools.getFilesFromDirectoryByName(scrapbookdir, "scrapbook.xml");
//		ExtractTrainingData etd = new ExtractTrainingData(sbFiles);
//		myInstance = etd;
//		return true;
//	}
		
	private Element stringsToElement(Collection<String> strings, String elemName) {
		Element elem = new Element(elemName);
		StringBuffer sb = new StringBuffer();
		for(String string : strings) {
			sb.append(string);
			sb.append("\n");
		}
		elem.appendChild(sb.toString());
		return elem;
	}
	
	/**Produces an XML serialization of the data.
	 * 
	 * @return The XML Element containing the serialization.
	 */
	public Element toXML() {
		Element etdElem = new Element("etd");
		etdElem.appendChild(stringsToElement(chemicalWords, "chemicalWords"));
		etdElem.appendChild(stringsToElement(nonChemicalWords, "nonChemicalWords"));
		etdElem.appendChild(stringsToElement(chemicalNonWords, "chemicalNonWords"));
		etdElem.appendChild(stringsToElement(nonChemicalNonWords, "nonChemicalNonWords"));
		etdElem.appendChild(stringsToElement(afterHyphen, "afterHyphen"));
		etdElem.appendChild(stringsToElement(notForPrefix, "notForPrefix"));
		etdElem.appendChild(stringsToElement(pnStops, "pnStops"));
		etdElem.appendChild(stringsToElement(polysemous, "polysemous"));
		etdElem.appendChild(stringsToElement(rnEnd, "rnEnd"));
		etdElem.appendChild(stringsToElement(rnMid, "rnMid"));
		return etdElem;
	}

	private void readXML(Element xml) {
		chemicalWords = readStringsFromElement(xml.getFirstChildElement("chemicalWords"));
		nonChemicalWords = readStringsFromElement(xml.getFirstChildElement("nonChemicalWords"));
		chemicalNonWords = readStringsFromElement(xml.getFirstChildElement("chemicalNonWords"));
		nonChemicalNonWords = readStringsFromElement(xml.getFirstChildElement("nonChemicalNonWords"));
		afterHyphen = readStringsFromElement(xml.getFirstChildElement("afterHyphen"));
		notForPrefix = readStringsFromElement(xml.getFirstChildElement("notForPrefix"));
		pnStops = readStringsFromElement(xml.getFirstChildElement("pnStops"));
		polysemous = readStringsFromElement(xml.getFirstChildElement("polysemous"));
		rnEnd = readStringsFromElement(xml.getFirstChildElement("rnEnd"));
		rnMid = readStringsFromElement(xml.getFirstChildElement("rnMid"));
	}
	
	private Set<String> readStringsFromElement(Element elem) {
		try {
			Set<String> strings = new HashSet<String>();
			BufferedReader br = new BufferedReader(new StringReader(elem.getValue()));
			String line = br.readLine();
			while(line != null) {
				strings.add(line.trim());
				line = br.readLine();
			} 
			return strings;
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	/**Produce a hash code for the current ExtractTrainingData.
	 * 
	 * @return The hash code.
	 */
	public int makeHash() {
		return toXML().toXML().hashCode();
	}

	private ExtractTrainingData() {
		initSets();
	}
	
	/**Makes a new ExtractTrainingData from a collection of (ScrapBook) files.
	 * 
	 * @param files The files.
	 */
	public ExtractTrainingData(Collection<File> files) {
		init(files);
	}

	private void initSets() {
		chemicalWords = new HashSet<String>();
		nonChemicalWords = new HashSet<String>();
		afterHyphen = new HashSet<String>();
		chemicalNonWords = new HashSet<String>();
		nonChemicalNonWords = new HashSet<String>();
		pnStops = new HashSet<String>();
		notForPrefix = new HashSet<String>();
		polysemous = new HashSet<String>();
		rnEnd = new HashSet<String>();
		rnMid = new HashSet<String>();
	}
	
	private void init(Collection<File> files) {
		Set<String> goodPn;
		goodPn = new HashSet<String>();

		initSets();
		clear();
		try {
			HyphenTokeniser.reinitialise();
		} catch (Exception e) {
			
		}		
		Bag<String> cwBag = new Bag<String>();
		Bag<String> cnwBag = new Bag<String>();
		Bag<String> ncwBag = new Bag<String>();
		Bag<String> ncnwBag = new Bag<String>();
				
		int paperCount = 0;
		for(File f : files) {
			//System.out.println(f);
			try {
				Document doc = new Builder().build(f);
				
				Nodes n = doc.query("//cmlPile");
				for(int i=0;i<n.size();i++) n.get(i).detach();
				
				Document copy = new Document((Element)XOMTools.safeCopy(doc.getRootElement()));
				n = copy.query("//ne");
				for(int i=0;i<n.size();i++) XOMTools.removeElementPreservingText((Element)n.get(i));
				Document safDoc = InlineToSAF.extractSAFs(doc, copy, "foo");
				doc = copy;
				
				
				ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(doc, true, false, false, safDoc);
				//NameRecogniser nr = new NameRecogniser();
				//nr.halfProcess(doc);
				//n = doc.query(XMLStrings.CHEMICAL_PLACES_XPATH);
				if(Oscar3Props.getInstance().verbose) System.out.println(f);
				for(TokenSequence tokSeq : procDoc.getTokenSequences()) {
					afterHyphen.addAll(tokSeq.getAfterHyphens());
					Map<String, List<List<String>>> neMap = tokSeq.getNes();
					List<List<String>> neList = new ArrayList<List<String>>();
					if(neMap.containsKey(NETypes.COMPOUND)) neList.addAll(neMap.get(NETypes.COMPOUND));
					if(neMap.containsKey(NETypes.ADJECTIVE)) neList.addAll(neMap.get(NETypes.ADJECTIVE));
					if(neMap.containsKey(NETypes.REACTION)) neList.addAll(neMap.get(NETypes.REACTION));
					if(neMap.containsKey(NETypes.ASE)) neList.addAll(neMap.get(NETypes.ASE));

					// Stuff for alternate annotation scheme
					if(neMap.containsKey("CHEMICAL")) neList.addAll(neMap.get("CHEMICAL"));
					if(neMap.containsKey("LIGAND")) neList.addAll(neMap.get("LIGAND"));
					if(neMap.containsKey("FORMULA")) neList.addAll(neMap.get("FORMULA"));
					//if(neMap.containsKey("CLASS")) neList.addAll(neMap.get("CLASS"));

					// Don't include CPR here
					for(List<String> ne : neList) {
						if(ne.size() == 1) {
							if(ne.get(0).matches(".*[a-z][a-z].*")) {
								cwBag.add(ne.get(0));
							} else if(ne.get(0).matches(".*[A-Z].*")) {
								cnwBag.add(ne.get(0));
							}
						} else {
							if(ne.get(0).matches("[A-Z][a-z][a-z]+")) {
								goodPn.add(ne.get(0));
								while(ne.size() > 3 && StringTools.hyphens.contains(ne.get(2)) && ne.get(2).matches("[A-Z][a-z][a-z]+")) {
									ne = ne.subList(2, ne.size());
									goodPn.add(ne.get(0));
								}
							} else {
								for(String neStr : ne) {
									if(neStr.matches(".*[a-z][a-z].*")) cwBag.add(neStr);
								}
							}
						}
					}
					if(neMap.containsKey(NETypes.REACTION)) {
						for(List<String> ne : neMap.get(NETypes.REACTION)) {
							if(ne.size() > 1) {
								rnEnd.add(ne.get(ne.size() - 1));
								for(int j=1;j<ne.size()-1;j++) {
									String s = ne.get(j);
									if(s.matches("[a-z].+")) rnMid.add(s);
								}
							}
						}
					}
					
					for(String nonNe : tokSeq.getNonNes()) {
						if(nonNe.matches(".*[a-z][a-z].*")) {
							ncwBag.add(nonNe.toLowerCase());
						}
						if(nonNe.matches("[A-Z][a-z][a-z]+")) {
							pnStops.add(nonNe);
						}
						Matcher m = notForPrefixPattern.matcher(nonNe);
						if(m.matches()) {
							notForPrefix.add(m.group(1));
						}
						if(nonNe.matches(".*[A-Z].*") && !nonNe.matches("[A-Z][a-z][a-z]+")) {// && !neStrs.contains(token)) {
							ncnwBag.add(nonNe);
						}						
					}
				}		
			} catch (Exception e) {
				e.printStackTrace();
			}
			paperCount++;
		}
		
		for(String s : cwBag.getSet()) {
			if(cwBag.getCount(s) > 0 && ncwBag.getCount(s) == 0) chemicalWords.add(s);
		}
		for(String s : ncwBag.getSet()) {
			if(ncwBag.getCount(s) > 0 && cwBag.getCount(s) == 0) nonChemicalWords.add(s);
		}
		for(String s : cnwBag.getSet()) {
			if(cnwBag.getCount(s) > 0 && ncnwBag.getCount(s) == 0) chemicalNonWords.add(s);
		}
		for(String s : ncnwBag.getSet()) {
			if(ncnwBag.getCount(s) > 0 && cnwBag.getCount(s) == 0) nonChemicalNonWords.add(s);
		}
		Set<String> allChem = new HashSet<String>();
		allChem.addAll(cwBag.getSet());
		allChem.addAll(cnwBag.getSet());
		Set<String> allNonChem = new HashSet<String>();
		allNonChem.addAll(ncwBag.getSet());
		allNonChem.addAll(ncnwBag.getSet());
		
		for(String s : allChem) {
			if(allNonChem.contains(s)) {
				polysemous.add(s);
			}
		}
		
		for(String s : goodPn) {
			if(pnStops.contains(s)) pnStops.remove(s);
		}
		for(String s : nonChemicalWords) {
			if(s.matches("[a-z][a-z][a-z]+")) {
				String newWord = s.substring(0,1).toUpperCase() + s.substring(1);
				if(!goodPn.contains(newWord)) pnStops.add(newWord);
			}
		}
		for(String s : chemicalWords) {
			if(s.matches("[a-z][a-z][a-z]+")) {
				String newWord = s.substring(0,1).toUpperCase() + s.substring(1);
				if(!goodPn.contains(newWord)) pnStops.add(newWord);
			}
		}
		
		try {
			HyphenTokeniser.reinitialise();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
