package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ImmutableChemNameDict;
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
				"uk/ac/cam/ch/wwmm/oscar/core/testDict.xml");
		ImmutableChemNameDict dict = new ImmutableChemNameDict(
				new URI("http://www.example.org"), Locale.ENGLISH,in);
		assertEquals(3, dict.chemRecords.size());
		assertTrue(dict.hasName("pyridinium chlorochromate"));
		assertTrue(dict.hasName("PCC"));
		assertTrue(dict.hasName("MgSO4"));
		assertTrue(dict.hasName("Ac2O"));
		Set <String> smiles = dict.getAllSmiles("PCC");
		assertEquals(1, smiles.size());
		assertTrue(smiles.contains("[OH-].O.O.Cl.[Cr+].c1cccnc1"));
		Set <String> inchis = dict.getStdInchis("PCC");
		assertEquals(1, inchis.size());
		assertTrue(inchis.contains("InChI=1S/C5H5N.ClH.Cr.3O/c1-2-4-6-5-3-1;;;;;/h1-5H;1H;;;;/q;;+1;;;-1"));
	}
	
	@Test
	public void testGetSmiles() throws URISyntaxException, DataFormatException {
		InputStream in = ClassLoader.getSystemResourceAsStream(
				"uk/ac/cam/ch/wwmm/oscar/core/testDict.xml");
		ImmutableChemNameDict dict = new ImmutableChemNameDict(
				new URI("http://www.example.org"), Locale.ENGLISH,in);
		assertEquals(1, dict.getAllSmiles("PCC").size());
		assertEquals(0, dict.getAllSmiles("unrecognisedName").size());
	}
	
	@Test
	public void testGetStdInchi() throws URISyntaxException, DataFormatException {
		InputStream in = ClassLoader.getSystemResourceAsStream(
				"uk/ac/cam/ch/wwmm/oscar/core/testDict.xml");
		ImmutableChemNameDict dict = new ImmutableChemNameDict(
				new URI("http://www.example.org"), Locale.ENGLISH,in);
		assertEquals(1, dict.getStdInchis("PCC").size());
		assertEquals(0, dict.getStdInchis("unrecognisedName").size());
	}
}
