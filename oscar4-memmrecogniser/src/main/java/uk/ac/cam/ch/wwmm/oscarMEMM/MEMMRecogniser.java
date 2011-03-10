package uk.ac.cam.ch.wwmm.oscarMEMM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMM;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.data.MEMMModel;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.ChemPapersModel;
import uk.ac.cam.ch.wwmm.oscarrecogniser.finder.DFAONTCPRFinder;
import uk.ac.cam.ch.wwmm.oscarrecogniser.saf.StandoffResolver;

/**
 * Name recognition using the Maximum Entropy Markov Model
 *
 * @author j_robinson
 */
public class MEMMRecogniser implements ChemicalEntityRecogniser {

	private MEMMModel model;
//    private MEMM memm;
    private double memmThreshold = 0.2;
    private DFAONTCPRFinder ontologyTermFinder;
	private double ontPseudoConfidence = 0.2;
	private double custPseudoConfidence = 0.2;
	private double cprPseudoConfidence = 0.2;
	private boolean deprioritiseOnts = false;

    public MEMMRecogniser() {
        
    }


    public MEMMModel getModel() {
        if (model == null) {
            model = new ChemPapersModel();
        }
        return model;
    }

    public void setModel(MEMMModel model) {
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
            ontologyTermFinder = DFAONTCPRFinder.getDefaultInstance();
        }
        return ontologyTermFinder;
    }

    public void setOntologyTermFinder(DFAONTCPRFinder ontologyTermFinder) {
        this.ontologyTermFinder = ontologyTermFinder;
    }
    

    public List<NamedEntity> findNamedEntities(IProcessingDocument procDoc) {
        return findNamedEntities(procDoc.getTokenSequences());
    }

    public List<NamedEntity> findNamedEntities(List<ITokenSequence> tokSeqList) {

        // Generate named entity list
        List<NamedEntity> neList = generateNamedEntities(tokSeqList);

        // Add ontology terms
        neList.addAll(generateOntologyTerms(tokSeqList));

        // Merge named entity ontIds/custTypes
        mergeNamedEntities(neList);

        setPseudoConfidences(neList);

        List<NamedEntity> rsList = StandoffResolver.resolveStandoffs(neList);
        for (NamedEntity ne : neList) {
            ne.setBlocked(true);
        }
        for (NamedEntity rs : rsList) {
            rs.setBlocked(false);
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
     * @param tokSeqList
     * @return
     */
    private List<NamedEntity> generateNamedEntities(List<ITokenSequence> tokSeqList) {
        List<NamedEntity> neList = new ArrayList<NamedEntity>();
        MEMM memm = new MEMM(getModel(), memmThreshold/5);
        for (ITokenSequence tokseq : tokSeqList) {
            for (NamedEntity ne : memm.findNEs(tokseq)) {
                if (ne.getConfidence() > memmThreshold) {
                    neList.add(ne);
                }
            }
        }
        memm.rescore(neList);
        return neList;
    }


    /**
     * Detects named entities using ontology terms.
     * @param tokSeqList
     * @return
     */
    private List<NamedEntity> generateOntologyTerms(List<ITokenSequence> tokSeqList) {
        List<NamedEntity> neList = new ArrayList<NamedEntity>();
        DFAONTCPRFinder ontologyTermFinder = getOntologyTermFinder();
        for (ITokenSequence t : tokSeqList) {
            neList.addAll(ontologyTermFinder.findNamedEntities(t));
        }
        return neList;
    }


    void setPseudoConfidences(List<NamedEntity> neList) {
        for (NamedEntity ne : neList) {
            double pseudoConf = Double.NaN;
            NamedEntityType type = ne.getType();
            if (NamedEntityType.ONTOLOGY.isInstance(type)) {
                pseudoConf = ontPseudoConfidence;
            }
            else if (NamedEntityType.LOCANTPREFIX.isInstance(type)) {
                pseudoConf = cprPseudoConfidence;
            }
            else if (NamedEntityType.CUSTOM.isInstance(type)) {
                pseudoConf = custPseudoConfidence;
            }
            ne.setPseudoConfidence(pseudoConf);
            ne.setDeprioritiseOnt(deprioritiseOnts);
        }
    }


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


	public void setDeprioritiseOnts(boolean deprioritiseOnts) {
		this.deprioritiseOnts  = deprioritiseOnts;
	}
    
}
