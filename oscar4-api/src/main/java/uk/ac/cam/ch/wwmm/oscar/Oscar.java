package uk.ac.cam.ch.wwmm.oscar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.ITokeniser;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.ont.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscarMEMM.MEMMRecogniser;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.data.MEMMModel;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.ChemPapersModel;
import uk.ac.cam.ch.wwmm.oscarrecogniser.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * Helper class with a simple API to access OSCAR functionality. Setter methods
 * are provided to override default components of the OSCAR workflow using
 * dependency injection. 
 *
 * @author egonw
 * @author dmj30
 */
public class Oscar {

    private static final Log LOG = LogFactory.getLog(Oscar.class);

    private ChemNameDictRegistry dictionaryRegistry;
    private ITokeniser tokeniser;
    private ChemicalEntityRecogniser recogniser;
    private OntologyTerms ontologyTerms;
    private MEMMModel memmModel;
    /*
     * TokenClassifier -> Tokeniser
     * 
     * MEMMModel -> MEMMRecogniser
     * OntologyTerms -> MEMMRecogniser
     * 
     * MEMMModel embeds ManualAnnotations -> PatternRecogniser
     * neTerms -> PatternRecogniser
     * TokenClassifier -> PatternRecogniser
     * OntologyTerms -> PatternRecogniser
     * ChemNameDictRegistry -> PatternRecogniser
     * 
     */


    /**
     * Returns the {@link ChemNameDictRegistry} used in this {@link Oscar}
     * instance.
     *
     * @return The current chemical name dictionary registry.
     */
    public synchronized ChemNameDictRegistry getDictionaryRegistry() {
        if (dictionaryRegistry == null) {
        	dictionaryRegistry = new ChemNameDictRegistry();
        }
    	return dictionaryRegistry;
    }

    public void setDictionaryRegistry(ChemNameDictRegistry dictionaryRegistry) {
		this.dictionaryRegistry = dictionaryRegistry;
		
	}

    
    /**
     * Returns the tokeniser used by this {@link Oscar} instance for splitting
     * sentences up into tokens. Defaults to the inbuilt Oscar {@link Tokeniser}.
     *
     * @return the active {@link ITokeniser}.
     * @see    #setTokeniser(ITokeniser)
     */
    public synchronized ITokeniser getTokeniser() {
        if (tokeniser == null) {
            tokeniser = Tokeniser.getDefaultInstance();
        }
        return tokeniser;
    }

    /**
     * Sets the {@link ITokeniser} implementation to be used for sentence
     * splitting.
     *
     * @param tokeniser and {@link ITokeniser} implementation.
     * @see   #getTokeniser()
     */
    public void setTokeniser(ITokeniser tokeniser) {
        if (tokeniser == null) {
            throw new IllegalArgumentException("Null tokeniser");
        }
        this.tokeniser = tokeniser;
    }


    /**
     * Returns the chemical entity recogniser used by this Oscar to
     * identify chemical named entities. Defaults to the {@link MEMMRecogniser}
     * using the default {@link MEMMModel} and {@link OntologyTerms} objects,
     * unless overridden using {@link #setMemmModel(MEMMModel)} and
     * {@link #setOntologyTerms(OntologyTerms)} respectively.
     * 
     *
     * @return a {@link ChemicalEntityRecogniser}.
     * @see #setRecogniser(ChemicalEntityRecogniser)
     * @see #setMemmModel(MEMMModel)
     * @see #setOntologyTerms(OntologyTerms)
     */
    public synchronized ChemicalEntityRecogniser getRecogniser() {
        if (recogniser == null) {
            recogniser = new MEMMRecogniser(getMemmModel(), getOntologyTerms());
        }
        return recogniser;
    }

    /**
     * Sets the chemical name recogniser to be used for
     * named entity recognition.
     *
     * @param recogniser the new {@link ChemicalEntityRecogniser}.
     * @see Oscar#getRecogniser()
     */
    public void setRecogniser(ChemicalEntityRecogniser recogniser) {
        if (recogniser == null) {
            throw new IllegalArgumentException("Null recogniser");
        }
        this.recogniser = recogniser;
    }

    /**
     * Returns the {@link OntologyTerms} to be recognised by this
     * {@link Oscar} instance. Defaults to the inbuilt {@link OntologyTerms}
     * derived from ChEBI, FIX and REX.
     *
     * @return the active {@link OntologyTerms}.
     * @see    #setOntologyTerms(OntologyTerms)
     */
	public synchronized OntologyTerms getOntologyTerms() {
		if (ontologyTerms == null) {
			ontologyTerms = OntologyTerms.getDefaultInstance();
		}
		return ontologyTerms;
	}

	/**
	 * Sets the ({@link OntologyTerms} to be used by the default {@link MEMMRecogniser}
	 * during named entity recognition. Will not override the behaviour of a
	 * {@link ChemicalEntityRecogniser} injected using the
	 * {@link #setRecogniser(ChemicalEntityRecogniser) method.
	 * 
	 * @param ontologyTerms
	 * @see Oscar#getOntologyTerms()
	 */
	public void setOntologyTerms(OntologyTerms ontologyTerms) {
		this.ontologyTerms = ontologyTerms;
	}

	/**
	 * Gets the {@link MEMMModel} to be used by the {@link MEMMRecogniser}
	 * during named entity recognition. Defaults to the {@link ChemPapersModel}.
	 *  
	 * @return
	 * @see Oscar#setMemmModel(MEMMModel)
	 */
	public synchronized MEMMModel getMemmModel() {
		if (memmModel == null) {
			memmModel = new ChemPapersModel();
		}
		return memmModel;
	}

	/**
	 * Sets the {@link MEMMModel} to be used by the default {@link MEMMRecogniser}
	 * during named entity recognition. Will not override the behaviour of a
	 * {@link ChemicalEntityRecogniser} injected using the
	 * {@link #setRecogniser(ChemicalEntityRecogniser)} method.
	 * 
	 * @param memmModel
	 * @see Oscar#getMemmModel()
	 */
	public void setMemmModel(MEMMModel memmModel) {
		this.memmModel = memmModel;
	}
    
	
	
	
	
    /**
     * Wrapper method for identification of named entities. It calls the methods
     * {@link #normalise(String)}, {@link #tokenise(String)}, and
     * {@link #recogniseNamedEntities(List)}.
     *
     * @param input the input text.
     * @return the recognised chemical entities.
     */
    public List<NamedEntity> findNamedEntities(String input) {
        input = normalise(input);
        List<ITokenSequence> tokens = tokenise(input);
        List<NamedEntity> entities = recogniseNamedEntities(tokens);
        return entities;
    }

    /**
     * Wrapper method for the identification of chemical named entities
     * and their resolution to connection tables. It calls the methods
     * {@link #normalise(String)}, {@link #tokenise(String)},
     * {@link #recogniseNamedEntities(List)}, and {@link #resolveNamedEntities(List)}.
     *
     * @param input String with input.
     * @return the recognised chemical entities as a Map of NamedEntities to InChI strings
     */
    public Map<NamedEntity,String> findResolvedEntities(String input) {
        List<NamedEntity> entities = findNamedEntities(input);
        Map<NamedEntity,String> molecules = resolveNamedEntities(entities);
        return molecules;
    }

    /**
     * Converts named entities into chemical structures, represented by their
     * InChIs returned as {@link String}s.
     *
     * @param  entities a {@link List} of {@link NamedEntity}s.
     * @return          a {@link Map} linking {@link NamedEntity}s to InChIs 
     */
    public Map<NamedEntity,String> resolveNamedEntities(List<NamedEntity> entities) {
        Map<NamedEntity,String> hits = new HashMap<NamedEntity,String>();
        for (NamedEntity entity : entities) {
            String inchi = hits.get(entity);
            if (inchi == null) {
                inchi = resolveNamedEntity(entity.getSurface());
                if (inchi != null) {
                    hits.put(entity, inchi);
                }
            }
        }
        return hits;
    }

    private String resolveNamedEntity(String name) {
        Set<String> inchis = getDictionaryRegistry().getInChI(name);
        if (inchis.size() == 0) {
            return null;
        }
        if (inchis.size() > 1) {
            // TODO - should we handle this in the dictionary registry
            LOG.warn(name + ": multiple hits, returning only one");
        }
        return inchis.iterator().next();
    }

    /**
     * Converts a text into token sequences, one for each sentence.
     *
     * @param  input a text to analyse.
     * @return       a {@link List} of {@link ITokenSequence}s.
     */
    public List<ITokenSequence> tokenise(String input) {
        IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance()
                .makeTokenisedDocument(getTokeniser(), input);
        return procDoc.getTokenSequences();
    }


    /**
     * Normalise the text. Text normalisation involves, among others, converting
     * all hyphens into one character, simplifying the subsequent named entity
     * detection.
     * 
     * Not yet implemented.
     *
     * @param  input the unnormalised text.
     * @return       the normalised text.
     */
    private String normalise(String input) {
    	//TODO implement this method?
        return input;
    }

    /**
     * Extracts named entities from a text represented as a {@link List}
     * of {@link ITokenSequence}s.
     *
     * @param  entities a {@link List} of {@link ITokenSequence}s.
     * @return          a {@link List} of {@link NamedEntity}s.
     */
    public List<NamedEntity> recogniseNamedEntities(List<ITokenSequence> tokens) {
        return getRecogniser().findNamedEntities(tokens);
    }

	
    

}
