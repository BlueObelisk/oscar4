package uk.ac.cam.ch.wwmm.oscartokeniser;

import static org.junit.Assert.*;

import org.junit.Test;

public class HyphenTokeniserTest {

	@Test
	public void indexOfSplittableHyphenNoHyphen() {
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("foo"));
	}
	
	
	@Test
	public void indexOfSplittableHyphen() {
		assertEquals(7, HyphenTokeniser.indexOfSplittableHyphen("alcohol-consuming"));
	}
	
	
	@Test
	public void indexOfNonsplittableHyphen() {
		assertEquals(-1, HyphenTokeniser.indexOfSplittableHyphen("n-butane"));
	}
}
