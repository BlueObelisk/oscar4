package uk.ac.cam.ch.wwmm.oscarpattern;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.scixml.TextToSciXML;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * @author egonw
 * @author dmj30
 */
public class PatternRecogniserTest {

	private static PatternRecogniser recogniser;
	private static PatternRecogniser recogniserForCustomisation;

	@BeforeClass
	public static void setUp() {
		recogniser = new PatternRecogniser();
		recogniserForCustomisation = new PatternRecogniser();
	}
	
	@AfterClass
	public static void cleanUp() {
		recogniser = null;
	}
	
	@Test public void testConstructor() {
		Assert.assertNotNull(recogniser);
	}

	@Test
	public void test_findNamedEntities() throws Exception
	{
		ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar/test/testcard/");
		String s = rg.getString("testcard.txt");
		assertTrue("Have testcard string", s != null && s.length() > 0);
		Document doc = TextToSciXML.textToSciXML(s);

		IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
			Tokeniser.getDefaultInstance(), doc);
		assertTrue(procDoc != null);
		List<NamedEntity> neList;
		neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neList != null);
		assertTrue(neList.size() > 0);
	}
	
	@Test
	public void testFindNamedEntitiesFromString() throws Exception {
		String text = "Hello acetone world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("acetone", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindMultipleTokenEntity() throws Exception {
		String text = "Hello ethyl acetate world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("ethyl acetate", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindNonDictionaryEntity() throws Exception {
		String text = "Hello 1-methyl-2-ethyl-3-propyl-4-butyl-5-pentyl-6-hexylbenzene world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("1-methyl-2-ethyl-3-propyl-4-butyl-5-pentyl-6-hexylbenzene", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindNonDictionaryMultipleTokenEntity() throws Exception {
		String text = "Hello 1,2-difluoro-1-chloro-2-methyl-ethyl acetate world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("1,2-difluoro-1-chloro-2-methyl-ethyl acetate", neList.get(0).getSurface());
	}
	
	
	@Test
	public void testSetOntPseudoConfidence() {
		assertEquals(0.2, recogniserForCustomisation.getOntPseudoConfidence(), 0.00001);
		recogniserForCustomisation.setOntPseudoConfidence(0.3);
		assertEquals(0.3, recogniserForCustomisation.getOntPseudoConfidence(), 0.00001);
	}
	
	@Test
	public void testSetCustPseudoConfidence() {
		assertEquals(0.2, recogniserForCustomisation.getCustPseudoConfidence(), 0.00001);
		recogniserForCustomisation.setCustPseudoConfidence(0.3);
		assertEquals(0.3, recogniserForCustomisation.getCustPseudoConfidence(), 0.00001);
	}
	
	@Test
	public void testSetCprPseudoConfidence() {
		assertEquals(0.2, recogniserForCustomisation.getCprPseudoConfidence(), 0.00001);
		recogniserForCustomisation.setCprPseudoConfidence(0.3);
		assertEquals(0.3, recogniserForCustomisation.getCprPseudoConfidence(), 0.00001);
	}
	
	@Test
	public void testSetPseudoConfidences() {
		List <NamedEntity> nes = new ArrayList<NamedEntity>();
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument
				(Tokeniser.getDefaultInstance(), "foo");
		List <IToken> tokens = procDoc.getTokenSequences().get(0).getTokens();
		nes.add(new NamedEntity(tokens, "", NamedEntityType.COMPOUND));
		nes.add(new NamedEntity(tokens, "", NamedEntityType.ONTOLOGY));
		nes.add(new NamedEntity(tokens, "", NamedEntityType.CUSTOM));
		nes.add(new NamedEntity(tokens, "", NamedEntityType.LOCANTPREFIX));
		
		recogniserForCustomisation.setOntPseudoConfidence(0.4);
		recogniserForCustomisation.setCustPseudoConfidence(0.5);
		recogniserForCustomisation.setCprPseudoConfidence(0.6);
		recogniserForCustomisation.setPseudoConfidences(nes);
		assertEquals(Double.NaN, nes.get(0).getPseudoConfidence(), 0.00001);
		assertEquals(0.4, nes.get(1).getPseudoConfidence(), 0.00001);
		assertEquals(0.5, nes.get(2).getPseudoConfidence(), 0.00001);
		assertEquals(0.6, nes.get(3).getPseudoConfidence(), 0.00001);
	}
	
	@Test
	public void testSetNgramThreshold() {
		assertEquals(-2, recogniserForCustomisation.getNgramThreshold(), 0.00001);
		recogniserForCustomisation.setNgramThreshold(42);
		assertEquals(42, recogniserForCustomisation.getNgramThreshold(), 0.00001);
	}

}
