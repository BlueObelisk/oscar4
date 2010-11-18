package uk.ac.cam.ch.wwmm.oscar.terms;

import static org.junit.Assert.*;

import org.junit.Test;

public class TermSetsTest {

	@Test
	public void loadSplitSuffixes() {
		assertTrue(TermSets.getDefaultInstance().getSplitSuffixes().contains("biomonitor"));
		assertFalse(TermSets.getDefaultInstance().getSplitSuffixes().contains("foobar"));
	}
}
