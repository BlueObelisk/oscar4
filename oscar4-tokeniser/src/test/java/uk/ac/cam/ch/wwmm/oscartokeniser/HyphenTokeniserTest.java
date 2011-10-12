package uk.ac.cam.ch.wwmm.oscartokeniser;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**
 * 
 * @author dmj30
 *
 */
public class HyphenTokeniserTest {

	@Test
	public void testSuffixContainedInSplitSuffix() {
		HyphenTokeniser tokeniser = HyphenTokeniser.getInstance();
		assertFalse(tokeniser.suffixContainedInSplitSuffix("foo-bar", 3));
		assertTrue(tokeniser.suffixContainedInSplitSuffix("foo-labelled", 3));
	}
	
	@Test
	public void testTermMatchesProperNounPattern() {
		HyphenTokeniser tokeniser = HyphenTokeniser.getInstance();
		assertTrue(tokeniser.termMatchesPropernounPattern("Baeyer-Villiger"));
		assertTrue(tokeniser.termMatchesPropernounPattern("Tert-Villiger"));
		assertFalse(tokeniser.termMatchesPropernounPattern("baeyer-villiger"));
		assertFalse(tokeniser.termMatchesPropernounPattern("baeyer-Villiger"));
		assertFalse(tokeniser.termMatchesPropernounPattern("Baeyer-villiger"));
		assertTrue(tokeniser.termMatchesPropernounPattern("Baeyer-Baeyer-Villiger"));
		assertTrue(tokeniser.termMatchesPropernounPattern("Baeyer's-Villiger"));
		assertTrue(tokeniser.termMatchesPropernounPattern("Baeyer-Villigers'"));
	}
	
	@Test
	public void testSuffixStartsWithSplitSuffix() {
		HyphenTokeniser tokeniser = HyphenTokeniser.getInstance();
		assertFalse(tokeniser.suffixStartsWithSplitSuffix("foo-poisone", 3));
		assertTrue(tokeniser.suffixStartsWithSplitSuffix("foo-poisoned", 3));
		assertTrue(tokeniser.suffixStartsWithSplitSuffix("foo-poisoneds", 3));
		
		assertFalse(tokeniser.suffixStartsWithSplitSuffix("foo-monopoisone", 3));
		assertTrue(tokeniser.suffixStartsWithSplitSuffix("foo-monopoisoned", 3));
		
		assertTrue(tokeniser.suffixStartsWithSplitSuffix("foo-form", 3));
		assertTrue(tokeniser.suffixStartsWithSplitSuffix("foo-forms", 3));
		assertFalse(tokeniser.suffixStartsWithSplitSuffix("foo-formyl", 3));
	}
	
	@Test
	public void testLowercaseEitherSideOfHyphen() {
		HyphenTokeniser tokeniser = HyphenTokeniser.getInstance();
		assertTrue(tokeniser.lowercaseEitherSideOfHyphen("aa-aa", 2));
		assertFalse(tokeniser.lowercaseEitherSideOfHyphen("Aa-aa", 2));
		assertFalse(tokeniser.lowercaseEitherSideOfHyphen("aA-aa", 2));
		assertFalse(tokeniser.lowercaseEitherSideOfHyphen("aa-Aa", 2));
		assertFalse(tokeniser.lowercaseEitherSideOfHyphen("aa-aA", 2));
		assertTrue(tokeniser.lowercaseEitherSideOfHyphen("Aaa-aa", 3));
		assertFalse(tokeniser.lowercaseEitherSideOfHyphen("11-aa", 2));
	}

	
	
	@Test
	public void testIndexOfSplittableHyphenNoHyphen() {
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("foo"));
	}
	
	@Test
	public void testIndexOfSplittableHyphen() {
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen("foo-bar"));
		assertEquals(7, HyphenTokeniser.indexOfSplittableHyphen("alcohol-consuming"));
	}
	
	@Test
	public void testIndexOfNonsplittableHyphen() {
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("tert-butane"));
	}
	

	@Test
	public void testEnDash() {
		String simple = "foo" + StringTools.enDash + "bar";
		String noSplitPrefix = "tert" + StringTools.enDash + "foo";
		String splitSuffix = "foo" + StringTools.enDash + "poisoned";
		String noSplitPrefixSplitSuffix = "tert" + StringTools.enDash + "posioned";
		String withCaps = "FOO" + StringTools.enDash + "bar";
		
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen(simple));
		assertEquals(4, HyphenTokeniser.indexOfSplittableHyphen(noSplitPrefix));
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen(splitSuffix));
		assertEquals(4, HyphenTokeniser.indexOfSplittableHyphen(noSplitPrefixSplitSuffix));
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen(withCaps));
	}
	
	@Test
	public void testEmDash() {
		String simple = "foo" + StringTools.emDash + "bar";
		String noSplitPrefix = "tert" + StringTools.emDash + "foo";
		String splitSuffix = "foo" + StringTools.emDash + "poisoned";
		String noSplitPrefixSplitSuffix = "tert" + StringTools.emDash + "posioned";
		String withCaps = "FOO" + StringTools.emDash + "bar";
		
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen(simple));
		assertEquals(4, HyphenTokeniser.indexOfSplittableHyphen(noSplitPrefix));
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen(splitSuffix));
		assertEquals(4, HyphenTokeniser.indexOfSplittableHyphen(noSplitPrefixSplitSuffix));
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen(withCaps));
	}
	
	@Test
	public void testOntontologyTerm() {
		HyphenTokeniser tokeniser = HyphenTokeniser.getInstance();
		String termInOntologyWithSpace = "inorganic-group";
		assertEquals(true, tokeniser.termContainedInHyphTokable(termInOntologyWithSpace, termInOntologyWithSpace.indexOf('-')));
		String notInOntology = "foo-bar";
		assertEquals(false, tokeniser.termContainedInHyphTokable(notInOntology, notInOntology.indexOf('-')));
	}

	@Test
	public void testSplitSuffix() {
		String splitSuffix = "foo-labelled";
		String splitSuffixNoSplitPrefix = "tert-labelled";
		String withCaps = "FOO-labelled";
		
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen(splitSuffix));
		assertEquals(4, HyphenTokeniser.indexOfSplittableHyphen(splitSuffixNoSplitPrefix));
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen(withCaps));
	}
	
	@Test
	public void testProperNouns() {
		String simple = "Baeyer-Villiger";
		String nosplitPrefix = "Tert-Villiger";
		String tripleBarrelled = "Baeyer-Baeyer-Villiger";
		String withApostrophe1 = "Baeyer's-Villiger";
		String withApostrophe2 = "Baeyer-Villigers'";
		
		assertEquals(6, HyphenTokeniser.indexOfSplittableHyphen(simple));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen(nosplitPrefix));
		assertEquals(13, HyphenTokeniser.indexOfSplittableHyphen(tripleBarrelled));
		assertEquals(8, HyphenTokeniser.indexOfSplittableHyphen(withApostrophe1));
		assertEquals(6, HyphenTokeniser.indexOfSplittableHyphen(withApostrophe2));
	}
	
	@Test
	public void testSplitSuffixes() {
		String simple = "foo-poisoned";
		String suffixPrefix = "foo-monopoisoned";
		String noSplitPrefix = "tert-poisoned";
		String withCaps = "FOO-poisoned";
		
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen(simple));
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen(suffixPrefix));
		assertEquals(4, HyphenTokeniser.indexOfSplittableHyphen(noSplitPrefix));
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen(withCaps));
	}
	
	@Test
	public void testNoSplitPrefix() {
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("tert-foo"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("2-tert-foo"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("fooyl-bar"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("ter-phenyl"));
		assertEquals(5, HyphenTokeniser.indexOfSplittableHyphen("water-phenyl"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("methylazo-bar"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("fmoc-bar"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("2fmoc-bar"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("2,3-dichloro-bar"));
	}
	
	@Test
	public void testLowercaseEitherSide() {
		assertEquals(2, HyphenTokeniser.indexOfSplittableHyphen("aa-aa"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("Aa-aa"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("aA-aa"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("aa-Aa"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("aa-aA"));
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen("Aaa-aa"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("11-aa"));
	}
	
	
	@Test
	public void testMinTwoCharactersAfterHyphen() {
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen("foo-foo"));
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen("foo-fo"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("foo-f"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("foo-"));
	}
	
	@Test
	public void minTwoCharactersBeforeHyphen() {
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen("foo-foo"));
		assertEquals(2, HyphenTokeniser.indexOfSplittableHyphen("fo-foo"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("f-foo"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("-foo"));
	}
	
	@Test
	public void testNoTokenisationInsideBrackets() {
		assertEquals(3, HyphenTokeniser.indexOfSplittableHyphen("foo-foo"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("(foo-foo)"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("[foo-foo]"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("{foo-foo}"));
	}
	
	@Test
	public void noTokenisationUnlessLowerCaseEitherSide() {
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("foo-Foo"));
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("FOO-foo"));
	}
	
	
}
