package uk.ac.cam.ch.wwmm.oscar.ont;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.exceptions.ResourceInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.ont.OntologyTerms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author Sam Adams
 * @author dmj30
 */
public class OntologyTermsTest {

    @Test
    public void testOntologyNotNull() throws ResourceInitialisationException {
        OntologyTerms ontologyTerms = OntologyTerms.getDefaultInstance();
        assertNotNull(ontologyTerms.getOntology());
    }

    @Test
    public void testOntologyNotEmpty() throws ResourceInitialisationException {
        OntologyTerms ontologyTerms = OntologyTerms.getDefaultInstance();
        assertFalse(ontologyTerms.getOntology().isEmpty());
    }

    
    @Test (expected = UnsupportedOperationException.class)
    public void testUnmodifiable() throws ResourceInitialisationException {
    	OntologyTerms ontologyTerms = OntologyTerms.getDefaultInstance();
    	ontologyTerms.getOntology().put("foo", "bar");
    }
}
