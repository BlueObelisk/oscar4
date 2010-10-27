package uk.ac.cam.ch.wwmm.oscar.interfaces;

import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;

/**
 * Abstraction of name recognition. Set chemicalEntityRecogniser system property "chemicalEntityRecogniser" to the implementing class
 * @author j_robinson
 * @author egonw
 */
public interface ChemicalEntityRecogniser {

	public List<NamedEntity> findNamedEntities(List<TokenSequence> tokenSequences) throws Exception;

}
