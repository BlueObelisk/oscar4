package uk.ac.cam.ch.wwmm.oscarMEMM.memm.document;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;

/**Takes a list of SciXML files, and provides an iterator over the strings
 * corresponding to the usable parts of those documents.
 * 
 * @author ptc24
 *
 */
//TODO this class doesn't seem to be in use, and is duplicated in uk.ac.cam.ch.wwmm.oscarpattern.document - is it still needed?
public final class StringSource implements Iterable<String>, Iterator<String> {
	
	private List<File> files;
	private int filePointer;
	private LinkedList<String> strings;
	private String nextString;
	private boolean bundleNEs;
	
	/**Make a StringSource from the list of files.
	 * 
	 * @param files The files to use.
	 */
	public StringSource(Collection<File> files) {
		this(files, false);
	}

	/**Make a StringSource from the list of files.
	 * 
	 * @param files The files to use.
	 * @param bundleNEs Whether to replace whitespace with underscore
	 * characters within named entities.
	 */
	public StringSource(Collection<File> files, boolean bundleNEs) {
		this.bundleNEs = bundleNEs;
		this.files = new ArrayList<File>(files);
		filePointer = 0;
		strings = new LinkedList<String>();
		nextString = null;
	}
	
	/**Rewinds the StringSource back to the start.
	 * 
	 */
	public void reset() {
		filePointer = 0;
		strings = new LinkedList<String>();
		nextString = null;
	}
	
	private void primeNextString() {
		if(nextString != null) return;
		while(strings.size() == 0) {
			if(filePointer == files.size()) return;
			try {
				Document doc = new Builder().build(files.get(filePointer++));
				
				
				Nodes n;
				
				if(bundleNEs) {
					n = doc.query("//ne");
					for (int i = 0; i < n.size(); i++) {
						String s = n.get(i).getValue();
						s = s.replaceAll("\\s+", "_");
						Element e = (Element)n.get(i);
						while(e.getChildCount() > 0) e.getChild(0).detach();
						e.appendChild(s);
					}
				}
				
				n = XMLStrings.getInstance().getChemicalPlaces(doc);
				for (int i = 0; i < n.size(); i++) {
					String s = n.get(i).getValue();
					if(s != null) strings.add(s);
				}
			} catch (Exception e) {
				strings.clear();
			}
		} 
		nextString = strings.poll();
	}
	
	/**Does this StringSource have more strings to offer?
	 * 
	 */
	public boolean hasNext() {
		primeNextString();
		return nextString != null;
	}
	
	/**Get the next string from this StringSource.
	 * 
	 */
	public String next() {
		primeNextString();
		String s = nextString;
		nextString = null;
		return s;
	}
	
	/**Unsupported.
	 * 
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	/**Returns the StringSource itself for use as an iterator, resetting itself
	 * in the process.
	 */
	public Iterator<String> iterator() {
		reset();
		// TODO Auto-generated method stub
		return this;
	}
	
}
