package uk.ac.cam.ch.wwmm.oscarpattern.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import uk.ac.cam.ch.wwmm.oscarpattern.PatternRecogniser;
import uk.ac.cam.ch.wwmm.oscarpattern.chemnamedict.ChemNameDictSingleton;

/** Sets up and maintains a properties file for Oscar3.
 * 
 * @author ptc24
 * @author jat45
 *
 */
public final class Oscar3Props {

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

	private static ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscarpattern/tools/", true);
	
	private static Oscar3Props myInstance;
	
	public static Oscar3Props getInstance() {
		if(myInstance == null) {
			myInstance = new Oscar3Props();
			try {
				myInstance.initialise();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return myInstance;
	}
	
	public static void initialiseWithProperties(Properties props) {
		myInstance = new Oscar3Props();
		myInstance.initialise(props);
	}

	public static void initialiseWithFile(File propsFile) {
		myInstance = new Oscar3Props();
		myInstance.initialise(propsFile);
	}
	
	public static void reloadProperties() {
		if(myInstance != null) {
			File propsFile = myInstance.propsFile;
			myInstance = new Oscar3Props();
			myInstance.initialise(propsFile);
		} else {
			myInstance = null;
			getInstance();			
		}
	}
	
	private Oscar3Props() {
	}
	
	private void initialise() throws Exception {
//		try {
			if(myProperties == null) {
				myProperties = new Properties(getDefaults());
				propsFile = new File("Properties.dat");
				File propsTxtFile = new File("Properties.txt");
				if(!propsFile.exists() && propsTxtFile.exists()) propsFile = propsTxtFile;
				if(!propsFile.exists()) {
					myProperties = getDefaults();
					savePropertiesInternal();
//					Deployment.deployInChI();
				}
				myProperties.load(new FileInputStream(propsFile));
			}
			propsToVariables();
//		} catch (Exception e) {
//			throw new Error("Problem loading properties!", e);
//		}		
	}

	private void initialise(File propsFile) {
		this.propsFile = propsFile;
		try {
			if(myProperties == null) {
				myProperties = new Properties(getDefaults());
				if(!propsFile.exists()) {
					myProperties = getDefaults();
					savePropertiesInternal();
//					Deployment.deployInChI();
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
		chemicalEntityRecogniser = PatternRecogniser.class.getName();//getPropertyOrNone("chemicalEntityRecogniser");
		
		polymerMode = "yes".equals(myProperties.getProperty("polymerMode"));
		//override useMEMM if operating in polymerMode
		if (polymerMode) {
			useMEMM = false;
			chemicalEntityRecogniser = PatternRecogniser.class.getName();
		}
		cacheExtensionNameResolver = "yes".equals(myProperties.getProperty("cacheExtensionNameResolver"));
		useOPSIN = "yes".equals(myProperties.getProperty("useOPSIN"));
		scrapBookIEFix = "yes".equals(myProperties.getProperty("scrapBookIEFix"));
		urlEncodeCML = "yes".equals(myProperties.getProperty("urlEncodeCML"));
		
		ngramThreshold = Double.parseDouble(myProperties.getProperty("ngramThreshold"));
		neThreshold = Double.parseDouble(myProperties.getProperty("neThreshold"));
		ontProb = Double.parseDouble(myProperties.getProperty("ontProb"));
		cprProb = Double.parseDouble(myProperties.getProperty("cprProb"));
		custProb = Double.parseDouble(myProperties.getProperty("custProb"));

		dfaSize = Integer.parseInt(myProperties.getProperty("dfaSize"));
		port = Integer.parseInt(myProperties.getProperty("port"));
		
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
		def.load(rg.getStream("DefaultProperties.dat"));
		return def;
	}
		
	public static void configureServer() throws Exception {
		getInstance().configureServerInternal();
	}
	
	private void configureServerInternal() throws Exception {
		BufferedReader stdinReader = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Configuring the Oscar3 web server");
		System.out.println();
		String serverType = null;
		System.out.println("Would you like to set up a full web server,");
		System.out.println("or a cut-down demonstration server?");
		while(!"full".equals(serverType) && !"cutdown".equals(serverType)) {
			System.out.println("Please enter 'full' or 'cutdown'");
			serverType = stdinReader.readLine();
			if(!"full".equals(serverType) && !"cutdown".equals(serverType)) {
				System.out.println("Invalid answer. Please try again.");
				System.out.println();
			}
		}
		setProperty("serverType", serverType);
		savePropertiesInternal();
		propsToVariables();
		
		if(serverType.equals("full")) {
			if(myProperties.get("workspace").equals("none")) {
				System.out.println("To do this you'll need to set up a workspace: ");
				System.out.println();
				configureWorkspaceInternal();
				System.out.println();
				System.out.println("Continuing with server configuration");
				System.out.println();
			}
		} 
		
		System.out.println("Would you like to lock the server down so that it can only be accessed from the machine it is running on (y/n)?");
		System.out.println("Please select 'yes', unless you know that your computer is behind a firewall that blocks incoming traffic on port 8181");
		System.out.println("If you select 'no', then people will be able to access and alter the contents of your Oscar3 workspace - this may cause problems if it will contain material that is confidential or someone else's copyright.");
		System.out.println("If in doubt, just answer 'yes'");
		boolean lockdown = IOTools.askYN();
		if(lockdown) {
			setProperty("lockdown", "yes");
			setProperty("hostname", "127.0.0.1");
		} else {
			System.out.println("Setting up the server to be accessible from anywhere that your firewall doesn't protect you from, as you request...");

			setProperty("lockdown", "no");
			setProperty("hostname", InetAddress.getLocalHost().getCanonicalHostName());
		}
		propsToVariables();
		savePropertiesInternal();
	}
	
	public static void configureWorkspace() throws Exception {
		getInstance().configureWorkspaceInternal();
	}
	
	private void configureWorkspaceInternal() throws Exception {
		
		// the workspace can be set in the properties file so check there first
		boolean done = false;
		String wsdir = myProperties.getProperty("workspace");
		
		try {
			if(!wsdir.equals("none")) {
				File f = new File(wsdir);
				if (f.exists() && f.isDirectory()) {
					done = true;
					System.out.println("OK, using an existing directory as your workspace.");
					System.out.println();
				}
				f.mkdir();
				wsdir = f.getCanonicalPath();
				done = true;
				System.out.println("Made your workspace at: " + wsdir);				
			}
		} catch (Exception e) {
			System.out.println("Can't create workspace automatically");
			done = false;
			wsdir = null;
		}

		// workspace is not valid, so prompt the user
		if (!done) {
			BufferedReader stdinReader = new BufferedReader(
					new InputStreamReader(System.in));

			System.out.println("Setting up an Oscar3 workspace.");
			System.out.println();
			System.out
					.println("This will be used for papers that you have parsed, ");
			System.out
					.println("indexes of those papers (so you can search them), ");
			System.out.println("an updatable dictionary of chemical names ");
			System.out.println("and a ScrapBook.");
			System.out.println();
			System.out
					.println("Please enter a directory to be your OSCAR workspace");
			System.out
					.println("or leave blank to use the current working directory");
			while (!done) {
				wsdir = stdinReader.readLine();
				try {
					File f = new File(wsdir);
					if (f.exists() && f.isDirectory()) {
						done = true;
						System.out
								.println("OK, using an existing directory as your workspace.");
						System.out.println();
					}
					f.mkdir();
					wsdir = f.getCanonicalPath();
					done = true;
					System.out.println("Made your workspace at: " + wsdir);
				} catch (Exception e) {
					System.out.println("That didn't work");
				}
				if (!done) {
					System.out.println("Please try again: enter a directory to be your OSCAR workspace");
				}
			}
		}
		setProperty("workspace", wsdir);

		ChemNameDictSingleton.makeFromScratch();
		File chemnamedictdir = new File(wsdir, "chemnamedict");
		if (!chemnamedictdir.exists())
			chemnamedictdir.mkdir();
		ChemNameDictSingleton.save();

		File scrapbookdir = new File(wsdir, "scrapbook");
		if (!scrapbookdir.exists())
			scrapbookdir.mkdir();

		File resdir = new File(wsdir, "resources");
		if (!resdir.exists())
			resdir.mkdir();

		File modelsdir = new File(wsdir, "models");
		if (!modelsdir.exists())
			modelsdir.mkdir();
		
		File corporadir = new File(wsdir, "corpora");
		if (!corporadir.exists())
			corporadir.mkdir();
		propsToVariables();
		savePropertiesInternal();
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

		//myProperties.store(new FileOutputStream(propsFile), "Autogenerated by Oscar");
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
