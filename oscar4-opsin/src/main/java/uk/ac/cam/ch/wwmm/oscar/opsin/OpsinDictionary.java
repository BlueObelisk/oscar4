package uk.ac.cam.ch.wwmm.oscar.opsin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
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
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ISMILESProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.IChemRecord;

/**
 * A class to interface OSCAR name resolution with the Open Parser for
 * Systematic IUPAC Nomenclature (OPSIN). 
 *
 */
public class OpsinDictionary implements IChemNameDict, IInChIProvider, ICMLProvider, ISMILESProvider {

	private URI uri;
	
	public OpsinDictionary() {
		try{
			uri = new URI("http://wwmm.cam.ac.uk/oscar/dictionary/opsin/");
		}
		catch (URISyntaxException e) {
			// Should not be thrown, as URL is valid.
			throw new RuntimeException(e);
		}
	}

	public URI getURI() {
		return uri;
	}

	/**
	 * Returns false. 
	 */
	public boolean hasStopWord(String queryWord) {
		return false;
	}

	/**
	 * Returns {@link Collections#emptySet()}, since the OPSIN dictionary
	 * does not store stopwords.
	 */
	public Set<String> getStopWords() {
		return Collections.emptySet();
	}

	/***
	 * Returns false for performance reasons. To check whether a name is
	 * interpretable by OPSIN use getCML().size()!=0
	 */
	public boolean hasName(String queryName) {
		return false;
	}

	public Set<String> getInChI(String queryName) {
		NameToStructure nts;
		try {
			nts = NameToStructure.getInstance();
		} catch (NameToStructureException e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
		OpsinResult result = nts.parseChemicalName(queryName);
		if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
			Set<String> inchis = new HashSet<String>();
			String inchi = NameToInchi.convertResultToInChI(result);
			inchis.add(inchi);
			return inchis;
		}
		return Collections.emptySet();
	}

	
	/**
	 * Returns {@link Collections#emptySet()}, since OPSIN does not support
	 * InChI-to-name conversion.
	 */
	public Set<String> getNames(String inchi) {
		return Collections.emptySet();
	}

	/**
	 * Returns {@link Collections#emptySet()}, since the OPSIN dictionary
	 * interprets rather than stores names.
	 */
	public Set<String> getNames() {
		return Collections.emptySet();
	}

	/**
	 * Returns {@link Collections#emptySet()}, since the OPSIN dictionary
	 * does not store names. 
	 */
	public Set<String> getOrphanNames() {
		return Collections.emptySet();
	}

	/**
	 * Returns {@link Collections#emptySet()}, since the OPSIN dictionary
	 * does not store names. 
	 */
	public Set<IChemRecord> getChemRecords() {
		return Collections.emptySet();
	}

	/**
	 * Returns false, since the OPSIN dictionary does not store ontology
	 * ids.
	 */
	public boolean hasOntologyIdentifier(String identifier) {
		return false;
	}

	public Set<Element> getCML(String queryName) {
		try {
			NameToStructure nameToStructure = NameToStructure.getInstance();
			OpsinResult result = nameToStructure.parseChemicalName(queryName);
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

	public Locale getLanguage() {
		return Locale.ENGLISH;
	}

	public Set<String> getSMILES(String queryName) {
		try {
			NameToStructure nameToStructure = NameToStructure.getInstance();
			OpsinResult result = nameToStructure.parseChemicalName(queryName);
			if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
				Set<String> smiles = new HashSet<String>();
				smiles.add(result.getSmiles());
				return smiles;
			}
		} catch (NameToStructureException e) {
			e.printStackTrace();			
		}
		return Collections.emptySet();
	}

	public String getShortestSMILES(String queryName) {
		Set<String> smiles = getSMILES(queryName);
		if (smiles.size()>0){
			return smiles.iterator().next();
		}
		return null;
	}
}
