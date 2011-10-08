package uk.ac.cam.ch.wwmm.oscar.ont;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * 
 * @author dmj30
 *
 */
public class OntologyTermsTest {

	@Test
	public void testGetInstance() {
		OntologyTerms instance = OntologyTerms.getDefaultInstance();
		assertNotNull(instance);
	}
	
	@Test
	public void testCustomConstructor() {
		ListMultimap<String, String> ontology = ArrayListMultimap.create();
		ontology.put("key1", "value1");
		ontology.put("key1", "value2");
		ontology.put("key2", "value2");
		OntologyTerms ontologyTerms = new OntologyTerms(ontology);
		assertEquals(3, ontologyTerms.getOntology().size());
		assertEquals(2, ontologyTerms.getOntology().get("key1").size());
		assertEquals(1, ontologyTerms.getOntology().get("key2").size());
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
		strings.add("foo baz");
		
		Set <String> hyphTokable = OntologyTerms.getDefaultInstance().makeHyphTokable(strings);
		assertEquals(1, hyphTokable.size());
		assertTrue(hyphTokable.contains("foo baz"));
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
    
    @Test (expected = UnsupportedOperationException.class)
    public void testUnmodifiableCustomOntologyTerms() {
    	ListMultimap<String, String> ontology = ArrayListMultimap.create();
    	OntologyTerms ontologyTerms = new OntologyTerms(ontology);
    	ontologyTerms.getOntology().put("foo", "bar");
    }
    
    @Test (expected = UnsupportedOperationException.class)
    public void testUnmodifiableIdsForTerm() {
    	OntologyTerms ontologyTerms = OntologyTerms.getDefaultInstance();
    	ontologyTerms.getIdsForTerm("acid").clear();
    }
    
    @Test (expected = UnsupportedOperationException.class)
    public void testUnmodifiableHyphTokable() {
    	OntologyTerms ontologyTerms = OntologyTerms.getDefaultInstance();
    	ontologyTerms.getHyphTokable().clear();
    }
    
    @Test (expected = UnsupportedOperationException.class)
    public void testGetUnmodifiableGetAllTerms() {
    	OntologyTerms ontologyTerms = OntologyTerms.getDefaultInstance();
    	ontologyTerms.getAllTerms().clear();
    }
}
