package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import java.util.ArrayList;
import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;

/**Used by subclasses of DFAFinder.
 * 
 * @author ptc24
 *
 */
public class NECollector implements ResultsCollector {

	private List<NamedEntity> nes;
	
	public NECollector() {
		nes = new ArrayList<NamedEntity>();
	}
	
	public void collect(NamedEntity ne) {
		nes.add(ne);
	}
	
	public List<NamedEntity> getNes() {
		return nes;
	}
}
