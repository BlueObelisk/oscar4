package uk.ac.cam.ch.wwmm.oscar;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;

public class OscarTest {

	@Test
	public void testConstructor() throws Exception {
		Oscar oscar = new Oscar();
		Assert.assertNotNull(oscar);
	}
	
	@Test
	public void testGetChemNameDict() throws Exception {
		Oscar oscar = new Oscar();
		Assert.assertNotNull(oscar.getDictionaryRegistry());
	}
	
	@Test
	public void testOscarLoadingOfDefaultDictionaries() throws Exception {
		Oscar oscar = new Oscar();
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

	@Test
	public void testGetNamedEntities() throws Exception {
		Oscar oscar = new Oscar();
		List<NamedEntity> entities =
			oscar.getNamedEntities("Then we mix benzene with toluene.");
		Assert.assertNotNull(entities);
		Assert.assertNotSame(0, entities.size());
	}

	@Test
	public void testGetResolvedEntities() throws Exception {
		Oscar oscar = new Oscar();
		Map<NamedEntity,String> entities =
			oscar.getResolvedEntities("Then we mix benzene with toluene.");
		Assert.assertNotNull(entities);
		Assert.assertNotSame(0, entities.size());
	}
}
