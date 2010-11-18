package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import java.util.ArrayList;
import java.util.List;

import dk.brics.automaton.RunAutomaton;

/** A class to hold DFA state, used by DFANEFinder.
 * 
 * @author ptc24
 *
 */
public class AutomatonState implements Cloneable {

    private int state;
	private int startToken;
	private RunAutomaton aut;
	private String type;
	private List<String> reps;
	
	AutomatonState(RunAutomaton a, String type, int st) {
		aut = a;
		startToken = st;
		state = a.getInitialState();
		this.type = type;
		reps = new ArrayList<String>();
	}

	private AutomatonState(RunAutomaton a, String type, int state, int st, List<String> reps) {
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

    public String getType() {
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
