package uk.ac.cam.ch.wwmm.oscar.obo;

import static junit.framework.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;

/**
 * 
 * @author dmj30
 *
 */
public class TermsFileReaderTest {

	@Test
	public void testLoadTermMap() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/obo/testTerms.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		Map <String, String> terms = TermsFileReader.loadTermMap(br, false);
		
		assertEquals(6, terms.size());
		assertEquals("TERMS", terms.get("this is a term"));
		assertEquals("TERMS", terms.get("this term references a definition"));
		assertEquals("MORETERMS", terms.get("This term teSTS the      STRING     NorMALISATION."));
		assertEquals("MORETERMS", terms.get("this term tests the STRING normalisation."));
		assertEquals("MORETERMS", terms.get("this term checks there's no problem with $UNDECLARED $declarations"));
		assertEquals("MORETERMS", terms.get("this is a multi-line term"));
	}
	
	@Test (expected = DataFormatException.class)
	public void testLoadBrokenTermMap() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/obo/brokenTestTerms.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		TermsFileReader.loadTermMap(br, false);
	}
	
}
