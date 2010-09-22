package uk.ac.cam.ch.wwmm.oscarpattern;

import java.util.List;

import uk.ac.cam.ch.wwmm.oscarpattern.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscarpattern.document.ProcessingDocument;

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
	public List<NamedEntity> findNamedEntities(ProcessingDocument procDoc) throws Exception;
}
