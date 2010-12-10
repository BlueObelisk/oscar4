package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.IChemRecord;

public interface IMutableChemNameDict extends IChemNameDict {

	public void addStopWord(String word);

	public void addChemRecord(String inchi, String smiles,
			Set<String> names, Set<String> ontIDs);

	public void addChemRecord(IChemRecord record);

	public void addName(String name);

	public void addChemical(String name, String smiles, String inchi);

	public void importChemNameDict(IChemNameDict cnd);
}