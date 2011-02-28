package uk.ac.cam.ch.wwmm.oscarMEMM;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Document;

import org.junit.AfterClass;
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
 * @author j_robinson
 * @author dmj30
 */
public class MEMMRecogniserTest {

	private static MEMMRecogniser recogniser;
	private static MEMMRecogniser recogniserForCustomisation;
	
	@BeforeClass
	public static void setUp() {
		recogniser = new MEMMRecogniser();
		recogniserForCustomisation = new MEMMRecogniser();
	}
	
	@AfterClass
	public static void cleanUp() {
		recogniser = null;
		recogniserForCustomisation = null;
	}
	
	@Test public void testConstructor() {
		assertNotNull(recogniser);
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
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neList != null);
		assertEquals("Only acetone should be recognised", 1, neList.size());
		assertEquals("acetone", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindNamedEntitiesFromString() {
		String source = "Hello acetone world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(Tokeniser.getInstance(), source);
		List <NamedEntity> neList = recogniser.findNamedEntities(procDoc);
		assertEquals(1, neList.size());
		assertEquals("acetone", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindMultipleTokenEntity() throws Exception {
		String text = "Hello ethyl acetate world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		
		//the memmRecogniser finds blocked named entities as well as the one we're expecting, so...
		assertTrue(neListContainsCorrectNe(neList, "ethyl acetate"));
	}
	
	@Test
	public void testFindNonDictionaryEntity() throws Exception {
		String text = "Hello 1-methyl-2-ethyl-3-propyl-4-butyl-5-pentyl-6-hexylbenzene world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "1-methyl-2-ethyl-3-propyl-4-butyl-5-pentyl-6-hexylbenzene"));
	}
	
	@Test
	public void testFindNonDictionaryMultipleTokenEntity() throws Exception {
		String text = "Hello 1,2-difluoro-1-chloro-2-methyl-ethyl acetate world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "1,2-difluoro-1-chloro-2-methyl-ethyl acetate"));
	}
	
	
	@Test
	public void testFindFeThree() {
		String text = "The quick brown Fe(III) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Fe(III)"));
	}

	private boolean neListContainsCorrectNe(List<NamedEntity> neList, String desiredNe) {
		for (NamedEntity namedEntity : neList) {
			if (desiredNe.equals(namedEntity.getSurface())) {
				return true;
			}
		}
		return false;
	}
	
	@Test
	public void testFindFeThreeLowercase() {
		String text = "The quick brown Fe(iii) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Fe(iii)"));
	}
	
	@Test
	public void testFindFeThreePlus() {
		String text = "The quick brown Fe(3+) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Fe(3+)"));
	}
	
	@Test
	public void testFindFeNought() {
		String text = "The quick brown Fe(0) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Fe(0)"));
	}
	
	@Test
	public void testFindIronThree() {
		String text = "The quick brown Iron(III) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Iron(III)"));
	}
	
	@Test
	public void testFindIronThreeLowercase() {
		String text = "The quick brown Iron(iii) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Iron(iii)"));
	}
	
	@Test
	public void testFindIronThreePlus() {
		String text = "The quick brown Iron(3+) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Iron(3+)"));
	}
	
	@Test
	public void testFindIronNought() {
		String text = "The quick brown Iron(0) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Iron(0)"));
	}
	
	@Test
	public void testCuTwoCompound() {
		String text = "The resultant mixture was added dropwise to Cu(II) nitrate hexahydrate (1.00 mmol ) .";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Cu(II) nitrate hexahydrate"));
	}
	
	@Test
	public void testCuLowercaseTwoCompound() {
		String text = "The resultant mixture was added dropwise to Cu(ii) hydroxide (1.00 mmol ) .";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Cu(ii) hydroxide"));
	}

	@Test
	public void testCuTwoPlusCompound() {
		String text = "The resultant mixture was added dropwise to Cu(2+) chloride (1.00 mmol ) .";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Cu(2+) chloride"));
	}

	@Test
	public void testCopperTwoCompound() {
		String text = "The resultant mixture was added dropwise to Copper(II) acetate (1.00 mmol ) .";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Copper(II) acetate"));
	}

	@Test
	public void testCopperTwoLowercaseCompound() {
		String text = "The resultant mixture was added dropwise to Copper(ii) sulfate pentahydrate (1.00 mmol ) .";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Copper(ii) sulfate pentahydrate"));
	}

	@Test
	public void testCopperTwoPlusCompound() {
		String text = "The resultant mixture was added dropwise to Copper(2+) triflate (1.00 mmol ) .";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neListContainsCorrectNe(neList, "Copper(2+) triflate"));
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
				(Tokeniser.getInstance(), "foo");
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
	public void testDeprioritiseOnts() {
		String text = "Acetone, ethyl acetate and other solvents...";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument
				(Tokeniser.getInstance(), text);
		List <NamedEntity> nes = recogniserForCustomisation.findNamedEntities(procDoc);
		assertEquals(4, nes.size());
		for (NamedEntity ne : nes) {
			assertFalse(ne.getDeprioritiseOnt());
		}
		
		recogniserForCustomisation.setDeprioritiseOnts(true);
		nes = recogniserForCustomisation.findNamedEntities(procDoc);
		assertEquals(4, nes.size());
		for (NamedEntity ne : nes) {
			assertTrue(ne.getDeprioritiseOnt());
		}
	}
}
