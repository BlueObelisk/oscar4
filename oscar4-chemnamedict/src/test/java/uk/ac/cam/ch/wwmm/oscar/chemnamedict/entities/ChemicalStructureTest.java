package uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class ChemicalStructureTest {

	@Test
	public void testConstructor() throws URISyntaxException {
		String value = "CCC";
		FormatType type = FormatType.SMILES;
		URI source = new URI("http://www.example.org/dictionary");
		ChemicalStructure entity = new ChemicalStructure(value, type, source);
		assertEquals(value, entity.getValue());
		assertTrue(type == entity.getType());
		assertTrue(source == entity.getSource());
	}
	
}
