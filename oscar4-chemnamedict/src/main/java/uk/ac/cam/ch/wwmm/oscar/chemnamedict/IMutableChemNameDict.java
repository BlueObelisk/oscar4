package uk.ac.cam.ch.wwmm.oscar.chemnamedict;

import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.ChemRecord;

public interface IMutableChemNameDict extends IChemNameDict {

	public void addStopWord(String word) throws Exception;

	public void addChemRecord(String inchi, String smiles,
			Set<String> names, Set<String> ontIDs) throws Exception;

	public void addChemRecord(ChemRecord record) throws Exception;

	public void addName(String name) throws Exception;

	public void addChemical(String name, String smiles, String inchi)
			throws Exception;

	public void importChemNameDict(ChemNameDict cnd) throws Exception;

}