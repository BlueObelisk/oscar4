package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import java.util.HashMap;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**A suffix tree, or a node of a suffix tree. Copied from LexerNode. 
 * @author ptc24
 *
 */
final class SuffixTree {
	
	/**All of the possible continuations of the token.*/
	private HashMap<Character, SuffixTree> next = new HashMap<Character, SuffixTree>();
	/**If this node could represent the final character of a token, that token.
	 * Otherwise null.
	 */
	private String contents;

	/**Initialises a root node.
	 * 
	 * @param contents The token used for initialisation.
	 */
	SuffixTree(String contents) {
		this(contents, 0);
	}
	
	/**Initialises a node.
	 * 
	 * @param contents The token used for initialisation.
	 * @param pathlen The character offset along the token that this node represents.
	 */
	private SuffixTree(String contents, int pathlen) {
		if(pathlen == contents.length()) {
			this.contents = contents;
		} else {
			this.contents = null;
			next.put(contents.charAt(pathlen), new SuffixTree(contents, pathlen+1));
		}
	}

	/**Adds a new token to the node, treating the node as a root node.
	 * 
	 * @param contents The token to add.
	 */
	void addContents(String contents) {
		addContents(contents, 0);
	}
	
	/**Adds a new token to the node.
	 * 
	 * @param contents The token to add.
	 * @param pathlen The character offset along the token that this node represents.
	 */
	private void addContents(String contents, int pathlen) {
		if(pathlen == contents.length()) {
			this.contents = contents;
		} else if(!next.containsKey(contents.charAt(pathlen))) {
			next.put(contents.charAt(pathlen), new SuffixTree(contents, pathlen+1));
		} else {
			next.get(contents.charAt(pathlen)).addContents(contents, pathlen+1);
		}
	}
	
	Automaton toAutomaton() {
		Automaton a = new Automaton();
		a.getInitialState();		
		a.setInitialState(makeState());
		return a;
	}
	
	private State makeState() {
		State s = new State();
		if(contents != null) s.setAccept(true);
		for(Character c : next.keySet()) {
			State nextState = next.get(c).makeState();
			Transition t = new Transition(c, nextState);
			s.addTransition(t);
		}		
		return s;
	}
	
}
