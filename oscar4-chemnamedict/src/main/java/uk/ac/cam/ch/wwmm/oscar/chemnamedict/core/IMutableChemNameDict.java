package uk.ac.cam.ch.wwmm.oscar.chemnamedict.core;

import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.records.IChemRecord;

public interface IMutableChemNameDict extends IChemNameDict {

	public void addStopWord(String word);

	public void addChemRecord(String stdInchi, String smiles,
			Set<String> names, Set<String> ontIDs);

	public void addChemRecord(IChemRecord record);

	public void addName(String name);

	public void addChemical(String name, String smiles, String stdInchi);

	public void importChemNameDict(IChemNameDict cnd);
}