package uk.ac.cam.ch.wwmm.oscar.normalize;

import java.util.HashMap;
import java.util.Map;

public class Normalizer implements ITextNormalizer {

	private Map<Character,String> normals;

	private static Normalizer myInstance = null;

	@SuppressWarnings("serial")
	private Normalizer() {
		normals = new HashMap<Character, String>() {{
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
            put('\u03b1',"alpha");//greeks
            put('\u03b2',"beta");
            put('\u03b3',"gamma");
            put('\u03b4',"delta");
            put('\u03b5',"epsilon");
            put('\u03b6',"zeta");
            put('\u03b7',"eta");
            put('\u03b8',"theta");
            put('\u03b9',"iota");
            put('\u03ba',"kappa");
            put('\u03bb',"lambda");
            put('\u03bc',"mu");
            put('\u03bd',"nu");
            put('\u03be',"xi");
            put('\u03bf',"omicron");
            put('\u03c0',"pi");
            put('\u03c1',"rho");
            put('\u03c2',"stigma");
            put('\u03c3',"sigma");
            put('\u03c4',"tau");
            put('\u03c5',"upsilon");
            put('\u03c6',"phi");
            put('\u03c7',"chi");
            put('\u03c8',"psi");
            put('\u03c9',"omega");
	        put('\uFEFF',"");//BOM-found at the start of some UTF files
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
