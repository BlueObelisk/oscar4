package uk.ac.cam.ch.wwmm.oscarMEMM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ResolvableStandoff;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMSingleton;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.finder.DFAONTCPRFinder;
import uk.ac.cam.ch.wwmm.oscarMEMM.saf.StandoffResolver;
import uk.ac.cam.ch.wwmm.oscarMEMM.types.NETypes;

/**
 * Name recognition using the Maximum Entropy Markov Model
 * 
 * @author j_robinson
 */
public class MEMMRecogniser implements ChemicalEntityRecogniser {

	public List<NamedEntity> findNamedEntities(List<TokenSequence> toxicList)
			throws Exception {

		List<NamedEntity> neList = new ArrayList<NamedEntity>();


		for (TokenSequence t : toxicList) {
			neList
					.addAll(MEMMSingleton.getInstance().findNEs(t, null)
							.keySet());

		}
		System.out.println("===");
		for (NamedEntity entity : neList) {
			System.out.println("Ent: " + entity);
		}

		MEMMSingleton.getInstance().rescore(neList);
		List<NamedEntity> filteredNeList = new ArrayList<NamedEntity>();
		for (NamedEntity ne : neList) {
			if (ne.getConfidence() > OscarProperties.getInstance().neThreshold) {
				filteredNeList.add(ne);
			}

		}
		System.out.println("=====");
		for (NamedEntity entity : filteredNeList) {
			System.out.println("Ent: " + entity);
		}

		List<NamedEntity> filteredNeList2 = new ArrayList<NamedEntity>();
		for (NamedEntity ne : filteredNeList) {
			if (!partialWord(ne, filteredNeList)) {
				filteredNeList2.add(ne);
			}

		}
		neList = filteredNeList;
		System.out.println("=======");
		for (NamedEntity entity : neList) {
			System.out.println("Ent: " + entity);
		}

		
//		for (TokenSequence t : toxicList) {
//			neList.addAll(DFAONTCPRFinder.getInstance().getNEs(t));
//		}
		System.out.println("=========");
		for (NamedEntity entity : neList) {
			System.out.println("Ent: " + entity);
		}

		// Make sure all NEs at a position share their ontIds and custTypes
		Map<String, Set<String>> ontIdsForNePos = new HashMap<String, Set<String>>();
		Map<String, Set<String>> custTypesForNePos = new HashMap<String, Set<String>>();

		for (NamedEntity ne : neList) {
			String posStr = ne.getStart() + ":" + ne.getEnd();
			Set<String> ontIds = ne.getOntIds();

			if (ontIds != null) {
				if (ontIdsForNePos.containsKey(posStr)) {
					ontIdsForNePos.get(posStr).addAll(ontIds);
				} else {
					ontIdsForNePos.put(posStr, new HashSet<String>(ontIds));
				}
			}
			Set<String> custTypes = ne.getCustTypes();
			if (custTypes != null) {
				if (custTypesForNePos.containsKey(posStr)) {
					custTypesForNePos.get(posStr).addAll(custTypes);
				} else {
					custTypesForNePos.put(posStr,
							new HashSet<String>(custTypes));
				}
			}
		}

		for (NamedEntity ne : neList) {
			String posStr = ne.getStart() + ":" + ne.getEnd();
			Set<String> ontIds = ontIdsForNePos.get(posStr);
			if (ontIds != null)
				ne.setOntIds(ontIds);
			Set<String> custTypes = custTypesForNePos.get(posStr);
			if (custTypes != null)
				ne.setCustTypes(custTypes);
		}
		System.out.println("==============");
		for (NamedEntity entity : neList) {
			System.out.println("Ent: " + entity);
		}

		setPseudoConfidences(neList);

		List<ResolvableStandoff> rsList = StandoffResolver
				.resolveStandoffs(neList);
		for (NamedEntity ne : neList) {
			ne.setBlocked(true);
		}

		for (ResolvableStandoff rs : rsList) {
			((NamedEntity) rs).setBlocked(false);
		}

		return neList;
	}

	private boolean partialWord(NamedEntity ne, List<NamedEntity> neList) {
		// TODO Auto-generated method stub
		String word = ne.getSurface();
		boolean partialFlag = false;
		for (NamedEntity otherNE : neList) {
			if (!word.equals(otherNE.getSurface())) {
				if (otherNE.getSurface().contains(word)) {
					if (otherNE.getStart() == ne.getStart()
							|| otherNE.getEnd() == ne.getEnd()) {
						
						partialFlag = true;
					}
				}
			}
		}
		return partialFlag;
	}

	public void setPseudoConfidences(List<NamedEntity> neList) {
		for (NamedEntity ne : neList) {
			double pseudoConf = Double.NaN;
			String type = ne.getType();
			if (type.equals(NETypes.ONTOLOGY))
				pseudoConf = OscarProperties.getInstance().ontProb;
			if (type.equals(NETypes.LOCANTPREFIX))
				pseudoConf = OscarProperties.getInstance().cprProb;
			if (type.equals(NETypes.CUSTOM))
				pseudoConf = OscarProperties.getInstance().custProb;
			ne.setPseudoConfidence(pseudoConf);
			ne.setDeprioritiseOnt(OscarProperties.getInstance().deprioritiseONT);
		}
	}// setPseudoConfidences
}
