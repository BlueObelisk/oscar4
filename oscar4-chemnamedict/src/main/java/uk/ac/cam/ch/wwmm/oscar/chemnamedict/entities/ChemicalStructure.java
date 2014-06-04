package uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities;

import java.net.URI;

/**
 * A class to hold a chemical structure of a given {@link FormatType},
 * e.g. InChI, standard InChI, standard InChI key or SMILES.
 * 
 * @author dmj30
 * 
 */
public class ChemicalStructure {

	private String value;
	private FormatType type;
	private URI source;

	
	/**
	 * Creates a new record of a chemical structure.
	 * 
	 * @param value the value to be recorded, e.g. "CCC"
	 * @param type the format in which the value is encoded, e.g. SMILES
	 * @param source the URI for the dictionary from which the value
	 * was retrieved
	 */
	public ChemicalStructure(String value, FormatType type, URI source) {
		this.value = value;
		this.type = type;
		this.source = source;
	}

	
	public URI getSource() {
		return source;
	}

	public FormatType getType() {
		return type;
	}

	public String getValue() {
		return value;
	}


    @Override
    public String toString() {
        return "[Structure:"+getType()+":"+getValue()+"]";
    }

}
