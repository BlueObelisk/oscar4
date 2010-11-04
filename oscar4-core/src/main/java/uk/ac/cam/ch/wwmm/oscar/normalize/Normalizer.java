package uk.ac.cam.ch.wwmm.oscar.normalize;

import java.util.HashMap;
import java.util.Map;

public class Normalizer implements ITextNormalizer {

	private Map<Character,String> normals;

	private static Normalizer myInstance = null;

	@SuppressWarnings("serial")
	private Normalizer() {
		normals = new HashMap<Character, String>() {{
			put('\uFB00',"ff");
			put('\uFB01',"fi");
			put('\uFB02',"fl");
			put('\u2010',"-");
			put('\u2011',"-");
			put('\u2012',"-");
			put('\u2013',"-");
			put('\u2014',"-");
			put('\u03b1',"alpha");
			put('\u03b2',"beta");
			put('\u03b3',"gamma");
		}};
	}

	public static Normalizer getInstance() {
		if (myInstance == null) {
			myInstance = new Normalizer();
		}
		return myInstance;
	}

	public String normalize(String string) {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<string.length(); i++) {
			Character character = string.charAt(i);
			if (normals.containsKey(character)) {
				builder.append(normals.get(character));
			} else {
				builder.append(character);
			}
		}
		return builder.toString();
	}

	public String normalize(char character) {
		if (normals.containsKey(character)) {
			return normals.get(character);
		}
		return null;
	}

}
