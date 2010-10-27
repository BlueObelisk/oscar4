package uk.ac.cam.ch.wwmm.oscar;

import org.junit.Assert;
import org.junit.Test;

public class OscarTest {

	@Test
	public void testConstructore() {
		Oscar oscar = new Oscar(getClass().getClassLoader());
		Assert.assertNotNull(oscar);
	}
	
	@Test
	public void testGetChemNameDict() {
		Oscar oscar = new Oscar(getClass().getClassLoader());
		Assert.assertNotNull(oscar.getChemNameDict());
	}
	
}
