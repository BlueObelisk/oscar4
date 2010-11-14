package uk.ac.cam.ch.wwmm.oscar.normalize;

public interface ITextNormalizer {

	/**
	 * String of text which is to be normalized, which can contain one or
	 * more characters and or strings to be normalized.
	 *
	 * @param string Text to be normalized.
	 * @return       The normalized text.
	 */
	public String normalize(String string);

	/**
	 * Returns an expanded version of the character, or null if there is no
	 * expansion.
	 *
	 * @param character
	 * @return
	 */
	public String normalize(char character);

	/**
	 * Returns an expanded version of the {@link String}, or null if there is
	 * no expansion.
	 *
	 * @param string String to be normalized.
	 * @return
	 */
	public String normalizable(String string);

}
