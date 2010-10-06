package uk.ac.cam.ch.wwmm.oscar.tools;

import java.io.BufferedInputStream;
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
	
	public boolean fulldb;
	public boolean lockdown;
	public boolean makeCML;
	public boolean splitOnEnDash;
	public boolean useONT;
	public boolean useDSO;
	public boolean deprioritiseONT;
	public boolean useFormulaRegex;
	public boolean useWordShapeHeuristic;
	public boolean minimizeDFA;
	public boolean useJNIInChI;
	public boolean useMEMM;
	public boolean rescoreMEMM;
	public boolean verbose;
	public boolean interpretPoly;
	public boolean dataOnlyInExperimental;
	public boolean polymerMode;
	public boolean cacheExtensionNameResolver;
	public boolean useOPSIN;
	public boolean scrapBookIEFix;
	public boolean urlEncodeCML;
	public boolean useCachedResources; 
		
	public double ngramThreshold;
	public double ontProb;
	public double cprProb;
	public double custProb;
	public double neThreshold;

	public int dfaSize;
	public int port;
	
	public String dbname;
	public String dbaddress;
	public String dbusername;
	public String dbpasswd;
	public String rdbms;
	public String safdbusername;
	public String safdbpasswd;
	
	public String serverType;
	
	public String hostname;
	
	public String oscarFlow;
	
	public String workspace;
	public String geniaPath;
	public String pcdir;
	public String InChI;
	public String stdInChI;
	public String svdlibc;
	public String openBabel;
	public String model;
	public String yahooKey;
	public String extensionNameResolver;
	public String initScript;
	public String resourcePrefix;
	public String chemNameDict;
	public String serverRoot;
	public String xmlStrings;
	public String chemicalEntityRecogniser;
	
	private Properties myProperties;

	private static ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar/tools/", true);
	
	private static OscarProperties myInstance;
	
	public static OscarProperties getInstance() {
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
	
	private OscarProperties() {
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
		fulldb = "yes".equals(myProperties.getProperty("fulldb"));
		lockdown = "yes".equals(myProperties.getProperty("lockdown"));
		makeCML = "yes".equals(myProperties.getProperty("makeCML"));
		splitOnEnDash = "yes".equals(myProperties.getProperty("splitOnEnDash"));
		useONT = "yes".equals(myProperties.getProperty("useONT"));
		useDSO = "yes".equals(myProperties.getProperty("useDSO"));
		deprioritiseONT = "yes".equals(myProperties.getProperty("deprioritiseONT"));
		useFormulaRegex = "yes".equals(myProperties.getProperty("useFormulaRegex"));
		useWordShapeHeuristic = "yes".equals(myProperties.getProperty("useWordShapeHeuristic"));
		minimizeDFA = "yes".equals(myProperties.getProperty("minimizeDFA"));
		useJNIInChI = "yes".equals(myProperties.getProperty("useJNIInChI"));
		useMEMM = "yes".equals(myProperties.getProperty("useMEMM"));
		rescoreMEMM = "yes".equals(myProperties.getProperty("rescoreMEMM"));
		verbose = "yes".equals(myProperties.getProperty("verbose"));
		interpretPoly = "yes".equals(myProperties.getProperty("interpretPoly"));
		dataOnlyInExperimental = "yes".equals(myProperties.getProperty("dataOnlyInExperimental"));
		chemicalEntityRecogniser = getPropertyOrNone("chemicalEntityRecogniser");
		polymerMode = "yes".equals(myProperties.getProperty("polymerMode"));
		//override useMEMM if operating in polymerMode
		if (polymerMode) {
			useMEMM = false;
		}
		cacheExtensionNameResolver = "yes".equals(myProperties.getProperty("cacheExtensionNameResolver"));
		useOPSIN = "yes".equals(myProperties.getProperty("useOPSIN"));
		scrapBookIEFix = "yes".equals(myProperties.getProperty("scrapBookIEFix"));
		urlEncodeCML = "yes".equals(myProperties.getProperty("urlEncodeCML"));
		useCachedResources = "yes".equals(myProperties.getProperty("useCachedResources"));
		
//		ngramThreshold = Double.parseDouble(myProperties.getProperty("ngramThreshold"));
//		neThreshold = Double.parseDouble(myProperties.getProperty("neThreshold"));
//		ontProb = Double.parseDouble(myProperties.getProperty("ontProb"));
//		cprProb = Double.parseDouble(myProperties.getProperty("cprProb"));
//		custProb = Double.parseDouble(myProperties.getProperty("custProb"));

//		dfaSize = Integer.parseInt(myProperties.getProperty("dfaSize"));
//		port = Integer.parseInt(myProperties.getProperty("port"));
		
		dbname = getPropertyOrNone("dbname");
		dbaddress = getPropertyOrNone("dbaddress");
		dbusername = getPropertyOrNone("dbusername");
		dbpasswd = getPropertyOrNone("dbpasswd");
		rdbms = getPropertyOrNone("rdbms");
		safdbusername = getPropertyOrNone("safdbusername");
		safdbpasswd = getPropertyOrNone("safdbpasswd");
		serverType = getPropertyOrNone("serverType");
		hostname = getPropertyOrNone("hostname");
		oscarFlow = getPropertyOrNone("oscarFlow");
		workspace = getPropertyOrNone("workspace");
		geniaPath = getPropertyOrNone("geniaPath");
		pcdir = getPropertyOrNone("pcdir");
		InChI = getPropertyOrNone("InChI");
		stdInChI = getPropertyOrNone("stdInChI");
		openBabel = getPropertyOrNone("openBabel");
		svdlibc = getPropertyOrNone("svdlibc");
		model = getPropertyOrNone("model");
		yahooKey = getPropertyOrNone("yahooKey");
		extensionNameResolver = getPropertyOrNone("extensionNameResolver");
		initScript = getPropertyOrNone("initScript");
		resourcePrefix = getPropertyOrNone("resourcePrefix");
		chemNameDict = getPropertyOrNone("chemNameDict");
		serverRoot = getPropertyOrNone("serverRoot");
		xmlStrings = getPropertyOrNone("xmlStrings");
		
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
		getInstance().setPropertyInternal(name, value);
	}

	private synchronized void setPropertyInternal(String name, String value) {
		myProperties.setProperty(name, value);
		propsToVariables();
	}
	
	public static void saveProperties() throws Exception {
		getInstance().savePropertiesInternal();
	}
		
	private synchronized void savePropertiesInternal() throws Exception {
		storeSorted(myProperties, new FileOutputStream(propsFile), "Autogenerated by Oscar");
	}
	
	public static void writeProperties(OutputStream os) throws Exception {
		getInstance().writePropertiesInternal(os);
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
