package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class NameResolutionResult {

	private String name;
	private URI uri;
	private Set<String> smileses;
	private Set<String> inchis;

	public NameResolutionResult(String name, IChemNameDict dictionary) {
		uri = dictionary.getURI();
		this.name = name;
		if (dictionary instanceof ISMILESProvider){
			ISMILESProvider smiDict = (ISMILESProvider) dictionary;
			smileses = smiDict.getSMILES(name);
		}
		if (smileses==null){
			smileses = Collections.emptySet();
		}
		if (dictionary instanceof IInChIProvider){
			IInChIProvider inchiDict = (IInChIProvider) dictionary;
			inchis = inchiDict.getInChI(name);
		}
		if (inchis==null){
			inchis = Collections.emptySet();
		}
	}
	
	/**
	 * Returns the URI of the dictionary used to produce these resolution results
	 * @return
	 */
	URI getUri() {
		return uri;
	}
	
	/**
	 * Returns the name used to produce these resolution results
	 * @return
	 */
	String getName() {
		return name;
	}

	/**
	 * Returns the set of SMILESes this dictionary returned
	 * @return
	 */
	Set<String> getSmileses() {
		return smileses;
	}

	/**
	 * Returns the set of InChIs this dictionary returned
	 * @return
	 */
	Set<String> getInchis() {
		return inchis;
	}
	
}
