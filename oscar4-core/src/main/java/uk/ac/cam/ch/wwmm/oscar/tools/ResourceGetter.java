package uk.ac.cam.ch.wwmm.oscar.tools;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

import org.apache.commons.io.IOUtils;

/**Gets resource files from packages. Useful for including data in JAR files.
 * 
 * @author ptc24
 * @author egonw
 * @author dmj30
 *
 * Modified from ptc's original version
 */
public final class ResourceGetter {

	private String resourcePath;
	
	private ClassLoader classLoader;
	
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
		if(resourcePath.startsWith("/")) resourcePath = resourcePath.substring(1);
		this.resourcePath = resourcePath;
	}
	


	/**Fetches a data file from resourcePath,
	 * and parses it to an XML Document.
	 *
	 * @param resourceName The name of the file to parse.
	 * @return The parsed document.
	 * 
	 * @throws IOException if an I/O error occurs
	 * @throws FileNotFoundException if the specified file cannot be found
	 * @throws ParsingException if the document is not well-formed
	 * @throws ValidityException if the document is invalid
	 */
	public Document getXMLDocument(String resourceName) throws ValidityException, ParsingException, FileNotFoundException, IOException {
		InputStream is = getStream(resourceName);
		try {
			return new Builder().build(is);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	/**Fetches a data file from resourcePath as an InputStream.
	 * 
	 * @param resourceName The name of the file to get an InputStream for
	 * @return An InputStream corresponding to the file, never null
	 * @throws FileNotFoundException if the resource file couldn't be found 
	 */
    public InputStream getStream(String resourceName) throws FileNotFoundException {
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
        
        throw new FileNotFoundException("failed to find resource: " + resourcePath + "/" + resourceName);
	}

    private InputStream getStream(String resourceName, ClassLoader classLoader) {
        if (classLoader != null) {
            InputStream inStream = classLoader.getResourceAsStream(resourcePath+resourceName);
            return inStream;
        }
        return null;
    }


	/**Fetches a data file from resourcePath as an InputStream, reads the content
	 * using the UTF-8 encoding, removes comments starting with \s#, and
	 * returns each line in a list.
	 * 
	 * @param name The name of the file to get an InputStream of.
	 * @return A List of Strings corresponding to the file.
	 * @throws IOException if the resource file couldn't be found
	 */	
	public List<String> getStrings(String name) throws IOException {
		return getStrings(name, "UTF-8");
	}

	@Deprecated
	//TODO this isn't called - do we need it?
    public List<String> getFilesFromClasspath() {
		List<String> result = new ArrayList<String>();
		String classPath = System.getProperty("java.class.path");
		String[] pathElements = classPath.split(System.getProperty("path.separator"));
		for (String element : pathElements) {
			try {
				File newFile = new File(element);
				if (newFile.isDirectory()) {
					result.addAll(findResourceInDirectory(newFile));
				} else {
					result.addAll(findResourceInFile(newFile));
				}
			} catch(IOException e) {
				System.err.println("An error occurred getting files from the classpath");
                e.printStackTrace();
			}
		}
		return result;
	}
	

	/**Fetches a data file from resourcePath as an InputStream, reads the content
	 * using the specified encoding, removes comments starting with \s#, and
	 * returns each line in a list.
	 * 
	 * @param name The name of the file to read
	 * @param encoding the character encoding to be used
	 * @return A List of Strings corresponding to the file
	 * @throws UnsupportedEncodingException if the specified encoding is not supported
	 * @throws FileNotFoundException if the specified resource could not be found
	 * @throws IOException if an I/O error occurs
	 */	
	public List<String> getStrings(String name, String encoding) throws IOException {
		InputStream is = getStream(name);
        List<String> lines;
        try {
        	lines = IOUtils.readLines(is, encoding);
        }
        finally {
        	IOUtils.closeQuietly(is);
        }
        return removeComments(lines);
    }

    public static List<String> removeComments(List<String> lines) {
        List<String> results = new ArrayList<String>(lines.size());
        for (String line : lines) {
        	if (line.length() == 0) continue; // skip empty lines
            int i = findComment(line);
            if (i == -1) {
                results.add(line);
            } else if (i > 0) {
                results.add(line.substring(0, i));
            }
        }
        return results;
    }

    private static int findComment(String line) {
        int i = line.indexOf('#');
        while (i > 0 && Character.isWhitespace(line.charAt(i-1))) {
            i--;
        }
        return i;
    }
    

	/**Fetches a data file from resourcePath as an InputStream, removes comments starting with \s#, and
	 * returns each line in a set.
	 * 
	 * @param name The name of the file to get an InputStream of.
	 * @return A Set of Strings corresponding to the file.
	 * @throws Exception If the resouce file couldn't be found.
	 */	
    @Deprecated
    //TODO this isn't called - do we need it?
	public Set<String> getStringSet(String name) throws Exception {
		Set<String> results = new HashSet<String>();
    	BufferedReader br = new BufferedReader(new InputStreamReader(getStream(name), "UTF-8"));
    	String line = br.readLine();
    	while (line != null) {
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
	
	/**
     * Fetches a data file from resourcePath, and returns the entire contents as a UTF-8 encoded string.
	 * 
	 * @param name The file to fetch.
	 * @return The string.
	 * @throws IOException
	 */
	public String getString(String name) throws IOException {
        InputStream is = getStream(name);
        try {
        	return IOUtils.toString(is, "UTF-8");
        }
        finally {
        	IOUtils.closeQuietly(is);
        }
	}

	private List<String> findResourceInFile(File resourceFile) throws IOException {
		List<String> result = new ArrayList<String>();
		if (resourceFile.canRead() && resourceFile.getAbsolutePath().endsWith(".jar")) {
			JarFile jarFile = new JarFile(resourceFile);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry singleEntry = entries.nextElement();
				result.add(jarFile.getName() + "/" + singleEntry.getName());
			}
		}
		return result;
	}

	private List<String> findResourceInDirectory(File directory) throws IOException {
		List<String> result = new ArrayList<String>();
		File[] files = directory.listFiles();
		for (File currentFile : files) {
			if (currentFile.isDirectory()) {
				result.addAll(findResourceInDirectory(currentFile));
			}
			else if (currentFile.canRead() && currentFile.getAbsolutePath().endsWith(".jar")) {
				result.addAll(findResourceInFile(currentFile));
			} else {
				result.add(currentFile.getAbsolutePath());
			}
		}
		return result;
	}
	
}
