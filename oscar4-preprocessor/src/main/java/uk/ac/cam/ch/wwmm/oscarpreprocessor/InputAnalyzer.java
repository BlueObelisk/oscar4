package uk.ac.cam.ch.wwmm.oscarpreprocessor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.ibm.icu.text.CharsetDetector;

/**
 * Analyzes the input and determines the character encoding and format type
 * of the input.
 *
 * @author egonw
 */
public class InputAnalyzer {

	/**
	 * Convenience method that uses ICU4J to detect the character encoding
	 * of the given {@link InputStream}.
	 *
	 * @param stream       {@link InputStream} to detect the encoding for
	 * @return             a String representation of the encoding
	 * @throws IOException exception when the IO on the stream failed
	 */
	public static String guessEncoding(InputStream stream) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(stream);
		CharsetDetector cd = new CharsetDetector();
		cd.setText(bis);
		return cd.detect().getName();
	}

	public static String guessFormat(InputStream stream) {
		throw new UnsupportedOperationException(
			"Not implemented yet"
		);
	}

}
