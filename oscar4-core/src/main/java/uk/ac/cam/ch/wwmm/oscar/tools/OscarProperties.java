package uk.ac.cam.ch.wwmm.oscar.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

	public static void initialiseWithProperties(Properties props) {
		myInstance = new OscarProperties();
		myInstance.initialise(props);
	}

	public static void initialiseWithFile(File propsFile) {
		myInstance = new OscarProperties();
		myInstance.initialise(propsFile);
	}
	
	public static void reloadProperties() {
		if(myInstance != null) {
			File propsFile = myInstance.propsFile;
			myInstance = new OscarProperties();
			myInstance.initialise(propsFile);
		} else {
			myInstance = null;
			getInstance();			
		}
	}
	
	private void initialise() {
		try {
			if(myProperties == null) {
				myProperties = new Properties(getDefaults());
				propsFile = new File("Properties.dat");
				File propsTxtFile = new File("Properties.txt");
				if(!propsFile.exists() && propsTxtFile.exists()) propsFile = propsTxtFile;
				if(!propsFile.exists()) {
					myProperties = getDefaults();
					savePropertiesInternal();
				}
				myProperties.load(new FileInputStream(propsFile));
			}
			propsToVariables();
		} catch (Exception e) {
			throw new Error("Problem loading properties!", e);
		}		
	}

	private void initialise(File propsFile) {
		this.propsFile = propsFile;
		try {
			if(myProperties == null) {
				myProperties = new Properties(getDefaults());
				if(!propsFile.exists()) {
					myProperties = getDefaults();
					savePropertiesInternal();
				}
				myProperties.load(new FileInputStream(propsFile));
			}
			propsToVariables();
		} catch (Exception e) {
			throw new Error("Problem loading properties!");
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
	
	public static void saveProperties() throws Exception {
		getMyself().savePropertiesInternal();
	}
		
	private synchronized void savePropertiesInternal() throws Exception {
		storeSorted(myProperties, new FileOutputStream(propsFile), "Autogenerated by Oscar");
	}
	
	public static void writeProperties(OutputStream os) throws Exception {
		getMyself().writePropertiesInternal(os);
	}

	public void writePropertiesInternal(OutputStream os) throws Exception {
		storeSorted(myProperties, os, "");
		//myProperties.store(os, "");
	}
	
	private static void storeSorted(Properties props, OutputStream os, String comment) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		props.store(baos, comment);
		List<String> lines = new ArrayList<String>();
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		BufferedReader br = new BufferedReader(new InputStreamReader(bais));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
		for(String line = br.readLine();line != null;line = br.readLine()) {
			if(line.startsWith("#"))  {
				pw.println(line);
			} else {
				lines.add(line);
			}
		}
		Collections.sort(lines);
		for(String line : lines) {
			pw.println(line);
		}
		pw.flush();
	}

}
