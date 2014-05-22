package uk.ac.cam.ch.wwmm.oscar.exceptions;


/**
 * 
 * Indicates that a resource file contained an unrecoverable
 * formatting error.
 * 
 * @author dmj30
 *
 */
public class DataFormatException extends OscarException {

	/**
	 * Creates a new DataFormatException with the specified message
	 * 
	 * @param message
	 */
	public DataFormatException(String message) {
		super(message);
	}

	public DataFormatException(String message, Exception e) {
		super(message, e);
	}

}
