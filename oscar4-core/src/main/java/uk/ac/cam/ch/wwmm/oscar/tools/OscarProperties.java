package uk.ac.cam.ch.wwmm.oscar.tools;

import java.io.File;
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

	public File propsFile;

	private Properties myProperties;

	private static ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar/tools/", true);

	private OscarPropertiesData oscarProperties;
	private static OscarProperties myInstance;

	private OscarProperties() {
		oscarProperties = new OscarPropertiesData();
	}

	public static OscarPropertiesData getInstance() {
		if(myInstance == null) myInstance = getMyself();
		return myInstance.oscarProperties;
	}

	public static OscarProperties getMyself() {
		if(myInstance == null) {
			myInstance = new OscarProperties();
			myInstance.initialise();
		}
		return myInstance;
	}

	public void initialiseWithProperties(Properties props) {
		myInstance = new OscarProperties();
		myInstance.initialise(props);
	}

	private void initialise() {
		try {
			if(myProperties == null) {
				myProperties = new Properties(getDefaults());
			}
			propsToVariables();
		} catch (Exception e) {
			throw new Error("Problem loading properties!", e);
		}		
	}

	private void initialise(Properties props) {
		try {
			myProperties = new Properties(getDefaults());
			for(Object o : props.keySet()) {
				String s = (String)o;
				myProperties.setProperty(s, props.getProperty(s));
			}		
			propsToVariables();
		} catch (Exception e) {
			throw new Error("Problem loading properties!");
		}		
}
		
	private void propsToVariables() {
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
		oscarProperties.chemicalEntityRecogniser = getPropertyOrNone("chemicalEntityRecogniser");
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
		
		oscarProperties.dbname = getPropertyOrNone("dbname");
		oscarProperties.dbaddress = getPropertyOrNone("dbaddress");
		oscarProperties.dbusername = getPropertyOrNone("dbusername");
		oscarProperties.dbpasswd = getPropertyOrNone("dbpasswd");
		oscarProperties.rdbms = getPropertyOrNone("rdbms");
		oscarProperties.safdbusername = getPropertyOrNone("safdbusername");
		oscarProperties.safdbpasswd = getPropertyOrNone("safdbpasswd");
		oscarProperties.serverType = getPropertyOrNone("serverType");
		oscarProperties.hostname = getPropertyOrNone("hostname");
		oscarProperties.oscarFlow = getPropertyOrNone("oscarFlow");
		oscarProperties.workspace = getPropertyOrNone("workspace");
		oscarProperties.geniaPath = getPropertyOrNone("geniaPath");
		oscarProperties.pcdir = getPropertyOrNone("pcdir");
		oscarProperties.InChI = getPropertyOrNone("InChI");
		oscarProperties.stdInChI = getPropertyOrNone("stdInChI");
		oscarProperties.openBabel = getPropertyOrNone("openBabel");
		oscarProperties.svdlibc = getPropertyOrNone("svdlibc");
		oscarProperties.model = getPropertyOrNone("model");
		oscarProperties.yahooKey = getPropertyOrNone("yahooKey");
		oscarProperties.extensionNameResolver = getPropertyOrNone("extensionNameResolver");
		oscarProperties.initScript = getPropertyOrNone("initScript");
		oscarProperties.resourcePrefix = getPropertyOrNone("resourcePrefix");
		oscarProperties.chemNameDict = getPropertyOrNone("chemNameDict");
		oscarProperties.serverRoot = getPropertyOrNone("serverRoot");
		oscarProperties.xmlStrings = getPropertyOrNone("xmlStrings");
		
	}

	protected Properties getProperties() {
		return myProperties;
	}
	
	private String getPropertyOrNone(String propName) {
		String prop = myProperties.getProperty(propName);
		if(prop == null) return "none";
		return prop;
	}
	
	private Properties getDefaults() throws Exception {
		Properties def = new Properties();
		InputStream stream = rg.getStream("DefaultProperties.dat");
		def.load(stream);
		return def;
	}
		
	public static void setProperty(String name, String value) {
		getMyself().setPropertyInternal(name, value);
	}

	private synchronized void setPropertyInternal(String name, String value) {
		myProperties.setProperty(name, value);
		propsToVariables();
	}

}
