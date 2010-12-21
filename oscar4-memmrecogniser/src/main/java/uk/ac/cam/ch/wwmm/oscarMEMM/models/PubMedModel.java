package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import nu.xom.Document;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.data.MEMMModel;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGramBuilder;

public class PubMedModel extends MEMMModel {

	public PubMedModel() {
		Document modelDoc = new ResourceGetter(
			MEMMModel.class.getClassLoader(),
			"uk/ac/cam/ch/wwmm/oscarMEMM/models/"
		).getXMLDocument("pubmed.xml");
		try {
			this.readModel(modelDoc);
		} catch (Exception exception) {
			throw new Error(
				"Error while loading the pubmed MEMM model: " + exception,
				exception
				);
		}
		nGram = NGramBuilder.buildOrDeserialiseModel(manualAnnotations);
	}
}
