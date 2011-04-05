package uk.ac.cam.ch.wwmm.oscardata;

import static org.junit.Assert.*;

import java.util.List;

import nu.xom.Document;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.scixml.TextToSciXML;
import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

public class DataParserTest {

	@Test
	public void testHnmr() { 
		Document doc = TextToSciXML.textToSciXML("H NMR (d6-DMSO, 400 MHz): 9.25" +
				" (t, J=6.4, 1H), 9.16 (d, J=8.4, 1H), 8.56 (d, J=8.4, 1H), 7.86" +
				" (dd, J=8.4, 4.1, 1H), 7.41 (dd, J=8.4, 5.7, 2H), 7.16, t," +
				" J=8.8, 2H), 4.60 (d, 6.3, 2H), 4.00-3.70 (m, 2H), 3.65-3.45 (m, 2H)" +
				", 2.35-2.10 (m, 3H), 1.7 (m, 1H)");
		
		assertEquals(0, doc.query("//spectrum").size());
		DataParser.dataParse(doc);
		assertEquals(1, doc.query("//spectrum").size());
	}
	
	@Test
	public void testHnmrWithDataOnlyInExperimental() {
		Document doc = TextToSciXML.textToSciXML("H NMR (d6-DMSO, 400 MHz): 9.25" +
				" (t, J=6.4, 1H), 9.16 (d, J=8.4, 1H), 8.56 (d, J=8.4, 1H), 7.86" +
				" (dd, J=8.4, 4.1, 1H), 7.41 (dd, J=8.4, 5.7, 2H), 7.16, t," +
				" J=8.8, 2H), 4.60 (d, 6.3, 2H), 4.00-3.70 (m, 2H), 3.65-3.45 (m, 2H)" +
				", 2.35-2.10 (m, 3H), 1.7 (m, 1H)");
		
		assertEquals(0, doc.query("//spectrum").size());
		DataParser.dataParse(doc, true, XMLStrings.getDefaultInstance());
		assertEquals(0, doc.query("//spectrum").size());
	}
	
	@Test
	public void testHnmrWithDataNotOnlyInExperimental() {
		Document doc = TextToSciXML.textToSciXML("H NMR (d6-DMSO, 400 MHz): 9.25" +
				" (t, J=6.4, 1H), 9.16 (d, J=8.4, 1H), 8.56 (d, J=8.4, 1H), 7.86" +
				" (dd, J=8.4, 4.1, 1H), 7.41 (dd, J=8.4, 5.7, 2H), 7.16, t," +
				" J=8.8, 2H), 4.60 (d, 6.3, 2H), 4.00-3.70 (m, 2H), 3.65-3.45 (m, 2H)" +
				", 2.35-2.10 (m, 3H), 1.7 (m, 1H)");
		
		assertEquals(0, doc.query("//spectrum").size());
		DataParser.dataParse(doc, false, XMLStrings.getDefaultInstance());
		assertEquals(1, doc.query("//spectrum").size());
	}
	
	@Test
	public void testHnmrWithDelta() {
		Document doc = TextToSciXML.textToSciXML("1H-NMR (CDCl3) \u03B4 8.13 (d, 1H)," +
				" 7.59 (d, 1H), 7.50 (d, 2H), 7.30 (m, 2H), 6.97 (d, 2H), 6.68 (s, 1H)" +
				", 4.08 (t, 2H), 2.72 (s, 3H), 2.4 (m, 6H), 1.63 (m, 6H), 1.45 (m, 2H)" +
				", 2.35-2.10 (m, 3H), 1.7 (m, 1H)");
		
		assertEquals(0, doc.query("//spectrum").size());
		DataParser.dataParse(doc);
		assertEquals(1, doc.query("//spectrum").size());
	}
	
	@Test
	public void testCnmr() {
		Document doc = TextToSciXML.textToSciXML("13C NMR of 6 (CDCl3, 100 MHz):" +
				" 169.7,156.3,154.5,143.9,137.1,132.4, 128.0, 126.1, 124.2, 53.4.");
		
		assertEquals(0, doc.query("//spectrum").size());
		DataParser.dataParse(doc);
		assertEquals(1, doc.query("//spectrum").size());
	}
	
	@Test
	public void testMassSpec() {
		
		Document doc = TextToSciXML.textToSciXML("MS (ESI) m/z 413 (MH+)");

		assertEquals(0, doc.query("//spectrum").size());
		DataParser.dataParse(doc);
		assertEquals(1, doc.query("//spectrum").size());
	}
	
	@Test
	public void testFindData() {
		String source = "Lorem ipsum dolor sit amet, consectetur adipisicing" +
				" elit, sed 1H NMR (d6-DMSO, 400 MHz): 9.25 (t, J=6.4, 1H)," +
				" 9.16 (d, J=8.4, 1H) do eiusmod tempor incididunt ut labore" +
				" et dolore magna aliqua.";
		Tokeniser tokeniser = Tokeniser.getDefaultInstance();
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(tokeniser, source);
		
		List <DataAnnotation> annotations = DataParser.findData(procDoc);
		assertEquals(1, annotations.size());
		assertEquals("1H NMR (d6-DMSO, 400 MHz): 9.25 (t, J=6.4, 1H), 9.16 (d, J=8.4, 1H)", annotations.get(0).getSurface());
		assertEquals("<spectrum type=\"hnmr\">" +
						"1H NMR (" +
							"<quantity type=\"solvent\">" +
								"d6-DMSO" +
							"</quantity>" +
							", " +
							"<quantity type=\"frequency\">" +
								"<value>" +
									"<point>400</point>" +
								"</value>" +
								" " +
								"<units>MHz</units>" +
							"</quantity>" +
							"): " +
							"<peaks type=\"..\">" +
								"<peak>" +
									"<quantity type=\"shift\">" +
										"<value>" +
											"<point>9.25</point>" +
										"</value>" +
									"</quantity>" +
									" (" +
									"<quantity type=\"peaktype\">" +
										"t" +
									"</quantity>" +
									", J=" +
									"<quantity type=\"coupling\">" +
										"<value>" +
											"<point>6.4</point>" +
										"</value>" +
									"</quantity>" +
									", " +
									"<quantity type=\"integral\">" +
										"<value>" +
											"<point>1</point>" +
										"</value>" +
										"<units>H</units>" +
									"</quantity>" +
									")" +
								"</peak>" +
								", " +
								"<peak>" +
									"<quantity type=\"shift\">" +
										"<value>" +
											"<point>9.16</point>" +
										"</value>" +
									"</quantity>" +
									" (" +
									"<quantity type=\"peaktype\">" +
										"d" +
									"</quantity>" +
									", J=" +
									"<quantity type=\"coupling\">" +
										"<value>" +
											"<point>8.4</point>" +
										"</value>" +
									"</quantity>" +
									", " +
									"<quantity type=\"integral\">" +
										"<value>" +
											"<point>1</point>" +
										"</value>" +
										"<units>H</units>" +
									"</quantity>" +
									")" +
								"</peak>" +
							"</peaks>" +
						"</spectrum>"
							, annotations.get(0).getAnnotatedElement().toXML());
	}
	
	@Test
	public void testFindMultipleData() {
		String source = "Lorem ipsum dolor sit amet, consectetur adipisicing" +
				" elit, sed 1H NMR (d6-DMSO, 400 MHz): 9.25 (t, J=6.4, 1H)," +
				" 9.16 (d, J=8.4, 1H) do eiusmod tempor MS (ESI) m/z 413 (MH+)" +
				" incididunt ut labore et dolore magna aliqua.";
		Tokeniser tokeniser = Tokeniser.getDefaultInstance();
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(tokeniser, source);
		assertEquals(1, procDoc.getTokenSequences().size());
		
		List <DataAnnotation> annotations = DataParser.findData(procDoc);
		assertEquals(2, annotations.size());
		assertEquals("1H NMR (d6-DMSO, 400 MHz): 9.25 (t, J=6.4, 1H), 9.16 (d, J=8.4, 1H)", annotations.get(0).getSurface());
		assertEquals("m/z 413 (MH+)", annotations.get(1).getSurface());
		assertEquals("<spectrum type=\"massSpec\">" +
						"m/z " +
						"<peaks type=\"..\">" +
							"<peak>" +
								"<quantity type=\"mass\">" +
									"<value>" +
										"<point>413</point>" +
									"</value>" +
								"</quantity>" +
								" (" +
								"<quantity type=\"assignment\">" +
									"MH+" +
								"</quantity>" +
								")" +
							"</peak>" +
						"</peaks>" +
					"</spectrum>"
						, annotations.get(1).getAnnotatedElement().toXML());
		
	}
	
	@Test
	public void testOrderingOfMultipleDataAnnotations() {
		String source = "The title compound was synthesized from ethyl 3-cyclopropyl-4H-furo[3,2-b]pyrrole-5-carboxylate (110 mg, 0.50 mmol)" +
				" according to General Procedure 2 and was purified by flash chromatography (Isco CombiFlash, 0-60% EtOAc/heptane)" +
				" to afford 3-cyclopropyl-4H-furo[3,2-b]pyrrole-5-carboxylic acid 31 (34 mg, 35%). 1H NMR (400 MHz, CD3OD) \u03B4" +
				" ppm 0.67-0.72 (m, 2H), 0.86-0.92 (m, 2H), 1.75-1.84 (m, 1H), 6.64 (s, 1H), 7.34 (d, J=0.83 Hz, 1H); LCMS-MS (ESI\u2212) 189.8 (M\u2212H); HPLC (UV=95.9%), (ELSD=100%). ";
		Tokeniser tokeniser = Tokeniser.getDefaultInstance();
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(tokeniser, source);
		assertEquals(1, procDoc.getTokenSequences().size());
		
		List <DataAnnotation> annotations = DataParser.findData(procDoc);
		assertEquals(5, annotations.size());
		//this will also make sure the annotations are recognised in the correct order
		assertEquals("110 mg, 0.50 mmol", annotations.get(0).getSurface());
		assertEquals("(34 mg, 35%)", annotations.get(1).getSurface());
		assertEquals("1H NMR (400 MHz, CD3OD) \u03B4 ppm 0.67-0.72 (m, 2H), 0.86-0.92 (m, 2H), 1.75-1.84 (m, 1H), 6.64 (s, 1H), 7.34 (d, J=0.83 Hz, 1H)", annotations.get(2).getSurface());
		assertEquals("LCMS-MS (ESI\u2212) 189.8 (M\u2212H)", annotations.get(3).getSurface());
		assertEquals("UV=95.9", annotations.get(4).getSurface());
	}
	
	@Test
	public void testFindMultipleDataAcrossMultipleTokenSequences() {
		String source1 = "Lorem ipsum dolor sit amet, consectetur adipisicing" +
		" elit, sed 1H NMR (d6-DMSO, 400 MHz): 9.25 (t, J=6.4, 1H)," +
		" 9.16 (d, J=8.4, 1H) do eiusmod tempor incididunt ut labore" +
		" et dolore magna aliqua.";
		Tokeniser tokeniser = Tokeniser.getDefaultInstance();
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(tokeniser, source1);
		assertEquals(1, procDoc.getTokenSequences().size());
		assertEquals(1, DataParser.findData(procDoc).size());
		
		String source2 = "Lorem ipsum dolor sit amet, consectetur adipisicing" +
		" elit, sed MS (ESI) m/z 413 (MH+) do eiusmod tempor incididunt ut " +
		"labore et dolore magna aliqua.";
		TokenSequence tokenSequence = tokeniser.tokenise(source2, procDoc, 0, null);
		procDoc.addTokenSequence(tokenSequence);
		assertEquals(2, procDoc.getTokenSequences().size());
		assertEquals(2, DataParser.findData(procDoc).size());
	}
}
