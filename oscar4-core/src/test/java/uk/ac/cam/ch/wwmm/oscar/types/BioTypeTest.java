package uk.ac.cam.ch.wwmm.oscar.types;

import org.junit.Assert;
import org.junit.Test;

public class BioTypeTest {

	@Test
	public void testBioType() {
		BioType type = new BioType(BioTag.B, NamedEntityType.COMPOUND);
		Assert.assertEquals(BioTag.B, type.getBio());
		Assert.assertEquals(NamedEntityType.COMPOUND, type.getType());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNullNamedEntityType() {
		new BioType(BioTag.B, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNullBioType() {
		new BioType(null, NamedEntityType.COMPOUND);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNullParameters() {
		new BioType(null, null);
	}

	@Test
	public void testEquals() {
		Assert.assertEquals(
			new BioType(BioTag.B, NamedEntityType.COMPOUND),
			new BioType(BioTag.B, NamedEntityType.COMPOUND)
		);
	}

	@Test
	public void testHashcode() {
		Assert.assertEquals(
			new BioType(BioTag.B, NamedEntityType.COMPOUND).hashCode(),
			new BioType(BioTag.B, NamedEntityType.COMPOUND).hashCode()
		);
	}

	@Test
	public void testToString() {
		Assert.assertNotNull(
			new BioType(BioTag.B, NamedEntityType.COMPOUND).toString()
		);
	}

}
