package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import static org.junit.Assert.*;

import java.io.InputStream;

import nu.xom.Builder;
import nu.xom.Document;

import org.junit.Test;

public class TrainingDataExtractorTest {

	@Test
	public void testCollectFromDoc() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream(
				"uk/ac/cam/ch/wwmm/oscarMEMM/models/scrapbook1.xml");
		Document doc = new Builder().build(in);
		
		TrainingDataExtractor extractor = new TrainingDataExtractor(doc);
		
		assertEquals(1, extractor.afterHyphen.size());
		assertTrue(extractor.afterHyphen.contains("based"));
		
		assertEquals(1, extractor.chemicalNonWords.size());
		assertTrue(extractor.chemicalNonWords.contains("H2SO4"));
		
		assertEquals(5, extractor.chemicalWords.size());
		assertTrue(extractor.chemicalWords.contains("silicon"));
		assertTrue(extractor.chemicalWords.contains("ethyl"));
		assertTrue(extractor.chemicalWords.contains("acetate"));
		assertTrue(extractor.chemicalWords.contains("hydrolysis"));
		assertTrue(extractor.chemicalWords.contains("aqueous"));
		
		
		assertEquals(1, extractor.nonChemicalNonWords.size());
		assertTrue(extractor.nonChemicalNonWords.contains("C3PO"));
		
		assertEquals(3, extractor.nonChemicalWords.size());
		assertTrue(extractor.nonChemicalWords.contains("based"));
		assertTrue(extractor.nonChemicalWords.contains("life"));
		assertTrue(extractor.nonChemicalWords.contains("3-fold"));
		
		assertEquals(1, extractor.notForPrefix.size());
		assertTrue(extractor.notForPrefix.contains("fold"));
		
		// no tests for pnStop, the whole logic seems suspect

		assertEquals(1, extractor.polysemous.size());
		assertTrue(extractor.polysemous.contains("lead"));
		
		assertEquals(1, extractor.rnEnd.size());
		assertTrue(extractor.rnEnd.contains("reaction"));
		assertEquals(0, extractor.rnMid.size());
	}
}
