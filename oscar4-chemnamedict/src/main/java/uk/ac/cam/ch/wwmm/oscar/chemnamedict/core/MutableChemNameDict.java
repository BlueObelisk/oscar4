package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.net.URI;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.ChemRecord;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.IChemRecord;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.IOntologyChemRecord;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.ISMILESChemRecord;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.IStdInChIChemRecord;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**
 * Extension of {@link ImmutableChemNameDict} that can be mutated.
 *
 * @author egonw
 * @author ptc
 */
public class MutableChemNameDict extends ImmutableChemNameDict implements IMutableChemNameDict {

	public MutableChemNameDict(URI uri, Locale language) {
		super(uri, language);
	}

	public void addStopWord(String word) {
		if (StringUtils.isEmpty(word)) {
			throw new IllegalArgumentException("The word cannot be null or empty, but was '" + String.valueOf(word)
						+ "'");
		}

		stopWords.add(StringTools.normaliseName(word));
	}

	public void addChemRecord(String stdInchi, String smiles, Set<String> names,
			Set<String> ontIDs) {
		ChemRecord record = new ChemRecord();
		record.setStdInChI(stdInchi);
		record.setSMILES(smiles);
		if(names != null) record.addNames(names);
		if(ontIDs != null) record.addOntologyIdentifiers(ontIDs);
		addChemRecord(record);
	}

	public void addChemRecord(IChemRecord record) {
		IChemRecord recordToAdd = null;
		// check if we know this compound already (by Standard InChI)
		if (record instanceof IStdInChIChemRecord) {
			IStdInChIChemRecord stdInchiRecord = (IStdInChIChemRecord)record;
			String stdInchi = stdInchiRecord.getStdInChI();
			if(stdInchi != null && indexByStdInchi.containsKey(stdInchi)) {
				recordToAdd = indexByStdInchi.get(stdInchi);
			}
		}
		if (recordToAdd == null) { // we do not know it yet
			recordToAdd = record;
		} else {
			mergeRecords(recordToAdd, record);
		}
		chemRecords.add(recordToAdd);
		addToIndices(recordToAdd);
	}

	/**
	 * Adds the record to the various indices.
	 * 
	 * @param recordToAdd
	 *            the {@link IChemRecord} to add to the indices.
	 */
	private void addToIndices(IChemRecord recordToAdd) {
		// add the names to the index
		for (String name : recordToAdd.getNames()) {
			name = StringTools.normaliseName(name);
			if (!indexByName.containsKey(name)) {
				indexByName.put(name, new HashSet<IChemRecord>());
			}
			indexByName.get(name).add(recordToAdd);
			orphanNames.remove(name);
		}
		if (recordToAdd instanceof IOntologyChemRecord) {
			// add the ontology identifiers to the index
			IOntologyChemRecord ontoRecord = (IOntologyChemRecord) recordToAdd;
			for (String ontID : ontoRecord.getOntologyIdentifiers()) {
				if (!indexByOntID.containsKey(ontID)) {
					indexByOntID.put(ontID, new HashSet<IChemRecord>());
				}
				indexByOntID.get(ontID).add(recordToAdd);
			}
		}

		if (recordToAdd instanceof IStdInChIChemRecord) {
			String stdInChi = ((IStdInChIChemRecord) recordToAdd).getStdInChI();
			if (stdInChi != null) {
				indexByStdInchi.put(stdInChi, recordToAdd);
			}
		}

	}

	/**
	 * Merges the information from the second record into the first.
	 *
	 * @param sourceRecord {@link IChemRecord} from which information is extracted
	 * @param mergeRecord {@link IChemRecord} into which everything is merged
	 */
	private void mergeRecords(IChemRecord mergeRecord, IChemRecord sourceRecord) {
		for(String name : sourceRecord.getNames()) {
			name = StringTools.normaliseName(name);
			mergeRecord.addName(name);
		}
		if (sourceRecord instanceof IOntologyChemRecord &&
			mergeRecord instanceof IOntologyChemRecord) {
			for(String ontID : ((IOntologyChemRecord)sourceRecord).getOntologyIdentifiers()) {
				((IOntologyChemRecord)mergeRecord).addOntologyIdentifier(ontID);
			}
		}
		if (sourceRecord instanceof ISMILESChemRecord &&
			mergeRecord instanceof ISMILESChemRecord) {
			if (((ISMILESChemRecord)mergeRecord).getSMILES() != null) {
				// keep the original
			} else {
				((ISMILESChemRecord)mergeRecord).setSMILES(
					((ISMILESChemRecord)sourceRecord).getSMILES()
				);
			}
		}
	}

	public void addName(String name) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("The name cannot be null or empty, but was '" + String.valueOf(name)
						+ "'");
		}

		name = StringTools.normaliseName(name);
		addChemical(name, null, null);
	}

	public void addChemical(String name, String smiles, String stdInchi) {
		ChemRecord record = new ChemRecord();
		record.setStdInChI(stdInchi);
		record.setSMILES(smiles);
		record.addName(name);
		addChemRecord(record);
	}

	public void importChemNameDict(IChemNameDict cnd) {
		for(IChemRecord record : cnd.getChemRecords()) {
			addChemRecord(record);
		}
	}
}
