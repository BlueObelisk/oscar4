package uk.ac.cam.ch.wwmm.oscar.chemnamedict.data;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IInChIProvider;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.IMutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ISMILESProvider;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**
 * Extension of {@link ImmutableChemNameDict} that can be mutated.
 *
 * @author egonw
 * @author ptc
 */
public class MutableChemNameDict extends ImmutableChemNameDict
implements IMutableChemNameDict, IInChIProvider, ISMILESProvider {

	public MutableChemNameDict(URI uri) {
		super(uri);
	}

	public void addStopWord(String word) throws Exception {
		if(word == null || word.trim().length() == 0) throw new Exception();
		stopWords.add(StringTools.normaliseName(word));
	}

	public void addChemRecord(String inchi, String smiles, Set<String> names,
			Set<String> ontIDs) throws Exception {
		ChemRecord record = new ChemRecord();
		record.inchi = inchi;
		record.smiles = smiles;
		if(names != null) record.names.addAll(names);
		if(ontIDs != null) record.ontIDs.addAll(ontIDs);
		addChemRecord(record);
	}

	public void addChemRecord(ChemRecord record) throws Exception {
			String inchi = record.inchi;
			if(inchi != null && indexByInchi.containsKey(inchi)) {
				ChemRecord mergeRecord = indexByInchi.get(inchi);
				for(String name : record.names) {
					name = StringTools.normaliseName(name);
					mergeRecord.names.add(name);
					if(!indexByName.containsKey(name)) {
						indexByName.put(name, new HashSet<ChemRecord>());
					}
					indexByName.get(name).add(mergeRecord);
					orphanNames.remove(name);
				}
				for(String ontID : record.ontIDs) {
					mergeRecord.ontIDs.add(ontID);
					if(!indexByOntID.containsKey(ontID)) {
						indexByOntID.put(ontID, new HashSet<ChemRecord>());
					}
					indexByOntID.get(ontID).add(mergeRecord);
				}
				if(record.smiles != null && mergeRecord.smiles == null)
					mergeRecord.smiles = record.smiles;
			} else {
				// Record is new. Add and index
				chemRecords.add(record);
				indexByInchi.put(inchi, record);
				for(String name : record.names) {
					name = StringTools.normaliseName(name);
					if(!indexByName.containsKey(name)) {
						indexByName.put(name, new HashSet<ChemRecord>());
					}
					indexByName.get(name).add(record);
					orphanNames.remove(name);
				}
				for(String ontID : record.ontIDs) {
					if(!indexByOntID.containsKey(ontID)) {
						indexByOntID.put(ontID, new HashSet<ChemRecord>());
					}
					indexByOntID.get(ontID).add(record);
				}
			}
	}

	public void addName(String name) throws Exception {
		if(name == null || name.trim().length() == 0) throw new Exception();
		name = StringTools.normaliseName(name);
		addChemical(name, null, null);
	}

	public void addChemical(String name, String smiles, String inchi)
			throws Exception {
		ChemRecord record = new ChemRecord();
		record.inchi = inchi;
		record.smiles = smiles;
		record.names.add(name);
		addChemRecord(record);
	}

	public void importChemNameDict(IChemNameDict cnd) throws Exception {
		for(ChemRecord record : cnd.getChemRecords()) {
			addChemRecord(record);
		}
	}

}
