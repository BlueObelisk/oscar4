package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that defines phrases that are commonly followed by a period but that do not
 * indicate the end of a sentence.
 *
 * @author ptc24
 * @author egonw
 */
public class NonSentenceEndings {

	// Extracted from SentenceFinder.

	@SuppressWarnings("serial")
	private final Set<String> impossibleAfter = new HashSet<String>(Arrays.asList(
		"Fig",
		"al", // et al.
		"i.e",
		"ie",
		"eg",
		"e.g",
		"ref",
		"Dr",
		"Prof",
		"Sir"
	));

	/**
	 * Returns true if the sentence is not ended by the period following the given phrase.
	 *
	 * @param  phrase
	 * @return true, if the sentence does not end after the phrase
	 */
	public boolean contains(String phrase) {
		return impossibleAfter.contains(phrase);
	}

	// TODO: I guess functionality can be added later to interactive add entries, by adding a add(String) method

}
