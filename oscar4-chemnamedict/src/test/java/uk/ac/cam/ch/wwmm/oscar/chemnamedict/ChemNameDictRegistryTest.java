package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.net.URI;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class ChemNameDictRegistryTest {

	@Test
	public void testConstructor() {
		Assert.assertNotNull(ChemNameDictRegistry.getInstance());
	}

	@Test
	public void testRegister() throws Exception {
		ChemNameDictRegistry registry = ChemNameDictRegistry.getInstance();
		registry.clear();
		Assert.assertEquals(0, registry.listDictionaries().size());
		ChemNameDict dict = new ChemNameDict(new URI("http://www.example.org/"));
		registry.register(dict);
		Assert.assertEquals(1, registry.listDictionaries().size());
	}

	@Test
	public void testListDictionaries() throws Exception {
		ChemNameDictRegistry registry = ChemNameDictRegistry.getInstance();
		registry.clear();
		Assert.assertEquals(0, registry.listDictionaries().size());
		URI uri = new URI("http://www.example.org/");
		Assert.assertFalse(registry.listDictionaries().contains(uri));
		ChemNameDict dict = new ChemNameDict(uri);
		registry.register(dict);
		Assert.assertEquals(1, registry.listDictionaries().size());
		Assert.assertTrue(registry.listDictionaries().contains(uri));
	}

	@Test
	public void testGetDictionary() throws Exception {
		ChemNameDictRegistry registry = ChemNameDictRegistry.getInstance();
		registry.clear();
		URI uri = new URI("http://www.example.org/");
		ChemNameDict dict = new ChemNameDict(uri);
		registry.register(dict);
		Assert.assertEquals(dict, registry.getDictionary(uri));
	}

	@Test
	public void testHasName() throws Exception {
		ChemNameDictRegistry registry = ChemNameDictRegistry.getInstance();
		registry.clear();
		Assert.assertFalse(registry.hasName("propanol"));
		ChemNameDict dict = new ChemNameDict(new URI("http://www.example.org/"));
		registry.register(dict);
		dict.addName("propanol");
		Assert.assertTrue(registry.hasName("propanol"));
	}

	@Test
	public void testGetSMILES() throws Exception {
		ChemNameDictRegistry registry = ChemNameDictRegistry.getInstance();
		registry.clear();
		ChemNameDict dict = new ChemNameDict(new URI("http://www.example.org/1/"));
		dict.addChemical("propanol", "CCO", "not relevant");
		registry.register(dict);
		dict = new ChemNameDict(new URI("http://www.example.org/2/"));
		registry.register(dict);
		dict.addChemical("propanol", "C1.C1O", "not relevant");

		Assert.assertNotNull(registry.getSMILES("butanol"));
		Assert.assertEquals(0, registry.getSMILES("butanol").size());

		Set<String> smileses = registry.getSMILES("propanol");
		Assert.assertEquals(2, registry.getSMILES("propanol").size());
		Assert.assertTrue(smileses.contains("CCO"));
		Assert.assertTrue(smileses.contains("C1.C1O"));
	}

	@Test
	public void testGetShortestSMILES() throws Exception {
		ChemNameDictRegistry registry = ChemNameDictRegistry.getInstance();
		registry.clear();
		ChemNameDict dict = new ChemNameDict(new URI("http://www.example.org/1/"));
		dict.addChemical("propanol", "CCO", "not relevant");
		registry.register(dict);
		dict = new ChemNameDict(new URI("http://www.example.org/2/"));
		registry.register(dict);
		dict.addChemical("propanol", "C1.C1O", "not relevant");

		Assert.assertNull(registry.getShortestSMILES("butanol"));

		String smiles = registry.getShortestSMILES("propanol");
		Assert.assertEquals("CCO", smiles);
	}

	@Test
	public void testGetInChI() throws Exception {
		ChemNameDictRegistry registry = ChemNameDictRegistry.getInstance();
		registry.clear();
		ChemNameDict dict = new ChemNameDict(new URI("http://www.example.org/1/"));
		dict.addChemical("methane", "C", "InChI=1/CH4/h1H4");
		registry.register(dict);

		Assert.assertNotNull(registry.getInChI("butanol"));
		Assert.assertEquals(0, registry.getInChI("butanol").size());

		Set<String> inchis = registry.getInChI("methane");
		Assert.assertEquals(1, inchis.size());
		Assert.assertEquals("InChI=1/CH4/h1H4", inchis.iterator().next());
	}

	@Test
	public void testGetNames_InChI() throws Exception {
		ChemNameDictRegistry registry = ChemNameDictRegistry.getInstance();
		registry.clear();
		ChemNameDict dict = new ChemNameDict(new URI("http://www.example.org/1/"));
		dict.addChemical("methane", "C", "InChI=1/CH4/h1H4");
		dict.addChemical("methaan", "C", "InChI=1/CH4/h1H4");
		registry.register(dict);

		Set<String> names = registry.getNames("InChI=1/CH5/h1H5");
		Assert.assertNotNull(names);
		Assert.assertEquals(0, names.size());

		names = registry.getNames("InChI=1/CH4/h1H4");
		Assert.assertNotNull(names);
		Assert.assertEquals(2, names.size());
		Assert.assertTrue(names.contains("methaan"));
		Assert.assertTrue(names.contains("methane"));
	}
}
