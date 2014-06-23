package uk.ac.cam.ch.wwmm.oscarpattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.ont.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata.ExtractedTrainingData;
import uk.ac.cam.ch.wwmm.oscarrecogniser.finder.DFAFinder;
import uk.ac.cam.ch.wwmm.oscarrecogniser.finder.DFANEFinder;
import uk.ac.cam.ch.wwmm.oscarrecogniser.finder.TermMaps;
import uk.ac.cam.ch.wwmm.oscarrecogniser.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscarrecogniser.saf.StandoffResolver;
import uk.ac.cam.ch.wwmm.oscarrecogniser.saf.StandoffResolver.ResolutionMode;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGram;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGramBuilder;
import uk.ac.cam.ch.wwmm.oscartokeniser.TokenClassifier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Name recognition using patterns
 * 
 * @author ptc24
 * @author j_robinson
 * @author dmj30
 */
public class PatternRecogniser implements ChemicalEntityRecogniser {

	private NGram nGram;
	private DFANEFinder finder;
	
	private double ontPseudoConfidence = 0.2;
	private double custPseudoConfidence = 0.2;
	private double cprPseudoConfidence = 0.2;
	private double ngramThreshold = -2;
	private boolean deprioritiseOnts = false;
	private Set<String> registryNames;


	/**
	 * Create a default PatternRecogniser that employs an {@link NGram} model customised
	 * according to the default (chempapers) model and the default instance of
	 * the {@link DFANEFinder}.
	 */
	public PatternRecogniser() {
		this(ExtractedTrainingData.getDefaultInstance(), TermMaps.getInstance().getNeTerms(),
				TokenClassifier.getDefaultInstance(), OntologyTerms.getDefaultInstance(),
				ChemNameDictRegistry.getDefaultInstance());
	}
	
	/**
	 * Create a customised PatternRecogniser that employs an {@link NGram} model customised
	 * according to the given {@link ExtractedTrainingData} and {@link ChemNameDictRegistry}
	 * and that uses the specified {@link DFAFinder}.
	 *  
	 * @param etd the {@link ExtractedTrainingData} object to be used for NGram customisation. Pass
	 * null to create an un-customised model.
	 * @param finder the {@link DFANEFinder} object to be used to identify named entities.
	 * @param neTerms the set of patterns to be used for multi-token named entity recognition
	 * @param registry the {@link ChemNameDictRegistry} for containing the dictionaries to use.
	 * A copy of the chemical names will be created and used internally.
	 */
	public PatternRecogniser(ExtractedTrainingData etd, Map<String, NamedEntityType> neTerms,
			TokenClassifier classifier, OntologyTerms ontologyTerms, ChemNameDictRegistry registry) {
		this.registryNames = Collections.unmodifiableSet(registry.getAllNames());
		this.nGram = NGramBuilder.buildOrDeserialiseModel(etd, registryNames);
		this.finder = new DFANEFinder(neTerms, classifier, ontologyTerms, registryNames);
	}
	

	public List<NamedEntity> findNamedEntities(IProcessingDocument procDoc) {
		return findNamedEntities(procDoc.getTokenSequences());
	}

	
	public List<NamedEntity> findNamedEntities(List<TokenSequence> tokenSequences) {
		return findNamedEntities(tokenSequences, ResolutionMode.REMOVE_BLOCKED);
	}
	
	
	public List<NamedEntity> findNamedEntities(List<TokenSequence> tokenSequences, ResolutionMode resolutionMode) {

	 	//run the DFANEFinder
		List<NamedEntity> neList = new ArrayList<NamedEntity>();
	 	for(TokenSequence t : tokenSequences) {
			neList.addAll(finder.findNamedEntities(t, nGram, ngramThreshold));
		}
	 	
		//make a list of ONT, CUST and CPR nes
		List<NamedEntity> preserveNes = new ArrayList<NamedEntity>();
		for (NamedEntity ne : neList) {
			if(NamedEntityType.ONTOLOGY.equals(ne.getType()) || NamedEntityType.LOCANTPREFIX.equals(ne.getType()) || NamedEntityType.CUSTOM.equals(ne.getType())) {
				preserveNes.add(ne);
			}
		}

		mergeOntIdsAndCustTypes(neList);
		//identify and remove blocked named entities
		if (resolutionMode == ResolutionMode.REMOVE_BLOCKED) {
			StandoffResolver.resolveStandoffs(neList);	
		}
		else if (resolutionMode == ResolutionMode.MARK_BLOCKED) {
			StandoffResolver.markBlockedStandoffs(neList);
		}
		else {
			throw new RuntimeException(resolutionMode + " not yet implemented");
		}
		

		handlePotentialAcronyms(tokenSequences, neList);
		removeStopwords(neList);

		// Some CPRs and ONTs will have been lost in the stopwording process
		// dmj30 really? why?
		//TODO investigate whether this step is necessary
		// Re-introduce them, and do the resolution process again
//		neList.addAll(preserveNes);
//		setPseudoConfidences(neList);
//		neList = StandoffResolver.resolveStandoffs(neList);

		
		return neList;
	}//findNamedEntities

	
	/**
	 * Removes from the neList all named entities that are of type STOP
	 * @param neList
	 */
	static void removeStopwords(List<NamedEntity> neList) {
		int i = 0;
		while(i < neList.size()) {
			NamedEntity ne = neList.get(i);
			if(NamedEntityType.STOP.equals(ne.getType())) {
				neList.remove(i);
			} else {
				i++;
			}
		}
	}

	/**
	 * Finds acronyms (technically, abbreviations) that fit the pattern "$NE ($AHA)" where "$NE"
	 * is a named entity and "($AHA)" is an acronym wrapped in round brackets. The acronym
	 * must be composed of letters that occur in that order in the $NE surface text, e.g.
	 * "PS" and "PY" are acceptable acronyms for polystyrene but "PZ" is not. Those potential
	 * acronyms, and further occurences of that string, that fit this requirement are converted
	 * to the named entity type of $NE, while those that do not are removed from the neList.
	 *
	 * @param tokenSequences
	 * @param neList
	 */
	static void handlePotentialAcronyms(List<TokenSequence> tokenSequences, List<NamedEntity> neList) {
		
		Map<Integer,NamedEntity> endToNe = new HashMap<Integer,NamedEntity>();
		for(NamedEntity ne : neList) {
			endToNe.put(ne.getEnd(), ne);
		}
		
	 	Map<Integer,Token> tokensByStart = new HashMap<Integer,Token>();
	 	for (TokenSequence tokSeq : tokenSequences) {
	 		for (Token token : tokSeq.getTokens()) {
				tokensByStart.put(token.getStart(), token);
			}
		}
		
		Map<String, NamedEntityType> acroMap = identifyAcronyms(neList, endToNe, tokensByStart);

		//set named entity types for the detected acronyms & remove other POTENTIALACRONYM entities 
		int j = 0;
		while(j < neList.size()) {
			NamedEntity ne = neList.get(j);
			if(NamedEntityType.POTENTIALACRONYM.equals(ne.getType())) {
				if(acroMap.containsKey(ne.getSurface())) {
					ne.setType(acroMap.get(ne.getSurface()));
					j++;
				} else {
					neList.remove(j);
				}
			} else {
				j++;
			}
		}
	}

	
	/**
	 * Determines which potential acronyms fit the acronym requirement. 
	 * 
	 * @param neList
	 * @param endToNe
	 * @param tokensByStart
	 * @return a Map of surface strings to appropriate named entity type
	 */
	static Map<String, NamedEntityType> identifyAcronyms(List<NamedEntity> neList,
			Map<Integer, NamedEntity> endToNe, Map<Integer, Token> tokensByStart) {

		Map<String,NamedEntityType> acroMap = new HashMap<String,NamedEntityType>();
		for(NamedEntity ne : neList) {
			if(NamedEntityType.POTENTIALACRONYM.equals(ne.getType())) {
				Token t = tokensByStart.get(ne.getStart());
				if(t != null && t.getNAfter(-2) != null && t.getNAfter(1) != null) {
					Token prev = t.getNAfter(-1);
					Token next = t.getNAfter(1);
					Token prevPrev = t.getNAfter(-2);
					if(prev.getSurface().equals("(") && next.getSurface().endsWith(")")) {
						if(endToNe.containsKey(prevPrev.getEnd())) {
							NamedEntity acronymOf = endToNe.get(prevPrev.getEnd());
							if(StringTools.testForAcronym(ne.getSurface(), acronymOf.getSurface())) {
								if(NamedEntityType.ASE.equals(acronymOf.getType()) || NamedEntityType.ASES.equals(acronymOf.getType())) {
									//System.out.println("Skip ASE acronym");
								} else {
									if (acroMap.containsKey(ne.getSurface())) {
										NamedEntityType newValue = ne.getType();
										NamedEntityType oldValue = acroMap.get(ne.getSurface());
										if (NamedEntityType.POLYMER.equals(newValue)) acroMap.put(ne.getSurface(), acronymOf.getType());
										else if (NamedEntityType.COMPOUND.equals(newValue) && !NamedEntityType.POLYMER.equals(oldValue)) acroMap.put(ne.getSurface(), acronymOf.getType());
									}
									else {
										acroMap.put(ne.getSurface(), acronymOf.getType());
									}
								}
							}
						}
					}
				}
			}
		}
		return acroMap;
	}

	
	/**
	 * Make sure all NEs at a position share their ontIds and custTypes
	 * @param neList
	 */
	static void mergeOntIdsAndCustTypes(List<NamedEntity> neList) {
		//TODO this code is duplicated in MEMMRecogniser
		// populate the ...ForNePos indexes
		SetMultimap<String, String> ontIdsForNePos = HashMultimap.create();
		SetMultimap<String, String> custTypesForNePos = HashMultimap.create();
		for(NamedEntity ne : neList) {
			String posStr = ne.getStart() + ":" + ne.getEnd();
			ontIdsForNePos.putAll(posStr, ne.getOntIds());
			custTypesForNePos.putAll(posStr, ne.getCustTypes());
		}

		//set the ontIds and custIds
		for(NamedEntity ne : neList) {
			String posStr = ne.getStart() + ":" + ne.getEnd();
			Set<String> ontIds = ontIdsForNePos.get(posStr);
			if(ontIds.size() > 0) {
				ne.setOntIds(ontIds);
			}
			Set<String> custTypes = custTypesForNePos.get(posStr);
			if(custTypes.size() > 0) {
				ne.setCustTypes(custTypes);
			}
		}
	}
	

	void setPseudoConfidences(List<NamedEntity> neList) {
		for(NamedEntity ne : neList) {
			double pseudoConf = Double.NaN;
			NamedEntityType type = ne.getType();
			if(type.equals(NamedEntityType.ONTOLOGY)) {
				pseudoConf = ontPseudoConfidence;
			}
			if(type.equals(NamedEntityType.LOCANTPREFIX)) {
				pseudoConf = cprPseudoConfidence;
			}
			if(type.equals(NamedEntityType.CUSTOM)) {
				pseudoConf = custPseudoConfidence;
			}
			ne.setPseudoConfidence(pseudoConf);
			ne.setDeprioritiseOnt(deprioritiseOnts);
		}
	}//setPseudoConfidences

	
	public double getOntPseudoConfidence() {
		return ontPseudoConfidence;
	}

	/**
	 * Sets the pseudoconfidence score to be assigned to name entities
	 * of type ONT
	 * 
	 * @param ontPseudoConfidence
	 */
	public void setOntPseudoConfidence(double ontPseudoConfidence) {
		this.ontPseudoConfidence = ontPseudoConfidence;
	}

	public double getCustPseudoConfidence() {
		return custPseudoConfidence;
	}

	/**
	 * Sets the pseudoconfidence score to be assigned to name entities
	 * of type CUST
	 * 
	 * @param custPseudoConfidence
	 */
	public void setCustPseudoConfidence(double custPseudoConfidence) {
		this.custPseudoConfidence = custPseudoConfidence;
	}

	public double getCprPseudoConfidence() {
		return cprPseudoConfidence;
	}

	/**
	 * Sets the pseudoconfidence score to be assigned to name entities
	 * of type CPR
	 * 
	 * @param cprPseudoConfidence
	 */
	public void setCprPseudoConfidence(double cprPseudoConfidence) {
		this.cprPseudoConfidence = cprPseudoConfidence;
	}

	/**
	 * Sets the ngram threshold for the recogniser. The ngram threshold is the value
	 * of ln(p(chemical|word)) - ln(p(nonchemical|word)) which must be exceeded for
	 * the token to be considered chemical.
	 * 
	 * @param ngramThreshold
	 */
	public void setNgramThreshold(double ngramThreshold) {
		this.ngramThreshold = ngramThreshold;
	}

	/**
	 * @return the current ngram threshold for the recogniser
	 */
	public double getNgramThreshold() {
		return ngramThreshold;
	}

	public void setDeprioritiseOnts(boolean deprioritiseOnts) {
		this.deprioritiseOnts  = deprioritiseOnts;
	}

	public Set<String> getRegistryNames() {
		return registryNames;
	}

}
