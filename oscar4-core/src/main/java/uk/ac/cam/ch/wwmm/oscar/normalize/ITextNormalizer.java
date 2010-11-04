package uk.ac.cam.ch.wwmm.oscar.normalize;

public interface ITextNormalizer {

	public String normalize(String string);

	/**
	 * Returns an expanded version of the character, or null if there is no
	 * expansion.
	 *
	 * @param character
	 * @return
	 */
	public String normalize(char character);

}
