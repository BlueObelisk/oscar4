package uk.ac.cam.ch.wwmm.oscarrecogniser.etd;

import nu.xom.Document;
import nu.xom.Element;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Extracts and holds useful data from hand-annotated text.
 *
 * @author ptc24
 * @author dmj30
 */
public final class ExtractedTrainingData {

    private static ExtractedTrainingData currentInstance;
    private static ExtractedTrainingData defaultInstance;

    /**Words only found in chemical named entities.*/
    public final Collection<String> chemicalWords;
    /**Words never found in chemical named entities.*/
    public final Collection<String> nonChemicalWords;
    /**Nonwords only found in chemical named entities.*/
    public final Set<String> chemicalNonWords;
    /**Nonwords never found in chemical named entities.*/
    public final Set<String> nonChemicalNonWords;
    /**Words that occur after a hyphen, with a chemical named entity before
     * the hyphen. E.g. "based" from "acetone-based".
     */
    public final Set<String> afterHyphen;
    /**Words where a prefix like 3- should not be interpreted as a CPR*/
    public final Set<String> notForPrefix;
    /**Words with initial capitalisation that are not likely to be
     * proper nouns.
     */
    public final Set<String> pnStops;
    /**Strings seen both in and not in chemical named entities.*/
    public final Set<String> polysemous;
    /**Words found at the end of multi-word reaction names.*/
    public final Set<String> rnEnd;
    /**Words found in the middle of multi-word reaction names.*/
    public final Set<String> rnMid;


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


    public ExtractedTrainingData(Element xml) {
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

    /**Get the current singleton. If this does not exist, initialise it using
     * the current model file.
     *
     * @return The singleton.
     */
    public static ExtractedTrainingData getInstance() {
        if (currentInstance == null) {
            currentInstance = getDefaultInstance();
        }
        return currentInstance;
    }

    public static ExtractedTrainingData getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = loadDefaultInstance();
        }
        return defaultInstance;
    }

    private static ExtractedTrainingData loadDefaultInstance() {
        String modelName = OscarProperties.getData().model;
        Element etdElement = loadEtdElement(modelName);
        return new ExtractedTrainingData(etdElement);
    }


    static Element loadEtdElement(String modelName) {
        ResourceGetter rg = new ResourceGetter("/uk/ac/cam/ch/wwmm/oscarrecogniser/models/");
        Document modelDoc = rg.getXMLDocument(modelName + ".xml");
        if (modelDoc == null) {
            return null;
        }
        return modelDoc.getRootElement().getFirstChildElement("etd");
    }

    /**
     * Re-initialise the current singleton, using the current model file.
     */
    public static void reinitialise() {
        currentInstance = getDefaultInstance();
    }

    /**Re-initialise the current singleton, given an XML serialization
     * produced by toXML.
     *
     * @param elem The XML serialized data.
     */
    public static ExtractedTrainingData reinitialise(Element elem) {
        currentInstance = new ExtractedTrainingData(elem);
        return currentInstance;
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


    private Set<String> readStringsFromElement(Element elem) {
        try {
            Set<String> strings = new HashSet<String>();
            BufferedReader br = new BufferedReader(new StringReader(elem.getValue()));
            String line = br.readLine();
            while(line != null) {
                strings.add(line.trim());
                line = br.readLine();
            }
            return Collections.unmodifiableSet(strings);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}