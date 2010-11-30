package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.net.URI;
import java.util.HashSet;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictIO;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.IChemRecord;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.MutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.NameOnlyChemRecord;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**
 * Dictionary with polymer entity related names. Being rather ill-defined,
 * names are not associated with InChIs or SMILESes.
 *
 * @author egonw
 */
public class PolymerDictionary
extends MutableChemNameDict
implements IChemNameDict {

	public PolymerDictionary() throws Exception {
		super(new URI("http://wwmm.ch.cam.ac.uk/dictionary/polymer/"));
		ChemNameDictIO.readXML(
			new ResourceGetter("uk/ac/cam/ch/wwmm/oscar/chemnamedict/")
				.getXMLDocument("polymerCompounds.xml"),
			this
		);
	}

	public void addName(String name) throws Exception {
		if(name == null || name.trim().length() == 0) throw new Exception();
		name = StringTools.normaliseName(name);
		IChemRecord record = new NameOnlyChemRecord();
		record.addName(name);
		addChemRecord(record);
	}

    public void addChemRecord(IChemRecord record) throws Exception {
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
