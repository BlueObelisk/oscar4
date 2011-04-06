package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;

public class ResolvedNamedEntity {

	private final List<NameResolutionResult> resolutionResults = new ArrayList<NameResolutionResult>();
	private final NamedEntity namedEntity;

	public ResolvedNamedEntity(NamedEntity namedEntity, ChemNameDictRegistry chemNameDictRegistry) {
		if (namedEntity==null){
			throw new IllegalArgumentException("Input named entity was null");
		}
		if (chemNameDictRegistry==null){
			throw new IllegalArgumentException("Input chemNameDictRegistry was null");
		}
		String name = namedEntity.getSurface();
		List<URI> dictionaryURIs = chemNameDictRegistry.listDictionaries();
		for (URI uri : dictionaryURIs) {
			IChemNameDict dictionary = chemNameDictRegistry.getDictionary(uri);
			resolutionResults.add(new NameResolutionResult(name, dictionary));
		}
		this.namedEntity = namedEntity;
	}
	
	public NamedEntity getNamedEntity() {
		return namedEntity;
	}

	public List<NameResolutionResult> getNameResolutionResults(){
		return resolutionResults;
	}
	
	public String getFirstSmiles() {
		for (NameResolutionResult resolutionResult : resolutionResults) {
			Set<String> smileses = resolutionResult.getSmileses();
			if (smileses.size()>0){
				return smileses.iterator().next();
			}
		}
		return null;
	}
	
	public String getFirstInChI() {
		for (NameResolutionResult resolutionResult : resolutionResults) {
			Set<String> inchis = resolutionResult.getInchis();
			if (inchis.size()>0){
				return inchis.iterator().next();
			}
		}
		return null;
	}
}
