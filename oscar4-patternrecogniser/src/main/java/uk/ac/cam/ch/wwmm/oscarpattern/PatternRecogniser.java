package uk.ac.cam.ch.wwmm.oscarpattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ResolvableStandoff;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityTypes;
import uk.ac.cam.ch.wwmm.oscarpattern.saf.StandoffResolver;
import uk.ac.cam.ch.wwmm.oscarrecogniser.finder.DFANEFinder;

/**
 * Name recognition using patterns
 * @author j_robinson
 */
public class PatternRecogniser implements ChemicalEntityRecogniser
{

	public List<NamedEntity> findNamedEntities(IProcessingDocument procDoc) throws Exception
	{
		return findNamedEntities(procDoc.getTokenSequences());
	}

	public List<NamedEntity> findNamedEntities(List<TokenSequence> tokenSequences) throws Exception
	{
			 	List<NamedEntity> stopNeList;

		//String text = doc.getValue();

	 	List<NamedEntity> neList = new ArrayList<NamedEntity>();

	 	Map<Integer,Token> tokensByStart = new HashMap<Integer,Token>();
	 	Map<Integer,Token> tokensByEnd = new HashMap<Integer,Token>();

	 	for(ITokenSequence t : tokenSequences) {
			neList.addAll(DFANEFinder.getInstance().getNEs(t));
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
			if(NamedEntityTypes.ONTOLOGY.equals(ne.getType()) || NamedEntityTypes.LOCANTPREFIX.equals(ne.getType()) || NamedEntityTypes.CUSTOM.equals(ne.getType())) {
				preserveNes.add(ne);
			}
			String posStr = ne.getStart() + ":" + ne.getEnd();
			Set<String> ontIds = ontIdsForNePos.get(posStr);
			if(ontIds != null) ne.setOntIds(ontIds);
			Set<String> custTypes = custTypesForNePos.get(posStr);
			if(custTypes != null) ne.setCustTypes(custTypes);
		}

		List<ResolvableStandoff> rsList = StandoffResolver.resolveStandoffs(neList);
		neList.clear();
		for(ResolvableStandoff rs : rsList) {
			neList.add((NamedEntity)rs);
		}

		//for(NamedEntity ne : neList) System.out.println(ne);

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

		Map<String,String> acroMap = new HashMap<String,String>();

		Map<Integer,NamedEntity> endToNe = new HashMap<Integer,NamedEntity>();
		Map<Integer,NamedEntity> startToNe = new HashMap<Integer,NamedEntity>();

		for(NamedEntity ne : neList) {
			endToNe.put(ne.getEnd(), ne);
			startToNe.put(ne.getStart(), ne);
		}

		// Potential acronyms
		for(NamedEntity ne : neList) {
			if(ne.getType().equals(NamedEntityTypes.POTENTIALACRONYM)) {
				int start = ne.getStart();
				//int end = ne.getEnd();
				
				Token t = tokensByStart.get(start);
				if(t != null) System.out.println("NOT NULL AHA: " + t.getValue());
				if(t != null && t.getNAfter(-2) != null && t.getNAfter(1) != null) {
					Token prev = t.getNAfter(-1);
					Token next = t.getNAfter(1);
					Token prevPrev = t.getNAfter(-2);
					if(prev.getValue().equals("(") && next.getValue().endsWith(")")) {
						//boolean matched = false;
						if(endToNe.containsKey(prevPrev.getEnd())) {
							NamedEntity acronymOf = endToNe.get(prevPrev.getEnd());
							if(StringTools.testForAcronym(ne.getSurface(), acronymOf.getSurface())) {
								//System.out.println(ne.getSurface() + " is " + acronymOf.getSurface());
								if(acronymOf.getType().equals(NamedEntityTypes.ASE) || acronymOf.getType().equals(NamedEntityTypes.ASES)) {
									//System.out.println("Skip ASE acronym");
								} else {
									//matched = true;
									if (acroMap.containsKey(ne.getSurface())) {
										String newValue = ne.getType();
										String oldValue = acroMap.get(ne.getSurface());
										if (newValue == NamedEntityTypes.POLYMER) acroMap.put(ne.getSurface(), acronymOf.getType());
										else if (newValue == NamedEntityTypes.COMPOUND && !oldValue.equals(NamedEntityTypes.POLYMER)) acroMap.put(ne.getSurface(), acronymOf.getType());
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
			if(ne.getType().equals(NamedEntityTypes.POTENTIALACRONYM)) {
				if(acroMap.containsKey(ne.getSurface())) {
					ne.setType(acroMap.get(ne.getSurface()));
					i++;
				} else {
					neList.remove(i);
				}
			} else if(ne.getType().equals(NamedEntityTypes.STOP)) {
				//System.out.println("STOP: " + neList.get(i).getSurface());
				neList.remove(i);
				stopNeList.add(ne);
			} else {
				i++;
			}
		}

		// Some CPRs and ONTs will have been lost in the stopwording process
		// Re-introduce them, and do the resolution process again
		//for(NamedEntity ne : preserveNes) System.out.println(ne);
		neList.addAll(preserveNes);
		setPseudoConfidences(neList);
		rsList = StandoffResolver.resolveStandoffs(neList);
		neList.clear();
		for(ResolvableStandoff rs : rsList) {
			neList.add((NamedEntity)rs);
		}

		
		return neList;
	}//findNamedEntities

	public void setPseudoConfidences(List<NamedEntity> neList) {
		for(NamedEntity ne : neList) {
			double pseudoConf = Double.NaN;
			String type = ne.getType();
			if(type.equals(NamedEntityTypes.ONTOLOGY)) pseudoConf = OscarProperties.getData().ontProb;
			if(type.equals(NamedEntityTypes.LOCANTPREFIX)) pseudoConf = OscarProperties.getData().cprProb;
			if(type.equals(NamedEntityTypes.CUSTOM)) pseudoConf = OscarProperties.getData().custProb;
			ne.setPseudoConfidence(pseudoConf);
			ne.setDeprioritiseOnt(OscarProperties.getData().deprioritiseONT);
		}
	}//setPseudoConfidences

	

}
