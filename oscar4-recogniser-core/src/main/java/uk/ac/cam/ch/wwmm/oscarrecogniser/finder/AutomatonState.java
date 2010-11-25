package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import dk.brics.automaton.RunAutomaton;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

import java.util.ArrayList;
import java.util.List;

/** A class to hold DFA state, used by DFANEFinder.
 * 
 * @author ptc24
 *
 */
public class AutomatonState implements Cloneable {

    private int state;
	private int startToken;
	private RunAutomaton aut;
	private NamedEntityType type;
	private List<String> reps;
	
	AutomatonState(RunAutomaton a, NamedEntityType type, int st) {
		aut = a;
		startToken = st;
		state = a.getInitialState();
		this.type = type;
		reps = new ArrayList<String>();
	}

	private AutomatonState(RunAutomaton a, NamedEntityType type, int state, int st, List<String> reps) {
		aut = a;
		startToken = st;
		this.state = state;
		this.type = type;
		this.reps = reps;
	}

    public int getState() {
        return state;
    }

    public int getStartToken() {
        return startToken;
    }

    public NamedEntityType getType() {
        return type;
    }

    boolean isAccept() {
		return aut.isAccept(state);
	}
	
	void step(char c) {
		state = aut.step(state, c);
	}
	
	void addRep(String rep) {
		List<String> newReps = new ArrayList<String>(reps);
		newReps.add(rep);
		reps = newReps;
	}
	
	/*public List<String> getReps() {
		return new ArrayList<String>(reps);
	}*/
	
	public AutomatonState clone() {
		return new AutomatonState(aut, type, state, startToken, reps);
	}	
}
