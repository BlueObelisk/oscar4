package uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;

/**
 * A class to link a {@link NamedEntity} with its corresponding
 * chemical structures.
 * 
 * @author dmj30
 *
 */
public class ResolvedNamedEntity {
	
	private NamedEntity namedEntity;
	private List<ChemicalStructure> structures;
	
	public ResolvedNamedEntity(NamedEntity ne, List<ChemicalStructure> structures) {
		this.namedEntity = ne;
		this.structures = Collections.unmodifiableList(
				new ArrayList<ChemicalStructure>(structures));
	}

	public NamedEntity getNamedEntity() {
		return namedEntity;
	}
	
	public List<ChemicalStructure> getChemicalStructures() {
		return structures;
	}

	
	/**
	 * Returns the full set of {@link ChemicalStructure}s of the given
	 * {@link FormatType}
	 */
	public List<ChemicalStructure> getChemicalStructures(FormatType type) {
		List <ChemicalStructure> typedStructures = new ArrayList<ChemicalStructure>();
		for (ChemicalStructure structure : structures) {
			if (structure.getType() == type) {
				typedStructures.add(structure);
			}
		}
		return typedStructures;
	}

	/**
	 * Returns the first {@link ChemicalStructure} of the given
	 * {@link FormatType}
	 * 
	 */
	public ChemicalStructure getFirstChemicalStructure(FormatType type) {
		for (ChemicalStructure structure : structures) {
			if (structure.getType() == type) {
				return structure;
			}
		}
		return null;
	}
	
	
	
}
