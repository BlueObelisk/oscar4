package uk.ac.cam.ch.wwmm.oscarpattern;

import static org.junit.Assert.*;

import java.util.List;

import nu.xom.Document;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscar.scixml.TextToSciXML;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * @author egonw
 * @author dmj30
 */
public class PatternRecogniserTest {

	@Test public void testConstructor() {
		Assert.assertNotNull(new PatternRecogniser());
	}

	@Test
	public void test_findNamedEntities() throws Exception
	{
		ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar/test/testcard/");
		String s = rg.getString("testcard.txt");
		assertTrue("Have testcard string", s != null && s.length() > 0);
		Document doc = TextToSciXML.textToSciXML(s);

		IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
			Tokeniser.getInstance(), doc);
		assertTrue(procDoc != null);
		List<NamedEntity> neList;
		ChemicalEntityRecogniser cei = new PatternRecogniser();
		neList = cei.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neList != null);
		assertTrue(neList.size() > 0);
	}
	
	@Test
	public void testFindNamedEntitiesFromString() throws Exception {
		String text = "Hello acetone world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = new PatternRecogniser().findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("acetone", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindMultipleTokenEntity() throws Exception {
		String text = "Hello ethyl acetate world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = new PatternRecogniser().findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("ethyl acetate", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindNonDictionaryEntity() throws Exception {
		String text = "Hello 1-methyl-2-ethyl-3-propyl-4-butyl-5-pentyl-6-hexylbenzene world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = new PatternRecogniser().findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("1-methyl-2-ethyl-3-propyl-4-butyl-5-pentyl-6-hexylbenzene", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindNonDictionaryMultipleTokenEntity() throws Exception {
		String text = "Hello 1,2-difluoro-1-chloro-2-methyl-ethyl acetate world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = new PatternRecogniser().findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("1,2-difluoro-1-chloro-2-methyl-ethyl acetate", neList.get(0).getSurface());
	}
}
