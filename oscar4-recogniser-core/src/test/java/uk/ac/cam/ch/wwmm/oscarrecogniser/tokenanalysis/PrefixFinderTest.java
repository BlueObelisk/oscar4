package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscarrecogniser.manualAnnotations.ManualAnnotations;

public class PrefixFinderTest {

	@Test
	public void testGetHardcodedPrefix() {
		assertEquals("m-", PrefixFinder.getPrefix("m-chlorotoluene"));
		assertEquals("m-", PrefixFinder.getPrefix("m-chlorotoluene", null));
	}
	
	@Test
	public void testDontGetNonPrefix() {
		assertNull(PrefixFinder.getPrefix("foo-chlorotoluene"));
		assertNull(PrefixFinder.getPrefix("foo-chlorotoluene", null));
	}
	
	@Test
	public void testDontGetEtdNotForPrefix() {
		ManualAnnotations mockAnnotations = mock(ManualAnnotations.class);
		Set <String> notForPrefix = new HashSet<String>();
		stub(mockAnnotations.getNotForPrefix()).toReturn(notForPrefix);
		
		assertEquals("m-", PrefixFinder.getPrefix("m-chlorotoluene", mockAnnotations));
		
		notForPrefix.add("chlorotoluene");
		assertNull(PrefixFinder.getPrefix("m-chlorotoluene", mockAnnotations));
	}
}