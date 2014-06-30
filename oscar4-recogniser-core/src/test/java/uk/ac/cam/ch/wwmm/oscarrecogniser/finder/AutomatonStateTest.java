package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import org.junit.Assert;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RunAutomaton;

public class AutomatonStateTest {

	@Test
	public void testConstructor() {
		AutomatonState state = new AutomatonState(
			new RunAutomaton(new Automaton(), false),
				NamedEntityType.COMPOUND, 0
	    );
		Assert.assertNotNull(state);
		Assert.assertEquals(
			NamedEntityType.COMPOUND,
			state.getType()
		);
		Assert.assertEquals(
			0, state.getStartToken()
		);
	}

	@Test
	public void testAddRep() {
		AutomatonState state = new AutomatonState(
			new RunAutomaton(new Automaton(), false),
				NamedEntityType.COMPOUND, 0
	    );
		Assert.assertFalse(
			state.toString().contains("sulfuric")
		);
		state.addRep("sulfuric");
		Assert.assertTrue(
			state.toString().contains("sulfuric")
		);
	}

	@Test
	public void testClone() {
		AutomatonState state = new AutomatonState(
			new RunAutomaton(new Automaton(), false),
				NamedEntityType.COMPOUND, 0
	    );
		state.addRep("sulfuric");

		AutomatonState clone = state.clone();
		Assert.assertEquals(
			NamedEntityType.COMPOUND,
			clone.getType()
		);
		Assert.assertEquals(
			0, clone.getStartToken()
		);
		Assert.assertTrue(
			clone.toString().contains("sulfuric")
		);
	}
}
