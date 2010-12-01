package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import nu.xom.Document;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMModel;

public class ChemPapersModel extends MEMMModel {

	public ChemPapersModel() {
		Document modelDoc = new ResourceGetter(
			MEMMModel.class.getClassLoader(),
			"uk/ac/cam/ch/wwmm/oscarMEMM/models/"
		).getXMLDocument("chempapers.xml");
		try {
			this.readModel(modelDoc);
		} catch (Exception exception) {
			throw new Error(
				"Error while loading the chempaper MEMM model: " + exception,
				exception
				);
		}
	}
}
