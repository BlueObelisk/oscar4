package uk.ac.cam.ch.wwmm.oscarpreprocessor;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;

import org.junit.Ignore;
import org.junit.Test;

public class InputAnalyzerTest {

	@Test
	public void testUTF8() throws IOException {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream(
			"utf8.txt"
		);
		Assert.assertEquals("UTF-8", InputAnalyzer.guessEncoding(stream));
	}

	@Test
	public void testISO88591() throws IOException {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream(
			"iso8859-1.txt"
		);
		Assert.assertEquals("ISO-8859-2", InputAnalyzer.guessEncoding(stream));
	}

	@Ignore("Test fails because Kate failed to create a proper file.")
	public void testUTF16() throws IOException {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream(
			"utf16.txt"
		);
		Assert.assertEquals("UTF-16", InputAnalyzer.guessEncoding(stream));
	}

}
