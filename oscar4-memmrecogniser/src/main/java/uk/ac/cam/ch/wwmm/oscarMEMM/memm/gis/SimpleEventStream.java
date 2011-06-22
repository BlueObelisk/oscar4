package uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis;

import java.io.IOException;
import java.util.List;

import opennlp.model.Event;
import opennlp.model.EventStream;

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

	public Event next() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
