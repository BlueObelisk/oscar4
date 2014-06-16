package uk.ac.cam.ch.wwmm.oscar.opsin;

import static org.junit.Assert.*;

import java.util.Locale;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.XPathContext;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries.ChEBIDictionary;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ChemicalStructure;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.FormatType;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ResolvedNamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

/**
 * Tests for the use of the OpsinDictionary for the resolution
 * of named entities 
 * 
 * @author dmj30
 *
 */
public class OpsinResolutionTest {

	@Test
	public void testOnlyOpsin() throws Exception {
		//TODO improve test so that it will pass on platforms that don't have jni-inchi
//		JniInchiWrapper.loadLibrary();
		
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		registry.register(new OpsinDictionary());
		NamedEntity ne = new NamedEntity("methane", 0, 0, NamedEntityType.COMPOUND);
		ResolvedNamedEntity rne = registry.resolveNamedEntity(ne);

		assertEquals(5, rne.getChemicalStructures().size());
		assertEquals("C", rne.getFirstChemicalStructure(FormatType.SMILES).getValue());
		assertEquals("InChI=1S/CH4/h1H4", rne.getFirstChemicalStructure(FormatType.STD_INCHI).getValue());
		assertEquals("VNWKTOKETHGBQD-UHFFFAOYSA-N", rne.getFirstChemicalStructure(FormatType.STD_INCHI_KEY).getValue());
		Element cmlElement = new Builder().build(
				IOUtils.toInputStream(rne.getFirstChemicalStructure(FormatType.CML).getValue(), "UTF-8")).getRootElement();
		XPathContext xpc = new XPathContext("cml", "http://www.xml-cml.org/schema");
		assertEquals(5, cmlElement.query("//cml:atom", xpc).size());
		assertEquals(1, cmlElement.query("//cml:atom[@elementType='C']", xpc).size());
		assertEquals(4, cmlElement.query("//cml:atom[@elementType='H']", xpc).size());
	}
	
	@Test
	public void testOpsinAndChebi() {
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		registry.register(new OpsinDictionary());
		registry.register(ChEBIDictionary.getInstance());
		NamedEntity ne = new NamedEntity("methane", 0, 0, NamedEntityType.COMPOUND);
		ResolvedNamedEntity rne = registry.resolveNamedEntity(ne);
		
		assertEquals(8, rne.getChemicalStructures().size());
		assertEquals(2, rne.getChemicalStructures(FormatType.SMILES).size());
		for (ChemicalStructure structure : rne.getChemicalStructures(FormatType.SMILES)) {
			assertTrue(structure.getValue().equals("C") || structure.getValue().equals("[H]C([H])([H])[H]"));
		}
		assertEquals(2, rne.getChemicalStructures(FormatType.STD_INCHI).size());
		for (ChemicalStructure structure : rne.getChemicalStructures(FormatType.STD_INCHI)) {
			assertTrue(structure.getValue().equals("InChI=1S/CH4/h1H4"));
		}
		for (ChemicalStructure structure : rne.getChemicalStructures(FormatType.STD_INCHI_KEY)) {
			assertTrue(structure.getValue().equals("VNWKTOKETHGBQD-UHFFFAOYSA-N"));
		}
		assertEquals(1, rne.getChemicalStructures(FormatType.CML).size());
	}
	
}
