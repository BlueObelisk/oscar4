package uk.ac.cam.ch.wwmm.oscar.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;

/**
 * Sets up and maintains a properties file for Oscar. Unlike the Oscar3Props
 * past, it does not take care of anything but properties. Therefore, it
 * does not deploy InChI libraries, or interactively set up workspace, or
 * servers. 
 * 
 * @author ptc24
 * @author jat45
 * @author egonw
 */
public class OscarProperties {

	private static ResourceGetter rg = new ResourceGetter(OscarProperties.class.getClassLoader(), "uk/ac/cam/ch/wwmm/oscar/tools/");

	private OscarPropertiesData oscarProperties;
	private static OscarProperties myInstance;

	private OscarProperties() {
		oscarProperties = new OscarPropertiesData();
	}

	public static OscarPropertiesData getData() {
		if(myInstance == null) myInstance = getInstance();
		return myInstance.oscarProperties;
	}

	public static OscarProperties getInstance() {
		if(myInstance == null) {
			myInstance = new OscarProperties();
			myInstance.initialise();
		}
		return myInstance;
	}

	public void initialiseWithProperties(Properties props) {
		myInstance = new OscarProperties();
		propsToVariables(props);
	}

	private void initialise() {
		propsToVariables(getDefaults());
	}

	private Properties variablesToProps() {
		Properties props = new Properties();
		props.setProperty("splitOnEnDash", oscarProperties.splitOnEnDash ? "yes" : "no");
		props.setProperty("useONT", oscarProperties.useONT ? "yes" : "no");
		props.setProperty("useDSO", oscarProperties.useDSO ? "yes" : "no");
		props.setProperty("deprioritiseONT", oscarProperties.deprioritiseONT ? "yes" : "no");
		props.setProperty("useFormulaRegex", oscarProperties.useFormulaRegex ? "yes" : "no");
		props.setProperty("useWordShapeHeuristic", oscarProperties.useWordShapeHeuristic ? "yes" : "no");
		props.setProperty("polymerMode", oscarProperties.polymerMode ? "yes" : "no");
		
		props.setProperty("neThreshold", "" + oscarProperties.neThreshold);

		props.setProperty("model", oscarProperties.model);
		props.setProperty("xmlStrings", oscarProperties.xmlStrings);
		return props;
	}

	private void propsToVariables(Properties myProperties) {
		oscarProperties.splitOnEnDash = "yes".equals(myProperties.getProperty("splitOnEnDash"));
		oscarProperties.useONT = "yes".equals(myProperties.getProperty("useONT"));
		oscarProperties.useDSO = "yes".equals(myProperties.getProperty("useDSO"));
		oscarProperties.deprioritiseONT = "yes".equals(myProperties.getProperty("deprioritiseONT"));
		oscarProperties.useFormulaRegex = "yes".equals(myProperties.getProperty("useFormulaRegex"));
		oscarProperties.useWordShapeHeuristic = "yes".equals(myProperties.getProperty("useWordShapeHeuristic"));
		oscarProperties.polymerMode = "yes".equals(myProperties.getProperty("polymerMode"));
		
		oscarProperties.neThreshold = Double.parseDouble(myProperties.getProperty("neThreshold"));

		oscarProperties.model = getPropertyOrNone(myProperties, "model");
		oscarProperties.xmlStrings = getPropertyOrNone(myProperties, "xmlStrings");
	}

	private String getPropertyOrNone(Properties myProperties, String propName) {
		String prop = myProperties.getProperty(propName);
		if(prop == null) return "none";
		return prop;
	}

	/**
	 * Returns a read-only copy of the current propeties.
	 *
	 * @return A {@link Properties} copy of the current settings.
	 */
	public Properties getProperties() {
		return new Properties(variablesToProps());
	}
	
	public Properties getDefaults() {
		Properties def = new Properties();
		InputStream stream = null;
		try {
			stream = rg.getStream("DefaultProperties.dat");
			def.load(stream);
		} catch (IOException e) {
			throw new OscarInitialisationException("failed to load default OSCAR properties", e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
		
		return def;
	}
		
	public static void setProperty(String name, String value) {
		getInstance().setPropertyInternal(name, value);
	}

	private synchronized void setPropertyInternal(String name, String value) {
		Properties props = variablesToProps();
		props.setProperty(name, value);
		propsToVariables(props);
	}

}
