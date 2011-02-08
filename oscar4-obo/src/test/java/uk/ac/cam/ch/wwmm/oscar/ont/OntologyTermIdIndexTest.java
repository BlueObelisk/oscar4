package uk.ac.cam.ch.wwmm.oscar.ont;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;


public class OntologyTermIdIndexTest {

	@Test
	public void testGetInstance() {
		OntologyTermIdIndex instance = OntologyTermIdIndex.getInstance();
		assertNotNull(instance);
	}

	@Test
	public void testContainsAcid() {
		OntologyTermIdIndex instance = OntologyTermIdIndex.getInstance();
		assertTrue(instance.containsTerm("acid"));
		List<String> identifiers = instance.getIdsForTerm("acid");
		assertEquals(1, identifiers.size());
		assertTrue(
			"Missing ChEBI identifier: CHEBI:37527",
			identifiers.contains("CHEBI:37527")
		);
	}

	@Test
	public void testGetAllTerms() {
		OntologyTermIdIndex instance = OntologyTermIdIndex.getInstance();
		Set<String> allTerms = instance.getAllTerms();
		assertNotSame(0, allTerms.size());
		assertTrue(allTerms.contains("acid"));
	}
	
	
	@Test
	public void testMakeHyphTokable() {
		Set <String> strings = new HashSet<String>();
		strings.add("foobar");
		strings.add("foo-bar");
		strings.add("gay bar");
		
		Set <String> hyphTokable = OntologyTermIdIndex.getInstance().makeHyphTokable(strings);
		assertEquals(2, hyphTokable.size());
		assertTrue(hyphTokable.contains("foo bar"));
		assertTrue(hyphTokable.contains("gay bar"));
	}
	
}
