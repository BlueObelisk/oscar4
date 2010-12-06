package uk.ac.cam.ch.wwmm.oscardata;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

public class DataAnnotationTest {

	@Test
	public void testConstructor() {
		DataAnnotation annotation = new DataAnnotation(12, 42, "foo");
		assertEquals(12, annotation.getStart());
		assertEquals(42, annotation.getEnd());
		assertEquals("foo", annotation.getSurface());
		assertTrue(NamedEntityType.DATA == annotation.getType());
	}
	
}
