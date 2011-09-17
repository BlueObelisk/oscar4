package uk.ac.cam.ch.wwmm.oscarMEMM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.ont.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMModel;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.ChemPapersModel;
import uk.ac.cam.ch.wwmm.oscarrecogniser.finder.DFAONTCPRFinder;
import uk.ac.cam.ch.wwmm.oscarrecogniser.finder.DFASupplementaryTermFinder;
import uk.ac.cam.ch.wwmm.oscarrecogniser.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscarrecogniser.saf.StandoffResolver;
import uk.ac.cam.ch.wwmm.oscarrecogniser.saf.StandoffResolver.ResolutionMode;

/**
 * Name recognition using the Maximum Entropy Markov Model
 *
 * @author ptc24
 * @author j_robinson
 * @author dmj30
 */
public class MEMMRecogniser implements ChemicalEntityRecogniser {

	 private static final Logger LOG = LoggerFactory.getLogger(MEMMRecogniser.class);
	
	private MEMMModel model;
    private double memmThreshold = 0.2;
    private DFAONTCPRFinder ontologyAndPrefixTermFinder;
	private double ontPseudoConfidence = 0.2;
	private double custPseudoConfidence = 0.2;
	private double cprPseudoConfidence = 0.2;
	private boolean deprioritiseOnts = false;
	private boolean useRescorer = true;
	private DFASupplementaryTermFinder supplementaryTermFinder;

	/**
	 * Creates the default MEMMRecogniser, using the {@link ChemPapersModel},
	 * the default {@link OntologyTerms} (defined in ontology.txt), and no
	 * supplementary chemical names.
	 */
    public MEMMRecogniser() {
        this (new ChemPapersModel(), OntologyTerms.getDefaultInstance(), new ChemNameDictRegistry(Locale.ENGLISH));
    }
    
    /**
     * Creates a custom MEMMRecogniser.
     * 
     * @param model the MEMMModel to be used
     * @param ontTerms the {@link OntologyTerms} to be identified
     * @param supplementaryNameRegistry a {@link ChemNameDictRegistry}
     * defining a set of additional chemical names to be annotated.
     * The performance for a large number of terms is currently poor.
     */
    public MEMMRecogniser(MEMMModel model, OntologyTerms ontTerms, ChemNameDictRegistry supplementaryNameRegistry) {
    	this.model = model;
    	this.ontologyAndPrefixTermFinder = new DFAONTCPRFinder(ontTerms);
    	Set <String> supplementaryNames = supplementaryNameRegistry.getAllNames();
    	if (supplementaryNames.size() > 0) {
    		this.supplementaryTermFinder = new DFASupplementaryTermFinder(supplementaryNameRegistry);	
    	}
    	
    }


    public MEMMModel getModel() {
        return model;
    }

    public double getMemmThreshold() {
        return memmThreshold;
    }

    public void setMemmThreshold(double memmThreshold) {
        this.memmThreshold = memmThreshold;
    }


    public DFAONTCPRFinder getOntologyAndPrefixTermFinder() {
        return ontologyAndPrefixTermFinder;
    }


    public List<NamedEntity> findNamedEntities(IProcessingDocument procDoc) {
        return findNamedEntities(procDoc.getTokenSequences());
    }

    public List<NamedEntity> findNamedEntities(List<TokenSequence> tokSeqList) {
    	return findNamedEntities(tokSeqList, ResolutionMode.REMOVE_BLOCKED);
    }
    
    public List<NamedEntity> findNamedEntities(List<TokenSequence> tokSeqList, ResolutionMode resolutionMode) {

        // Generate named entity list
        List<NamedEntity> neList = generateNamedEntities(tokSeqList);
        
        // Add supplementary named entities
        if (supplementaryTermFinder != null) {
        	neList.addAll(generateSupplementaryNameTerms(tokSeqList));
        }

        // Add ontology terms
        neList.addAll(generateOntologyAndPrefixTerms(tokSeqList));

        // Merge named entity ontIds/custTypes
        mergeNamedEntities(neList);

        setPseudoConfidences(neList);
        
        Collections.sort(neList);

        if (resolutionMode == ResolutionMode.REMOVE_BLOCKED) {
        	StandoffResolver.resolveStandoffs(neList);
        }
        else if (resolutionMode == ResolutionMode.MARK_BLOCKED) {
        	StandoffResolver.markBlockedStandoffs(neList);
        }
        else {
			throw new RuntimeException(resolutionMode + " not yet implemented");
		}

        return neList;
    }

    
	/**
     * Make sure all NEs at a position share their ontIds and custTypes
     * @param neList
     */
    private void mergeNamedEntities(List<NamedEntity> neList) {
    	//TODO this code is duplicated (and refactored) in PatternRecogniser
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
    private List<NamedEntity> generateNamedEntities(List<TokenSequence> tokSeqList) {
        List<NamedEntity> neList = new ArrayList<NamedEntity>();
        for (TokenSequence tokseq : tokSeqList) {
        	//TODO check divide by five (this was also in OSCAR3)
        	neList.addAll(model.findNEs(tokseq, memmThreshold/5));
        }
    	if (useRescorer){
            if (model.getRescorer() != null) {
        		model.getRescorer().rescore(neList, model.getChemNameDictNames());	
        	} 
        	else {
        		LOG.info("Model does not contain a rescorer");
        	}
    	}
        
        List<NamedEntity> nesSatisfyingThreshold = new ArrayList<NamedEntity>();
        for (NamedEntity ne : neList) {
            if (ne.getConfidence() > memmThreshold) {
            	nesSatisfyingThreshold.add(ne);
            }
        }
        return nesSatisfyingThreshold;
    }


    /**
     * Detects named entities using ontology terms.
     * @param tokSeqList
     * @return
     */
    private List<NamedEntity> generateOntologyAndPrefixTerms(List<TokenSequence> tokSeqList) {
        List<NamedEntity> neList = new ArrayList<NamedEntity>();
        for (TokenSequence t : tokSeqList) {
            neList.addAll(ontologyAndPrefixTermFinder.findNamedEntities(t));
        }
        return neList;
    }
    
    private List<NamedEntity> generateSupplementaryNameTerms(List<TokenSequence> tokSeqList) {
		List<NamedEntity> neList = new ArrayList<NamedEntity>();
		for (TokenSequence t : tokSeqList) {
			neList.addAll(supplementaryTermFinder.findNamedEntities(t));
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

	/**
	 * Is the recogniser set to always prefer other entities to ontology entities
	 * when finding in REMOVE_BLOCKED mode
	 * @return
	 */
	public boolean isDeprioritiseOnts() {
		return deprioritiseOnts;
	}

	/**
	 * Sets whether the recogniser should always prefer other entities to ontology entities
	 * when finding in REMOVE_BLOCKED mode
	 * @param deprioritiseOnts
	 */
	public void setDeprioritiseOnts(boolean deprioritiseOnts) {
		this.deprioritiseOnts  = deprioritiseOnts;
	}
	
	/**
	 * Is the MEMM rescorer being used
	 * The rescorer takes into account the occurrence of entities to modify the confidence of other entities
	 * @return
	 */
	public boolean isUseRescorer() {
		return useRescorer;
	}

	/**
	 * Sets whether the MEMM rescorer should be used
	 * The rescorer takes into account the occurrence of entities to modify the confidence of other entities
	 * @param useRescorer
	 */
	public void setUseRescorer(boolean useRescorer) {
		this.useRescorer = useRescorer;
	}
}
