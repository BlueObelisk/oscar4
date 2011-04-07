package uk.ac.cam.ch.wwmm.oscarMEMM.memm.data;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.collections.set.UnmodifiableSet;

import opennlp.maxent.GISModel;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer.MEMMOutputRescorer;
import uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata.ExtractedTrainingData;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGramBuilder;

public class MutableMEMMModel extends MEMMModel {

	public MutableMEMMModel(UnmodifiableSet chemNameDictNames) {
		super.nGram = NGramBuilder.buildOrDeserialiseModel();
		super.chemNameDictNames = chemNameDictNames;
	}
	
	public void setRescorer(MEMMOutputRescorer rescorer) {
		super.rescorer = rescorer;
	}

	public void makeEntityTypesAndZeroProbs() {
		super.makeEntityTypesAndZeroProbs();
	}

	public void setUberModel(GISModel trainModel) {
		super.ubermodel = trainModel;
	}
	
	public void putGISModel(BioType prev, GISModel gisModel) {
		super.gmByPrev.put(prev, gisModel);
	}

	public void addTag(BioType prev) {
		super.tagSet.add(prev);
	}

	public void setExtractedTrainingData(ExtractedTrainingData etd) {
		super.etd = etd;
	}
	
	@Override
	public Set<BioType> getTagSet() {
		return tagSet;
	}
	 
}
