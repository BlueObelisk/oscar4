package uk.ac.cam.ch.wwmm.oscar.tools;

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
public class OscarPropertiesData {

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

}
