package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import nu.xom.Element;

/** Extracts and holds useful data from hand-annotated text.
 * 
 * @author ptc24
 *
 */
public final class ExtractedTrainingData {
	
	private static ExtractedTrainingData myInstance;
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

	/**Get the current singleton. If this does not exist, initialise it using
	 * the current model file.
	 * 
	 * @return The singleton.
	 */
	public static ExtractedTrainingData getInstance() {
		if(myInstance == null) {
			new ChemPapersModel().getExtractedTrainingData();
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

	/**Re-initialise the current singleton, given an XML serialization
	 * produced by toXML.
	 * 
	 * @param elem The XML serialized data.
	 */
	public static void reinitialise(Element elem) {
		myInstance = new ExtractedTrainingData();
		myInstance.readXML(elem);
	}

	/**Destroy the current singleton.
	 * 
	 */
	public static void clear() {
		myInstance = new ExtractedTrainingData();		
	}
	
	/**Looks in the workspace for scrapbook files, extracts the data therein,
	 * and initialises the singleton from it.
	 * 
	 * @return If the procedure succeeded.
	 * Commented out on 27th Jan
	 */
//	public static boolean trainFromScrapbook() {
//	OscarPropertiesoperties.getInstance().workspace.equals("none")) return false;
//		File scrapbookdir = nOscarPropertiesarProperties.getInstance().workspace, "scrapbook");
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

	private ExtractedTrainingData() {
		initSets();
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

}
