package uk.ac.cam.ch.wwmm.oscarMEMM;

import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ResolvableStandoff;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityTypes;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMM;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.Model;
import uk.ac.cam.ch.wwmm.oscarMEMM.saf.StandoffResolver;
import uk.ac.cam.ch.wwmm.oscarrecogniser.finder.DFAONTCPRFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Name recognition using the Maximum Entropy Markov Model
 *
 * @author j_robinson
 */
public class MEMMRecogniser implements ChemicalEntityRecogniser {

	private Model model;
//    private MEMM memm;
    private double memmThreshold;
    private DFAONTCPRFinder ontologyTermFinder;

    public MEMMRecogniser() {
        this.memmThreshold = OscarProperties.getData().neThreshold;
    }


    public Model getModel() {
        if (model == null) {
            model = Model.getDefaultInstance();
        }
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }


    public double getMemmThreshold() {
        return memmThreshold;
    }

    public void setMemmThreshold(double memmThreshold) {
        this.memmThreshold = memmThreshold;
    }


    public DFAONTCPRFinder getOntologyTermFinder() {
        if (ontologyTermFinder == null) {
            ontologyTermFinder = DFAONTCPRFinder.getInstance();
        }
        return ontologyTermFinder;
    }

    public void setOntologyTermFinder(DFAONTCPRFinder ontologyTermFinder) {
        this.ontologyTermFinder = ontologyTermFinder;
    }
    

    public List<NamedEntity> findNamedEntities(IProcessingDocument procDoc) throws Exception {
        return findNamedEntities(procDoc.getTokenSequences());
    }

    public List<NamedEntity> findNamedEntities(List<TokenSequence> tokSeqList) throws Exception {

        // Generate named entity list
        List<NamedEntity> neList = generateNamedEntities(tokSeqList);

        // Add ontology terms
        neList.addAll(generateOntologyTerms(tokSeqList));

        // Merge named entity ontIds/custTypes
        mergeNamedEntities(neList);

        setPseudoConfidences(neList);

        List<ResolvableStandoff> rsList = StandoffResolver.resolveStandoffs(neList);
        for (NamedEntity ne : neList) {
            ne.setBlocked(true);
        }
        for (ResolvableStandoff rs : rsList) {
            ((NamedEntity) rs).setBlocked(false);
        }

        return neList;
    }

    /**
     * Make sure all NEs at a position share their ontIds and custTypes
     * @param neList
     */
    private void mergeNamedEntities(List<NamedEntity> neList) {
        Map<String, Set<String>> posOntIdMap = new HashMap<String, Set<String>>();
        Map<String, Set<String>> posCustTypeMap = new HashMap<String, Set<String>>();

        for (NamedEntity ne : neList) {
            String pos = ne.getStart() + ":" + ne.getEnd();

            Set<String> neOntIds = ne.getOntIds();
            if (neOntIds != null) {
                Set<String> posOntIds = posOntIdMap.get(pos);
                if (posOntIds == null) {
                    posOntIds = new HashSet<String>(neOntIds);
                    posOntIdMap.put(pos, posOntIds);
                } else {
                    posOntIds.addAll(neOntIds);
                }
            }

            Set<String> neCustTypes = ne.getCustTypes();
            if (neCustTypes != null) {
                Set<String> posCustTypes = posCustTypeMap.get(pos);
                if (posCustTypes == null) {
                    posCustTypes = new HashSet<String>(neCustTypes);
                    posCustTypeMap.put(pos, posCustTypes);
                } else {
                    posCustTypes.addAll(neCustTypes);
                }
            }
        }

        for (NamedEntity ne : neList) {
            String posStr = ne.getStart() + ":" + ne.getEnd();
            Set<String> ontIds = posOntIdMap.get(posStr);
            if (ontIds != null) {
                ne.setOntIds(ontIds);
            }
            Set<String> custTypes = posCustTypeMap.get(posStr);
            if (custTypes != null) {
                ne.setCustTypes(custTypes);
            }
        }
    }


    /**
     * Detects named entities using MEMM.
     * @param toxicList
     * @return
     */
    private List<NamedEntity> generateNamedEntities(List<TokenSequence> toxicList) {
        List<NamedEntity> neList = new ArrayList<NamedEntity>();
        MEMM memm = getModel().getMemm();
        for (TokenSequence tokseq : toxicList) {
            for (NamedEntity ne : memm.findNEs(tokseq, null).keySet()) {
                if (ne.getConfidence() > getMemmThreshold()) {
                    neList.add(ne);
                }
            }
        }
        memm.rescore(neList);
        return neList;
    }


    /**
     * Detects named entities using ontology terms.
     * @param toxicList
     * @return
     */
    private List<NamedEntity> generateOntologyTerms(List<TokenSequence> toxicList) {
        List<NamedEntity> neList = new ArrayList<NamedEntity>();
        DFAONTCPRFinder ontologyTermFinder = getOntologyTermFinder();
        for (TokenSequence t : toxicList) {
            neList.addAll(ontologyTermFinder.getNEs(t));
        }
        return neList;
    }


    private void setPseudoConfidences(List<NamedEntity> neList) {
        for (NamedEntity ne : neList) {
            double pseudoConf = Double.NaN;
            String type = ne.getType();
            if (NamedEntityTypes.ONTOLOGY.equals(type)) {
                pseudoConf = OscarProperties.getData().ontProb;
            }
            else if (NamedEntityTypes.LOCANTPREFIX.equals(type)) {
                pseudoConf = OscarProperties.getData().cprProb;
            }
            else if (NamedEntityTypes.CUSTOM.equals(type)) {
                pseudoConf = OscarProperties.getData().custProb;
            }
            ne.setPseudoConfidence(pseudoConf);
            ne.setDeprioritiseOnt(OscarProperties.getData().deprioritiseONT);
        }
    }
    
}
