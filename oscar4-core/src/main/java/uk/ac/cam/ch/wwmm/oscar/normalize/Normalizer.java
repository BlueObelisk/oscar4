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

    private static Normalizer defaultInstance = null;

    private Normalizer() {
        normalizedCharacters = new HashMap<Character, String>();
        normalizedCharacters.put('\u00C6',"ae");//common ligatures
        normalizedCharacters.put('\u00E6',"ae");
        normalizedCharacters.put('\u0152',"oe");
        normalizedCharacters.put('\u0153',"oe");
        normalizedCharacters.put('\u0132',"ij");
        normalizedCharacters.put('\u0133',"ij");
        normalizedCharacters.put('\u1D6B',"ue");
        normalizedCharacters.put('\uFB00',"ff");
        normalizedCharacters.put('\uFB01',"fi");
        normalizedCharacters.put('\uFB02',"fl");
        normalizedCharacters.put('\uFB03',"ffi");
        normalizedCharacters.put('\uFB04',"ffl");
        normalizedCharacters.put('\uFB06',"st");
        normalizedCharacters.put('\u2010',"-");
        normalizedCharacters.put('\u2011',"-");
        normalizedCharacters.put('\u2012',"-");
        normalizedCharacters.put('\u2013',"-");
        normalizedCharacters.put('\u2014',"-");
        normalizedCharacters.put('\u2015',"-");
        normalizedCharacters.put('\uFEFF',"");//BOM-found at the start of some UTF files
        normalizedCharacters.put('\uFEFF',"");//BOM-found at the start of some UTF files
        // punctuation
        normalizedCharacters.put('\u2047',"??");
        normalizedCharacters.put('\uFE56',"?");
        normalizedCharacters.put('\u2048',"?!");
        normalizedCharacters.put('\u2049',"!?");
        normalizedCharacters.put('\u203D',"?!");
        normalizedCharacters.put('\u01C3',"!");
        normalizedCharacters.put('\uA71D',"!");
        normalizedCharacters.put('\u203C',"!!");
        normalizedCharacters.put('\uFE57',"!");
        normalizedCharacters.put('\uFF01',"!");
        normalizedCharacters.put('\u2018',"'");
        normalizedCharacters.put('\u2019',"'");
        normalizedCharacters.put('\u2032',"'");
        normalizedCharacters.put('\u2033',"''");
        normalizedCharacters.put('\u2033',"''");
        normalizedCharacters.put('\u201C',"\"");
        normalizedCharacters.put('\u201D',"\"");

        // FIXME: the below feels wrong... we dunno even if they are tokens!!!
        // E.g. "pi-bonding", versus "pipe"
        // So, appended a '-' behind them for now...
        normalizedStrings = new HashMap<String, String>();
        normalizedStrings.put("alpha-", "\u03b1-"); //greeks
        normalizedStrings.put("beta-", "\u03b2-");
        normalizedStrings.put("gamma-", "\u03b3-");
        normalizedStrings.put("delta-", "\u03b4-");
        normalizedStrings.put("epsilon-", "\u03b5-");
        normalizedStrings.put("zeta-", "\u03b6-");
        normalizedStrings.put("eta-", "\u03b7-");
        normalizedStrings.put("theta-", "\u03b8-");
        normalizedStrings.put("iota-", "\u03b9-");
        normalizedStrings.put("kappa-", "\u03ba-");
        normalizedStrings.put("lambda-", "\u03bb-");
        normalizedStrings.put("mu-", "\u03bc-");
        normalizedStrings.put("nu-", "\u03bd-");
        normalizedStrings.put("xi-", "\u03be-");
        normalizedStrings.put("omicron-", "\u03bf-");
        normalizedStrings.put("pi-", "\u03c0-");
        normalizedStrings.put("rho-", "\u03c1-");
        normalizedStrings.put("stigma-", "\u03c2-");
        normalizedStrings.put("sigma-", "\u03c3-");
        normalizedStrings.put("tau-", "\u03c4-");
        normalizedStrings.put("upsilon-", "\u03c5-");
        normalizedStrings.put("phi-", "\u03c6-");
        normalizedStrings.put("chi-", "\u03c7-");
        normalizedStrings.put("psi-", "\u03c8-");
        normalizedStrings.put("omega-", "\u03c9-");

    }

    public Normalizer(Map<Character,String> normalizedCharacters, Map<String,String> normalizedStrings) {
        if (normalizedCharacters == null || normalizedStrings == null) {
            throw new IllegalArgumentException("The given normalization maps must not be null.");
        }
        this.normalizedCharacters = normalizedCharacters;
        this.normalizedStrings = normalizedStrings;
    }

    public static synchronized Normalizer getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new Normalizer();
        }
        return defaultInstance;
    }

    /** {@inheritDoc} */
    public String normalize(String string) {
        // step 1: normalize characters
        StringBuilder builder = new StringBuilder();
        for (int i=0; i < string.length(); i++) {
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
                string = string.replace(normalizableString, normalizedStrings.get(normalizableString));
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
