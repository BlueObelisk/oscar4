package uk.ac.cam.ch.wwmm.oscar.util;

import org.junit.Assert;
import org.junit.Test;

public class CacheMapTest {

	@Test
	public void testCapacity() {
		CacheMap<String,String> map = new CacheMap<String,String>(1);
		map.put("Foo", "Bar");
		Assert.assertEquals(1, map.size());
		map.put("Bar", "Foo");
		Assert.assertEquals(1, map.size());
	}

	@Test
	public void testDeletion() {
		CacheMap<String,String> map = new CacheMap<String,String>(2);
		map.put("Foo", "Bar");
		map.put("Foo2", "Bar");
		Assert.assertTrue(map.containsKey("Foo"));
		Assert.assertTrue(map.containsKey("Foo2"));
		Assert.assertEquals(2, map.size());
		map.put("Bar", "Foo");
		Assert.assertEquals(2, map.size());
		Assert.assertFalse(map.containsKey("Foo"));
		Assert.assertTrue(map.containsKey("Foo2"));
		Assert.assertTrue(map.containsKey("Bar"));
	}

}
