package uk.ac.cam.ch.wwmm.oscarMEMM.subtypes;

import java.util.List;

import nu.xom.Element;

/**An interface for inner classes that handle events in subtype processing.
 * 
 * @author ptc24
 *
 */
interface NESubtypeHandler {

	public void handle(Element annot, String type, String subtype, List<String> features);
	
}
