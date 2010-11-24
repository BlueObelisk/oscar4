package uk.ac.cam.ch.wwmm.oscar.document;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class NonSentenceEndingsTest {

	@Test
	public void testContains() throws IOException {
		NonSentenceEndings endings = new NonSentenceEndings();
		Assert.assertTrue(endings.contains("Prof"));
	}
}
