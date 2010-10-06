package uk.ac.cam.ch.wwmm.oscarMEMM.terms;

import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**Experimental: a class to support a custom ontology format.
 * 
 * @author ptc24
 *
 */
final class DSOtoOBO {

	public static OBOOntology readDSO() throws Exception {
		ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar3Memm/terms/");
		String parentID = null;
		
		//Map<String,OntologyTerm> termsByID = new HashMap<String,OntologyTerm>();
		
		OBOOntology oo = new OBOOntology();
		
		
		int id = 0;
		for(String string : rg.getStrings("ptcontology.dso")) {
			if(string.matches("\\[.*\\]")) {
				id++;
				String termID = "PTCO:" + padInt(id, 6);
				String name = string.substring(1, string.length()-1);
				oo.addTerm(new OntologyTerm(termID, name));
				//termsByID.put(termID, new OntologyTerm(termID, name));
				parentID = termID;
			} else {
				id++;
				String termID = "PTCO:" + padInt(id, 6);
				String [] termNames = string.split("//");
				String mainTerm = termNames[0].trim();
				OntologyTerm term = new OntologyTerm(termID, mainTerm);
				oo.addTerm(term);
				//termsByID.put(termID, term);
				for(int i=1;i<termNames.length;i++) {
					term.addSynonym(termNames[i].trim());
				}
				term.addIsA(parentID);
				oo.getTerms().get(parentID).addIsTypeOf(termID);
			}
		}
		return oo;
	}
	
	public static String padInt(int num, int length) {
		String numStr = Integer.toString(num);
		return StringTools.multiplyString("0", length-numStr.length()) + numStr;
	}
		
}
