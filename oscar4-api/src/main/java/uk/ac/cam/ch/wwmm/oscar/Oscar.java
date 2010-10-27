package uk.ac.cam.ch.wwmm.oscar;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.document.ITokeniser;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;

import nu.xom.Builder;
import nu.xom.Document;

/**
 * Helper class with a simple API to access Oscar functionality.
 *
 * @author egonw
 */
public class Oscar {

	/**
	 * The default tokeniser is used when no other tokeniser is defined.
	 * The default tokeniser is {@value}.
	 */
	public final String DEFAULT_TOKENISER =
		"uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser";

	/**
	 * The default recogniser is used when no other recogniser is defined.
	 * The default recogniser is {@value}.
	 */
	public final String DEFAULT_RECOGISER =
		"uk.ac.cam.ch.wwmm.oscarMEMM.MEMMRecogniser";

	private ChemNameDictRegistry registry;
	private String tokeniser = DEFAULT_TOKENISER;
	private String recogiser = DEFAULT_RECOGISER;

	public Oscar() {
		registry = ChemNameDictRegistry.getInstance();
	};

	public ChemNameDictRegistry getChemNameDict() {
		return registry;
	}

	/**
	 * Wrapper methods that runs the follow Oscar workflow. It calls the methods
	 * {@link #normalize(String)}, {@link #tokenize(String)},
	 * {@link #recognizeNamedEntities(List)}, and {@link #resolveNamedEntities(List)}.
	 * 
	 * @param input String with input.
	 * @return      the recognized chemical entities. 
	 * @throws Exception 
	 */
	public Map<NamedEntity,String> getNamedEntities(String input)
	throws Exception {
		input = normalize(input);
		List<TokenSequence> tokens = tokenize(input);
		List<NamedEntity> entities = recognizeNamedEntities(tokens);
		Map<NamedEntity,String> molecules = resolveNamedEntities(entities);
		return molecules;
	}

	public Map<NamedEntity,String> resolveNamedEntities(List<NamedEntity> entities) {
		Map<NamedEntity,String> hits = new HashMap<NamedEntity,String>();
		for (NamedEntity entity : entities) {
			String name = entity.getSurface();
			System.out.println("Entity: " + name);
			Set<String> inchis = registry.getInChI(name);
			if (inchis.size() == 1) {
				hits.put(entity, inchis.iterator().next());
			} else if (inchis.size() > 1) {
				System.out.println("Warning: multiple hits, returning only one");
				hits.put(entity, inchis.iterator().next());
			}
		}
		return hits;
	}

	public List<TokenSequence> tokenize(String input) throws Exception {
		Builder parser = new Builder();
		Document doc = parser.build(
			"<P>" + input + "</P>",
			"http://whatever.example.org/"
		);
		// load the tokenizer
		ITokeniser tokenizer = (ITokeniser)this.getClass().getClassLoader().loadClass(
			tokeniser
		).newInstance();
		ProcessingDocument procDoc = new ProcessingDocumentFactory().
			makeTokenisedDocument(
				tokenizer, doc, true, false, false
			);
		List<TokenSequence> tokenSequences = procDoc.getTokenSequences();
		for (TokenSequence tokens : tokenSequences) {
			for (Token token : tokens.getTokens())
				System.out.println("token: " + token.getValue());
		}
		return tokenSequences;
	}

	public String normalize(String input) {
		return input;
	}

	public List<NamedEntity> recognizeNamedEntities(List<TokenSequence> tokens) throws Exception {
		ChemicalEntityRecogniser recogniserInstance =
			(ChemicalEntityRecogniser)this.getClass().getClassLoader().
			loadClass(recogiser).newInstance();
		return recogniserInstance.findNamedEntities(tokens);
	}

	public void setTokeniser(String tokeniser) {
		this.tokeniser = tokeniser;
	}

	public String getTokeniser() {
		return tokeniser;
	}

	public void setRecogiser(String recogiser) {
		this.recogiser = recogiser;
	}

	public String getRecogiser() {
		return recogiser;
	}

}
