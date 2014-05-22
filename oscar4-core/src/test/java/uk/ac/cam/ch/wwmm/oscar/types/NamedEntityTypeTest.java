package uk.ac.cam.ch.wwmm.oscar.types;

import static org.junit.Assert.*;


import org.junit.Test;

public class NamedEntityTypeTest {

	@Test
	public void testRegisteredTag() {
		NamedEntityType type = NamedEntityType.valueOf("CM");
		assertNotNull(type);
		assertEquals("CM", type.getName());
	}
	
	@Test
	public void testUnregisteredTag() {
		NamedEntityType type = NamedEntityType.valueOf("UNREGISTERED");
		assertNotNull(type);
		assertEquals("UNREGISTERED", type.getName());
	}	
	
	@Test
	public void testRegisteredTypesReturnSameObject() {
		assertTrue(NamedEntityType.COMPOUND == NamedEntityType.valueOf("CM"));
		assertTrue(NamedEntityType.valueOf("CM") == NamedEntityType.valueOf("CM"));
		assertFalse(NamedEntityType.valueOf("UNREGISTERED") == NamedEntityType.valueOf("UNREGISTERED"));
	}
	
	@Test
	public void testValueOf() {
		assertEquals(NamedEntityType.ASE, NamedEntityType.valueOf("ASE"));
		assertEquals(NamedEntityType.valueOf("UNREGISTERED"), NamedEntityType.valueOf("UNREGISTERED"));
	}

	@Test
	public void testIsInstance() {
		assertTrue(NamedEntityType.COMPOUND.isInstance(NamedEntityType.COMPOUND));
		assertFalse(NamedEntityType.COMPOUND.isInstance(NamedEntityType.REACTION));
		assertTrue(NamedEntityType.valueOf("UNREGISTERED").isInstance(NamedEntityType.valueOf("UNREGISTERED")));
		assertFalse(NamedEntityType.valueOf("UNKNOWN").isInstance(NamedEntityType.valueOf("UNREGISTERED")));
		assertTrue(NamedEntityType.valueOf("CM").isInstance(NamedEntityType.valueOf("CM-SUBTYPE")));
		assertFalse(NamedEntityType.valueOf("CM-SUBTYPE").isInstance(NamedEntityType.valueOf("CM")));
	}
	
	@Test
	public void testGetParent() {
		assertNull(NamedEntityType.COMPOUND.getParent());
		assertEquals(NamedEntityType.COMPOUND, NamedEntityType.valueOf("CM-SUBTYPE").getParent());
	}
}
