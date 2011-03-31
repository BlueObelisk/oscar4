package uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

/** Extracts and holds useful data from hand-annotated text.
 *
 * @author ptc24
 * @author dmj30
 */
public class ExtractedTrainingData {

    private static ExtractedTrainingData defaultInstance;

    /**Words only found in chemical named entities.*/
    private final Set<String> chemicalWords;
    /**Words never found in chemical named entities.*/
    private final Set<String> nonChemicalWords;
    /**Nonwords only found in chemical named entities.*/
    private final Set<String> chemicalNonWords;
    /**Nonwords never found in chemical named entities.*/
    private final Set<String> nonChemicalNonWords;
    /**Words that occur after a hyphen, with a chemical named entity before
     * the hyphen. E.g. "based" from "acetone-based".
     */
    private final Set<String> afterHyphen;
    /**Words where a prefix like 3- should not be interpreted as a CPR*/
    private final Set<String> notForPrefix;
    /**Words with initial capitalisation that are not likely to be
     * proper nouns.
     */
    private final Set<String> pnStops;
    /**Strings seen both in and not in chemical named entities.*/
    private final Set<String> polysemous;
    /**Words found at the end of multi-word reaction names.*/
    private final Set<String> rnEnd;
    /**Words found in the middle of multi-word reaction names.*/
    private final Set<String> rnMid;


    /**
     * Creates an ExtractedTrainingData object in which all the term sets
     * are initialised but empty.
     */
    public ExtractedTrainingData() {
        chemicalWords = Collections.emptySet();
        nonChemicalWords = Collections.emptySet();
        afterHyphen = Collections.emptySet();
        chemicalNonWords = Collections.emptySet();
        nonChemicalNonWords = Collections.emptySet();
        pnStops = Collections.emptySet();
        notForPrefix = Collections.emptySet();
        polysemous = Collections.emptySet();
        rnEnd = Collections.emptySet();
        rnMid = Collections.emptySet();
    }


    /**
     * 
     * Creates a ExtractedTrainingData object in which the term sets
     * are loaded from the given XML element.
     */
    public ExtractedTrainingData(Element xml) {
        try {
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
        catch (IOException e) {
        	throw new OscarInitialisationException("failed to load ExtractedTrainingData", e);
        }
    }


    /**
     * Creates a ExtractedTrainingData object in which the term sets
     * are loaded from the default (chempapers) model file.
     */
    public static ExtractedTrainingData getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = loadExtractedTrainingData("chempapers");
        }
        return defaultInstance;
    }

    /**
     * Creates a ExtractedTrainingData object in which the term sets
     * are loaded from the specified model file.
     * @param modelName the name of the model file, excluding ".xml" 
     */
    public static ExtractedTrainingData loadExtractedTrainingData(String modelName) {
    	Element etdElement = loadEtdElement(modelName);
        return new ExtractedTrainingData(etdElement);
    }

    static Element loadEtdElement(String modelName) {
        ResourceGetter rg = new ResourceGetter(ExtractedTrainingData.class.getClassLoader(),"/uk/ac/cam/ch/wwmm/oscarrecogniser/models/");
        Document modelDoc;
		try {
			modelDoc = rg.getXMLDocument(modelName + ".xml");
		} catch (ParsingException e) {
			throw new OscarInitialisationException("failed to load ExtractedTrainingData for model: " + modelName, e);
		} catch (IOException e) {
			throw new OscarInitialisationException("failed to load ExtractedTrainingData for model: " + modelName, e);
		}
        if (modelDoc == null) {
            return null;
        }
        return modelDoc.getRootElement().getFirstChildElement("etd");
    }


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


    private Set<String> readStringsFromElement(Element elem) throws IOException {
        Set<String> strings = new HashSet<String>();
        BufferedReader br = new BufferedReader(new StringReader(elem.getValue()));
        String line = br.readLine();
        while(line != null) {
            strings.add(line.trim());
            line = br.readLine();
        }
        return Collections.unmodifiableSet(strings);
    }


	public Set <String> getNotForPrefix() {
		return notForPrefix;
	}

	public Set <String> getNonChemicalWords() {
		return nonChemicalWords;
	}

	public Set<String> getChemicalWords() {
		return chemicalWords;
	}

	public Set<String> getChemicalNonWords() {
		return chemicalNonWords;
	}

	public Set<String> getNonChemicalNonWords() {
		return nonChemicalNonWords;
	}

	public Set<String> getAfterHyphen() {
		return afterHyphen;
	}

	public Set<String> getPnStops() {
		return pnStops;
	}

	public Set<String> getPolysemous() {
		return polysemous;
	}

	public Set<String> getRnEnd() {
		return rnEnd;
	}

	public Set<String> getRnMid() {
		return rnMid;
	}

}
