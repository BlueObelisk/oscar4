package uk.ac.cam.ch.wwmm.oscarMEMM.memm.gis;

import java.util.List;

import opennlp.maxent.Event;
import opennlp.maxent.EventCollector;

/**An EventCollector, for use with opennlp.maxent classes.
 * 
 * @author ptc24
 *
 */
public final class SimpleEventCollector implements EventCollector {
	public Event [] events;
	
	public SimpleEventCollector(List<Event> ev) {
		events = (Event [])ev.toArray(new Event[0]);
	}
	
	public Event[] getEvents(boolean arg0) {
		// TODO Auto-generated method stub
		return getEvents();
	}
	
	public Event[] getEvents() {
		// TODO Auto-generated method stub
		return events;
	}
}