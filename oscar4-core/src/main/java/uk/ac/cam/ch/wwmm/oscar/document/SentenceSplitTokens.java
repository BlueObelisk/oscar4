package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author egonw
 *
 * @see NonSentenceEndings
 */
public class SentenceSplitTokens {

	private final Set<String> splitTokens =
		new HashSet<String>(Arrays.asList(
			".",
		    "?",
		    "!",
		    "\""
		));

	/**
	 * Returns true if the token is recognized as a token splitting a sentence.
	 */
	public boolean contains(String token) {
		return splitTokens.contains(token);
	}
}
