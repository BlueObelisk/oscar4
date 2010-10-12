package uk.ac.cam.ch.wwmm.oscarMEMM;

import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;

/**
 * Abstraction of name recognition. Set chemicalEntityRecogniser system property "chemicalEntityRecogniser" to the implementing class
 * @author j_robinson
 */
public interface ChemicalEntityRecogniser
{
	/**
	 *
	 * @param procDoc The input data
	 * @return The list of detected Named Entities
	 * @throws Exception
	 */
	public List<NamedEntity> findNamedEntities(List<TokenSequence> tokenSequences) throws Exception;
//	public List<NamedEntity> findNamedEntities(ProcessingDocument procDoc) throws Exception;
}
