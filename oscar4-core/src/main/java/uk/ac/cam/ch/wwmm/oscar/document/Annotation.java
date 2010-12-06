package uk.ac.cam.ch.wwmm.oscar.document;

import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;


/**
 * An annotation in an OSCAR3 document, either a named entity or a piece of data.
 * 
 * @author dmj30
 *
 */
public interface Annotation {

	/**
	 * @return the integer position of the end of the annotation
	 */
	int getEnd();
	
	/**
	 * @return the integer position of the start of the annotation
	 */
	int getStart();
	
	/**
	 * @return the string to which the annotation applies
	 */
	String getSurface();
	
	/**
	 * @return the type of the annotation, e.g. CM/RN/DATA
	 */
	NamedEntityType getType();
	
}
