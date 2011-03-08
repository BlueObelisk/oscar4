package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

public class TermMapsTest {

	@Test
	public void testGetDefaultNeTerms() {
		Map <String, NamedEntityType> defaultNeTerms = TermMaps.getInstance().getNeTerms();
		assertEquals("CM", defaultNeTerms.get("molecular sieve").getName());
		assertEquals("CM", defaultNeTerms.get("$-ile $-ide").getName());
		assertFalse(defaultNeTerms.containsKey("foobar"));
		for (String key : defaultNeTerms.keySet()) {
			assertFalse(key.startsWith("#"));
		}
	}
	
	@Test
	public void testLoadNeTerms() throws DataFormatException, IOException {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscarrecogniser/finder/testNeTerms.txt");
		Map <String, NamedEntityType> neTerms = TermMaps.loadNeTermMap(in, "UTF-8");
		assertEquals(3, neTerms.size());
		assertEquals("RN", neTerms.get("foo bar").getName());
		assertEquals("CM", neTerms.get("$CM").getName());
		assertEquals("CM", neTerms.get("$-ene $-yl").getName());
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testLoadNeTermsIsUnmodifiable() throws DataFormatException, IOException {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscarrecogniser/finder/testNeTerms.txt");
		Map <String, NamedEntityType> neTerms = TermMaps.loadNeTermMap(in, "UTF-8");
		neTerms.clear();
	}
	
}
