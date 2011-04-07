package uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Locale;

import nu.xom.Document;
import nu.xom.ParsingException;

import org.apache.commons.lang.StringUtils;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictIO;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.IChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.MutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.IChemRecord;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.NameOnlyChemRecord;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**
 * Dictionary with polymer entity related names. Being rather ill-defined,
 * names are not associated with InChIs or SMILES strings.
 *
 * @author egonw
 */
public class PolymerDictionary extends MutableChemNameDict implements IChemNameDict {

	private static final URI POLYMER_DICTIONARY_URL;

	static {
		try {
			POLYMER_DICTIONARY_URL = new URI("http://wwmm.ch.cam.ac.uk/dictionary/polymer/");
		}
		catch (URISyntaxException e) {
			// Should not be thrown, as URL is valid.
			throw new RuntimeException(e);
		}
	}

	public PolymerDictionary() {
		super(POLYMER_DICTIONARY_URL, Locale.ENGLISH);

		ResourceGetter rg = new ResourceGetter(PolymerDictionary.class.getClassLoader(),"uk/ac/cam/ch/wwmm/oscar/chemnamedict/");
		Document sourceDoc;
		try {
			sourceDoc = rg.getXMLDocument("polymerCompounds.xml");
		} catch (ParsingException e) {
			throw new OscarInitialisationException("failed to load polymer dictionary");
		} catch (IOException e) {
			throw new OscarInitialisationException("failed to load polymer dictionary");
		}
		ChemNameDictIO.readXML(sourceDoc, this);
	}

	@Override
	public void addName(String name) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("The name cannot be null or empty, but was '" + String.valueOf(name)
						+ "'");
		}

		name = StringTools.normaliseName(name);
		IChemRecord record = new NameOnlyChemRecord();
		record.addName(name);
		addChemRecord(record);
	}

    @Override
	public void addChemRecord(IChemRecord record) {
		// Record is new. Add and index
		chemRecords.add(record);
		for(String name : record.getNames()) {
			name = StringTools.normaliseName(name);
			if(!indexByName.containsKey(name)) {
				indexByName.put(name, new HashSet<IChemRecord>());
			}
			indexByName.get(name).add(record);
			orphanNames.remove(name);
		}
	}
}
