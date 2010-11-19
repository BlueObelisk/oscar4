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
		Document doc = TextToSciXML.textToSciXML("1H NMR: 2.3, 3.4");
		
		assertEquals(0, doc.query("//spectrum").size());
		DataParser.dataParse(doc);
		assertEquals(1, doc.query("//spectrum").size());
	}
}
