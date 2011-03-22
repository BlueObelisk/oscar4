package uk.ac.cam.ch.wwmm.oscar.interfaces;

import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;

/**
 * Abstraction of name recognition.
 * @author j_robinson
 * @author egonw
 * @author dmj30
 */
public interface ChemicalEntityRecogniser {

	public List<NamedEntity> findNamedEntities(List<ITokenSequence> tokenSequences);

	public List<NamedEntity> findNamedEntities(List<ITokenSequence> tokenSequences, boolean removeBlockedEntities);
}
