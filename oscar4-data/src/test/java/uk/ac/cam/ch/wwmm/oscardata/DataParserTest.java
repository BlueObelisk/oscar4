package uk.ac.cam.ch.wwmm.oscardata;

import static org.junit.Assert.*;
import nu.xom.Document;

import org.junit.Before;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.scixml.TextToSciXML;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;

public class DataParserTest {

	@Before
	public void setDataOnlyInExperimental() {
		OscarProperties.setProperty("dataOnlyInExperimental", "no");
	}
	
	@Test
	public void testHnmr() throws Exception { 
		Document doc = TextToSciXML.textToSciXML("H NMR (d6-DMSO, 400 MHz): 9.25" +
				" (t, J=6.4, 1H), 9.16 (d, J=8.4, 1H), 8.56 (d, J=8.4, 1H), 7.86" +
				" (dd, J=8.4, 4.1, 1H), 7.41 (dd, J=8.4, 5.7, 2H), 7.16, t," +
				" J=8.8, 2H), 4.60 (d, 6.3, 2H), 4.00-3.70 (m, 2H), 3.65-3.45 (m, 2H)" +
				", 2.35-2.10 (m, 3H), 1.7 (m, 1H)");
		
		assertEquals(0, doc.query("//spectrum").size());
		long start = System.currentTimeMillis();
		DataParser.dataParse(doc);
		long stop = System.currentTimeMillis();
		System.out.println("took " + (stop-start) + " ms");
		assertEquals(1, doc.query("//spectrum").size());
		
	}
	
	@Test
	public void testHnmrWithDelta() throws Exception {
		Document doc = TextToSciXML.textToSciXML("1H-NMR (CDCl3) δ 8.13 (d, 1H)," +
				" 7.59 (d, 1H), 7.50 (d, 2H), 7.30 (m, 2H), 6.97 (d, 2H), 6.68 (s, 1H)" +
				", 4.08 (t, 2H), 2.72 (s, 3H), 2.4 (m, 6H), 1.63 (m, 6H), 1.45 (m, 2H)" +
				", 2.35-2.10 (m, 3H), 1.7 (m, 1H)");
		
		assertEquals(0, doc.query("//spectrum").size());
		long start = System.currentTimeMillis();
		DataParser.dataParse(doc);
		long stop = System.currentTimeMillis();
		System.out.println("took " + (stop-start) + " ms");
		assertEquals(1, doc.query("//spectrum").size());
	}
	
	@Test
	public void testCnmr() throws Exception {
		Document doc = TextToSciXML.textToSciXML("13C NMR of 6 (CDCl3, 100 MHz):" +
				" 169.7,156.3,154.5,143.9,137.1,132.4, 128.0, 126.1, 124.2, 53.4.");
		
		assertEquals(0, doc.query("//spectrum").size());
		long start = System.currentTimeMillis();
		DataParser.dataParse(doc);
		long stop = System.currentTimeMillis();
		System.out.println("took " + (stop-start) + " ms");
		assertEquals(1, doc.query("//spectrum").size());
	}
	
	@Test
	public void testMassSpec() throws Exception {
		
		Document doc = TextToSciXML.textToSciXML("MS (ESI) m/z 413 (MH+)");

		assertEquals(0, doc.query("//spectrum").size());
		long start = System.currentTimeMillis();
		DataParser.dataParse(doc);
		long stop = System.currentTimeMillis();
		System.out.println("took " + (stop-start) + " ms");
		assertEquals(1, doc.query("//spectrum").size());
	}
}
