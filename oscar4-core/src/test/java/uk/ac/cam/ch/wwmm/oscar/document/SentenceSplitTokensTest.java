package uk.ac.cam.ch.wwmm.oscar.document;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class SentenceSplitTokensTest {

	@Test
	public void testContains() throws IOException {
		SentenceSplitTokens endings = new SentenceSplitTokens();
		Assert.assertTrue(endings.contains("."));
	}
}
