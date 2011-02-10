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
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
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
	
	/**
	 * Create a PatternRecogniser that employs an NGram model customised
	 * according to the default model, as set in OscarProperties.
	 */
	public PatternRecogniser() {
		nGram = NGramBuilder.buildOrDeserialiseModel(ManualAnnotations.getDefaultInstance());
	}
	
	/**
	 * Create a PatternRecogniser that employs an NGram model customised
	 * according to the given extracted training data. Pass null as an
	 * argument to create an un-customised NGram model.
	 */
	public PatternRecogniser(ManualAnnotations etd) {
		nGram = NGramBuilder.buildOrDeserialiseModel(etd);
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
	 	Map<Integer,Token> tokensByEnd = new HashMap<Integer,Token>();

	 	for(ITokenSequence t : tokenSequences) {
			neList.addAll(DFANEFinder.getInstance().findNamedEntities(t, nGram));
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
					if(prev.getValue().equals("(") && next.getValue().endsWith(")")) {
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

	public void setPseudoConfidences(List<NamedEntity> neList) {
		for(NamedEntity ne : neList) {
			double pseudoConf = Double.NaN;
			NamedEntityType type = ne.getType();
			if(type.equals(NamedEntityType.ONTOLOGY)) pseudoConf = OscarProperties.getData().ontProb;
			if(type.equals(NamedEntityType.LOCANTPREFIX)) pseudoConf = OscarProperties.getData().cprProb;
			if(type.equals(NamedEntityType.CUSTOM)) pseudoConf = OscarProperties.getData().custProb;
			ne.setPseudoConfidence(pseudoConf);
			ne.setDeprioritiseOnt(OscarProperties.getData().deprioritiseONT);
		}
	}//setPseudoConfidences

	

}
