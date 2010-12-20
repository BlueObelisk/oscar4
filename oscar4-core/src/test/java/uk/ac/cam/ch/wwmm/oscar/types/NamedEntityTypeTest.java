package uk.ac.cam.ch.wwmm.oscar.types;

import org.junit.Assert;
import org.junit.Test;

public class NamedEntityTypeTest {

	@Test
	public void testValueOf() {
		Assert.assertEquals(NamedEntityType.ASE, NamedEntityType.valueOf("ASE"));
	}

	@Test
	public void testNonExistingTag() {
		NamedEntityType type = NamedEntityType.valueOf("DOESNOTEXIST");
		Assert.assertNotNull(type);
		Assert.assertEquals("DOESNOTEXIST", type.getName());
	}
	
}
