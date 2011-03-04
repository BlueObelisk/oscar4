package uk.ac.cam.ch.wwmm.oscarpattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarpattern.saf.StandoffResolver;
import uk.ac.cam.ch.wwmm.oscarrecogniser.finder.DFANEFinder;
import uk.ac.cam.ch.wwmm.oscarrecogniser.manualAnnotations.ManualAnnotations;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGram;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGramBuilder;

/**
 * Name recognition using patterns
 * @author j_robinson
 */
public class PatternRecogniser implements ChemicalEntityRecogniser {

	private NGram nGram;
	private DFANEFinder finder;
	
	private double ontPseudoConfidence = 0.2;
	private double custPseudoConfidence = 0.2;
	private double cprPseudoConfidence = 0.2;
	private double ngramThreshold = -2;
	private boolean deprioritiseOnts = false;
	
	
	/**
	 * Create a default PatternRecogniser that employs an NGram model customised
	 * according to the default (chempapers) model and the default instance of
	 * the DFANEFinder.
	 */
	public PatternRecogniser() {
		this.nGram = NGramBuilder.buildOrDeserialiseModel(ManualAnnotations.getDefaultInstance());
		this.finder = DFANEFinder.getDefaultInstance();
	}
	
	/**
	 * Create a customised PatternRecogniser that employs an NGram model customised
	 * according to the given extracted training data and that uses the specified
	 * DFAFinder.
	 *  
	 * @param etd the ManualAnnotations object to be used for NGram customisation. Pass
	 * null to create an un-customised model.
	 * @param finder the DFANEFinder object to be used to identify named entities.
	 */
	public PatternRecogniser(ManualAnnotations etd, DFANEFinder finder) {
		this.nGram = NGramBuilder.buildOrDeserialiseModel(etd);
		this.finder = finder;
	}

	public List<NamedEntity> findNamedEntities(IProcessingDocument procDoc) {
		return findNamedEntities(procDoc.getTokenSequences());
	}

	//TODO this method is enormous and needs refactoring
	public List<NamedEntity> findNamedEntities(List<ITokenSequence> tokenSequences) {
	 	List<NamedEntity> stopNeList;

		//String text = doc.getValue();

	 	List<NamedEntity> neList = new ArrayList<NamedEntity>();

	 	Map<Integer,Token> tokensByStart = new HashMap<Integer,Token>();

	 	for(ITokenSequence t : tokenSequences) {
			neList.addAll(finder.findNamedEntities(t, nGram, ngramThreshold));
		}

		// Make sure all NEs at a position share their ontIds
		Map<String,Set<String>> ontIdsForNePos = new HashMap<String,Set<String>>();
		Map<String,Set<String>> custTypesForNePos = new HashMap<String,Set<String>>();
		for(NamedEntity ne : neList) {
			String posStr = ne.getStart() + ":" + ne.getEnd();
			Set<String> ontIds = ne.getOntIds();
			if(ontIds != null) {
				if(ontIdsForNePos.containsKey(posStr)) {
					ontIdsForNePos.get(posStr).addAll(ontIds);
				} else {
					ontIdsForNePos.put(posStr, new HashSet<String>(ontIds));
				}
			}
			Set<String> custTypes = ne.getCustTypes();
			if(custTypes != null) {
				if(custTypesForNePos.containsKey(posStr)) {
					custTypesForNePos.get(posStr).addAll(custTypes);
				} else {
					custTypesForNePos.put(posStr, new HashSet<String>(custTypes));
				}
			}
		}

		List<NamedEntity> preserveNes = new ArrayList<NamedEntity>();

		for(NamedEntity ne : neList) {
			if(NamedEntityType.ONTOLOGY.equals(ne.getType()) || NamedEntityType.LOCANTPREFIX.equals(ne.getType()) || NamedEntityType.CUSTOM.equals(ne.getType())) {
				preserveNes.add(ne);
			}
			String posStr = ne.getStart() + ":" + ne.getEnd();
			Set<String> ontIds = ontIdsForNePos.get(posStr);
			if(ontIds != null) ne.setOntIds(ontIds);
			Set<String> custTypes = custTypesForNePos.get(posStr);
			if(custTypes != null) ne.setCustTypes(custTypes);
		}

		List<NamedEntity> rsList = StandoffResolver.resolveStandoffs(neList);
		neList.clear();
		for(NamedEntity rs : rsList) {
			neList.add(rs);
		}

		/*
		Collections.sort(neList, new NEComparator());

		// Filter NEs
		if(neList.size() > 0) {
			NamedEntity activeNe = neList.get(0);
			int j = 1;
			while(j < neList.size()) {
				if(activeNe.overlapsWith(neList.get(j))) {
					neList.remove(j);
				} else {
					activeNe = neList.get(j);
					j++;
				}
			}
		}*/

		Map<String,NamedEntityType> acroMap = new HashMap<String,NamedEntityType>();

		Map<Integer,NamedEntity> endToNe = new HashMap<Integer,NamedEntity>();
		Map<Integer,NamedEntity> startToNe = new HashMap<Integer,NamedEntity>();

		for(NamedEntity ne : neList) {
			endToNe.put(ne.getEnd(), ne);
			startToNe.put(ne.getStart(), ne);
		}

		// Potential acronyms
		for(NamedEntity ne : neList) {
			if(NamedEntityType.POTENTIALACRONYM.equals(ne.getType())) {
				int start = ne.getStart();
				//int end = ne.getEnd();
				
				IToken t = tokensByStart.get(start);
				if(t != null && t.getNAfter(-2) != null && t.getNAfter(1) != null) {
					IToken prev = t.getNAfter(-1);
					IToken next = t.getNAfter(1);
					IToken prevPrev = t.getNAfter(-2);
					if(prev.getSurface().equals("(") && next.getSurface().endsWith(")")) {
						//boolean matched = false;
						if(endToNe.containsKey(prevPrev.getEnd())) {
							NamedEntity acronymOf = endToNe.get(prevPrev.getEnd());
							if(StringTools.testForAcronym(ne.getSurface(), acronymOf.getSurface())) {
								if(NamedEntityType.ASE.equals(acronymOf.getType()) || NamedEntityType.ASES.equals(acronymOf.getType())) {
									//System.out.println("Skip ASE acronym");
								} else {
									//matched = true;
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

				/*int index = neList.indexOf(ne);
				if(index == 0) continue;
				NamedEntity previous = neList.get(index-1);
				int prevEnd = previous.getEnd();
				String inBetween = text.substring(prevEnd, start);
				try {
					String afterWards = text.substring(end);
					if(afterWards != null && afterWards.length() > 0 &&
							inBetween.matches("\\s*\\(\\s*") &&
							afterWards.startsWith(")") &&
							StringTools.testForAcronym(ne.getSurface(), previous.getSurface())) {
						System.out.println(ne.getSurface() + " is " + previous.getSurface());
						if(previous.getType(this).equals(NETypes.ASE) || previous.getType(this).equals(NETypes.ASES)) {
							System.out.println("Skip ASE acronym");
						} else {
							acroMap.put(ne.getSurface(), previous.getType(this));
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}*/

			}
		}

		stopNeList = new ArrayList<NamedEntity>();

		int i = 0;
		while(i < neList.size()) {
			NamedEntity ne = neList.get(i);
			if(NamedEntityType.POTENTIALACRONYM.equals(ne.getType())) {
				if(acroMap.containsKey(ne.getSurface())) {
					ne.setType(acroMap.get(ne.getSurface()));
					i++;
				} else {
					neList.remove(i);
				}
			} else if(NamedEntityType.STOP.equals(ne.getType())) {
				neList.remove(i);
				stopNeList.add(ne);
			} else {
				i++;
			}
		}

		// Some CPRs and ONTs will have been lost in the stopwording process
		// Re-introduce them, and do the resolution process again
		neList.addAll(preserveNes);
		setPseudoConfidences(neList);
		rsList = StandoffResolver.resolveStandoffs(neList);
		neList.clear();
		for(NamedEntity rs : rsList) {
			neList.add(rs);
		}

		
		return neList;
	}//findNamedEntities

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

	

}
