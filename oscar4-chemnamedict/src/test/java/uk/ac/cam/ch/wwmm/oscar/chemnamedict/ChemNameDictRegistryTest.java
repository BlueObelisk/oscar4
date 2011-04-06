package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import nu.xom.Element;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.DefaultDictionary;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.MutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ChemicalStructure;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.FormatType;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ResolvedNamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

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
	
	
	@Test
	public void testResolveNamedEntity() throws URISyntaxException {
		URI uri1 = new URI("http://www.example.org/dictionary");
		MutableChemNameDict dict1 = new MutableChemNameDict(uri1, Locale.ENGLISH);
		dict1.addChemical("propane", "CCC", "InChI=1S/C3H8/c1-3-2/h3H2,1-2H3");
		dict1.addChemical("methane", "C", "InChI=1S/CH4/h1H4");
		
		URI uri2 = new URI("http://www.example.com/dictionary");
		MutableChemNameDict dict2 = new MutableChemNameDict(uri2, Locale.ENGLISH);
		dict2.addChemical("propane", "C(C)C", null);
		dict2.addChemical("methane", "C", "InChI=1S/CH4/h1H4");
		
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		registry.register(dict1);
		registry.register(dict2);
		
		NamedEntity propane = new NamedEntity("propane", 0, 0, NamedEntityType.COMPOUND);
		uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ResolvedNamedEntity resolvedPropane = registry.resolveNamedEntity(propane);
		assertTrue(propane == resolvedPropane.getNamedEntity());
		assertEquals(3, resolvedPropane.getChemicalStructures().size());

		assertEquals(2, resolvedPropane.getChemicalStructures(FormatType.SMILES).size());
		assertTrue(structuresListContains(resolvedPropane.getChemicalStructures(), "CCC", FormatType.SMILES, uri1));
		assertTrue(structuresListContains(resolvedPropane.getChemicalStructures(), "C(C)C", FormatType.SMILES, uri2));
		
		assertEquals(1, resolvedPropane.getChemicalStructures(FormatType.INCHI).size());
		assertTrue(structuresListContains(resolvedPropane.getChemicalStructures(), "InChI=1S/C3H8/c1-3-2/h3H2,1-2H3", FormatType.INCHI, uri1));
		
		
		NamedEntity methane = new NamedEntity("methane", 0, 0, NamedEntityType.COMPOUND);
		uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ResolvedNamedEntity resolvedMethane = registry.resolveNamedEntity(methane);
		assertTrue(methane == resolvedMethane.getNamedEntity());
		assertEquals(4, resolvedMethane.getChemicalStructures().size());
		
		assertEquals(2, resolvedMethane.getChemicalStructures(FormatType.SMILES).size());
		assertTrue(structuresListContains(resolvedMethane.getChemicalStructures(), "C", FormatType.SMILES, uri1));
		assertTrue(structuresListContains(resolvedMethane.getChemicalStructures(), "C", FormatType.SMILES, uri2));
		
		assertEquals(2, resolvedMethane.getChemicalStructures(FormatType.INCHI).size());
		assertTrue(structuresListContains(resolvedMethane.getChemicalStructures(), "InChI=1S/CH4/h1H4", FormatType.INCHI, uri1));
		assertTrue(structuresListContains(resolvedMethane.getChemicalStructures(), "InChI=1S/CH4/h1H4", FormatType.INCHI, uri2));
	}

	/**
	 * Checks if the given list contains a record with the given value, format
	 * and source
	 * @param list 
	 */
	private boolean structuresListContains(List<ChemicalStructure> list, String value,
			FormatType type, URI source) {
		
		for (ChemicalStructure structure : list) {
			if (structure.getValue().equals(value)) {
				if (structure.getType() == type) {
					if (structure.getSource() == source) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Test
	public void testResolveNamedEntityToCml() throws URISyntaxException {
		CmlProvider cmlDict = new CmlProvider(new URI("http://www.example.org/dictionary"), Locale.ENGLISH);
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		registry.register(cmlDict);
		NamedEntity ne = new NamedEntity("foo", 0, 0, NamedEntityType.COMPOUND);
		ResolvedNamedEntity rne = registry.resolveNamedEntity(ne);
		assertEquals(1, rne.getChemicalStructures().size());
		assertTrue(FormatType.CML == rne.getChemicalStructures().get(0).getType());
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo/>",
				rne.getChemicalStructures().get(0).getValue().replaceAll("\\r", "").replaceAll("\\n", ""));
	}
	
	class CmlProvider extends MutableChemNameDict implements ICMLProvider {

		public CmlProvider(URI uri, Locale language) {
			super(uri, language);
		}

		public Set<Element> getCML(String queryName) {
			Element elem = new Element("foo");
			Set <Element> set = new HashSet<Element>();
			set.add(elem);
			return set;
		}
	}
}
