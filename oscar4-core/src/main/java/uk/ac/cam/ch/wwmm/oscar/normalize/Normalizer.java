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
