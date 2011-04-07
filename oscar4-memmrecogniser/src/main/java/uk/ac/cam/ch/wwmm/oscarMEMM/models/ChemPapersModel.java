package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import java.io.IOException;

import org.apache.commons.collections.set.UnmodifiableSet;

import nu.xom.Document;
import nu.xom.ParsingException;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.exceptions.OscarInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.data.MEMMModel;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGramBuilder;

public class ChemPapersModel extends MEMMModel {

	public ChemPapersModel() {
		Document modelDoc;
		
		try {
			modelDoc = new ResourceGetter(
					MEMMModel.class.getClassLoader(),
					"uk/ac/cam/ch/wwmm/oscarMEMM/models/"
				).getXMLDocument("chempapers.xml");
			this.readModel(modelDoc);
		} catch (IOException e) {
			throw new OscarInitialisationException("failed to load the ChemPapersModel", e);
		} catch (ParsingException e) {
			throw new OscarInitialisationException("failed to load the ChemPapersModel", e);
		}
		chemNameDictNames = (UnmodifiableSet) UnmodifiableSet.decorate(
				ChemNameDictRegistry.getDefaultInstance().getAllNames());
		nGram = NGramBuilder.buildOrDeserialiseModel(etd, chemNameDictNames);
	}
}
