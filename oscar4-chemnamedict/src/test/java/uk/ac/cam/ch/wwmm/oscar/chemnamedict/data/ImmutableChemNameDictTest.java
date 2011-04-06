package uk.ac.cam.ch.wwmm.oscar.chemnamedict.data;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.ImmutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;

public class ImmutableChemNameDictTest {

	@Test
	public void testConstructor() throws Exception {
		assertNotNull(
			new ImmutableChemNameDict(
				new URI("http://example.com/"),
				Locale.ENGLISH
			)
		);
	}

	@Test
	public void testLoadConstructor() throws URISyntaxException, DataFormatException {
		InputStream in = ClassLoader.getSystemResourceAsStream(
				"uk/ac/cam/ch/wwmm/oscar/data/testDict.xml");
		ImmutableChemNameDict dict = new ImmutableChemNameDict(
				new URI("http://www.example.org"), Locale.ENGLISH,in);
		assertEquals(3, dict.chemRecords.size());
		assertTrue(dict.hasName("pyridinium chlorochromate"));
		assertTrue(dict.hasName("PCC"));
		assertTrue(dict.hasName("MgSO4"));
		assertTrue(dict.hasName("Ac2O"));
		Set <String> smiles = dict.getSMILES("PCC");
		assertEquals(1, smiles.size());
		assertTrue(smiles.contains("[OH-].O.O.Cl.[Cr+].c1cccnc1"));
		Set <String> inchis = dict.getInChI("PCC");
		assertEquals(1, inchis.size());
		assertTrue(inchis.contains("InChI=1/C5H5N.ClH.Cr.3O/c1-2-4-6-5-3-1;;;;;/h1-5H;1H;;;;/q;;+1;;;-1"));
	}
	
	@Test
	public void testGetSmiles() throws URISyntaxException, DataFormatException {
		InputStream in = ClassLoader.getSystemResourceAsStream(
				"uk/ac/cam/ch/wwmm/oscar/data/testDict.xml");
		ImmutableChemNameDict dict = new ImmutableChemNameDict(
				new URI("http://www.example.org"), Locale.ENGLISH,in);
		assertEquals(1, dict.getSMILES("PCC").size());
		assertEquals(0, dict.getSMILES("unrecognisedName").size());
	}
	
	@Test
	public void testGetInchi() throws URISyntaxException, DataFormatException {
		InputStream in = ClassLoader.getSystemResourceAsStream(
				"uk/ac/cam/ch/wwmm/oscar/data/testDict.xml");
		ImmutableChemNameDict dict = new ImmutableChemNameDict(
				new URI("http://www.example.org"), Locale.ENGLISH,in);
		assertEquals(1, dict.getInChI("PCC").size());
		assertEquals(0, dict.getInChI("unrecognisedName").size());
	}
}
