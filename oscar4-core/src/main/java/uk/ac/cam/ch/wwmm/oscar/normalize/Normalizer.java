package uk.ac.cam.ch.wwmm.oscar.normalize;

import java.util.HashMap;
import java.util.Map;

/**
 * Normalizer class that normalizes text. It normalizes the text in two
 * steps: the first step is to normalize characters (e.g. all hyphens into
 * one specific hyphen); the second step is to replace strings by there
 * prefered string (e.g. alpha into its greek character equivalent).
 *
 * @author egonw
 */
public class Normalizer implements ITextNormalizer {

	/**
	 * Map that describes how certain characters (keys) are to be
	 * normalized into {@link String}s (values).
	 */
	private Map<Character,String> normalizedCharacters;

	/**
	 * Map that contains {@link String} (keys) that are normalized into
	 * other {@link String}s (values).
	 */
	private Map<String,String> normalizedStrings;

	private static Normalizer myInstance = null;

	@SuppressWarnings("serial")
	private Normalizer() {
		normalizedCharacters = new HashMap<Character, String>() {{
            put('\u00C6',"ae");//common ligatures
            put('\u00E6',"ae");
            put('\u0152',"oe");
            put('\u0153',"oe");
            put('\u0132',"ij");
            put('\u0133',"ij");
            put('\u1D6B',"ue");
            put('\uFB00',"ff");
            put('\uFB01',"fi");
            put('\uFB02',"fl");
            put('\uFB03',"ffi");
            put('\uFB04',"ffl");
            put('\uFB06',"st");
			put('\u2010',"-");
			put('\u2011',"-");
			put('\u2012',"-");
			put('\u2013',"-");
			put('\u2014',"-");
			put('\u2015',"-");
	        put('\uFEFF',"");//BOM-found at the start of some UTF files
	        put('\uFEFF',"");//BOM-found at the start of some UTF files
		}};
		// FIXME: the below feels wrong... we dunno even if they are tokens!!!
		// E.g. "pi-bonding", versus "pipe"
		// So, appended a '-' behind them for now...
		normalizedStrings = new HashMap<String, String>() {{
            put("alpha-", "\u03b1-"); //greeks
            put("beta-", "\u03b2-");
            put("gamma-", "\u03b3-");
            put("delta-", "\u03b4-");
            put("epsilon-", "\u03b5-");
            put("zeta-", "\u03b6-");
            put("eta-", "\u03b7-");
            put("theta-", "\u03b8-");
            put("iota-", "\u03b9-");
            put("kappa-", "\u03ba-");
            put("lambda-", "\u03bb-");
            put("mu-", "\u03bc-");
            put("nu-", "\u03bd-");
            put("xi-", "\u03be-");
            put("omicron-", "\u03bf-");
            put("pi-", "\u03c0-");
            put("rho-", "\u03c1-");
            put("stigma-", "\u03c2-");
            put("sigma-", "\u03c3-");
            put("tau-", "\u03c4-");
            put("upsilon-", "\u03c5-");
            put("phi-", "\u03c6-");
            put("chi-", "\u03c7-");
            put("psi-", "\u03c8-");
            put("omega-", "\u03c9-");
		}};
	}

	public static Normalizer getInstance() {
		if (myInstance == null) {
			myInstance = new Normalizer();
		}
		return myInstance;
	}

	/** {@inheritDoc} */
	public String normalize(String string) {
		// step 1: normalize characters
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<string.length(); i++) {
			String character = normalize(string.charAt(i));
			if (character == null) {
				builder.append(string.charAt(i));
			} else {
				builder.append(character);
			}
		}
		string = builder.toString();

		// step 2: normalize strings
		for (String normalizableString : normalizedStrings.keySet()) {
			while (string.contains(normalizableString)) {
				System.out.println("Replacing normalizableString...");
				string = string.replace(
					normalizableString,
					normalizedStrings.get(normalizableString)
				);
				System.out.println("New string: " + string);
			}
		}

		return string;
	}

	/** {@inheritDoc} */
	public String normalize(char character) {
		if (normalizedCharacters.containsKey(character)) {
			return normalizedCharacters.get(character);
		}
		return null;
	}

	/** {@inheritDoc} */
	public String normalizable(String string) {
		if (normalizedStrings.containsKey(string)) {
			return normalizedCharacters.get(string);
		}
		return null;
	}

}
