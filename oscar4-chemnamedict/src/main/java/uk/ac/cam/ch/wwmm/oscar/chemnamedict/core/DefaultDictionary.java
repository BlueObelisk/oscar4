package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.net.URI;
import java.net.URISyntaxException;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictIO;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.MutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;

public class DefaultDictionary extends MutableChemNameDict {

	private static final URI	DEFAULT_DICTIONARY_URL;

	static {
		try {
			DEFAULT_DICTIONARY_URL = new URI("http://wwmm.ch.cam.ac.uk/dictionary/default/");
		}
		catch (URISyntaxException e) {
			// Should not be thrown, as URL is valid.
			throw new RuntimeException(e);
		}
	}

	public DefaultDictionary() {
		super(DEFAULT_DICTIONARY_URL);

		ChemNameDictIO.readXML(
					new ResourceGetter("uk/ac/cam/ch/wwmm/oscar/chemnamedict/").getXMLDocument("defaultCompounds.xml"),
					this);
	}
}
