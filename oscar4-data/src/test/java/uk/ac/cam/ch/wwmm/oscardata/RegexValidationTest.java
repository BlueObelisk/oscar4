package uk.ac.cam.ch.wwmm.oscardata;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;


public class RegexValidationTest {

	@Test
	public void validateRegexes() throws ValidityException, ParsingException, IOException {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscardata/regexes.xml");
		Document doc = new Builder(true).build(in);
		assertNotNull(doc);
	}
}
