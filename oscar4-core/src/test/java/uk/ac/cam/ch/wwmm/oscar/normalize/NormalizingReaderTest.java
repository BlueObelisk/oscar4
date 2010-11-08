package uk.ac.cam.ch.wwmm.oscar.normalize;

import java.io.IOException;

import junit.framework.Assert;

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

}
