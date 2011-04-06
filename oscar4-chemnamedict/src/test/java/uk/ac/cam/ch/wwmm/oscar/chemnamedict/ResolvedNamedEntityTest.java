package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import static org.junit.Assert.*;

import java.util.List;


import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

public class ResolvedNamedEntityTest {

	private static NamedEntity chemicalEntity = new NamedEntity("methane", 0, 7, NamedEntityType.COMPOUND);

	@Test
	public void basicFunctionalityTest() {
		ResolvedNamedEntity rne = new ResolvedNamedEntity(chemicalEntity, ChemNameDictRegistry.getDefaultInstance());
		assertEquals(true, rne.getFirstInChI().contains("CH4"));//sanity check on InChI
		assertEquals(true, rne.getFirstSmiles().contains("C"));//sanity check on SMILES
		assertEquals("methane", rne.getNamedEntity().getSurface());
		assertEquals(true, rne.getNameResolutionResults().size()>0);
	}
	
	@Test
	public void resolutionResultsTest() {
		ResolvedNamedEntity rne = new ResolvedNamedEntity(chemicalEntity, ChemNameDictRegistry.getDefaultInstance());
		List<NameResolutionResult> results = rne.getNameResolutionResults();
		assertEquals(true, rne.getNameResolutionResults().size()>0);
		for (NameResolutionResult nameResolutionResult : results) {
			assertEquals("methane", nameResolutionResult.getName());
			assertNotNull(nameResolutionResult.getUri());
			assertNotNull(nameResolutionResult.getInchis());
			assertNotNull(nameResolutionResult.getSmileses());
		}
	}
}