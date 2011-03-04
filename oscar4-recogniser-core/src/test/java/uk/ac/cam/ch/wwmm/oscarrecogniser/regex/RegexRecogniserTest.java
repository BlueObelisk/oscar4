package uk.ac.cam.ch.wwmm.oscarrecogniser.regex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * 
 * @author dmj30
 *
 */
public class RegexRecogniserTest {

	@Test
	public void testConstructor() {
		RegexRecogniser recogniser = new RegexRecogniser("foo");
		assertEquals("foo", recogniser.getPattern().pattern());
	}
	
	@Test (expected = PatternSyntaxException.class)
	public void testConstructorBadRegex() {
		new RegexRecogniser("[");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstructorEmptyRegex() {
		new RegexRecogniser("");
	}
	
	
	@Test
	public void testFindNamedEntitiesSimple() {
		RegexRecogniser recogniser = new RegexRecogniser("NSC-\\d+");
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().
				makeTokenisedDocument(Tokeniser.getDefaultInstance(), "NSC-23432");
		List <NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());

		assertEquals(1, neList.size());
		assertEquals("NSC-23432", neList.get(0).getSurface());
		assertEquals(0, neList.get(0).getStart());
		assertEquals(9, neList.get(0).getEnd());
		
		assertEquals(1, neList.get(0).getTokens().size());
		assertEquals("NSC-23432", neList.get(0).getTokens().get(0).getSurface());
		assertEquals(0, neList.get(0).getTokens().get(0).getStart());
		assertEquals(9, neList.get(0).getTokens().get(0).getEnd());
	}
	
	@Test
	public void testFindNamedEntitiesInSentence() {
		RegexRecogniser recogniser = new RegexRecogniser("NSC-\\d+");
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().
				makeTokenisedDocument(Tokeniser.getDefaultInstance(), "foo NSC-23432 bar");
		List <NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("NSC-23432", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindNamedEntitiesMultipleEntities() {
		RegexRecogniser recogniser = new RegexRecogniser("NSC-\\d+");
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().
				makeTokenisedDocument(Tokeniser.getDefaultInstance(), "foo NSC-23432 NSC-123 bar");
		List <NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(2, neList.size());
		assertEquals("NSC-23432", neList.get(0).getSurface());
		assertEquals("NSC-123", neList.get(1).getSurface());
	}
	
	@Test
	public void testFindNamedEntitiesRequireTokenBoundaries() {
		RegexRecogniser recogniser = new RegexRecogniser("NSC-\\d+");
		
		ProcessingDocument procDoc1 = ProcessingDocumentFactory.getInstance().
				makeTokenisedDocument(Tokeniser.getDefaultInstance(), "foo NSC-23432bar");
		List <NamedEntity> neList1 = recogniser.findNamedEntities(procDoc1.getTokenSequences());
		assertEquals(0, neList1.size());
		
		ProcessingDocument procDoc2 = ProcessingDocumentFactory.getInstance().
		makeTokenisedDocument(Tokeniser.getDefaultInstance(), "fooNSC-23432 bar");
		List <NamedEntity> neList2 = recogniser.findNamedEntities(procDoc2.getTokenSequences());
		assertEquals(0, neList2.size());
	}
	
	@Test
	public void testFindNamedEntitiesAcrossMultipleTokens() {
		RegexRecogniser recogniser = new RegexRecogniser("NSC\\s\\d+");
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().
		makeTokenisedDocument(Tokeniser.getDefaultInstance(), "foo NSC 23432 bar");
		List <NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("NSC 23432", neList.get(0).getSurface());
		assertEquals(2, neList.get(0).getTokens().size());
	}
	
	@Test
	public void testNamedEntityTypes() {
		RegexRecogniser recogniser = new RegexRecogniser("NSC-\\d+");
		ProcessingDocument procDoc1 = ProcessingDocumentFactory.getInstance().
				makeTokenisedDocument(Tokeniser.getDefaultInstance(), "NSC-23432");
		List <NamedEntity> neList1 = recogniser.findNamedEntities(procDoc1.getTokenSequences());
		assertTrue(NamedEntityType.COMPOUND == neList1.get(0).getType());
		
		recogniser.setNamedEntityType(NamedEntityType.REACTION);
		ProcessingDocument procDoc2 = ProcessingDocumentFactory.getInstance().
				makeTokenisedDocument(Tokeniser.getDefaultInstance(), "NSC-23432");
		List <NamedEntity> neList2 = recogniser.findNamedEntities(procDoc2.getTokenSequences());
		assertTrue(NamedEntityType.REACTION == neList2.get(0).getType());
	}
}

