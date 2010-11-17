package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import nu.xom.Element;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.Model;

/**A MEMM singleton, for access by other components.
 * 
 * @author ptc24
 *
 */
public class MEMMSingleton {

	private static MEMM memm;

	/**Initialise the singleton, by loading the MEMM from an XML Element.
	 * 
	 * @param elem
	 */
	public static void load(Element elem) {
		try {
			memm = new MEMM();
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

	/**Gets the MEMM singleton instance, loading if necessary.
	 * 
	 * @return The MEMM singleton.
	 */
	public static MEMM getInstance() {
		if(memm == null) {
			Model.loadModel();
		}
		return memm;
	}

}
