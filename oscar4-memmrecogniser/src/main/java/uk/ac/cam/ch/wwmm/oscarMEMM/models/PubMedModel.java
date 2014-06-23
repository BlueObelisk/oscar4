package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import java.io.IOException;
import java.util.Collections;

import nu.xom.Document;
import nu.xom.ParsingException;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMModel;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGramBuilder;

public class PubMedModel extends MEMMModel {

	public PubMedModel() {
		Document modelDoc;
		
		try {
			modelDoc = new ResourceGetter(
					MEMMModel.class.getClassLoader(),
					"uk/ac/cam/ch/wwmm/oscarMEMM/models/"
				).getXMLDocument("pubmed.xml");
			this.readModel(modelDoc);
		} catch (IOException e) {
			throw new OscarInitialisationException("failed to load the PubMed model", e);
		} catch (ParsingException e) {
			throw new OscarInitialisationException("failed to load the PubMed model", e);
		}
		chemNameDictNames = Collections.unmodifiableSet(ChemNameDictRegistry
				.getDefaultInstance().getAllNames());
		nGram = NGramBuilder.buildOrDeserialiseModel(etd, chemNameDictNames);
	}
}
