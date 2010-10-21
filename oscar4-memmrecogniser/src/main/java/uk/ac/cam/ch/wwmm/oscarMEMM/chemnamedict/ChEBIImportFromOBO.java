package uk.ac.cam.ch.wwmm.oscarMEMM.chemnamedict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictIO;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictSingleton;
import uk.ac.cam.ch.wwmm.oscar.obo.OBOOntology;
import uk.ac.cam.ch.wwmm.oscar.obo.OntologyTerm;
import uk.ac.cam.ch.wwmm.oscar.obo.Synonym;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;

/** Imports chemical entries from the ChEBI OBO file into ChemNameDict. ChEBI is the EBI's
 *  small molecule database/ontology, 
 *  <a href="http://www.ebi.ac.uk/chebi/index.jsp">http://www.ebi.ac.uk/chebi/index.jsp</a>. 
 *  This class uses the chebi.obo file stored in uk/ac/cam/ch/wwmm/oscar3/terms/resources/.
 *  
 * @author ptc24
 *
 */
public final class ChEBIImportFromOBO {

	/*public static void fetchChEBI(PrintWriter out) throws Exception {
		URL url = new URL("ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi.obo");
		ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar3/terms/resources/");
		OutputStream os = rg.getOutputStream("chebi.obo");
		InputStream is = url.openConnection().getInputStream();
		out.println("Fetching chebi.obo...");
		out.flush();
		assert(is != null);
		//InputStream is = null;
		//InputStream is = mpr.getInputStream("file");
		int c = is.read();
		while(c != -1) {
			os.write(c);
			c = is.read();
		}
		os.close();
		out.println("chebi.obo fetched OK!");
		out.flush();
	}*/
		
	/** Imports entries from ChEBI into the ChemNameDictSingleton.
	 * 
	 * @throws Exception
	 */
	public static void importChEBI() throws Exception {
		long time = System.currentTimeMillis();
		ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscarMemm/chemnamedict/");
		
		Set<String> stops = rg.getStringSet("ChEBIStop.txt");
		
		OBOOntology o = OBOOntology.getInstance();

		Map<String,Set<String>> relatedBySource = new HashMap<String,Set<String>>();
		
		for(String id : o.getTerms().keySet()) {
			if(!id.startsWith("CHEBI:")) continue;
			//if(id.equals("CHEBI:16243")) System.out.println("foo!");
			OntologyTerm term = o.getTerms().get(id);
			Set<String> names = new LinkedHashSet<String>();
			names.add(term.getName());
			String inchi = null;
			String smiles = null;
			//System.out.println(term.getName());
			for(Synonym s : term.getSynonyms()){
				//if(id.equals("CHEBI:16243")) System.out.println(s);
				//System.out.println("\t" + s);
				if(s.getType().equals("RELATED InChI")) {
					inchi = s.getSyn();
				} else if(s.getType().equals("RELATED SMILES")) { 
					smiles = s.getSyn();
				} else if(s.getType().equals("EXACT") || s.getType().equals("EXACT IUPAC_NAME")) {
					names.add(s.getSyn());
				} else if(s.getType().equals("RELATED")) {
					if(!relatedBySource.containsKey(s.getSource())) relatedBySource.put(s.getSource(), new HashSet<String>());
					relatedBySource.get(s.getSource()).add(s.getSyn());
				} else {
					//System.out.println(s.getType() + "\t" + s.getSyn() + "\t" + s.getSource());
				}
			}
			if(inchi != null) {
				List<String> goodNames = new ArrayList<String>();
				for(String name : names) {
					if(stops.contains(name)) {
						continue;
					}
					List<String> tokens = StringTools.arrayToList(name.split("\\s+"));
					boolean containsBadToken = false;
					for(String token : tokens) {
						if(stops.contains(token)) {
							containsBadToken = true;
							break;
						}
					}
					if(containsBadToken) continue;
					goodNames.add(name);
				}
				if(goodNames.size() > 0) {
					/*for(String name : goodNames) {
						String newName = normaliseCaseFromChEBI(name);
						//System.out.println(name + "\t" + newName);
						ChemNameDictSingleton.addChemical(newName, smiles, inchi);
					}*/
					ChemNameDictSingleton.addOntologyId(id, inchi);
					Set<String> ids = new HashSet<String>();
					ids.add(id);
					//System.out.println(inchi + "\t" + smiles + "\t" + goodNames + "\t" + ids);
					ChemNameDictSingleton.addChemRecord(inchi, smiles, new HashSet<String>(goodNames), ids);
					//if(id.equals("CHEBI:16243")) System.out.println(smiles);
				}
			}
		}
		System.out.println(System.currentTimeMillis() - time);
		//ChemNameDictSingleton.save();
		/*for(String source : relatedBySource.keySet()) {
			System.out.println(source);
			for(String s : relatedBySource.get(source)) {
				System.out.println("\t" + s);
			}
		}*/
	}
	
	public static void main(String [] args) throws Exception {
		ChemNameDict cnd = new ChemNameDict();
		ChemNameDictIO.readXML(
			new ResourceGetter("uk/ac/cam/ch/wwmm/oscarMemm/chemnamedict/")
				.getXMLDocument("defaultCompounds.xml"),
			cnd
		);
	//fetchChEBI(new PrintWriter(System.out));
		ChemNameDictSingleton.purge();
		System.out.println("Importing dict");
		ChemNameDictSingleton.importDict(cnd);
		System.out.println("Importing ChEBI");
		ChEBIImportFromOBO.importChEBI();
		System.out.println("Done!");
		//for(String name : ChemNameDictSingleton.getAllGoodNames()) {
		//	System.out.println(name);
		//}
		System.out.println(ChemNameDictSingleton.getAllNames().size());
		//ChemNameDictSingleton.dumpToStdout();
		ChemNameDictSingleton.save();		
	}
	
}
