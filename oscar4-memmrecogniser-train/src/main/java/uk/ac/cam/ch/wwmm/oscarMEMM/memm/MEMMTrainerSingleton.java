package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.io.File;
import java.util.List;

import nu.xom.Element;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.Model;

/**A MEMM singleton, for access by other components.
 * 
 * @author ptc24
 * @author egonw
 */
public class MEMMTrainerSingleton {

	private static MEMMTrainer memm;

	/**Initialise the singleton, by loading the MEMM from an XML Element.
	 * 
	 * @param elem
	 */
	public static void load(Element elem) {
		try {
			memm = new MEMMTrainer();
			memm.readModel(elem);
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	/**Remove the MEMM singleton.
	 * 
	 */
	public static void clear() {
		memm = null;
	}
	
	/**Train a new MEMM, based on a list of ScrapBook files, and make it
	 * be the new singleton.
	 * 
	 * @param files The files to use as training data.
	 * @param rescore Whether to train a rescorer as well as a MEMM.
	 */
	public static void train(List<File> files, boolean rescore) {
		try {
			memm = new MEMMTrainer();
			if(rescore) {
				System.err.println("***************Requires Training on SB files with rescore which is commented out");
				//memm.trainOnSbFilesWithRescore(files, null);
			} else {
				System.err.println("***************Requires Training on SB files with rescore which is commented out");
				//memm.trainOnSbFiles(files, null);
			}
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	/**Gets the MEMM singleton instance, loading if necessary.
	 * 
	 * @return The MEMM singleton.
	 */
	public static MEMMTrainer getInstance() {
		if(memm == null) {
			Model.loadModel();
		}
		return memm;
	}

}
