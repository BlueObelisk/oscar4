package uk.ac.cam.ch.wwmm.oscar.types;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class NamedEntityTypesTest {

	@Test
	public void testGetTypeNames() {
		List<String> types = NamedEntityTypes.getTypeNames();
		Assert.assertNotNull(types);
		Assert.assertNotSame(0, types.size());
	}
	
}
