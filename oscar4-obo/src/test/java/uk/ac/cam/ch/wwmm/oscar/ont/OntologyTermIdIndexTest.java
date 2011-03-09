package uk.ac.cam.ch.wwmm.oscar.ont;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * 
 * @author dmj30
 *
 */
public class OntologyTermIdIndexTest {

	@Test
	public void testGetInstance() {
		OntologyTerms instance = OntologyTerms.getDefaultInstance();
		assertNotNull(instance);
	}

	@Test
	public void testContainsAcid() {
		OntologyTerms instance = OntologyTerms.getDefaultInstance();
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
		OntologyTerms instance = OntologyTerms.getDefaultInstance();
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
		
		Set <String> hyphTokable = OntologyTerms.getDefaultInstance().makeHyphTokable(strings);
		assertEquals(2, hyphTokable.size());
		assertTrue(hyphTokable.contains("foo bar"));
		assertTrue(hyphTokable.contains("gay bar"));
	}

	@Test
    public void testOntologyNotNull() {
        OntologyTerms ontologyTerms = OntologyTerms.getDefaultInstance();
        assertNotNull(ontologyTerms.getOntology());
    }

    @Test
    public void testOntologyNotEmpty() {
    	OntologyTerms ontologyTerms = OntologyTerms.getDefaultInstance();
        assertFalse(ontologyTerms.getOntology().isEmpty());
    }

    
    @Test (expected = UnsupportedOperationException.class)
    public void testUnmodifiable() {
    	OntologyTerms ontologyTerms = OntologyTerms.getDefaultInstance();
    	ontologyTerms.getOntology().put("foo", "bar");
    }
}
