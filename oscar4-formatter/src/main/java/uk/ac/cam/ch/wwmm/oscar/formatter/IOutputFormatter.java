package uk.ac.cam.ch.wwmm.oscar.formatter;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;

public interface IOutputFormatter {

	public void write(NamedEntity entity, String inchi);

}
