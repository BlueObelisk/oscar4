package uk.ac.cam.ch.wwmm.oscar.obo;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

public class OBOOntologyTest {

	@Test
	public void testGetInstance() {
		OBOOntology obo = OBOOntology.getInstance(false);
		assertNotNull(obo);
		assertNotSame(0, obo.getTerms().size());
	}

	@Test
	public void testChebiLoads() throws Exception {
		OBOOntology obo = new OBOOntology();
		assertEquals(0, obo.terms.size());
		
		obo.read("chebi.obo");
		assertTrue(obo.terms.containsKey("CHEBI:35729"));
		assertTrue(obo.terms.containsKey("CHEBI:15365"));
	}
	
	
	@Test
	public void testRead() throws Exception {
		OBOOntology ont = new OBOOntology();
		assertEquals(0, ont.terms.size());
		
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/obo/testOntology.obo");
		BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		ont.read(br);
		assertEquals(4, ont.terms.size());
		assertTrue(ont.terms.containsKey("CHEBI:24431"));
		assertTrue(ont.terms.containsKey("CHEBI:23367"));
		assertTrue(ont.terms.containsKey("CHEBI:24870"));
		assertTrue(ont.terms.containsKey("CHEBI:53439"));
	}
	
	
}
