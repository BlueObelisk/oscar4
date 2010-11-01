package uk.ac.cam.ch.wwmm.oscar;

import java.util.List;
import java.util.Map;

import uk.ac.cam.ch.wwmm.oscar.adv.AbstractOscar;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChEBIDictionary;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.DefaultDictionary;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;

/**
 * Helper class with a simple API to access Oscar functionality.
 *
 * @author egonw
 */
public class Oscar extends AbstractOscar {

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

	/**
	 * The dictionaries that are loading upon instantiation of this
	 * class. If they are not available from the classpath, it will
	 * silently fail though. The defaults are: {@value}.
	 */
	public Oscar() throws Exception {
		super(Oscar.class.getClassLoader());
		registry.register(new DefaultDictionary());
		registry.register(new ChEBIDictionary());
		tokenizerInstance = loadTokeniser(DEFAULT_TOKENISER);
		recogniserInstance = loadRecogiser(DEFAULT_RECOGISER);
	};

	/**
	 * Wrapper methods that runs the full Oscar workflow. It calls the methods
	 * {@link #normalize(String)}, {@link #tokenize(String)}, and
	 * {@link #recognizeNamedEntities(List)}.
	 * 
	 * @param input String with input.
	 * @return      the recognized chemical entities. 
	 * @throws Exception 
	 */
	public List<NamedEntity> getNamedEntities(String input)
	throws Exception {
		input = normalize(input);
		List<TokenSequence> tokens = tokenize(input);
		List<NamedEntity> entities = recognizeNamedEntities(tokens);
		return entities;
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
	public Map<NamedEntity,String> getResolvedEntities(String input)
	throws Exception {
		List<NamedEntity> entities = getNamedEntities(input);
		Map<NamedEntity,String> molecules = resolveNamedEntities(entities);
		return molecules;
	}

}
