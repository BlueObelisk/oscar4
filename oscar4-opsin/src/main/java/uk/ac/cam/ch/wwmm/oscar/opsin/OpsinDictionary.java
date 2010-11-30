package uk.ac.cam.ch.wwmm.oscar.opsin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nu.xom.Element;

import uk.ac.cam.ch.wwmm.opsin.NameToInchi;
import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.NameToStructureException;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult.OPSIN_RESULT_STATUS;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ICMLProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IInChIProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.IChemRecord;

public class OpsinDictionary implements IChemNameDict, IInChIProvider, ICMLProvider {

	private URI uri;
	
	public OpsinDictionary() throws URISyntaxException {
		this.uri = new URI("http://wwmm.cam.ac.uk/oscar/dictionary/opsin/");
	}

	public URI getURI() {
		return uri;
	}

	public boolean hasStopWord(String queryWord) {
		return false;
	}

	public Set<String> getStopWords() {
		return Collections.emptySet();
	}

	public boolean hasName(String queryName) {
		return getCML(queryName).size() != 0;
	}

	public Set<String> getInChI(String queryName) {
		try {
			NameToStructure nameToStructure = NameToStructure.getInstance();
			OpsinResult result = nameToStructure.parseChemicalName(
				queryName, false
			);
			if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
				Set<String> inchis = new HashSet<String>();
				String inchi = NameToInchi.convertResultToInChI(result, false);
				inchis.add(inchi);
				return inchis;
			}
		} catch (NameToStructureException e) {
			e.printStackTrace();			
		}
		return Collections.emptySet();
	}

	public Set<String> getNames(String inchi) {
		return Collections.emptySet();
	}

	public Set<String> getNames() {
		return Collections.emptySet();
	}

	public Set<String> getOrphanNames() {
		return Collections.emptySet();
	}

	public Set<IChemRecord> getChemRecords() {
		return Collections.emptySet();
	}

	public boolean hasOntologyIdentifier(String identifier) {
		// this ontology does not use ontology identifiers
		return false;
	}

	public Set<Element> getCML(String queryName) {
		try {
			NameToStructure nameToStructure = NameToStructure.getInstance();
			OpsinResult result = nameToStructure.parseChemicalName(
				queryName, false
			);
			if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
				Set<Element> cmls = new HashSet<Element>();
				Element cml = result.getCml();
				cmls.add(cml);
				return cmls;
			}
		} catch (NameToStructureException e) {
			e.printStackTrace();			
		}
		return Collections.emptySet();
	}
}
