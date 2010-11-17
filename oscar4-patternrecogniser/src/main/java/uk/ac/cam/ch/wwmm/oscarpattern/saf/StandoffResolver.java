package uk.ac.cam.ch.wwmm.oscarpattern.saf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.document.ResolvableStandoff;

/**Removes low-priority standoffs from a list, to produce a list of non-overlapping standoffs.
 * 
 * @author ptc24
 *
 */
public class StandoffResolver {

	@SuppressWarnings("unchecked")
	public static List<ResolvableStandoff> resolveStandoffs(List<? extends ResolvableStandoff> standoffs) {
		Collections.sort(standoffs);
		List<ResolvableStandoff> standoffBuffer = new ArrayList<ResolvableStandoff>();
		List<ResolvableStandoff> resolved = new ArrayList<ResolvableStandoff>();
		for(ResolvableStandoff rs : standoffs) {
			int i = 0;
			boolean addToBuffer = true;
			// Scan through previously checked standoffs
			while(i < standoffBuffer.size()) {
				ResolvableStandoff prs = standoffBuffer.get(i);
				// First, shift standoffs in buffer than end before this one starts into the
				// resolved list
				//if(prs.endOffset.compareTo(rs.startOffset) != 1) {
				if(prs.compareEndToStart(rs) != 1) {
					standoffBuffer.remove(i);
					resolved.add(prs);
				// Next, consider conflicts
				} else if(rs.conflictsWith(prs)) {
					//OSCAR specified properties e.g. deprioritiseOnt
					if (rs.comparePropertiesSpecifiedPrioritisation(prs) == 1){
						standoffBuffer.remove(i);
					// Or the other;
					} else if (rs.comparePropertiesSpecifiedPrioritisation(prs) == -1){
						addToBuffer = false;
						break;
					}
					// Confidence (calculated)
					else if(rs.compareCalculatedConfidenceTo(prs) == 1) {
						standoffBuffer.remove(i);						
					// Or the other;
					} else if(rs.compareCalculatedConfidenceTo(prs) == -1) {
						addToBuffer = false;
						break;
					// Is this one the leftmost
					} else if(rs.compareStart(prs) == -1) {
						// Remove the other
						standoffBuffer.remove(i);						
					// Or the other...
					} else if(rs.compareStart(prs) == 1) {
						// Drop this one, move onto the next
						addToBuffer = false;
						break;
					// Is this one the longest
					} else if(rs.compareEnd(prs) == 1) {
						// Remove the other
						standoffBuffer.remove(i);						
					// Or the other...
					} else if(rs.compareEnd(prs) == -1) {
						// Drop this one, move onto the next
						addToBuffer = false;
						break;
					// Confidence, but can be either calculate or pseudo confidence(as specified in OSCARprops)
					} else if(rs.comparePseudoOrCalculatedConfidenceTo(prs) == 1) {
						// Remove the other
						standoffBuffer.remove(i);
					// Or the other...
					} else if(rs.comparePseudoOrCalculatedConfidenceTo(prs) == -1) {
						// Drop this one, move onto the next
						addToBuffer = false;
						break;
                    // They're the same string. New priorities needed based arbitrarily on type
					} else if(rs.compareTypeTo(prs) == 1) {
						// Remove the other
						standoffBuffer.remove(i);
						// Or the other...
					} else if(rs.compareTypeTo(prs) == -1) {
						// Drop this one, move onto the next
						addToBuffer = false;
						break;						
						// Drop duplicates
					} else {
						addToBuffer = false;
						break;						
					}
					// If no conflict, move onto the next one.
				} else {
					i++;
				}
			}
			if(addToBuffer) standoffBuffer.add(rs);
		}
		resolved.addAll(standoffBuffer);
		
		return resolved;
	}
	
}
