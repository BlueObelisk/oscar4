package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict;

public class PolymerDictionaryTest extends AbstractDictionaryTest {

	@Test
	public void testACompound() {
		IChemNameDict dict = new PolymerDictionary();
		assertNotNull(dict);
		assertTrue(dict.hasName("HPEI25K"));
	}
	
	@Test
	public void testAcccessViaRegistry() {
		IChemNameDict dict = new PolymerDictionary();
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		registry.register(dict);
		assertTrue(registry.hasName("HPEI25K"));
	}

	@Override
	public IChemNameDict getDictionary() {
		return new PolymerDictionary();
	}
	
}
