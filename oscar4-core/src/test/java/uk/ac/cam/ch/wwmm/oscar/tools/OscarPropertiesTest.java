package uk.ac.cam.ch.wwmm.oscar.tools;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author egonw
 */
public class OscarPropertiesTest {

	@Test
	public void testLoadingOfDefaultValues() {
		OscarPropertiesData props = OscarProperties.getData();
		Assert.assertNotNull(props);
		Assert.assertEquals("chempapers", props.model);
		Assert.assertEquals(0.2, props.neThreshold, 0.01);
	}

	@Test
	public void testDefaultsAreImmutable() {
		Properties props = OscarProperties.getInstance().getDefaults();
		Assert.assertNotNull(props);
		Assert.assertEquals("chempapers", props.getProperty("model"));
		props.setProperty("model", "foo");
		props = OscarProperties.getInstance().getDefaults();
		Assert.assertNotNull(props);
		Assert.assertEquals("chempapers", props.getProperty("model"));
	}

	@Test
	public void testDataIsSet() throws Exception {
		Properties props = OscarProperties.getInstance().getProperties();
		Assert.assertNotNull(props);
		Assert.assertEquals("chempapers", props.getProperty("model"));
		OscarProperties.setProperty("model", "foo");
		Assert.assertEquals("foo", OscarProperties.getData().model);
	}
}
