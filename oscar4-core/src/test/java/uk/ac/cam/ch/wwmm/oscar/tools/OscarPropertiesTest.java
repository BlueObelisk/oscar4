package uk.ac.cam.ch.wwmm.oscar.tools;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author egonw
 */
public class OscarPropertiesTest {

	@Test
	public void testLoadingOfDefaultValues() {
		OscarProperties props = OscarProperties.getInstance();
		Assert.assertNotNull(props);
		Assert.assertEquals("chempapers", props.model);
	}

}
