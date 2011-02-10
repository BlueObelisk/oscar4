package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;

import java.util.HashSet;
import java.util.Set;

/**Uses simple algorithms for scoring nomenclature for small bio-oligomers,
 * such as Lys-Trp-Lys and Gal-Gal.
 * 
 * @author ptc24
 *
 */
//TODO this class isn't used - do we need it?
public class BiooligomerScorer {

	private Set<String> bioTokens;
	private Set<String> okTokens;
	
	private static BiooligomerScorer myInstance;
	
	public static BiooligomerScorer getInstance() {
		if(myInstance == null) myInstance = new BiooligomerScorer();
		return myInstance;
	}
	
	private BiooligomerScorer() {
		bioTokens = new HashSet<String>();
		okTokens = new HashSet<String>();
		bioTokens.add("Ala");
		bioTokens.add("Arg");
		bioTokens.add("Asn");
		bioTokens.add("Asp");
		bioTokens.add("Asx");
		bioTokens.add("Cys");
		bioTokens.add("Glu");
		bioTokens.add("Gln");
		bioTokens.add("Glx");
		bioTokens.add("Gly");
		bioTokens.add("His");
		bioTokens.add("Ile");
		bioTokens.add("Leu");
		bioTokens.add("Lys");
		bioTokens.add("Met");
		bioTokens.add("Phe");
		bioTokens.add("Pro");
		bioTokens.add("Ser");
		bioTokens.add("Thr");
		bioTokens.add("Trp");
		bioTokens.add("Tyr");
		bioTokens.add("Val");
		okTokens.add("All");
		bioTokens.add("Alt");
		bioTokens.add("Ara");
		bioTokens.add("Fru");
		bioTokens.add("Fuc");
		bioTokens.add("Gal");
		bioTokens.add("Glc");
		bioTokens.add("Gro");
		bioTokens.add("Gul");
		bioTokens.add("Ido");
		bioTokens.add("Lyx");
		bioTokens.add("Man");
		bioTokens.add("Psi");
		bioTokens.add("Qui");
		bioTokens.add("Rha");
		bioTokens.add("Rib");
		bioTokens.add("Rul");
		bioTokens.add("Sor");
		bioTokens.add("Tag");
		bioTokens.add("Tal");
		bioTokens.add("Xyl");
		bioTokens.add("Xul");
		bioTokens.add("Abe");
		bioTokens.add("Api");
		bioTokens.add("dRib");
		bioTokens.add("2dGlc");
		bioTokens.add("Lac");
		bioTokens.add("Mur");
		bioTokens.add("Neu");
		bioTokens.add("Tyv");
		okTokens.add("ol");
	}
	
	public double scoreWord(String word) {
		if(word == null || word.length() == 0) return 0.0;
		try {
			double score = 0.0;
			String [] ss = word.split("-");
			for (int i = 0; i < ss.length; i++) {
				String v = ss[i];
				if(v.endsWith("Ac")) v = v.substring(0, v.length()-2);
				if(v.endsWith("diN")) v = v.substring(0, v.length()-3);
				if(v.endsWith("N")) v = v.substring(0, v.length()-1);
				if(bioTokens.contains(v)) {
					score += 10.0;
				} else if(ss[i].length() < 2) {
					
				} else if(okTokens.contains(ss[i])) {
					
				} else {
					score -= 10.0;
				}
		}
		return score; 
		} catch (Exception e) {
			e.printStackTrace();
			return 0.0;
		}
	}	
}
