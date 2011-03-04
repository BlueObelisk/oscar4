package uk.ac.cam.ch.wwmm.oscardata;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

public class DataAnnotationTest {

	@Test
	public void testConstructor() {
		DataAnnotation annotation = new DataAnnotation(12, 42, "foo");
		assertEquals(12, annotation.getStart());
		assertEquals(42, annotation.getEnd());
		assertEquals("foo", annotation.getSurface());
		assertTrue(NamedEntityType.DATA == annotation.getType());
	}
	
	@Test
	public void testMultipleAnnotations() {
		String sentence = "The title compound was synthesized from ethyl 3-cyclopropyl-4H-furo[3,2-b]pyrrole-5-carboxylate (110 mg, 0.50 mmol) according to General Procedure 2 and was purified by flash chromatography (Isco CombiFlash, 0-60% EtOAc/heptane) to afford 3-cyclopropyl-4H-furo[3,2-b]pyrrole-5-carboxylic acid 31 (34 mg, 35%). 1H NMR (400 MHz, CD3OD) \u03B4 ppm 0.67-0.72 (m, 2H), 0.86-0.92 (m, 2H), 1.75-1.84 (m, 1H), 6.64 (s, 1H), 7.34 (d, J=0.83 Hz, 1H); LCMS-MS (ESI\u2212) 189.8 (M\u2212H); HPLC (UV=95.9%), (ELSD=100%). ";

		Tokeniser tokeniser = Tokeniser.getDefaultInstance();
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance()
				.makeTokenisedDocument(tokeniser, sentence);

		List<DataAnnotation> annotations = DataParser.findData(procDoc);

		for (DataAnnotation dataAnnotation : annotations) {
			Assert.assertEquals("Annotations ", dataAnnotation.getSurface(),
					sentence.substring(dataAnnotation.getStart(),
							dataAnnotation.getEnd()));
		}
	}
}
