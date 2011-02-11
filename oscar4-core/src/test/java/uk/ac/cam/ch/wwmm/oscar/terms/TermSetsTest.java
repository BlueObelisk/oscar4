package uk.ac.cam.ch.wwmm.oscar.terms;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Test;

public class TermSetsTest {

	@Test
	public void testLoadAbbreviations() {
		assertTrue(TermSets.getDefaultInstance().getAbbreviations().contains("e.g."));
		assertFalse(TermSets.getDefaultInstance().getAbbreviations().contains("foobar"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testUnmodifiableAbbreviations() {
		TermSets.getDefaultInstance().getAbbreviations().add("foo");
	}
	
	
	@Test
	public void testLoadChemAses() {
		assertTrue(TermSets.getDefaultInstance().getChemAses().contains("thiaminase"));
		assertFalse(TermSets.getDefaultInstance().getChemAses().contains("foobar"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testUnmodifiableChemAses() {
		TermSets.getDefaultInstance().getChemAses().add("foo");
	}

	
	@Test
	public void testLoadClosedClass() {
		assertTrue(TermSets.getDefaultInstance().getClosedClass().contains("with"));
		assertFalse(TermSets.getDefaultInstance().getClosedClass().contains("foobar"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testUnmodifiableClosedClass() {
		TermSets.getDefaultInstance().getClosedClass().add("foo");
	}
	
	
	@Test
	public void testLoadElements() {
		assertTrue(TermSets.getDefaultInstance().getElements().contains("Ar"));
		assertFalse(TermSets.getDefaultInstance().getElements().contains("foobar"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testUnmodifiableElements() {
		TermSets.getDefaultInstance().getElements().add("foo");
	}
	
	
	@Test
	public void testLoadLigands() {
		assertTrue(TermSets.getDefaultInstance().getLigands().contains("edta"));
		assertFalse(TermSets.getDefaultInstance().getLigands().contains("foobar"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testUnmodifiableLigands() {
		TermSets.getDefaultInstance().getLigands().add("foo");
	}
	
	
	@Test
	public void testLoadNonChemAses() {
		assertTrue(TermSets.getDefaultInstance().getNonChemAses().contains("catalase"));
		assertFalse(TermSets.getDefaultInstance().getNonChemAses().contains("foobar"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testUnmodifiableNonChemAses() {
		TermSets.getDefaultInstance().getNonChemAses().add("foo");
	}
	
	
	@Test
	public void testLoadNoSplitPrefixes() {
		assertTrue(TermSets.getDefaultInstance().getNoSplitPrefixes().contains("alpha"));
		assertFalse(TermSets.getDefaultInstance().getNoSplitPrefixes().contains("foobar"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testUnmodifiableNoSplitPrefixes() {
		TermSets.getDefaultInstance().getNoSplitPrefixes().add("foo");
	}
	
	
	@Test
	public void testLoadReactWords() {
		assertTrue(TermSets.getDefaultInstance().getReactWords().contains("substitution"));
		assertFalse(TermSets.getDefaultInstance().getReactWords().contains("foobar"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testUnmodifiableReactWords() {
		TermSets.getDefaultInstance().getReactWords().add("foo");
	}
	
	
	@Test
	public void testLoadSplitSuffixes() {
		assertTrue(TermSets.getDefaultInstance().getSplitSuffixes().contains("biomonitor"));
		assertFalse(TermSets.getDefaultInstance().getSplitSuffixes().contains("foobar"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testUnmodifiableSplitSuffixes() {
		TermSets.getDefaultInstance().getSplitSuffixes().add("foo");
	}
	
	
	@Test
	public void testLoadStopWords() {
		assertEquals(0, TermSets.getDefaultInstance().getStopWords().size());
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testUnmodifiableStopWords() {
		TermSets.getDefaultInstance().getStopWords().add("foo");
	}
	
	
	@Test
	public void testLoadUsrDictWords() {
		assertTrue(TermSets.getDefaultInstance().getUsrDictWords().contains("perambulate"));
		assertFalse(TermSets.getDefaultInstance().getUsrDictWords().contains("foobar"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testUnmodifiableUsrDictWords() {
		TermSets.getDefaultInstance().getUsrDictWords().add("foo");
	}
	
	
	@Test
	public void testEndingInElementNamePattern() {
		Pattern pattern = TermSets.getDefaultInstance().getEndingInElementNamePattern(); 
		assertTrue(pattern.matcher("chromatin").matches());
		assertFalse(pattern.matcher("chromaSn").matches());
	}
}
