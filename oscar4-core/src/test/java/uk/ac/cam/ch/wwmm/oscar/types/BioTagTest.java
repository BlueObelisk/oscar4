package uk.ac.cam.ch.wwmm.oscar.types;

import org.junit.Assert;
import org.junit.Test;

public class BioTagTest {

	@Test
	public void testValueOf() {
		Assert.assertEquals(BioTag.B, BioTag.valueOf("B"));
		Assert.assertEquals(BioTag.I, BioTag.valueOf("I"));
		Assert.assertEquals(BioTag.O, BioTag.valueOf("O"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNonExistingTag() {
		BioTag.valueOf("X");
	}
	
}
