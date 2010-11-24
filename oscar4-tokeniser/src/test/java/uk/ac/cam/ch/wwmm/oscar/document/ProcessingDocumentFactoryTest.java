package uk.ac.cam.ch.wwmm.oscar.document;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/*
 * Yes, the tokeniser package is a strange place for this test. The creation of
 * processing documents uses the tokeniser implementation, so it's here for now. 
 */
/**
 * @author dmj30
 */
public class ProcessingDocumentFactoryTest {

	@Test
	public void testMakeDocumentFromSimpleString() {
		String source = "The quick brown fox jumps over the lazy dog";
		Tokeniser tokeniser = Tokeniser.getInstance();
		ProcessingDocument procDoc = new ProcessingDocumentFactory().makeTokenisedDocument(tokeniser, source);
		assertEquals(1, procDoc.getTokenSequences().size());
		assertEquals(9, procDoc.getTokenSequences().get(0).size());
	}
	
	
}
