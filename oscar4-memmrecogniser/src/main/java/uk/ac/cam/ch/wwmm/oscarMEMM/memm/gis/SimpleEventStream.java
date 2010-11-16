package uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis;

import java.util.List;

import opennlp.maxent.Event;
import opennlp.maxent.EventStream;

/**An EventStream, for use with opennlp.maxent classes.
 * 
 * @author ptc24
 *
 */
public final class SimpleEventStream implements EventStream {

	private int pointer;
	private List<Event> events;
	
	public SimpleEventStream(List<Event> events) {
		this.events = events;
		pointer = 0;
	}
	
	public boolean hasNext() {
		return pointer < events.size();
	}

	public Event nextEvent() {
		if(!hasNext()) return null;
		return events.get(pointer++);
	}

}
