package uk.ac.cam.ch.wwmm.oscar.document;

import static org.junit.Assert.*;

import java.io.InputStream;


import nu.xom.Builder;
import nu.xom.Document;

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
		Tokeniser tokeniser = Tokeniser.getDefaultInstance();
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(tokeniser, source);
		assertEquals(1, procDoc.getTokenSequences().size());
		assertEquals(9, procDoc.getTokenSequences().get(0).getSize());
	}
	
	@Test
	public void testMakeDocumentFromSciXML() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/document/testDoc.xml");
		Document doc = new Builder().build(in);
		Tokeniser tokeniser = Tokeniser.getDefaultInstance();
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(tokeniser, doc);
		assertEquals(3, procDoc.getTokenSequences().size());
		
		assertEquals(0, procDoc.getTokenSequences().get(0).getSize());
		
		assertEquals(10, procDoc.getTokenSequences().get(1).getSize());
		assertEquals("The quick brown fox jumps over the lazy dog.", procDoc.getTokenSequences().get(1).getSurface());
		assertEquals(4, procDoc.getTokenSequences().get(1).getToken(1).getStart());
		assertEquals(0, procDoc.getTokenSequences().get(1).getOffset());
		
		assertEquals(10, procDoc.getTokenSequences().get(2).getSize());
		assertEquals("The slow green turtle sneaks under the watchful cat.", procDoc.getTokenSequences().get(2).getSurface());
		assertEquals(48, procDoc.getTokenSequences().get(2).getToken(1).getStart());
		assertEquals(44, procDoc.getTokenSequences().get(2).getOffset());
	}
}
