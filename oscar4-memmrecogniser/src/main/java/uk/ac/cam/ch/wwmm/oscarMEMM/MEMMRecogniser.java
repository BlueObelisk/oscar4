package uk.ac.cam.ch.wwmm.oscarMEMM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMSingleton;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.finder.DFAONTCPRFinder;
import uk.ac.cam.ch.wwmm.oscarMEMM.saf.ResolvableStandoff;
import uk.ac.cam.ch.wwmm.oscarMEMM.saf.StandoffResolver;
import uk.ac.cam.ch.wwmm.oscarMEMM.tools.Oscar3Props;
import uk.ac.cam.ch.wwmm.oscarMEMM.types.NETypes;

/**
 * Name recognition using the Maximum Entropy Markov Model
 * 
 * @author j_robinson
 */
public class MEMMRecogniser implements ChemicalEntityRecogniser {

	public List<NamedEntity> findNamedEntities(ProcessingDocument procDoc)
        throws Exception {

	     return findNamedEntities(procDoc.getTokenSequences());
	}

	public List<NamedEntity> findNamedEntities(List<TokenSequence> toxicList)
			throws Exception {

		List<NamedEntity> neList = new ArrayList<NamedEntity>();


		for (TokenSequence t : toxicList) {
			neList
					.addAll(MEMMSingleton.getInstance().findNEs(t, null)
							.keySet());

		}

		MEMMSingleton.getInstance().rescore(neList);
		List<NamedEntity> filteredNeList = new ArrayList<NamedEntity>();
		for (NamedEntity ne : neList) {
			if (ne.getConfidence() > Oscar3Props.getInstance().neThreshold) {
				filteredNeList.add(ne);
			}

		}


		
		for (TokenSequence t : toxicList) {
			neList.addAll(DFAONTCPRFinder.getInstance().getNEs(t));
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

	
	public void setPseudoConfidences(List<NamedEntity> neList) {
		for (NamedEntity ne : neList) {
			double pseudoConf = Double.NaN;
			String type = ne.getType();
			if (type.equals(NETypes.ONTOLOGY))
				pseudoConf = Oscar3Props.getInstance().ontProb;
			if (type.equals(NETypes.LOCANTPREFIX))
				pseudoConf = Oscar3Props.getInstance().cprProb;
			if (type.equals(NETypes.CUSTOM))
				pseudoConf = Oscar3Props.getInstance().custProb;
			ne.setPseudoConfidence(pseudoConf);
			ne.setDeprioritiseOnt(Oscar3Props.getInstance().deprioritiseONT);
		}
	}// setPseudoConfidences
}
