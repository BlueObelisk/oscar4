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
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ICMLProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IInChIProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ISMILESProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IStdInChIKeyProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IStdInChIProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.IChemRecord;

/**
 * A class to interface OSCAR name resolution with the Open Parser for
 * Systematic IUPAC Nomenclature (OPSIN).
 * 
 * @author egonw
 * @author dmj30
 */
public class OpsinDictionary implements IChemNameDict, IInChIProvider,
		IStdInChIProvider, IStdInChIKeyProvider, ICMLProvider, ISMILESProvider {

	private URI uri;

	public OpsinDictionary() {
		try {
			uri = new URI("http://wwmm.cam.ac.uk/oscar/dictionary/opsin/");
		} catch (URISyntaxException e) {
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
	 * Returns {@link Collections#emptySet()}, since the OPSIN dictionary does
	 * not store stopwords.
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

	/**
	 * Performs name to structure conversion on the given name A set containing
	 * 1 or 0 InChIs is returned depending on whether or not the name was
	 * interpretable
	 */
	public Set<String> getInchis(String queryName) {
		NameToStructure nts;
		try {
			nts = NameToStructure.getInstance();
		} catch (NameToStructureException e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
		OpsinResult result = nts.parseChemicalName(queryName);
		if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
			String inchi = NameToInchi.convertResultToInChI(result);
			if (inchi != null) {// null if conversion fails e.g. structure is a
								// polymer
				Set<String> inchis = new HashSet<String>();
				inchis.add(inchi);
				return inchis;
			}
		}
		return Collections.emptySet();
	}

	/**
	 * Performs name to structure conversion on the given name A set containing
	 * 1 or 0 StdInChIs is returned depending on whether or not the name was
	 * interpretable
	 */
	public Set<String> getStdInchis(String queryName) {
		NameToStructure nts;
		try {
			nts = NameToStructure.getInstance();
		} catch (NameToStructureException e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
		OpsinResult result = nts.parseChemicalName(queryName);
		if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
			String stdInchi = NameToInchi.convertResultToStdInChI(result);
			if (stdInchi != null) {// null if conversion fails e.g. structure is
									// a polymer
				Set<String> stdInchis = new HashSet<String>();
				stdInchis.add(stdInchi);
				return stdInchis;
			}
		}
		return Collections.emptySet();
	}

	/**
	 * Performs name to structure conversion on the given name A set containing
	 * 1 or 0 StdInChIKeys is returned depending on whether or not the name was
	 * interpretable
	 */
	public Set<String> getStdInchiKeys(String queryName) {
		NameToStructure nts;
		try {
			nts = NameToStructure.getInstance();
		} catch (NameToStructureException e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
		OpsinResult result = nts.parseChemicalName(queryName);
		if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
			String stdInchiKey = NameToInchi.convertResultToStdInChIKey(result);
			if (stdInchiKey != null) {// null if conversion fails e.g. structure
										// is a polymer
				Set<String> stdInchiKeys = new HashSet<String>();
				stdInchiKeys.add(stdInchiKey);
				return stdInchiKeys;
			}
		}
		return Collections.emptySet();
	}

	/**
	 * Returns {@link Collections#emptySet()}, since OPSIN does not support
	 * Standard InChI-to-name conversion.
	 */
	public Set<String> getNames(String stdInchi) {
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
	 * Returns {@link Collections#emptySet()}, since the OPSIN dictionary does
	 * not store names.
	 */
	public Set<String> getOrphanNames() {
		return Collections.emptySet();
	}

	/**
	 * Returns {@link Collections#emptySet()}, since the OPSIN dictionary does
	 * not store names.
	 */
	public Set<IChemRecord> getChemRecords() {
		return Collections.emptySet();
	}

	/**
	 * Returns false, since the OPSIN dictionary does not store ontology ids.
	 */
	public boolean hasOntologyIdentifier(String identifier) {
		return false;
	}

	/**
	 * Performs name to structure conversion on the given name A set containing
	 * 1 or 0 CML elements is returned depending on whether or not the name was
	 * interpretable
	 */
	public Set<Element> getCML(String queryName) {
		NameToStructure nts;
		try {
			nts = NameToStructure.getInstance();
		} catch (NameToStructureException e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
		OpsinResult result = nts.parseChemicalName(queryName);
		if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
			Set<Element> cmls = new HashSet<Element>();
			Element cml = result.getCml();
			cmls.add(cml);
			return cmls;
		}
		return Collections.emptySet();
	}

	public Locale getLanguage() {
		return Locale.ENGLISH;
	}

	/**
	 * Performs name to structure conversion on the given name A set containing
	 * 1 or 0 SMILES strings is returned depending on whether or not the name
	 * was interpretable
	 */
	public Set<String> getAllSmiles(String queryName) {
		NameToStructure nts;
		try {
			nts = NameToStructure.getInstance();
		} catch (NameToStructureException e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
		OpsinResult result = nts.parseChemicalName(queryName);
		if (result.getStatus() == OPSIN_RESULT_STATUS.SUCCESS) {
			Set<String> smiles = new HashSet<String>();
			smiles.add(result.getSmiles());
			return smiles;
		}
		return Collections.emptySet();
	}

	/**
	 * Performs name to structure conversion on the given name A SMILES string
	 * or null is returned depending on whether or not the name was
	 * interpretable
	 */
	public String getShortestSmiles(String queryName) {
		Set<String> smiles = getAllSmiles(queryName);
		if (smiles.size() > 0) {
			return smiles.iterator().next();
		}
		return null;
	}
}
