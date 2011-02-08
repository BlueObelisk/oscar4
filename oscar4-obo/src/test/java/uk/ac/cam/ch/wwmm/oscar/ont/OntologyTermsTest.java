package uk.ac.cam.ch.wwmm.oscar.ont;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Sam Adams
 * @author dmj30
 */
public class OntologyTermsTest {

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
