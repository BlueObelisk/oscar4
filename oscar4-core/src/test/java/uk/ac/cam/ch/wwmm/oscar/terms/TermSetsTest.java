package uk.ac.cam.ch.wwmm.oscar.terms;

import static org.junit.Assert.*;

import org.junit.Test;

public class TermSetsTest {

	@Test
	public void loadSplitSuffixes() {
		assertTrue(TermSets.getSplitSuffixes().contains("biomonitor"));
		assertFalse(TermSets.getSplitSuffixes().contains("foobar"));
	}
}
