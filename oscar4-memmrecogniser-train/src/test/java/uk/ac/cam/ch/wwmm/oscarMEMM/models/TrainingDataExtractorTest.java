package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;

import org.apache.commons.io.IOUtils;
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
	
	
	@Test
	public void testCollectFromDocumentList() throws Exception {
		List<Document> docs = new ArrayList<Document>();
		InputStream in1 = ClassLoader.getSystemResourceAsStream(
				"uk/ac/cam/ch/wwmm/oscarMEMM/models/scrapbook1.xml");
		InputStream in2 = ClassLoader.getSystemResourceAsStream(
				"uk/ac/cam/ch/wwmm/oscarMEMM/models/scrapbook2.xml");
		InputStream in3 = ClassLoader.getSystemResourceAsStream(
				"uk/ac/cam/ch/wwmm/oscarMEMM/models/scrapbook3.xml");
		
		try {
			docs.add(new Builder().build(in1));
			docs.add(new Builder().build(in2));
			docs.add(new Builder().build(in3));
		}
		finally {
			IOUtils.closeQuietly(in1);
			IOUtils.closeQuietly(in2);
			IOUtils.closeQuietly(in3);
		}
		
		TrainingDataExtractor extractor = new TrainingDataExtractor(docs);
		
		assertEquals(1, extractor.afterHyphen.size());
		assertTrue(extractor.afterHyphen.contains("based"));
		
		assertEquals(1, extractor.chemicalNonWords.size());
		assertTrue(extractor.chemicalNonWords.contains("H2SO4"));
		
		assertEquals(8, extractor.chemicalWords.size());
		assertTrue(extractor.chemicalWords.contains("silicon"));
		assertTrue(extractor.chemicalWords.contains("ethyl"));
		assertTrue(extractor.chemicalWords.contains("acetate"));
		assertTrue(extractor.chemicalWords.contains("hydrolysis"));
		assertTrue(extractor.chemicalWords.contains("aqueous"));
		assertTrue(extractor.chemicalWords.contains("chloride"));
		assertTrue(extractor.chemicalWords.contains("ethanolic"));
		assertTrue(extractor.chemicalWords.contains("carbon"));
		
		assertEquals(1, extractor.nonChemicalNonWords.size());
		assertTrue(extractor.nonChemicalNonWords.contains("C3PO"));
		
		assertEquals(8, extractor.nonChemicalWords.size());
		assertTrue(extractor.nonChemicalWords.contains("based"));
		assertTrue(extractor.nonChemicalWords.contains("life"));
		assertTrue(extractor.nonChemicalWords.contains("3-fold"));
		assertTrue(extractor.nonChemicalWords.contains("the"));
		assertTrue(extractor.nonChemicalWords.contains("quick"));
		assertTrue(extractor.nonChemicalWords.contains("brown"));
		assertTrue(extractor.nonChemicalWords.contains("fox"));
		assertTrue(extractor.nonChemicalWords.contains("solutions"));
		
		assertEquals(1, extractor.notForPrefix.size());
		assertTrue(extractor.notForPrefix.contains("fold"));
		
		assertEquals(1, extractor.polysemous.size());
		assertTrue(extractor.polysemous.contains("lead"));
		
		assertEquals(1, extractor.rnEnd.size());
		assertTrue(extractor.rnEnd.contains("reaction"));
		assertEquals(0, extractor.rnMid.size());
	}
}
