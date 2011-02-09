package uk.ac.cam.ch.wwmm.oscar.obo.dso;

import java.io.IOException;
import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.obo.OBOOntology;
import uk.ac.cam.ch.wwmm.oscar.obo.OntologyTerm;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/**
 * Experimental: a class to support a custom ontology format.
 * 
 * @author ptc24
 */
public class DSOtoOBO {

	public static OBOOntology readDSO() {
		OBOOntology oo = new OBOOntology();

		ResourceGetter rg = new ResourceGetter(
			oo.getClass().getClassLoader(),
			"uk/ac/cam/ch/wwmm/oscar/obo/terms/"
		);
		String parentID = null;
		
		//Map<String,OntologyTerm> termsByID = new HashMap<String,OntologyTerm>();
		
		
		
		int id = 0;
		List <String> strings;
		try {
			strings = rg.getStrings("ptcontology.dso");
		} catch (IOException e) {
			throw new OscarInitialisationException("failed to load custom ontology", e);
		}
		for(String string : strings) {
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
				for (int i = 1; i < termNames.length; i++) {
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
