package uk.ac.cam.ch.wwmm.oscar.adv;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.Oscar;
import uk.ac.cam.ch.wwmm.oscar.adv.AdvancedOscar;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictRegistry;

public class AdvancedOscarTest {

	@Test
	public void testConstructor() throws Exception {
		Oscar oscar = new AdvancedOscar(getClass().getClassLoader());
		Assert.assertNotNull(oscar);
	}
	
	@Test
	public void testGetChemNameDict() throws Exception {
		Oscar oscar = new AdvancedOscar(getClass().getClassLoader());
		Assert.assertNotNull(oscar.getDictionaryRegistry());
	}
	
	@Test
	public void testOscarLoadingOfDefaultDictionaries() throws Exception {
		Oscar oscar = new AdvancedOscar(this.getClass().getClassLoader());
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
