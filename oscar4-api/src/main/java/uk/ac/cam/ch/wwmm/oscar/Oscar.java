package uk.ac.cam.ch.wwmm.oscar;

import nu.xom.Document;
import nu.xom.Element;
import org.apache.log4j.Logger;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChEBIDictionary;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.DefaultDictionary;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ITokeniser;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscarMEMM.MEMMRecogniser;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper class with a simple API to access Oscar functionality.
 *
 * @author egonw
 */
public class Oscar {

    private static final Logger LOG = Logger.getLogger(Oscar.class);

    private ChemNameDictRegistry dictionaryRegistry = ChemNameDictRegistry.getInstance();

    private ITokeniser tokenizer;
    private ChemicalEntityRecogniser recogniser;


    /**
     * The dictionaries that are loading upon instantiation of this
     * class. If they are not available from the classpath, it will
     * silently fail though. The defaults are: {@value}.
     */
    public Oscar() throws Exception {
        // TODO - look at dictionary initialisation... don't like throws Exception here
        dictionaryRegistry.register(new DefaultDictionary());
        dictionaryRegistry.register(new ChEBIDictionary());
        tokenizer = newDefaultTokeniser();
        recogniser = newDefaultRecogniser();
    }


    public ChemNameDictRegistry getDictionaryRegistry() {
        return dictionaryRegistry;
    }


    public ITokeniser getTokenizer() {
        if (tokenizer == null) {
            tokenizer = newDefaultTokeniser();
        }
        return tokenizer;
    }

    public void setTokenizer(ITokeniser tokenizer) {
        if (tokenizer == null) {
            throw new IllegalArgumentException("Null tokenizer");
        }
        this.tokenizer = tokenizer;
    }

    private ITokeniser newDefaultTokeniser() {
        return Tokeniser.getInstance();
    }


    public ChemicalEntityRecogniser getRecogniser() {
        if (recogniser == null) {
            recogniser = newDefaultRecogniser();
        }
        return recogniser;
    }

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
     * {@link #normalize(String)}, {@link #tokenize(String)}, and
     * {@link #recognizeNamedEntities(List)}.
     *
     * @param input String with input.
     * @return      the recognized chemical entities.
     * @throws Exception
     */
    public List<NamedEntity> getNamedEntities(String input) throws Exception {
        input = normalize(input);
        List<TokenSequence> tokens = tokenize(input);
        List<NamedEntity> entities = recognizeNamedEntities(tokens);
        return entities;
    }

    /**
     * Wrapper methods that runs the full Oscar workflow, including resolving detected
     * entities to their chemical structures. It calls the methods
     * {@link #normalize(String)}, {@link #tokenize(String)},
     * {@link #recognizeNamedEntities(List)}, and {@link #resolveNamedEntities(List)}.
     *
     * @param input String with input.
     * @return      the recognized chemical entities.
     * @throws Exception
     */
    public Map<NamedEntity,String> getResolvedEntities(String input) throws Exception {
        List<NamedEntity> entities = getNamedEntities(input);
        Map<NamedEntity,String> molecules = resolveNamedEntities(entities);
        return molecules;
    }


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


    public List<TokenSequence> tokenize(String input) throws Exception {
        Document doc = createInputDocument(input);
        IProcessingDocument procDoc = new ProcessingDocumentFactory()
                .makeTokenisedDocument(tokenizer, doc, true, false, false);
        return procDoc.getTokenSequences();
    }

    private Document createInputDocument(String input) {
        Element paragraph = new Element("P");
        paragraph.appendChild(input);
        Document doc = new Document(paragraph);
        return doc;
    }


    public String normalize(String input) {
        return input;
    }


    public List<NamedEntity> recognizeNamedEntities(List<TokenSequence> tokens) throws Exception {
        return recogniser.findNamedEntities(tokens);
    }

}
