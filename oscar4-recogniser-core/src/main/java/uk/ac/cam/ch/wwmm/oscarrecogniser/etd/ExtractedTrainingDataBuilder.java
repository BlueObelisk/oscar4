package uk.ac.cam.ch.wwmm.oscarrecogniser.etd;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.tools.InlineToSAF;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityTypes;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;
import uk.ac.cam.ch.wwmm.oscarrecogniser.ptcDataStruct.Bag;
import uk.ac.cam.ch.wwmm.oscartokeniser.HyphenTokeniser;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sea36
 */
public class ExtractedTrainingDataBuilder {

    /**Words only found in chemical named entities.*/
    private Collection<String> chemicalWords;
    /**Words never found in chemical named entities.*/
    private Collection<String> nonChemicalWords;
    /**Nonwords only found in chemical named entities.*/
    private Set<String> chemicalNonWords;
    /**Nonwords never found in chemical named entities.*/
    private Set<String> nonChemicalNonWords;
    /**Words that occur after a hyphen, with a chemical named entity before
     * the hyphen. E.g. "based" from "acetone-based".
     */
    private Set<String> afterHyphen;
    /**Words where a prefix like 3- should not be interpreted as a CPR*/
    private Set<String> notForPrefix;
    /**Words with initial capitalisation that are not likely to be
     * proper nouns.
     */
    private Set<String> pnStops;
    /**Strings seen both in and not in chemical named entities.*/
    private Set<String> polysemous;
    /**Words found at the end of multi-word reaction names.*/
    private Set<String> rnEnd;
    /**Words found in the middle of multi-word reaction names.*/
    private Set<String> rnMid;


    private static Pattern notForPrefixPattern = Pattern.compile("[0-9]+-([a-z]+)");


    /**Makes a new ExtractTrainingData from a collection of (ScrapBook) files.
     *
     * @param files The files.
     */
    public ExtractedTrainingDataBuilder(Collection<File> files) {
        this();
        init(files);
    }

    private ExtractedTrainingDataBuilder() {
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
        Set<String> goodPn = new HashSet<String>();

        try {
            HyphenTokeniser.reinitialise();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Bag<String> chemicalWordBag = new Bag<String>();
        Bag<String> chemicalNonWordBag = new Bag<String>();
        Bag<String> nonChemicalWordBag = new Bag<String>();
        Bag<String> nonChemicalNonWordBag = new Bag<String>();

        int paperCount = 0;
        for (File f : files) {
            try {
                Document doc = new Builder().build(f);

                Nodes n = doc.query("//cmlPile");
                for (int i = 0; i < n.size(); i++) {
                    n.get(i).detach();
                }

                Document copy = new Document((Element)XOMTools.safeCopy(doc.getRootElement()));
                n = copy.query("//ne");
                for (int i = 0; i < n.size(); i++) {
                    XOMTools.removeElementPreservingText((Element)n.get(i));
                }
                Document safDoc = InlineToSAF.extractSAFs(doc, copy, "foo");
                doc = copy;

                IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(Tokeniser.getInstance(), doc, true, false, false, safDoc);
                for (TokenSequence tokSeq : procDoc.getTokenSequences()) {
                    afterHyphen.addAll(tokSeq.getAfterHyphens());
                    Map<String, List<List<String>>> namedEntityMap = tokSeq.getNes();
                    List<List<String>> namedEntityList = new ArrayList<List<String>>();
                    if(namedEntityMap.containsKey(NamedEntityTypes.COMPOUND)) namedEntityList.addAll(namedEntityMap.get(NamedEntityTypes.COMPOUND));
                    if(namedEntityMap.containsKey(NamedEntityTypes.ADJECTIVE)) namedEntityList.addAll(namedEntityMap.get(NamedEntityTypes.ADJECTIVE));
                    if(namedEntityMap.containsKey(NamedEntityTypes.REACTION)) namedEntityList.addAll(namedEntityMap.get(NamedEntityTypes.REACTION));
                    if(namedEntityMap.containsKey(NamedEntityTypes.ASE)) namedEntityList.addAll(namedEntityMap.get(NamedEntityTypes.ASE));

                    // Stuff for alternate annotation scheme
                    if(namedEntityMap.containsKey("CHEMICAL")) namedEntityList.addAll(namedEntityMap.get("CHEMICAL"));
                    if(namedEntityMap.containsKey("LIGAND")) namedEntityList.addAll(namedEntityMap.get("LIGAND"));
                    if(namedEntityMap.containsKey("FORMULA")) namedEntityList.addAll(namedEntityMap.get("FORMULA"));
                    //if(neMap.containsKey("CLASS")) neList.addAll(neMap.get("CLASS"));

                    // Don't include CPR here
                    for(List<String> ne : namedEntityList) {
                        if(ne.size() == 1) {
                            if(ne.get(0).matches(".*[a-z][a-z].*")) {
                                chemicalWordBag.add(ne.get(0));
                            } else if(ne.get(0).matches(".*[A-Z].*")) {
                                chemicalNonWordBag.add(ne.get(0));
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
                                    if(neStr.matches(".*[a-z][a-z].*")) chemicalWordBag.add(neStr);
                                }
                            }
                        }
                    }
                    if(namedEntityMap.containsKey(NamedEntityTypes.REACTION)) {
                        for(List<String> ne : namedEntityMap.get(NamedEntityTypes.REACTION)) {
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
                            nonChemicalWordBag.add(nonNe.toLowerCase());
                        }
                        if(nonNe.matches("[A-Z][a-z][a-z]+")) {
                            pnStops.add(nonNe);
                        }
                        Matcher m = notForPrefixPattern.matcher(nonNe);
                        if(m.matches()) {
                            notForPrefix.add(m.group(1));
                        }
                        if(nonNe.matches(".*[A-Z].*") && !nonNe.matches("[A-Z][a-z][a-z]+")) {// && !neStrs.contains(token)) {
                            nonChemicalNonWordBag.add(nonNe);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            paperCount++;
        }

        for(String s : chemicalWordBag.getSet()) {
            if(chemicalWordBag.getCount(s) > 0 && nonChemicalWordBag.getCount(s) == 0) chemicalWords.add(s);
        }
        for(String s : nonChemicalWordBag.getSet()) {
            if(nonChemicalWordBag.getCount(s) > 0 && chemicalWordBag.getCount(s) == 0) nonChemicalWords.add(s);
        }
        for(String s : chemicalNonWordBag.getSet()) {
            if(chemicalNonWordBag.getCount(s) > 0 && nonChemicalNonWordBag.getCount(s) == 0) chemicalNonWords.add(s);
        }
        for(String s : nonChemicalNonWordBag.getSet()) {
            if(nonChemicalNonWordBag.getCount(s) > 0 && chemicalNonWordBag.getCount(s) == 0) nonChemicalNonWords.add(s);
        }
        Set<String> allChem = new HashSet<String>();
        allChem.addAll(chemicalWordBag.getSet());
        allChem.addAll(chemicalNonWordBag.getSet());
        Set<String> allNonChem = new HashSet<String>();
        allNonChem.addAll(nonChemicalWordBag.getSet());
        allNonChem.addAll(nonChemicalNonWordBag.getSet());

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


    private static Element stringsToElement(Collection<String> strings, String elemName) {
        Element elem = new Element(elemName);
        StringBuffer sb = new StringBuffer();
        for(String string : strings) {
            sb.append(string);
            sb.append("\n");
        }
        elem.appendChild(sb.toString());
        return elem;
    }

}
