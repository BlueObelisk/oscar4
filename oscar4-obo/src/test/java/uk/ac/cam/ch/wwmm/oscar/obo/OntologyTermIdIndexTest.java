package uk.ac.cam.ch.wwmm.oscar.obo;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class OntologyTermIdIndexTest {

	@Test
	public void testGetInstance() {
		OntologyTermIdIndex instance = OntologyTermIdIndex.getInstance();
		Assert.assertNotNull(instance);
	}

	@Test
	public void testContainsAcid() {
		OntologyTermIdIndex instance = OntologyTermIdIndex.getInstance();
		Assert.assertTrue(instance.containsTerm("acid"));
		List<String> identifiers = instance.getIdsForTerm("acid");
		Assert.assertNotSame(0, identifiers.size());
		Assert.assertTrue(
			"Missing ChEBI identifier: CHEBI:37527",
			identifiers.contains("CHEBI:37527 CHEBI:37527")
		);
		// TODO: why is the index duplicated??
	}

	@Test
	public void testGetAllTerms() {
		OntologyTermIdIndex instance = OntologyTermIdIndex.getInstance();
		Set<String> allTerms = instance.getAllTerms();
		Assert.assertNotSame(0, allTerms.size());
		Assert.assertTrue(allTerms.contains("acid"));
	}
}
