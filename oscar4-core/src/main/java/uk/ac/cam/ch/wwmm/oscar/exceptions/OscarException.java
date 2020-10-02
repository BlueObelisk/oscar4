package uk.ac.cam.ch.wwmm.oscar.exceptions;


/**
 * 
 * The parent class of exceptions thrown by OSCAR4.
 * 
 * @author dmj30
 *
 */
public class OscarException extends Exception {
	
	/**
	 * Creates a new OscarException with the specified message
	 * 
	 * @param message The message of the exception.
	 */
	public OscarException(String message) {
		super(message);
	}

	/**
	 * Creates a new OscarException with the specified message and cause
	 * 
	 * @param message The message of the exception.
	 * @param cause The cause of the exception.
	 */
	public OscarException(String message, Exception cause) {
		super(message, cause);
	}

}
