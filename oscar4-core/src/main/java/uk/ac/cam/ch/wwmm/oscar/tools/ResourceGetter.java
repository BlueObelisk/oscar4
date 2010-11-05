package uk.ac.cam.ch.wwmm.oscar.tools;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

/**Gets resource files from packages. Useful for incuding data in JAR files.
 * 
 * @author ptc24
 * @author egonw
 *
 * Modified from ptc's original version
 */
public final class ResourceGetter {

	private String resourcePath;
	
	private ClassLoader classLoader;
	
	private boolean skipFiles; 

	/**
	 * Sets up a ResourceGetter to get resources from a particular path
	 * using the given {@link ClassLoader}. This constructor can be used
	 * of the given path is not accessible from the ClassLoader if
	 * this ResourceGetter.
	 *
	 * @param classLoader  ClassLoader that has access to the given
	 *                     <code>resourcePath</code>
	 * @param resourcePath path where to search for researches.
	 */
	public ResourceGetter(ClassLoader classLoader, String resourcePath) {
		this(resourcePath);
		this.classLoader = classLoader;
	}

	/**Sets up a resourceGetter to get resources from a particular path.
	 *  /-separated - e.g. uk.ac.ch.cam.wwmm.ptclib.files.resources should be
	 *  /uk/ac/cam/ch/wwmm/ptclib/files/resources/
	 *  
	 *  
	 * 
	 * @param resourcePath The /-separated resource path.
	 */
	public ResourceGetter(String resourcePath) {
		this(resourcePath, false);
	}
	
	/**Sets up a resourceGetter to get resources from a particular path.
	 *  /-separated - e.g. uk.ac.ch.cam.wwmm.ptclib.files.resources should be
	 *  /uk/ac/cam/ch/wwmm/ptclib/files/resources/
	 * 
	 * @param resourcePath The /-separated resource path.
	 * @param skipFiles Whether or not to skip reading files from the oscar3 workspace
	 */
	public ResourceGetter(String resourcePath, boolean skipFiles) {
		this.skipFiles = skipFiles;
		if(resourcePath.startsWith("/")) resourcePath = resourcePath.substring(1);
		this.resourcePath = resourcePath;
	}
//
//	private File getResDir() {
//		if(skipFiles) return null;
//		File resourcesTop = new File(Oscar3Props.getInstance().workspace, "resources");
//		return new File(resourcesTop, resourcePath);
//	}
//
//	private File getFile(String name) {
//		File f = new File(getResDir(), name);
//		if(f.isDirectory()) return null;
//		if(f.exists()) return f;
//		return null;
//	}
//
//	private File getFileForWriting(String name) {
//		if(skipFiles) return null;
//		File resourcesTop = new File(Oscar3Props.getInstance().workspace, "resources");
//		File resDir = new File(resourcesTop, resourcePath);
//		if(!resDir.exists()) resDir.mkdirs();
//		File f = new File(resDir, name);
//		return f;
//	}
	
	/**Sets up an output stream to which a resource file can be written; this
	 * resource file will be in a subdirectory of the resources directory in
	 * the workspace.
	 *
	 * @param name The name of the file to write.
	 * @return The output stream.
	 * @throws Exception
	 */
//	public OutputStream getOutputStream(String name) throws Exception {
//		if(skipFiles) return null;
//		File f = getFileForWriting(name);
//		return new FileOutputStream(f);
//	}

	/**Fetches a data file from resourcePath,
	 * and parses it to an XML Document.
	 *
	 * @param name The name of the file to parse.
	 * @return The parsed document.
	 * @throws Exception If the document can't be found, or can't parse, or is malformed/invalid.
	 */
//	public Document getXMLDocument(String name) throws Exception {
//		try {
//			File f = getFile(name);
//			if(f != null) {
//				return new Builder().build(f);
//			} else {
//				ClassLoader l = Thread.currentThread().getContextClassLoader();
//				if(!skipFiles && !"none".equals(Oscar3Props.getInstance().resourcePrefix)) {
//					URL url = l.getResource(Oscar3Props.getInstance().resourcePrefix + resourcePath + name);
//					try {
//						Document d = new Builder().build(url.toString());
//						if(d != null) return d;
//					} catch (Exception e) {
//						// Squelching the exceptions that come from failing to find a file here
//					}
//				}
//				URL url = l.getResource(resourcePath + name);
//				Document d = new Builder().build(url.toString());
//				return d;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new Exception("Could not get resource file: " + name);
//		}
//	}

	public Document getXMLDocument(String resourceName) {
		InputStream inStream = null;
		try {
			inStream = getStream(resourceName);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Document doc = null;
		try {
			doc = new Builder().build(inStream);
		} catch (ValidityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return doc;
		
	}
	
	/**Fetches a data file from resourcePath as an InputStream.
	 * 
	 * @param name The name of the file to get an InputStream of.
	 * @return An InputStream corresponding to the file.
	 * @throws Exception If the resouce file couldn't be found.
	 */
//	public InputStream getStream(String name) throws Exception {
//		if(name == null) name="";
//		try {
//			File f = getFile(name);
//			if(f != null) {
//				return new FileInputStream(f);
//			} else {
//				ClassLoader l = Thread.currentThread().getContextClassLoader();
//				if(!skipFiles && !"none".equals(Oscar3Props.getInstance().resourcePrefix)) {
//					URL url = l.getResource(Oscar3Props.getInstance().resourcePrefix + resourcePath + name);        					
//					try {
//						if(url != null) {
//							InputStream i = url.openStream();
//							if(i != null) return i;
//						}
//					} catch (Exception e) {
//						// Squelching the exceptions that come from failing to find a file here
//					}
//				}
//				URL url = l.getResource(resourcePath + name);
//				if(url == null) return null;
//				InputStream i = url.openStream();
//				return i;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new Exception("Could not get resource file: " + name);
//		}
//	}

    public InputStream getStream(String resourceName) {
		InputStream inStream = getStream(resourceName, Thread.currentThread().getContextClassLoader());
        if (inStream != null) {
            return inStream;
        }
        inStream = getStream(resourceName, classLoader);
        if (inStream != null) {
            return inStream;
        }
        inStream = getStream(resourceName, ClassLoader.getSystemClassLoader());
        if (inStream != null) {
            return inStream;
        }
        // TODO - should we throw exception (e.g. FileNotFoundException)
        return null;
	}

    private InputStream getStream(String resourceName, ClassLoader classLoader) {
        if (classLoader != null) {
            InputStream inStream = classLoader.getResourceAsStream(resourcePath+resourceName);
            return inStream;
        }
        return null;
    }

//	/**Fetches a data file from resourcePath, and writes it as the given file.
//	 * 
//	 * @param name The resource to write.
//	 * @param file The file to write it to.
//	 * @throws Exception If the files cannot be read or written.
//	 */
//	public void writeToFile(String name, File file) throws Exception {
//		FileOutputStream fos = new FileOutputStream(file);
//		FileTools.pipeStreamToStream(getStream(name), fos);
//		fos.close();
//	}
//
//	/**Copies the contents of the resourcePath into a new directory, recursively.
//	 * WARNING: the resources directory must not contain files with no dot in them,
//	 * as the presence/absence of a dot is taken to indicate whether or not a particular
//	 * resource is a directory or not.
//	 * 
//	 * @param file
//	 * @throws Exception
//	 */
//	public void writeDirRecursive(File file) throws Exception {
//		if(!file.exists()) file.mkdirs();
//		InputStream is = getStream("");
//		BufferedReader br = new BufferedReader(new InputStreamReader(is));
//		String line = br.readLine();
//		while(line != null) {
//			if(line.contains(".")) {
//				writeToFile(line, new File(file, line));
//			} else {
//				ResourceGetter subRg = new ResourceGetter(resourcePath + line + "/");
//				subRg.writeDirRecursive(new File(file, line));
//			}
//			line = br.readLine();
//		}
//	}
	
	/**Fetches a data file from resourcePath as an InputStream, removes comments starting with \s#, and
	 * returns each line in a list.
	 * 
	 * @param name The name of the file to get an InputStream of.
	 * @return A List of Strings corresponding to the file.
	 * @throws Exception If the resouce file couldn't be found.
	 */	
	public List<String> getStrings(String name) throws Exception {
		return getStrings(name, true);
	}

public List<String> getFilesFromClasspath()
	{
		List<String> result = new ArrayList<String>();
		String classPath = System.getProperty("java.class.path");
		String[] pathElements = classPath.split(System.getProperty("path.separator"));
		for(String element : pathElements)
		{
			try
			{
				File newFile = new File(element);
				if(newFile.isDirectory())
				{
					result.addAll(findResourceInDirectory(newFile));
				}
				else
				{
					result.addAll(findResourceInFile(newFile));
				}
			}
			catch(IOException e)
			{
				System.err.println("An error occurred getting files from the classpath");
			}
		}
		return result;
	}
	
	/**Gets a list of files that are available for this resourceGetter.
	 * 
	 * @return The available files.
	 * @throws Exception
	 */
//	public List<String> getFiles() throws Exception {
//		Set<String> seen = new LinkedHashSet<String>();
//		try {
//			seen.addAll(getFilesFromClasspath());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if(resourcePath.equals("/") || resourcePath.equals("")) {
//			seen.add("uk");
//		}
//		File resDir = getResDir();
//		if(resDir != null && resDir.exists() && resDir.isDirectory()) {
//			seen.addAll(StringTools.arrayToList(resDir.list()));
//		}
//		return new ArrayList<String>(seen);
//	}
	
	/**Fetches a data file from resourcePath as an InputStream, removes comments starting with \s#, and
	 * returns each line in a list.
	 * 
	 * @param name The name of the file to get an InputStream of.
	 * @param UTF8 Whether to load the strings in UTF8
	 * @return A List of Strings corresponding to the file.
	 * @throws Exception If the resouce file couldn't be found.
	 */	
	public List<String> getStrings(String name, boolean UTF8) throws Exception {
		List<String> results = new ArrayList<String>();
    	InputStream is = getStream(name);
    	InputStreamReader isr;
		if(UTF8) {
    		isr = new InputStreamReader(is, "UTF-8");
    	} else {
    		isr = new InputStreamReader(is);
    	}
		BufferedReader br = new BufferedReader(isr);
    	String line = br.readLine();
    	while(line != null) {
    		line = line.split("\\s*#")[0];
    		if(line.length() == 0) {
        		line = br.readLine();
    			continue;
    		}
    		results.add(line);
    		line = br.readLine();
    	}
    	return results;
	}

	/**Fetches a data file from resourcePath as an InputStream, removes comments starting with \s#, and
	 * returns each line in a set.
	 * 
	 * @param name The name of the file to get an InputStream of.
	 * @return A Set of Strings corresponding to the file.
	 * @throws Exception If the resouce file couldn't be found.
	 */	
	public Set<String> getStringSet(String name) throws Exception {
		Set<String> results = new HashSet<String>();
    	BufferedReader br = new BufferedReader(new InputStreamReader(getStream(name), "UTF-8"));
    	String line = br.readLine();
    	while(line != null) {
    		line = line.split("\\s*#")[0];
    		if(line.length() == 0) {
        		line = br.readLine();
    			continue;
    		}
    		results.add(line);
    		line = br.readLine();
    	}
    	return results;
	}
	
	/**Fetches a data file from resourcePath, and returns the entire contents
	 * as a string.
	 * 
	 * @param name The file to fetch.
	 * @return The string.
	 * @throws Exception
	 */
	public String getString(String name) throws Exception {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(getStream(name), "UTF-8"));
		StringBuffer sb = new StringBuffer();
		while(br.ready()) sb.append((char)br.read());
		br.close();
		return sb.toString();
		
	}

	

	private List<String> findResourceInFile(File resourceFile) throws IOException
	{
		List<String> result = new ArrayList<String>();
		if(resourceFile.canRead() && resourceFile.getAbsolutePath().endsWith(".jar"))
		{
			//System.err.println("jar file found: " + resourceFile.getAbsolutePath());
			JarFile jarFile = new JarFile(resourceFile);
			Enumeration<JarEntry> entries = jarFile.entries();
			while(entries.hasMoreElements())
			{
				JarEntry singleEntry = entries.nextElement();
				//System.err.println("jar entry: " + singleEntry.getName());
				result.add(jarFile.getName() + "/" + singleEntry.getName());
			}
		}
		return result;
	}

	private List<String> findResourceInDirectory(File directory) throws IOException
	{
		List<String> result = new ArrayList<String>();
		File[] files = directory.listFiles();
		for(File currentFile : files)
		{
			//System.err.println("current file name: " + currentFile.getAbsolutePath());

			if(currentFile.isDirectory())
			{
				result.addAll(findResourceInDirectory(currentFile));
			}
			else if(currentFile.canRead() && currentFile.getAbsolutePath().endsWith(".jar"))
			{
				result.addAll(findResourceInFile(currentFile));
			}
			else
			{
				result.add(currentFile.getAbsolutePath());
			}
		}
		return result;
	}
	
}
