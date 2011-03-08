package uk.ac.cam.ch.wwmm.oscartokeniser;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;


public class TokenClassifierTest {

	@Test
	public void testLoadDefaultInstance() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		assertNotNull(classifier);
		assertEquals(7, classifier.getTokenLevelRegexes().size());
	}
	
	@Test
	public void testTlrRegexesAreCopied() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscartokeniser/tokenLevelRegularExpressions.xml");
		Document tlrDoc = new Builder().build(in);
		Map<String, TokenClass> tokenLevelRegexes = TokenClassifier.readXML(tlrDoc);
		TokenClassifier classifier = new TokenClassifier(tokenLevelRegexes);
		assertTrue(classifier.isTokenLevelRegexMatch("C-H", "bondRegex"));
		tokenLevelRegexes.clear();
		assertTrue(classifier.isTokenLevelRegexMatch("C-H", "bondRegex"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testTlrRegexesAreUnmodifiable() {
		Map<String, TokenClass> tokenLevelRegexes = new HashMap<String, TokenClass>();
		TokenClassifier classifier = new TokenClassifier(tokenLevelRegexes);
		classifier.getTokenLevelRegexes().clear();
	}

	
	@Test (expected = IllegalArgumentException.class)
	public void testNonExistentTokenLevelRegexMatch() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		classifier.isTokenLevelRegexMatch("", "foobar");
	}
	
	@Test
	public void testBondRegexIsTokenLevelRegexMatch() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		assertTrue(classifier.isTokenLevelRegexMatch("C-H", "bondRegex"));
		assertTrue(classifier.isTokenLevelRegexMatch("N=C", "bondRegex"));
		assertFalse(classifier.isTokenLevelRegexMatch("c-h", "bondRegex"));
		assertFalse(classifier.isTokenLevelRegexMatch("foobar", "bondRegex"));
	}
	
	@Test
	public void testFormulaRegexIsTokenLevelRegexMatch() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		assertTrue(classifier.isTokenLevelRegexMatch("C6H12O2", "formulaRegex"));
		assertTrue(classifier.isTokenLevelRegexMatch("CH3CH2CH(OH)CH3", "formulaRegex"));
		assertFalse(classifier.isTokenLevelRegexMatch("C6H12O2foo", "formulaRegex"));
		assertFalse(classifier.isTokenLevelRegexMatch("foobar", "formulaRegex"));
	}
	
	@Test
	public void testGroupFormulaRegexIsTokenLevelRegexMatch() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		assertTrue(classifier.isTokenLevelRegexMatch("-CH3", "groupFormulaRegex"));
		assertFalse(classifier.isTokenLevelRegexMatch("foobar", "groupFormulaRegex"));
	}
	
	@Test
	public void testNumberedAtomRegexIsTokenLevelRegexMatch() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		assertTrue(classifier.isTokenLevelRegexMatch("Au(1)", "numberedAtomRegex"));
		assertTrue(classifier.isTokenLevelRegexMatch("Au(1')", "numberedAtomRegex"));
		assertTrue(classifier.isTokenLevelRegexMatch("3-H", "numberedAtomRegex"));
		assertTrue(classifier.isTokenLevelRegexMatch("3'-H", "numberedAtomRegex"));
		assertTrue(classifier.isTokenLevelRegexMatch("Na-8", "numberedAtomRegex"));
		assertTrue(classifier.isTokenLevelRegexMatch("Na-8'", "numberedAtomRegex"));
		assertFalse(classifier.isTokenLevelRegexMatch("foobar", "numberedAtomRegex"));
	}
	
	@Test
	public void testGeminalAtomRegexIsTokenLevelRegexMatch() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		assertTrue(classifier.isTokenLevelRegexMatch("3-H", "geminalAtomsRegex"));
		assertTrue(classifier.isTokenLevelRegexMatch("2-/3-H", "geminalAtomsRegex"));
		assertTrue(classifier.isTokenLevelRegexMatch("3-H2", "geminalAtomsRegex"));
		assertFalse(classifier.isTokenLevelRegexMatch("foobar", "geminalAtomsRegex"));
	}
	
	@Test
	public void testIsotopeRegexIsTokenLevelRegexMatch() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		assertTrue(classifier.isTokenLevelRegexMatch("1H", "isotopeRegex"));
		assertTrue(classifier.isTokenLevelRegexMatch("(2)H", "isotopeRegex"));
		assertFalse(classifier.isTokenLevelRegexMatch("foobar", "isotopeRegex"));
	}
	
	@Test
	public void testProperNounRegexIsTokenLevelRegexMatch() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		assertTrue(classifier.isTokenLevelRegexMatch("Anderson", "properNounRegex"));
		assertFalse(classifier.isTokenLevelRegexMatch("anderson", "properNounRegex"));
		assertFalse(classifier.isTokenLevelRegexMatch("The", "properNounRegex"));
		assertFalse(classifier.isTokenLevelRegexMatch("These", "properNounRegex"));
		assertTrue(classifier.isTokenLevelRegexMatch("McGuyver", "properNounRegex"));
		assertFalse(classifier.isTokenLevelRegexMatch("FedEx", "properNounRegex"));
		assertTrue(classifier.isTokenLevelRegexMatch("Diels-Alder", "properNounRegex"));
		assertFalse(classifier.isTokenLevelRegexMatch("Diels-alder", "properNounRegex"));
	}
	
	
	@Test
	public void testClassifyBondToken() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		Set <NamedEntityType> types = classifier.classifyToken("C-H");
		assertEquals(1, types.size());
		assertEquals("BOND", types.iterator().next().getName());
	}
	
	@Test
	public void testClassifyFormulaToken() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		Set <NamedEntityType> types = classifier.classifyToken("CH3CH2CH(OH)CH3");
		assertEquals(1, types.size());
		assertEquals("FORM", types.iterator().next().getName());
	}
	
	@Test
	public void testClassifyGroupFormulaToken() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		Set <NamedEntityType> types = classifier.classifyToken("-CH3");
		assertEquals(1, types.size());
		assertEquals("FORM", types.iterator().next().getName());
	}
	
	@Test
	public void testClassifyNumberedAtomToken() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		Set <NamedEntityType> types = classifier.classifyToken("Au(1')");
		assertEquals(1, types.size());
		assertEquals("ATOM", types.iterator().next().getName());
	}
	
	@Test
	public void testClassifyGeminalAtomToken() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		Set <NamedEntityType> types = classifier.classifyToken("3-H");
		assertEquals(2, types.size());
		boolean classifiedAsNot = false;
		boolean classifiedAsAtom = false;
		Iterator<NamedEntityType> it = types.iterator();
		while (it.hasNext()) {
			String type = it.next().getName();
			if ("NOT".equals(type)) {
				classifiedAsNot = true;
			}
			else if ("ATOM".equals(type)) {
				classifiedAsAtom = true;
			}
		}
		assertTrue(classifiedAsAtom);
		assertTrue(classifiedAsNot);
	}
	
	@Test
	public void testClassifyIsotopeToken() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		Set <NamedEntityType> types = classifier.classifyToken("(2)H");
		assertEquals(1, types.size());
		assertEquals("EM", types.iterator().next().getName());
	}
	
	@Test
	public void testClassifyProperNounToken() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		Set <NamedEntityType> types = classifier.classifyToken("McGuyver");
		assertEquals(1, types.size());
		assertEquals("PN", types.iterator().next().getName());
	}
	
	@Test
	public void testClassifyNonMatchingToken() {
		TokenClassifier classifier = TokenClassifier.getDefaultInstance();
		Set <NamedEntityType> types = classifier.classifyToken("foobar");
		assertEquals(0, types.size());
	}
	
	
	
}
