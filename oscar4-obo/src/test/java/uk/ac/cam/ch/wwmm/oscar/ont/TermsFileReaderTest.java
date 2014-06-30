package uk.ac.cam.ch.wwmm.oscar.ont;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;

import com.google.common.collect.ListMultimap;

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
		
		assertEquals(7, terms.size());
		assertEquals("TERMS", terms.get("this is a term"));
		assertEquals("TERMS", terms.get("this term references a definition"));
		assertEquals("MORETERMS", terms.get("this is a duplicated term"));
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
	
	
	@Test
	public void testLoadConcatenatedTermMap() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/obo/testTermsForConcatenation.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		ListMultimap<String,String> terms = TermsFileReader.loadConcatenatedTermMap(br);
		
		assertEquals(6, terms.size());
		assertEquals("TERMS", terms.get("this is a term").get(0));
		assertTrue(terms.get("this is a duplicated term").contains("MORETERMS"));
		assertTrue(terms.get("this is a duplicated term").contains("TERMS"));
		assertEquals("MORETERMS", terms.get("This term teSTS the      STRING     NorMALISATION.").get(0));
		assertEquals("MORETERMS", terms.get("this term tests the STRING normalisation.").get(0));
		assertEquals("MORETERMS", terms.get("this term checks there's no problem with $UNDECLARED $declarations").get(0));
	}
	
	@Test
	public void testLoadConcatenatedTermMapNormalisation() throws Exception {
		BufferedReader br = mock(BufferedReader.class);
		when(br.readLine()).thenReturn("[id1]", "foo", "Foo", null);
		ListMultimap<String,String> terms = TermsFileReader.loadConcatenatedTermMap(br);
		assertEquals(2, terms.size());
		assertEquals("id1", terms.get("foo").get(0));
		assertEquals("id1", terms.get("Foo").get(0));
	}
	
	@Test
	public void testLoadConcatenatedTermMapNormalisationReversed() throws Exception {
		BufferedReader br = mock(BufferedReader.class);
		when(br.readLine()).thenReturn("[id1]", "Foo", "foo", null);
		ListMultimap<String,String> terms = TermsFileReader.loadConcatenatedTermMap(br);
		assertEquals(2, terms.size());
		assertEquals("id1", terms.get("foo").get(0));
		assertEquals("id1", terms.get("Foo").get(0));
	}
	
	@Test
	public void testLoadConcatenatedTermMapMultiIdNormalisation() throws Exception {
		BufferedReader br = mock(BufferedReader.class);
		when(br.readLine()).thenReturn("[id1]", "foo", "[id2]", "Foo", null);
		ListMultimap<String,String> terms = TermsFileReader.loadConcatenatedTermMap(br);
		assertEquals(4, terms.size());
		assertTrue(terms.get("foo").contains("id1"));
		assertTrue(terms.get("foo").contains("id2"));
		assertTrue(terms.get("Foo").contains("id1"));
		assertTrue(terms.get("Foo").contains("id2"));
	}
	
	@Test
	public void testLoadConcatenatedTermMapMultiIdNormalisationReversed() throws Exception {
		BufferedReader br = mock(BufferedReader.class);
		when(br.readLine()).thenReturn("[id1]", "Foo", "[id2]", "foo", null);
		ListMultimap<String,String> terms = TermsFileReader.loadConcatenatedTermMap(br);
		assertEquals(4, terms.size());
		assertTrue(terms.get("foo").contains("id1"));
		assertTrue(terms.get("foo").contains("id2"));
		assertTrue(terms.get("Foo").contains("id1"));
		assertTrue(terms.get("Foo").contains("id2"));
	}
	
	@Test (expected = DataFormatException.class)
	public void testLoadBrokenConcatenatedTermMap() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/obo/brokenTestTerms.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		TermsFileReader.loadConcatenatedTermMap(br);
	}
	
	@Test (expected = DataFormatException.class)
	public void testLoadConcatenatedTermMapRejectsDefs() throws Exception {
		BufferedReader br = mock(BufferedReader.class);
		when(br.readLine()).thenReturn("#", "[DEFINE]", null);
		TermsFileReader.loadConcatenatedTermMap(br);
	}
	
	@Test (expected = DataFormatException.class)
	public void testLoadConcatenatedTermMapRejectsMultiLine() throws Exception {
		BufferedReader br = mock(BufferedReader.class);
		when(br.readLine()).thenReturn("#", "[id]", "foo >>>", "bar", null);
		TermsFileReader.loadConcatenatedTermMap(br);
	}
	
}
