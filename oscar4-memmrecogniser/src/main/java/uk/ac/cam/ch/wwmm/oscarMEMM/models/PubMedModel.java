package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import java.io.IOException;

import nu.xom.Document;
import nu.xom.ParsingException;
import uk.ac.cam.ch.wwmm.oscar.exceptions.ResourceInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.data.MEMMModel;
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
			throw new ResourceInitialisationException("failed to load the PubMed model", e);
		} catch (ParsingException e) {
			throw new ResourceInitialisationException("failed to load the PubMed model", e);
		}
		nGram = NGramBuilder.buildOrDeserialiseModel(manualAnnotations);
	}
}
