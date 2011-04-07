package uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ImmutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

/**
 * A dictionary of chemical names derived from ChEBI. 
 *
 */
public class ChEBIDictionary extends ImmutableChemNameDict {

	private static final URI CHEBI_DICTIONARY_URL;
	private static final ResourceGetter rg = new ResourceGetter(
			ChEBIDictionary.class.getClassLoader(),"uk/ac/cam/ch/wwmm/oscar/chemnamedict/");
	
	private static ChEBIDictionary instance;
	
	static {
		try {
			CHEBI_DICTIONARY_URL = new URI("http://wwmm.ch.cam.ac.uk/dictionary/chebi/");
		}
		catch (URISyntaxException e) {
			// Should not be thrown, as URL is valid.
			throw new RuntimeException(e);
		}
	}

	public static synchronized ChEBIDictionary getInstance() {
		if (instance == null) {
			try {
				instance = new ChEBIDictionary();
			} catch (DataFormatException e) {
				throw new OscarInitialisationException("failed to load ChEBI dictionary", e);
			} catch (FileNotFoundException e) {
				throw new OscarInitialisationException("failed to load ChEBI dictionary", e);
			}
		}
		return instance;
	}
	
	
	private ChEBIDictionary() throws DataFormatException, FileNotFoundException {
		super(CHEBI_DICTIONARY_URL, Locale.ENGLISH, rg.getStream("chemnamedict.xml"));
	}
}
