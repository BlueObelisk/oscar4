package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata.ExtractedTrainingData;

/**
 * 
 * @author dmj30
 *
 */
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
		ExtractedTrainingData mockAnnotations = mock(ExtractedTrainingData.class);
		Set <String> notForPrefix = new HashSet<String>();
		stub(mockAnnotations.getNotForPrefix()).toReturn(notForPrefix);
		
		assertEquals("m-", PrefixFinder.getPrefix("m-chlorotoluene", mockAnnotations));
		
		notForPrefix.add("chlorotoluene");
		assertNull(PrefixFinder.getPrefix("m-chlorotoluene", mockAnnotations));
	}
	
	@Test
	public void testFunnyHyphens() {
		String text = "1\u2010hydroxybenzotriazole";
		assertEquals("1\u2010", PrefixFinder.getPrefix(text));
	}
}
