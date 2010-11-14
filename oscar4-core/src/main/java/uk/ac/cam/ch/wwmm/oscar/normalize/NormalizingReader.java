package uk.ac.cam.ch.wwmm.oscar.normalize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NormalizingReader {

	private ITextNormalizer normalizer;

	private BufferedReader source;
	String currentReplacement;

	public NormalizingReader(InputStream stream) {
		this.source = new BufferedReader(
			new InputStreamReader(stream)
		);
		currentReplacement = null;
		normalizer = Normalizer.getDefaultInstance();
	}

	public String readLine() throws IOException {
		return normalizer.normalize(source.readLine());
	}

}
