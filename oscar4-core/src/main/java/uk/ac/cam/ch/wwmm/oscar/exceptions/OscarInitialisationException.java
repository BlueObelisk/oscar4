package uk.ac.cam.ch.wwmm.oscar.exceptions;


/**
 * Indicates that an OSCAR module failed to load its resources.
 * Consequently, OSCAR could not be correctly initialised. 
 * 
 * @author dmj30
 *
 */
public class OscarInitialisationException extends RuntimeException {
	
	/**
	 * Creates a new ResourceInitialisationException with the specified message
	 * 
	 * @param message
	 */
	public OscarInitialisationException(String message) {
		super(message);
	}

	/**
	 * Creates a new ResourceInitialisationException with the specified message
	 * and cause
	 * 
	 * @param message
	 * @param cause
	 */
	public OscarInitialisationException(String message, Exception cause) {
		super(message, cause);
	}

}
