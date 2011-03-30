package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.DefaultDictionary;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.MutableChemNameDict;

public class ChemNameDictRegistryTest {

	@Test
	public void testConstructor() {
		ChemNameDictRegistry registry = new ChemNameDictRegistry();
		assertEquals(Locale.ENGLISH, registry.getLanguage());
		assertEquals(2, registry.listDictionaries().size());
	}
	
	@Test
	public void testOtherConstructor() {
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.CHINESE);
		assertEquals(Locale.CHINESE, registry.getLanguage());
		assertEquals(0, registry.listDictionaries().size());
	}

	@Test
	public void testRegister() throws Exception {
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		assertEquals(0, registry.listDictionaries().size());
		IMutableChemNameDict dict = new MutableChemNameDict(
			new URI("http://www.example.org/"),
			Locale.ENGLISH
		);
		registry.register(dict);
		assertEquals(1, registry.listDictionaries().size());
	}

	@Test
	public void testListDictionaries() throws Exception {
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		assertEquals(0, registry.listDictionaries().size());
		URI uri = new URI("http://www.example.org/");
		assertFalse(registry.listDictionaries().contains(uri));
		IMutableChemNameDict dict = new MutableChemNameDict(uri, Locale.ENGLISH);
		registry.register(dict);
		assertEquals(1, registry.listDictionaries().size());
		assertTrue(registry.listDictionaries().contains(uri));
	}

	@Test
	public void testGetDictionary() throws Exception {
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		URI uri = new URI("http://www.example.org/");
		IMutableChemNameDict dict = new MutableChemNameDict(uri, Locale.ENGLISH);
		registry.register(dict);
		assertEquals(dict, registry.getDictionary(uri));
	}

	@Test
	public void testHasName() throws Exception {
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		assertFalse(registry.hasName("propanol"));
		IMutableChemNameDict dict = new MutableChemNameDict(
			new URI("http://www.example.org/"),
			Locale.ENGLISH
		);
		registry.register(dict);
		dict.addName("propanol");
		assertTrue(registry.hasName("propanol"));
	}

	@Test
	public void testGetSMILES() throws Exception {
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		IMutableChemNameDict dict = new MutableChemNameDict(
			new URI("http://www.example.org/1/"),
			Locale.ENGLISH
		);
		dict.addChemical("propanol", "CCO", "not relevant");
		registry.register(dict);
	    dict = new MutableChemNameDict(
	    	new URI("http://www.example.org/2/"),
	    	Locale.ENGLISH
	    );
		registry.register(dict);
		dict.addChemical("propanol", "C1.C1O", "not relevant");

		assertNotNull(registry.getSMILES("butanol"));
		assertEquals(0, registry.getSMILES("butanol").size());

		Set<String> smileses = registry.getSMILES("propanol");
		assertEquals(2, registry.getSMILES("propanol").size());
		assertTrue(smileses.contains("CCO"));
		assertTrue(smileses.contains("C1.C1O"));
	}

	@Test
	public void testGetShortestSMILES() throws Exception {
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		IMutableChemNameDict dict = new MutableChemNameDict(
			new URI("http://www.example.org/1/"),
			Locale.ENGLISH
		);
		dict.addChemical("propanol", "CCO", "not relevant");
		registry.register(dict);
		dict = new MutableChemNameDict(
			new URI("http://www.example.org/2/"),
			Locale.ENGLISH
		);
		registry.register(dict);
		dict.addChemical("propanol", "C1.C1O", "not relevant");

		assertNull(registry.getShortestSMILES("butanol"));

		String smiles = registry.getShortestSMILES("propanol");
		assertEquals("CCO", smiles);
	}

	@Test
	public void testGetInChI() throws Exception {
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		IMutableChemNameDict dict = new MutableChemNameDict(
			new URI("http://www.example.org/1/"),
			Locale.ENGLISH
		);
		dict.addChemical("methane", "C", "InChI=1/CH4/h1H4");
		registry.register(dict);

		assertNotNull(registry.getInChI("butanol"));
		assertEquals(0, registry.getInChI("butanol").size());

		Set<String> inchis = registry.getInChI("methane");
		assertEquals(1, inchis.size());
		assertEquals("InChI=1/CH4/h1H4", inchis.iterator().next());
	}

	@Test
	public void testGetNames_InChI() throws Exception {
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		IMutableChemNameDict dict = new MutableChemNameDict(
			new URI("http://www.example.org/1/"),
			Locale.ENGLISH
		);
		dict.addChemical("methane", "C", "InChI=1/CH4/h1H4");
		dict.addChemical("methaan", "C", "InChI=1/CH4/h1H4");
		registry.register(dict);

		Set<String> names = registry.getNames("InChI=1/CH5/h1H5");
		assertNotNull(names);
		assertEquals(0, names.size());

		names = registry.getNames("InChI=1/CH4/h1H4");
		assertNotNull(names);
		assertEquals(2, names.size());
		assertTrue(names.contains("methaan"));
		assertTrue(names.contains("methane"));
	}
	
	@Test
	public void testGetDefaultInstance() {
		ChemNameDictRegistry registry = ChemNameDictRegistry.getDefaultInstance();
		assertEquals(2, registry.listDictionaries().size());
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testDefaultInstanceIsImmutable() {
		ChemNameDictRegistry.getDefaultInstance().clear();
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testDefaultInstanceIsImmutable2() {
		ChemNameDictRegistry.getDefaultInstance().register(new DefaultDictionary());
	}
	
}
