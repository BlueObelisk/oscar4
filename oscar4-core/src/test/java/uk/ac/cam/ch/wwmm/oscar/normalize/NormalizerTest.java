package uk.ac.cam.ch.wwmm.oscar.normalize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

import org.junit.Assert;

import org.junit.Test;


public class NormalizerTest {

	@Test
	public void testUTF8() throws IOException {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream(
			"test.txt"
		);
		String string = convertStreamToString(stream);
		string = string.replaceAll("\n", "");

		Normalizer normalizer = Normalizer.getDefaultInstance();
		Assert.assertEquals(
			"ff wachten",
			normalizer.normalize(string)
		);
	}

	@Test
	public void testString() throws IOException {
		Normalizer normalizer = Normalizer.getDefaultInstance();
		Assert.assertEquals(
			"ff wachten",
			normalizer.normalize("ff wachten")
		);
	}

	@SuppressWarnings("serial")
	@Test
	public void testCustomMaps() throws IOException {
		Normalizer normalizer = new Normalizer(
			new HashMap<Character, String>() {{
				put('e', "ee");
			}},
			new HashMap<String, String>() {{
				put("alpha", "foo"); //greeks
			}}
		);
		Assert.assertEquals(
			"foo-pineenee",
			normalizer.normalize("alpha-pinene")
		);
	}

	@Test
	public void testGreek() throws IOException {
		Normalizer normalizer = Normalizer.getDefaultInstance();
		Assert.assertEquals(
			"\u03b1-pinene",
			normalizer.normalize("alpha-pinene")
		);
	}

	@Test
	public void testGreekHyphenCombo() throws IOException {
		Normalizer normalizer = Normalizer.getDefaultInstance();
		Assert.assertEquals(
			"\u03b1-pinene",
			normalizer.normalize("alpha\u2010pinene")
		);
	}

	@Test
	public void testPunctuation() throws IOException {
		Normalizer normalizer = Normalizer.getDefaultInstance();
		Assert.assertEquals(
			"\"quoted text\"",
			normalizer.normalize("\u201Cquoted text\u201D")
		);
		Assert.assertEquals(
			"WTF!?",
			normalizer.normalize("WTF\u2049")
		);
	}

	private String convertStreamToString(InputStream is)
	throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(
						new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {        
			return "";
		}
	}
}
