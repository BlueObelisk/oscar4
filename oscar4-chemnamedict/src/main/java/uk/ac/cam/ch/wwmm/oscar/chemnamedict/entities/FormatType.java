package uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities;

public enum FormatType {

	SMILES,
	/**
     * @deprecated Please use {@link #STD_INCHI} instead.
     */
	@Deprecated
	INCHI,
	STD_INCHI,
	STD_INCHI_KEY,
	CML
		
}
