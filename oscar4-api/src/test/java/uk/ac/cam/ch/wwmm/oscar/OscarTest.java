package uk.ac.cam.ch.wwmm.oscar;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictRegistry;

public class OscarTest {

	@Test
	public void testConstructore() {
		Oscar oscar = new Oscar(getClass().getClassLoader());
		Assert.assertNotNull(oscar);
	}
	
	@Test
	public void testGetChemNameDict() {
		Oscar oscar = new Oscar(getClass().getClassLoader());
		Assert.assertNotNull(oscar.getDictionaryRegistry());
	}
	
	@Test
	public void testOscarLoadingOfDefaultDictionaries() throws Exception {
		Oscar oscar = new Oscar(this.getClass().getClassLoader());
		oscar.loadDefaultDictionaries();
		ChemNameDictRegistry registry = oscar.getDictionaryRegistry();
		// test loading of the ChEBI dictionary
		Assert.assertEquals(
			"InChI=1/CH4/h1H4",
			registry.getInChI("methane").iterator().next()
		);
		// test loading of the default dictionary
		Assert.assertEquals(
			"InChI=1/C4H6O3/c1-3(5)7-4(2)6/h1-2H3",
			registry.getInChI("Ac2O").iterator().next()
		);
	}
}
