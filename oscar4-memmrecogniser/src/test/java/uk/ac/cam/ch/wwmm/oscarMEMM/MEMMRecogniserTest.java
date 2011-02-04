package uk.ac.cam.ch.wwmm.oscarMEMM;

import static org.junit.Assert.*;

import java.util.List;

import nu.xom.Document;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.exceptions.ResourceInitialisationException;
import uk.ac.cam.ch.wwmm.oscar.scixml.TextToSciXML;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * @author egonw
 * @author j_robinson
 * @author dmj30
 */
public class MEMMRecogniserTest {

	@Test public void testConstructor() {
		assertNotNull(new MEMMRecogniser());
	}

	@Test
	public void testFindNamedEntities() throws Exception {
		ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar3/test/testcard/resources/");
		String s = rg.getString("testcard.txt");
		assertTrue("Have testcard string", s != null && s.length() > 0);
		Document doc = TextToSciXML.textToSciXML(s);
		
		IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().
			makeTokenisedDocument(Tokeniser.getInstance(), doc);
		assertTrue(procDoc != null);
		List<NamedEntity> neList = new MEMMRecogniser().findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neList != null);
		assertEquals("Only acetone should be recognized", 1, neList.size());
		assertEquals("acetone", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindNamedEntitiesFromString() throws ResourceInitialisationException {
		String source = "Hello acetone world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(Tokeniser.getInstance(), source);
		List <NamedEntity> neList = new MEMMRecogniser().findNamedEntities(procDoc);
		assertEquals(1, neList.size());
		assertEquals("acetone", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindMultipleTokenEntity() throws Exception {
		String text = "Hello ethyl acetate world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = new MEMMRecogniser().findNamedEntities(procDoc.getTokenSequences());
		
		//the memmRecogniser finds blocked named entities as well as the one we're expecting, so...
		boolean foundCorrectNE = false;
		for (NamedEntity namedEntity : neList) {
			if ("ethyl acetate".equals(namedEntity.getSurface())) {
				foundCorrectNE = true;
			}
		}
		assertTrue(foundCorrectNE);
	}
	
	@Test
	public void testFindNonDictionaryEntity() throws Exception {
		String text = "Hello 1-methyl-2-ethyl-3-propyl-4-butyl-5-pentyl-6-hexylbenzene world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = new MEMMRecogniser().findNamedEntities(procDoc.getTokenSequences());
		boolean foundCorrectNE = false;
		for (NamedEntity namedEntity : neList) {
			if ("1-methyl-2-ethyl-3-propyl-4-butyl-5-pentyl-6-hexylbenzene".equals(namedEntity.getSurface())) {
				foundCorrectNE = true;
			}
		}
		assertTrue(foundCorrectNE);
	}
	
	@Test
	public void testFindNonDictionaryMultipleTokenEntity() throws Exception {
		String text = "Hello 1,2-difluoro-1-chloro-2-methyl-ethyl acetate world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = new MEMMRecogniser().findNamedEntities(procDoc.getTokenSequences());
		boolean foundCorrectNE = false;
		for (NamedEntity namedEntity : neList) {
			if ("1,2-difluoro-1-chloro-2-methyl-ethyl acetate".equals(namedEntity.getSurface())) {
				foundCorrectNE = true;
			}
		}
		assertTrue(foundCorrectNE);
	}
}
