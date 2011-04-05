package uk.ac.cam.ch.wwmm.oscartokeniser;

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
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.scixml.XMLStrings;

/**Takes a list of SciXML files, and provides an iterator over the 
 * TokenSequences corresponding to the usable parts of those documents.
 * 
 * @author ptc24
 *
 */
//TODO this class doesn't seem to be in use - is it still needed?
public final class TokenSequenceSource implements Iterable<TokenSequence>, Iterator<TokenSequence> {
	
	private List<File> files;
	private int filePointer;
	private LinkedList<TokenSequence> tokSeqs;
	private TokenSequence nextTokeniser;
	private boolean simple;
	
	/**Make a TokenSequenceSource from the list of files.
	 * 
	 * @param files The files to use.
	 */
	public TokenSequenceSource(Collection<File> files) {
		this(files, false);
	}
	
	/**Make a TokenSequenceSource from the list of files.
	 * 
	 * @param files The files to use.
	 * @param simple If true, do not use information from the source XML 
	 * (apart from the string to tokenise) when tokenising.
	 */
	public TokenSequenceSource(Collection<File> files, boolean simple) {
		this.simple = simple;
		this.files = new ArrayList<File>(files);
		filePointer = 0;
		tokSeqs = new LinkedList<TokenSequence>();
		nextTokeniser = null;
	}
	
	
	/**Rewinds the TokenSequenceSource back to the start.
	 * 
	 */
	public void reset() {
		filePointer = 0;
		tokSeqs = new LinkedList<TokenSequence>();
		nextTokeniser = null;
	}
	
	private void primeNextTokeniser() {
		if(nextTokeniser != null) return;
		while(tokSeqs.size() == 0) {
			if(filePointer == files.size()) return;
			try {
				File f = files.get(filePointer).getParentFile();
				Document doc = new Builder().build(new File(f, "source.xml"));
				
				if(simple) {
					Nodes placesForChemicals = XMLStrings.getDefaultInstance().getChemicalPlaces(doc);
					Tokeniser tokeniser = Tokeniser.getDefaultInstance();
					for (int i = 0; i < placesForChemicals.size(); i++) {
						Element e = (Element)placesForChemicals.get(i);
						String text = e.getValue();
						TokenSequence ts = tokeniser.tokenise(text);
						tokSeqs.add(ts);
					}
				} else {
					Document safDoc = null;
					if(!files.get(filePointer).getName().endsWith("source.xml")) safDoc = new Builder().build(new File(f, "saf.xml"));
					IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
							Tokeniser.getDefaultInstance(), doc, true, true, false, safDoc);
					if(safDoc != null) {
						tokSeqs.addAll(procDoc.getTokenSequences());
					} else {
						tokSeqs.addAll(procDoc.getTokenSequences());
					}					
				}
				filePointer++;
			} catch (Exception e) {
				e.printStackTrace();
				tokSeqs.clear();
			}
		} 
		nextTokeniser = tokSeqs.poll();
	}
	
	/**Does this StringSource have more strings to offer?
	 * 
	 */
	public boolean hasNext() {
		primeNextTokeniser();
		return nextTokeniser != null;
	}
	
	/**Get the next string from this StringSource.
	 * 
	 */
	public TokenSequence next() {
		primeNextTokeniser();
		TokenSequence t = nextTokeniser;
		nextTokeniser = null;
		return t;
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
	public Iterator<TokenSequence> iterator() {
		reset();
		return this;
	}
	
	/**Returns the file that the current TokenSequence was taken from.
	 * 
	 * @return The file that the current TokenSequence was taken from.
	 */
	public File getCurrentFile() {
		return files.get(filePointer-1);
	}
	
}
