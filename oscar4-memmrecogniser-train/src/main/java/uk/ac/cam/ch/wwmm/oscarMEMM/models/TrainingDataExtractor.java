package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.XOMBasedProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.XOMBasedProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.tools.InlineToSAF;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;
import uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata.ExtractedTrainingData;
import uk.ac.cam.ch.wwmm.oscarrecogniser.ptcDataStruct.Bag;
import uk.ac.cam.ch.wwmm.oscartokeniser.HyphenTokeniser;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * Extracts and holds useful data from hand-annotated text,
 * allowing the creation of {@link ExtractedTrainingData} objects.
 * 
 * @author ptc24
 * @author egonw
 */
public final class TrainingDataExtractor {

	/** Words only found in chemical named entities. */
	public Collection<String> chemicalWords;
	/** Words never found in chemical named entities. */
	public Collection<String> nonChemicalWords;
	/** Nonwords only found in chemical named entities. */
	public Set<String> chemicalNonWords;
	/** Nonwords never found in chemical named entities. */
	public Set<String> nonChemicalNonWords;
	/**
	 * Words that occur after a hyphen, with a chemical named entity before the
	 * hyphen. E.g. "based" from "acetone-based".
	 */
	public Set<String> afterHyphen;
	/** Words where a prefix like 3- should not be interpreted as a CPR */
	public Set<String> notForPrefix;
	/**
	 * Words with initial capitalisation that are not likely to be proper nouns.
	 */
	public Set<String> pnStops;
	/** Strings seen both in and not in chemical named entities. */
	public Set<String> polysemous;
	/** Words found at the end of multi-word reaction names. */
	public Set<String> rnEnd;
	/** Words found in the middle of multi-word reaction names. */
	public Set<String> rnMid;

	private static Pattern notForPrefixPattern = Pattern
			.compile("[0-9]+-([a-z]+)");

	private Element stringsToElement(Collection<String> strings, String elemName) {
		Element elem = new Element(elemName);
		StringBuffer sb = new StringBuffer();
		for (String string : strings) {
			sb.append(string);
			sb.append("\n");
		}
		elem.appendChild(sb.toString());
		return elem;
	}

	/**
	 * Produces an XML serialization of the data.
	 * 
	 * @return The XML Element containing the serialization.
	 */
	public Element toXML() {
		Element etdElem = new Element("etd");
		etdElem.appendChild(stringsToElement(chemicalWords, "chemicalWords"));
		etdElem.appendChild(stringsToElement(nonChemicalWords,
				"nonChemicalWords"));
		etdElem.appendChild(stringsToElement(chemicalNonWords,
				"chemicalNonWords"));
		etdElem.appendChild(stringsToElement(nonChemicalNonWords,
				"nonChemicalNonWords"));
		etdElem.appendChild(stringsToElement(afterHyphen, "afterHyphen"));
		etdElem.appendChild(stringsToElement(notForPrefix, "notForPrefix"));
		etdElem.appendChild(stringsToElement(pnStops, "pnStops"));
		etdElem.appendChild(stringsToElement(polysemous, "polysemous"));
		etdElem.appendChild(stringsToElement(rnEnd, "rnEnd"));
		etdElem.appendChild(stringsToElement(rnMid, "rnMid"));
		return etdElem;
	}

	/**
	 * Produce a hash code for the current ExtractTrainingData.
	 * 
	 * @return The hash code.
	 */
	public int makeHash() {
		return toXML().toXML().hashCode();
	}

	/**
	 * Makes a new ExtractTrainingData from a collection of (ScrapBook) documents.
	 * 
	 * @param docs
	 *            The files.
	 */
	public TrainingDataExtractor(Collection<Document> docs) {
		init(docs);
	}

	public TrainingDataExtractor(Document doc) {
		init(doc);
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

	private void init(Collection<Document> docs) {
		Set<String> goodPn = new HashSet<String>();

		initSets();
		HyphenTokeniser.reinitialise();
		Bag<String> cwBag = new Bag<String>();
		Bag<String> cnwBag = new Bag<String>();
		Bag<String> ncwBag = new Bag<String>();
		Bag<String> ncnwBag = new Bag<String>();

		int paperCount = 0;
		for (Document doc : docs) {
			loadAnnotations(goodPn, cwBag, cnwBag, ncwBag, ncnwBag, doc);
			paperCount++;
		}

		
		for (String s : cwBag.getSet()) {
			if (cwBag.getCount(s) > 0 && ncwBag.getCount(s) == 0)
				chemicalWords.add(s);
		}
		for (String s : ncwBag.getSet()) {
			if (ncwBag.getCount(s) > 0 && cwBag.getCount(s) == 0 && (!chemicalWords.contains(s)))
				nonChemicalWords.add(s);
		}
		for (String s : cnwBag.getSet()) {
			if (cnwBag.getCount(s) > 0 && ncnwBag.getCount(s) == 0)
				chemicalNonWords.add(s);
		}
		for (String s : ncnwBag.getSet()) {
			if (ncnwBag.getCount(s) > 0 && cnwBag.getCount(s) == 0)
				nonChemicalNonWords.add(s);
		}
		Set<String> allChem = new HashSet<String>();
		allChem.addAll(cwBag.getSet());
		allChem.addAll(cnwBag.getSet());
		Set<String> allNonChem = new HashSet<String>();
		allNonChem.addAll(ncwBag.getSet());
		allNonChem.addAll(ncnwBag.getSet());

		for (String s : allChem) {
			if (allNonChem.contains(s)) {
				polysemous.add(s);
			}
		}

		for (String s : goodPn) {
			if (pnStops.contains(s))
				pnStops.remove(s);
		}
		for (String s : nonChemicalWords) {
			if (s.matches("[a-z][a-z][a-z]+")) {
				String newWord = s.substring(0, 1).toUpperCase()
						+ s.substring(1);
				if (!goodPn.contains(newWord))
					pnStops.add(newWord);
			}
		}
		for (String s : chemicalWords) {
			if (s.matches("[a-z][a-z][a-z]+")) {
				String newWord = s.substring(0, 1).toUpperCase()
						+ s.substring(1);
				if (!goodPn.contains(newWord))
					pnStops.add(newWord);
			}
		}

		HyphenTokeniser.reinitialise();
	}

	private void init(Document doc) {
		Set<String> goodPn = new HashSet<String>();

		initSets();
		HyphenTokeniser.reinitialise();
		Bag<String> cwBag = new Bag<String>();
		Bag<String> cnwBag = new Bag<String>();
		Bag<String> ncwBag = new Bag<String>();
		Bag<String> ncnwBag = new Bag<String>();

		loadAnnotations(goodPn, cwBag, cnwBag, ncwBag, ncnwBag, doc);

		for (String s : cwBag.getSet()) {
			if (cwBag.getCount(s) > 0 && ncwBag.getCount(s) == 0)
				chemicalWords.add(s);
		}
		for (String s : ncwBag.getSet()) {
			if (ncwBag.getCount(s) > 0 && cwBag.getCount(s) == 0)
				nonChemicalWords.add(s);
		}
		for (String s : cnwBag.getSet()) {
			if (cnwBag.getCount(s) > 0 && ncnwBag.getCount(s) == 0)
				chemicalNonWords.add(s);
		}
		for (String s : ncnwBag.getSet()) {
			if (ncnwBag.getCount(s) > 0 && cnwBag.getCount(s) == 0)
				nonChemicalNonWords.add(s);
		}

		Set<String> allChem = new HashSet<String>();
		allChem.addAll(cwBag.getSet());
		allChem.addAll(cnwBag.getSet());
		Set<String> allNonChem = new HashSet<String>();
		allNonChem.addAll(ncwBag.getSet());
		allNonChem.addAll(ncnwBag.getSet());

		for (String s : allChem) {
			if (allNonChem.contains(s)) {
				polysemous.add(s);
			}
		}

		for (String s : goodPn) {
			if (pnStops.contains(s))
				pnStops.remove(s);
		}
		for (String s : nonChemicalWords) {
			if (s.matches("[a-z][a-z][a-z]+")) {
				String newWord = s.substring(0, 1).toUpperCase()
						+ s.substring(1);
				if (!goodPn.contains(newWord))
					pnStops.add(newWord);
			}
		}
		for (String s : chemicalWords) {
			if (s.matches("[a-z][a-z][a-z]+")) {
				String newWord = s.substring(0, 1).toUpperCase()
						+ s.substring(1);
				if (!goodPn.contains(newWord))
					pnStops.add(newWord);
			}
		}

		HyphenTokeniser.reinitialise();
	}

	private void loadAnnotations(Set<String> goodPn, Bag<String> cwBag,
			Bag<String> cnwBag, Bag<String> ncwBag, Bag<String> ncnwBag,
			Document doc) {
		Nodes n = doc.query("//cmlPile");
		for (int i = 0; i < n.size(); i++) {
			n.get(i).detach();
		}

		Document copy = new Document((Element) XOMTools.safeCopy(doc
				.getRootElement()));
		n = copy.query("//ne");
		for (int i = 0; i < n.size(); i++) {
			XOMTools.removeElementPreservingText((Element) n.get(i));
		}
		Document safDoc = InlineToSAF.extractSAFs(doc, copy, "foo");
		
		doc = copy;
		XOMBasedProcessingDocument procDoc = XOMBasedProcessingDocumentFactory
				.getInstance().makeTokenisedDocument(
						Tokeniser.getDefaultInstance(), doc, true, false, safDoc);
		for (TokenSequence tokSeq : procDoc.getTokenSequences()) {
			afterHyphen.addAll(tokSeq.getAfterHyphens());
			Map<NamedEntityType, List<List<String>>> neMap = tokSeq
					.getNes();
			List<List<String>> neList = new ArrayList<List<String>>();
			if (neMap.containsKey(NamedEntityType.COMPOUND))
				neList.addAll(neMap.get(NamedEntityType.COMPOUND));
			if (neMap.containsKey(NamedEntityType.ADJECTIVE))
				neList.addAll(neMap.get(NamedEntityType.ADJECTIVE));
			if (neMap.containsKey(NamedEntityType.REACTION))
				neList.addAll(neMap.get(NamedEntityType.REACTION));
			if (neMap.containsKey(NamedEntityType.ASE))
				neList.addAll(neMap.get(NamedEntityType.ASE));

			// Don't include CPR here

			for (List<String> ne : neList) {

				if (ne.size() == 1) {
					//single-token named entity
					if (ne.get(0).matches(".*[a-z][a-z].*")) {
						cwBag.add(ne.get(0));
					} else if (ne.get(0).matches(".*[A-Z].*")) {
						cnwBag.add(ne.get(0));
					}
				} else {
					//multi-token named entity
					if (ne.get(0).matches("[A-Z][a-z][a-z]+")) {
						goodPn.add(ne.get(0));
						while (ne.size() > 3
								&& StringTools.isHyphen(ne.get(2))
								&& ne.get(2).matches("[A-Z][a-z][a-z]+")) {
							ne = ne.subList(2, ne.size());
							goodPn.add(ne.get(0));
						}
					} else {
						for (String neStr : ne) {
							if (neStr.matches(".*[a-z][a-z].*"))
								cwBag.add(neStr);
						}
					}
				}
			}

			if (neMap.containsKey(NamedEntityType.REACTION)) {
				for (List<String> ne : neMap.get(NamedEntityType.REACTION)) {
					if (ne.size() > 1) {
						rnEnd.add(ne.get(ne.size() - 1));
						for (int j = 1; j < ne.size() - 1; j++) {
							String s = ne.get(j);
							if (s.matches("[a-z].+"))
								rnMid.add(s);
						}
					}
				}
			}

			
			for (String nonNe : tokSeq.getNonNes()) { 
				if (nonNe.matches(".*[a-z][a-z].*")) {
					
					ncwBag.add(nonNe.toLowerCase());
				}
				if (nonNe.matches("[A-Z][a-z][a-z]+")) {
					pnStops.add(nonNe);
				}
				Matcher m = notForPrefixPattern.matcher(nonNe);
				if (m.matches()) {
					notForPrefix.add(m.group(1));
				}
				if (nonNe.matches(".*[A-Z].*")
						&& !nonNe.matches("[A-Z][a-z][a-z]+")) {// &&
																// !neStrs.contains(token))
																// {
					ncnwBag.add(nonNe);
				}
			}
		}
	}
	
}
