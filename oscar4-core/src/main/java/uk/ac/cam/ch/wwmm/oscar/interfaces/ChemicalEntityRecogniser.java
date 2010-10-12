package uk.ac.cam.ch.wwmm.oscar.interfaces;

import java.util.List;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;

/**
 * Abstraction of name recognition. Set chemicalEntityRecogniser system property "chemicalEntityRecogniser" to the implementing class
 * @author j_robinson
 * @author egonw
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
