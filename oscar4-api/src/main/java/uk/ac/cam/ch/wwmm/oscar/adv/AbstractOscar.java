package uk.ac.cam.ch.wwmm.oscar.adv;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.document.ITokeniser;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;

public abstract class AbstractOscar {

	protected ChemNameDictRegistry registry;
	protected ClassLoader classLoader;

	protected ITokeniser tokenizerInstance;
	protected ChemicalEntityRecogniser recogniserInstance;

	public AbstractOscar(ClassLoader classLoader) {
		registry = ChemNameDictRegistry.getInstance();
		this.classLoader = classLoader;
	};

	public ChemNameDictRegistry getDictionaryRegistry() {
		return registry;
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
		ProcessingDocument procDoc = new ProcessingDocumentFactory().
			makeTokenisedDocument(
				tokenizerInstance, doc, true, false, false
			);
		List<TokenSequence> tokenSequences = procDoc.getTokenSequences();
		/*for (TokenSequence tokens : tokenSequences) {
			for (Token token : tokens.getTokens())
				System.out.println("token: " + token.getValue());
		}*/
		return tokenSequences;
	}

	public String normalize(String input) {
		return input;
	}

	public List<NamedEntity> recognizeNamedEntities(List<TokenSequence> tokens) throws Exception {
		return recogniserInstance.findNamedEntities(tokens);
	}

	protected ITokeniser loadTokeniser(String tokeniser)
	throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class tokenizerClass = this.classLoader.loadClass(
			tokeniser
		);
		Method getInstanceMethod = tokenizerClass.getMethod("getInstance");
		return (ITokeniser)getInstanceMethod.invoke(null);
	}

	protected ChemicalEntityRecogniser loadRecogiser(String recogiser)
	throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		return (ChemicalEntityRecogniser)this.classLoader.
			loadClass(recogiser).newInstance();
	}

	public void setTokeniser(ITokeniser tokeniser) {
		if (tokeniser == null) throw new NullPointerException();
		this.tokenizerInstance = tokeniser;
	}

	public void setRecogiser(ChemicalEntityRecogniser recogiser) {
		if (recogiser == null) throw new NullPointerException();
		this.recogniserInstance = recogiser;
	}

}
