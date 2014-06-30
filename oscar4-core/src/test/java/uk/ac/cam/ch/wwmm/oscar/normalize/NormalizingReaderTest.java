package uk.ac.cam.ch.wwmm.oscar.normalize;

import java.io.IOException;

import org.junit.Assert;

import org.junit.Ignore;
import org.junit.Test;

public class NormalizingReaderTest {

	@Ignore
	public void testUTF8() throws IOException {
		NormalizingReader stream = new NormalizingReader( 
			this.getClass().getClassLoader().getResourceAsStream(
				"test.txt"
			)
		);
		
		String string = stream.readLine();
		Assert.assertEquals(
			"ff wachten", string
		);
	}

	@Test
	public void testProcessAll() throws IOException {
		NormalizingReader stream = new NormalizingReader( 
			this.getClass().getClassLoader().getResourceAsStream(
				"multiline.txt"
			)
		);
		
		Assert.assertEquals("line 1", stream.readLine());
		Assert.assertEquals("line two", stream.readLine());
		Assert.assertEquals("line three", stream.readLine());
		Assert.assertEquals("line four", stream.readLine());
		Assert.assertEquals("line five", stream.readLine());
	}
}
