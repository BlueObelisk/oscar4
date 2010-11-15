package uk.ac.cam.ch.wwmm.oscarMEMM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ResolvableStandoff;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityTypes;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMSingleton;
import uk.ac.cam.ch.wwmm.oscarMEMM.saf.StandoffResolver;
import uk.ac.cam.ch.wwmm.oscarrecogniser.finder.DFAONTCPRFinder;

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
			if (ne.getConfidence() > OscarProperties.getData().neThreshold) {
				filteredNeList.add(ne);
			}

		}

		neList = filteredNeList;
		
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
			if (type.equals(NamedEntityTypes.ONTOLOGY))
				pseudoConf = OscarProperties.getData().ontProb;
			if (type.equals(NamedEntityTypes.LOCANTPREFIX))
				pseudoConf = OscarProperties.getData().cprProb;
			if (type.equals(NamedEntityTypes.CUSTOM))
				pseudoConf = OscarProperties.getData().custProb;
			ne.setPseudoConfidence(pseudoConf);
			ne.setDeprioritiseOnt(OscarProperties.getData().deprioritiseONT);
		}
	}// setPseudoConfidences
}
