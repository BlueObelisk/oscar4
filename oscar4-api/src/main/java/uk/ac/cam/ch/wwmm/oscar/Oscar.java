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
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscarMEMM.MEMMRecogniser;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * Helper class with a simple API to access Oscar functionality.
 *
 * @author egonw
 */
public class Oscar {

    private static final Log LOG = LogFactory.getLog(Oscar.class);

    private ChemNameDictRegistry dictionaryRegistry = ChemNameDictRegistry.getInstance();

    private ITokeniser tokeniser;
    private ChemicalEntityRecogniser recogniser;

    /**
     * The dictionaries that are loading upon instantiation of this
     * class. If they are not available from the classpath, it will
     * silently fail though. The defaults are: {@value}.
     */
    public Oscar() {
        tokeniser = newDefaultTokeniser();
        recogniser = newDefaultRecogniser();
    }

    /**
     * Returns the {@link ChemNameDictRegistry} used in this {@link Oscar}
     * instance.
     *
     * @return The current chemical name dictionary.
     */
    public ChemNameDictRegistry getDictionaryRegistry() {
        return dictionaryRegistry;
    }

    /**
     * Returns the tokeniser used in this text analyzer for splitting
     * sentences up in tokens.
     *
     * @return the active {@link ITokeniser}.
     * @see    #setTokeniser(ITokeniser)
     */
    public ITokeniser getTokeniser() {
        if (tokeniser == null) {
            tokeniser = newDefaultTokeniser();
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

    private ITokeniser newDefaultTokeniser() {
        return Tokeniser.getDefaultInstance();
    }

    /**
     * Returns the chemical entity recogniser used by this Oscar to
     * convert named entities into chemical structures.
     *
     * @return an {@link ChemicalEntityRecogniser}.
     * @see    #setRecogniser(ChemicalEntityRecogniser)
     */
    public ChemicalEntityRecogniser getRecogniser() {
        if (recogniser == null) {
            recogniser = newDefaultRecogniser();
        }
        return recogniser;
    }

    /**
     * Sets a new chemical name recogniser.
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

    private ChemicalEntityRecogniser newDefaultRecogniser() {
        return new MEMMRecogniser();
    }

    /**
     * Wrapper methods that runs the full Oscar workflow, except for resolving detected
     * entities to their chemical structures. It calls the methods
     * {@link #normalize(String)}, {@link #tokenise(String)}, and
     * {@link #recogniseNamedEntities(List)}.
     *
     * @param input String with input.
     * @return      the recognised chemical entities.
     */
    public List<NamedEntity> getNamedEntities(String input) {
        input = normalize(input);
        List<ITokenSequence> tokens = tokenise(input);
        List<NamedEntity> entities = recogniseNamedEntities(tokens);
        return entities;
    }

    /**
     * Wrapper methods that runs the full Oscar workflow, including resolving detected
     * entities to their chemical structures. It calls the methods
     * {@link #normalize(String)}, {@link #tokenise(String)},
     * {@link #recogniseNamedEntities(List)}, and {@link #resolveNamedEntities(List)}.
     *
     * @param input String with input.
     * @return the recognised chemical entities as a Map of NamedEntities to InChI strings
     */
    public Map<NamedEntity,String> getResolvedEntities(String input) {
        List<NamedEntity> entities = getNamedEntities(input);
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
        Set<String> inchis = dictionaryRegistry.getInChI(name);
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
     * @param  input a text to analyze.
     * @return       a {@link List} of {@link ITokenSequence}s.
     */
    public List<ITokenSequence> tokenise(String input) {
        IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance()
                .makeTokenisedDocument(tokeniser, input);
        return procDoc.getTokenSequences();
    }


    /**
     * Normalized the text. Text normalization involves, among others, converting
     * all hyphens into one character, simplifying the subsequent named entity
     * detection.
     * 
     * Not yet implemented.
     *
     * @param  input the unnormalized text.
     * @return       the normalized text.
     */
    public String normalize(String input) {
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
        return recogniser.findNamedEntities(tokens);
    }

}
