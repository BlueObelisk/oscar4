package uk.ac.cam.ch.wwmm.oscar.exceptions;


/**
 * Indicates that an OSCAR module failed to load its resources.
 * Consequently, OSCAR could not be correctly initialised. 
 * 
 * @author dmj30
 *
 */
public class ResourceInitialisationException extends RuntimeException {
	
	/**
	 * Creates a new ResourceInitialisationException with the specified message
	 * 
	 * @param message
	 */
	public ResourceInitialisationException(String message) {
		super(message);
	}

	/**
	 * Creates a new ResourceInitialisationException with the specified message
	 * and cause
	 * 
	 * @param message
	 * @param cause
	 */
	public ResourceInitialisationException(String message, Exception cause) {
		super(message, cause);
	}

}
