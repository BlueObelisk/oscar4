package uk.ac.cam.ch.wwmm.oscarrecogniser.saf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;

/**
 * A class to handle the treatment of overlapping named entities
 * 
 * @author ptc24
 * @author dmj30
 */
public class StandoffResolver {

	/**
	 *  The strategies for dealing with overlapping named entities;
	 * 
	 *  MARK_BLOCKED - retain all named entities but set the "blocked" flag
	 *  of those overlapping named entities with lower priority to "true".
	 *  
	 *  REMOVE_BLOCKED - remove those overlapping named entities with lower
	 *  priority from the NamedEntity list.
	 */
	public static enum ResolutionMode {
		MARK_BLOCKED, REMOVE_BLOCKED
	}
	
	public static void markBlockedStandoffs(List <NamedEntity> standoffs) {
		List <NamedEntity> resolved = new ArrayList<NamedEntity>(standoffs); 
		resolveStandoffs(resolved);
		for (NamedEntity ne : standoffs) {
			if (!resolved.contains(ne)) {
				ne.setBlocked(true);
			}
		}
	}
	
	public static void resolveStandoffs(List<NamedEntity> standoffs) {
		List<? extends NamedEntity> unresolved = new ArrayList<NamedEntity>(standoffs);
		Collections.sort(unresolved);
		List<NamedEntity> standoffBuffer = new ArrayList<NamedEntity>();
		List<NamedEntity> resolved = new ArrayList<NamedEntity>();
		for(NamedEntity rs : unresolved) {
			int i = 0;
			boolean addToBuffer = true;
			// Scan through previously checked standoffs
			while(i < standoffBuffer.size()) {
				NamedEntity prs = standoffBuffer.get(i);
				// First, shift standoffs in buffer that end before this one starts into the
				// resolved list
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
		standoffs.clear();
		resolved.addAll(standoffBuffer);
		standoffs.addAll(resolved);
	}

}
