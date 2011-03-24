package uk.ac.cam.ch.wwmm.oscar.interfaces;

import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscarrecogniser.saf.StandoffResolver.ResolutionMode;

/**
 * Abstraction of name recognition.
 * @author j_robinson
 * @author egonw
 * @author dmj30
 */
public interface ChemicalEntityRecogniser {

	/**
	 * Identifies named entities with the given TokenSequences, removing overlapping
	 * named entities of lower priority, using the heuristics defined by the
	 * StandoffResolver.
	 * 
	 * @param tokenSequences
	 */
	public List<NamedEntity> findNamedEntities(List<ITokenSequence> tokenSequences);

	
	/**
	 * Identifies named entities with the given TokenSequences.
	 * 
	 * @param tokenSequences
	 * @param resolutionMode how to deal with overlapping named entities
	 */
	public List<NamedEntity> findNamedEntities(List<ITokenSequence> tokenSequences, ResolutionMode resolutionMode);
}