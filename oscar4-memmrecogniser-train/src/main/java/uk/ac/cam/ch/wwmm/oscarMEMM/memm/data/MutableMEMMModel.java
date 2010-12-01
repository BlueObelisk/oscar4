package uk.ac.cam.ch.wwmm.oscarMEMM.memm.data;

import uk.ac.cam.ch.wwmm.oscarMEMM.memm.rescorer.MEMMOutputRescorer;

public class MutableMEMMModel extends MEMMModel {

	public void setRescorer(MEMMOutputRescorer rescorer) {
		super.rescorer = rescorer;
	}

}
