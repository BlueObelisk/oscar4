package uk.ac.cam.ch.wwmm.oscar.document;

/**A data structure that represents a span within a document. This may potentially be resolved
 * against other such spans, so as to remove low-priority non-overlapping spans.
 * 
 * @author ptc24
 *
 */
//TODO investigate whether ResolvableStandoff class is needed or can be removed
public abstract class ResolvableStandoff implements Comparable {

	protected String type;
	
	public abstract int comparePropertiesSpecifiedPrioritisation(ResolvableStandoff other);//e.g. deprioritiseOnt which is set in OSCARprops
	public abstract int compareCalculatedConfidenceTo(ResolvableStandoff other);
	public abstract int comparePseudoOrCalculatedConfidenceTo(ResolvableStandoff other);
	public abstract int compareTypeTo(ResolvableStandoff other);
	public abstract int compareStart(ResolvableStandoff other);
	public abstract int compareEnd(ResolvableStandoff other);
	public abstract int compareStartToEnd(ResolvableStandoff other);
	public abstract int compareEndToStart(ResolvableStandoff other);

	public int compareTo(Object o) {
		if(o instanceof ResolvableStandoff) {
			ResolvableStandoff other = (ResolvableStandoff)o;
			int startComparison = compareStart(other);
			int endComparison = compareEnd(other);
			if (startComparison < 0) {
				return -1;
			} else if (startComparison > 0) {
				return 1;
			} else if (endComparison < 0) {
				return -1;
			} else if (endComparison > 0) {
				return 1;
			} else {
				return 0;
			}			
		} else {
			return 0;
		}
	}
	
	public boolean conflictsWith(ResolvableStandoff other) {
		// If this starts after (or at) the end of the other, no conflict
		if (compareStartToEnd(other) >= 0) {
            return false;
        }
		// If this ends before (or at) the start of the other, no conflict
		if (compareEndToStart(other) <= 0) {
            return false;
        }
		// Therefore, there must be a conflict
		return true;
	}
	
}
