package uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

public class ResolvedNamedEntityTest {

	@Test
	public void testConstructor() {
		NamedEntity ne = new NamedEntity(null, 0, 0, null);
		List<ChemicalStructure> res = new ArrayList<ChemicalStructure>();
		ResolvedNamedEntity rne = new ResolvedNamedEntity(ne, res);
		assertNotNull(rne.getNamedEntity());
		assertNotNull(rne.getChemicalStructures());
	}

	
	@Test
	public void testGetNamedEntity() {
		NamedEntity ne = new NamedEntity(null, 0, 0, null);
		List<ChemicalStructure> res = new ArrayList<ChemicalStructure>();
		ResolvedNamedEntity rne = new ResolvedNamedEntity(ne, res);
		assertTrue(rne.getNamedEntity() == ne);
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testImmutableChemicalStructures() {
		NamedEntity ne = new NamedEntity(null, 0, 0, null);
		List<ChemicalStructure> res = new ArrayList<ChemicalStructure>();
		ResolvedNamedEntity rne = new ResolvedNamedEntity(ne, res);
		rne.getChemicalStructures().clear();
	}
	
	@Test
	public void testGetChemicalStructures() throws URISyntaxException {
		NamedEntity ne = new NamedEntity("propane", 0, 0, NamedEntityType.COMPOUND);
		List<ChemicalStructure> res = new ArrayList<ChemicalStructure>();
		res.add(new ChemicalStructure("CCC", FormatType.SMILES,
				new URI("http://www.example.org/dictionary")));
		res.add(new ChemicalStructure("InChI=1S/C3H8/c1-3-2/h3H2,1-2H3", FormatType.STD_INCHI,
				new URI("http://www.example.org/dictionary")));
		
		ResolvedNamedEntity rne = new ResolvedNamedEntity(ne, res);
		assertEquals(2, rne.getChemicalStructures().size());
		assertTrue(rne.getChemicalStructures().containsAll(res));
	}
	
	
	@Test
	public void testGetTypedChemicalStructures() throws URISyntaxException {
		NamedEntity ne = new NamedEntity(null, 0, 0, null);
		List<ChemicalStructure> res = new ArrayList<ChemicalStructure>();
		res.add(new ChemicalStructure("CCC", FormatType.SMILES,
				new URI("http://www.example.org/dictionary")));
		res.add(new ChemicalStructure("InChI=1S/C3H8/c1-3-2/h3H2,1-2H3", FormatType.STD_INCHI,
				new URI("http://www.example.org/dictionary")));
		res.add(new ChemicalStructure("C(C)C", FormatType.SMILES,
				new URI("http://www.example.org/dictionary")));
		ResolvedNamedEntity rne = new ResolvedNamedEntity(ne, res);
		assertEquals(2, rne.getChemicalStructures(FormatType.SMILES).size());
		assertEquals(1, rne.getChemicalStructures(FormatType.STD_INCHI).size());
	}
	
	
	@Test
	public void testGetFirstChemicalStructure() throws URISyntaxException {
		NamedEntity ne = new NamedEntity(null, 0, 0, null);
		List<ChemicalStructure> res = new ArrayList<ChemicalStructure>();
		res.add(new ChemicalStructure("CCC", FormatType.SMILES,
				new URI("http://www.example.org/dictionary")));
		res.add(new ChemicalStructure("InChI=1S/C3H8/c1-3-2/h3H2,1-2H3", FormatType.STD_INCHI,
				new URI("http://www.example.org/dictionary")));
		res.add(new ChemicalStructure("C(C)C", FormatType.SMILES,
				new URI("http://www.example.org/dictionary")));
		ResolvedNamedEntity rne = new ResolvedNamedEntity(ne, res);
		assertEquals("CCC", rne.getFirstChemicalStructure(FormatType.SMILES).getValue());
		assertTrue(FormatType.SMILES == rne.getFirstChemicalStructure(FormatType.SMILES).getType());
		assertEquals("InChI=1S/C3H8/c1-3-2/h3H2,1-2H3", rne.getFirstChemicalStructure(FormatType.STD_INCHI).getValue());
		assertTrue(FormatType.STD_INCHI == rne.getFirstChemicalStructure(FormatType.STD_INCHI).getType());
		assertNull(rne.getFirstChemicalStructure(FormatType.CML));
	}
	
}
