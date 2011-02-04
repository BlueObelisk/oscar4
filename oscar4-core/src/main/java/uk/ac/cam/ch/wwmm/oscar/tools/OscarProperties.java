package uk.ac.cam.ch.wwmm.oscar.tools;

import java.io.InputStream;
import java.util.Properties;

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
		try {
			propsToVariables(getDefaults());
		} catch (Exception e) {
			throw new Error("Problem loading properties!", e);
		}		
	}

	private Properties variablesToProps() {
		Properties props = new Properties();
		props.setProperty("fulldb", oscarProperties.fulldb ? "yes" : "no");
		props.setProperty("lockdown", oscarProperties.lockdown ? "yes" : "no");
		props.setProperty("makeCML", oscarProperties.makeCML ? "yes" : "no");
		props.setProperty("splitOnEnDash", oscarProperties.splitOnEnDash ? "yes" : "no");
		props.setProperty("useONT", oscarProperties.useONT ? "yes" : "no");
		props.setProperty("useDSO", oscarProperties.useDSO ? "yes" : "no");
		props.setProperty("deprioritiseONT", oscarProperties.deprioritiseONT ? "yes" : "no");
		props.setProperty("useFormulaRegex", oscarProperties.useFormulaRegex ? "yes" : "no");
		props.setProperty("useWordShapeHeuristic", oscarProperties.useWordShapeHeuristic ? "yes" : "no");
		props.setProperty("minimizeDFA", oscarProperties.minimizeDFA ? "yes" : "no");
		props.setProperty("useJNIInChI", oscarProperties.useJNIInChI ? "yes" : "no");
		props.setProperty("useMEMM", oscarProperties.useMEMM ? "yes" : "no");
		props.setProperty("rescoreMEMM", oscarProperties.rescoreMEMM ? "yes" : "no");
		props.setProperty("interpretPoly", oscarProperties.interpretPoly ? "yes" : "no");
		props.setProperty("dataOnlyInExperimental", oscarProperties.dataOnlyInExperimental ? "yes" : "no");
		props.setProperty("chemicalEntityRecogniser", oscarProperties.chemicalEntityRecogniser);
		props.setProperty("polymerMode", oscarProperties.polymerMode ? "yes" : "no");
		props.setProperty("cacheExtensionNameResolver", oscarProperties.cacheExtensionNameResolver ? "yes" : "no");
		props.setProperty("useOPSIN", oscarProperties.useOPSIN ? "yes" : "no");
		props.setProperty("scrapBookIEFix", oscarProperties.scrapBookIEFix ? "yes" : "no");
		props.setProperty("urlEncodeCML", oscarProperties.urlEncodeCML ? "yes" : "no");
		props.setProperty("useCachedResources", oscarProperties.useCachedResources ? "yes" : "no");
		
		props.setProperty("ngramThreshold", "" + oscarProperties.ngramThreshold);
		props.setProperty("neThreshold", "" + oscarProperties.neThreshold);
		props.setProperty("ontProb", "" + oscarProperties.ontProb);
		props.setProperty("cprProb", "" + oscarProperties.cprProb);
		props.setProperty("custProb", "" + oscarProperties.custProb);

		props.setProperty("dfaSize", "" + oscarProperties.dfaSize);
		props.setProperty("port", "" + oscarProperties.port);
		
		props.setProperty("chemicalEntityRecogniser", oscarProperties.chemicalEntityRecogniser);
		props.setProperty("dbaddress", oscarProperties.dbaddress);
		props.setProperty("dbusername", oscarProperties.dbusername);
		props.setProperty("dbpasswd", oscarProperties.dbpasswd);
		props.setProperty("rdbms", oscarProperties.rdbms);
		props.setProperty("safdbusername", oscarProperties.safdbusername);
		props.setProperty("safdbpasswd", oscarProperties.safdbpasswd);
		props.setProperty("serverType", oscarProperties.serverType);
		props.setProperty("hostname", oscarProperties.hostname);
		props.setProperty("oscarFlow", oscarProperties.oscarFlow);
		props.setProperty("geniaPath", oscarProperties.geniaPath);
		props.setProperty("pcdir", oscarProperties.pcdir);
		props.setProperty("InChI", oscarProperties.InChI);
		props.setProperty("stdInChI", oscarProperties.stdInChI);
		props.setProperty("openBabel", oscarProperties.openBabel);
		props.setProperty("svdlibc", oscarProperties.svdlibc);
		props.setProperty("model", oscarProperties.model);
		props.setProperty("yahooKey", oscarProperties.yahooKey);
		props.setProperty("extensionNameResolver", oscarProperties.extensionNameResolver);
		props.setProperty("initScript", oscarProperties.initScript);
		props.setProperty("resourcePrefix", oscarProperties.resourcePrefix);
		props.setProperty("chemNameDict", oscarProperties.chemNameDict);
		props.setProperty("serverRoot", oscarProperties.serverRoot);
		props.setProperty("xmlStrings", oscarProperties.xmlStrings);
		return props;
	}

	private void propsToVariables(Properties myProperties) {
		oscarProperties.fulldb = "yes".equals(myProperties.getProperty("fulldb"));
		oscarProperties.lockdown = "yes".equals(myProperties.getProperty("lockdown"));
		oscarProperties.makeCML = "yes".equals(myProperties.getProperty("makeCML"));
		oscarProperties.splitOnEnDash = "yes".equals(myProperties.getProperty("splitOnEnDash"));
		oscarProperties.useONT = "yes".equals(myProperties.getProperty("useONT"));
		oscarProperties.useDSO = "yes".equals(myProperties.getProperty("useDSO"));
		oscarProperties.deprioritiseONT = "yes".equals(myProperties.getProperty("deprioritiseONT"));
		oscarProperties.useFormulaRegex = "yes".equals(myProperties.getProperty("useFormulaRegex"));
		oscarProperties.useWordShapeHeuristic = "yes".equals(myProperties.getProperty("useWordShapeHeuristic"));
		oscarProperties.minimizeDFA = "yes".equals(myProperties.getProperty("minimizeDFA"));
		oscarProperties.useJNIInChI = "yes".equals(myProperties.getProperty("useJNIInChI"));
		oscarProperties.useMEMM = "yes".equals(myProperties.getProperty("useMEMM"));
		oscarProperties.rescoreMEMM = "yes".equals(myProperties.getProperty("rescoreMEMM"));
		oscarProperties.interpretPoly = "yes".equals(myProperties.getProperty("interpretPoly"));
		oscarProperties.dataOnlyInExperimental = "yes".equals(myProperties.getProperty("dataOnlyInExperimental"));
		oscarProperties.chemicalEntityRecogniser = getPropertyOrNone(myProperties, "chemicalEntityRecogniser");
		oscarProperties.polymerMode = "yes".equals(myProperties.getProperty("polymerMode"));
		//override useMEMM if operating in polymerMode
		if (oscarProperties.polymerMode) {
			oscarProperties.useMEMM = false;
		}
		oscarProperties.cacheExtensionNameResolver = "yes".equals(myProperties.getProperty("cacheExtensionNameResolver"));
		oscarProperties.useOPSIN = "yes".equals(myProperties.getProperty("useOPSIN"));
		oscarProperties.scrapBookIEFix = "yes".equals(myProperties.getProperty("scrapBookIEFix"));
		oscarProperties.urlEncodeCML = "yes".equals(myProperties.getProperty("urlEncodeCML"));
		oscarProperties.useCachedResources = "yes".equals(myProperties.getProperty("useCachedResources"));
		
		oscarProperties.ngramThreshold = Double.parseDouble(myProperties.getProperty("ngramThreshold"));
		oscarProperties.neThreshold = Double.parseDouble(myProperties.getProperty("neThreshold"));
		oscarProperties.ontProb = Double.parseDouble(myProperties.getProperty("ontProb"));
		oscarProperties.cprProb = Double.parseDouble(myProperties.getProperty("cprProb"));
		oscarProperties.custProb = Double.parseDouble(myProperties.getProperty("custProb"));

		oscarProperties.dfaSize = Integer.parseInt(myProperties.getProperty("dfaSize"));
		oscarProperties.port = Integer.parseInt(myProperties.getProperty("port"));
		
		oscarProperties.dbname = getPropertyOrNone(myProperties, "dbname");
		oscarProperties.dbaddress = getPropertyOrNone(myProperties, "dbaddress");
		oscarProperties.dbusername = getPropertyOrNone(myProperties, "dbusername");
		oscarProperties.dbpasswd = getPropertyOrNone(myProperties, "dbpasswd");
		oscarProperties.rdbms = getPropertyOrNone(myProperties, "rdbms");
		oscarProperties.safdbusername = getPropertyOrNone(myProperties, "safdbusername");
		oscarProperties.safdbpasswd = getPropertyOrNone(myProperties, "safdbpasswd");
		oscarProperties.serverType = getPropertyOrNone(myProperties, "serverType");
		oscarProperties.hostname = getPropertyOrNone(myProperties, "hostname");
		oscarProperties.oscarFlow = getPropertyOrNone(myProperties, "oscarFlow");
		oscarProperties.geniaPath = getPropertyOrNone(myProperties, "geniaPath");
		oscarProperties.pcdir = getPropertyOrNone(myProperties, "pcdir");
		oscarProperties.InChI = getPropertyOrNone(myProperties, "InChI");
		oscarProperties.stdInChI = getPropertyOrNone(myProperties, "stdInChI");
		oscarProperties.openBabel = getPropertyOrNone(myProperties, "openBabel");
		oscarProperties.svdlibc = getPropertyOrNone(myProperties, "svdlibc");
		oscarProperties.model = getPropertyOrNone(myProperties, "model");
		oscarProperties.yahooKey = getPropertyOrNone(myProperties, "yahooKey");
		oscarProperties.extensionNameResolver = getPropertyOrNone(myProperties, "extensionNameResolver");
		oscarProperties.initScript = getPropertyOrNone(myProperties, "initScript");
		oscarProperties.resourcePrefix = getPropertyOrNone(myProperties, "resourcePrefix");
		oscarProperties.chemNameDict = getPropertyOrNone(myProperties, "chemNameDict");
		oscarProperties.serverRoot = getPropertyOrNone(myProperties, "serverRoot");
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
	
	public Properties getDefaults() throws Exception {
		Properties def = new Properties();
		InputStream stream = rg.getStream("DefaultProperties.dat");
		def.load(stream);
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
