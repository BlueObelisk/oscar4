package uk.ac.cam.ch.wwmm.oscarMEMM.tokeniser;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.XOMBasedProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.XOMBasedProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;


/**
 * Tests for the learning-specific routines in the Tokeniser
 * 
 * @author dmj30
 */
public class TokeniserTest {

	@Test
	public void testTokeniseOnAnnotationBoundaries() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscarMEMM/tokeniser/sciXmlPaper.xml");
		Document sourceDoc = new Builder().build(in);
//		"the quick methylbrown fox jumps over thechlorinated dog"
		Tokeniser tokeniser = Tokeniser.getInstance();
		XOMBasedProcessingDocument procDoc = (XOMBasedProcessingDocument) XOMBasedProcessingDocumentFactory.getInstance().makeTokenisedDocument(tokeniser, sourceDoc, false, false, false);
		//empty tokenSequence for the empty HEADER in the sourceDoc
		assertEquals(2, procDoc.getTokenSequences().size());
		List <IToken> tokens = procDoc.getTokenSequences().get(1).getTokens(); 
		assertEquals(8, tokens.size());
		assertEquals("the", tokens.get(0).getValue());
		assertEquals("quick", tokens.get(1).getValue());
		assertEquals("methylbrown", tokens.get(2).getValue());
		assertEquals("fox", tokens.get(3).getValue());
		assertEquals("jumps", tokens.get(4).getValue());
		assertEquals("over", tokens.get(5).getValue());
		assertEquals("thechlorinated", tokens.get(6).getValue());
		assertEquals("dog", tokens.get(7).getValue());
		
		String sourceString = "the quick methylbrown fox jumps over thechlorinated dog";
		tokeniser.tokeniseOnAnnotationBoundaries(sourceString, procDoc, 0, procDoc.getDoc().getRootElement(), tokens);
		
		assertEquals(10, tokens.size());
		assertEquals("the", tokens.get(0).getValue());
		assertEquals("quick", tokens.get(1).getValue());
		assertEquals("methyl", tokens.get(2).getValue());
		assertEquals("brown", tokens.get(3).getValue());
		assertEquals("fox", tokens.get(4).getValue());
		assertEquals("jumps", tokens.get(5).getValue());
		assertEquals("over", tokens.get(6).getValue());
		assertEquals("the", tokens.get(7).getValue());
		assertEquals("chlorinated", tokens.get(8).getValue());
		assertEquals("dog", tokens.get(9).getValue());
	}
	
	@Test
	public void testMergeNeTokens() {
		String source = "foo ethyl acetate bar";
		Tokeniser tokeniser = Tokeniser.getInstance();
		IProcessingDocument procDoc = new ProcessingDocumentFactory().makeTokenisedDocument(tokeniser, source);
		List <IToken> tokens = procDoc.getTokenSequences().get(0).getTokens();
		assertEquals(4, tokens.size());
		assertEquals("foo", tokens.get(0).getValue());
		assertEquals("ethyl", tokens.get(1).getValue());
		assertEquals("acetate", tokens.get(2).getValue());
		assertEquals("bar", tokens.get(3).getValue());
		
		//FIXME manually setting the biotag is necessary as this is done by
		//Tokeniser.tokeniseOnAnnotationBoundaries
		tokens.get(0).setBioTag("O");
		tokens.get(1).setBioTag("B-CM");
		tokens.get(2).setBioTag("I-CM");
		tokens.get(3).setBioTag("O");
		tokeniser.mergeNeTokens(tokens, source, 0);
		
		assertEquals(3, tokens.size());
		assertEquals("foo", tokens.get(0).getValue());
		assertEquals("ethyl acetate", tokens.get(1).getValue());
		assertEquals("bar", tokens.get(2).getValue());
	}
	
	@Test
	public void testTidyHyphensAfterNE() throws Exception{
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscarMEMM/tokeniser/sciXmlPaper.xml");
		Document sourceDoc = new Builder().build(in);
		Tokeniser tokeniser = Tokeniser.getInstance();
		XOMBasedProcessingDocument procDoc = (XOMBasedProcessingDocument) XOMBasedProcessingDocumentFactory.getInstance().makeTokenisedDocument(tokeniser, sourceDoc, false, false, false);
		List <IToken> tokens = procDoc.getTokenSequences().get(1).getTokens(); 
		assertEquals(8, tokens.size());
		
		String sourceString = "the quick methylbrown fox jumps over thechlorinated dog";
		tokeniser.tokeniseOnAnnotationBoundaries(sourceString, procDoc, 0, procDoc.getDoc().getRootElement(), tokens);
		assertEquals(10, tokens.size());
		
		//produces "the" "quick" "methyl" "brown" "fox" "jumps" "over" "the" "chlorinated" "dog"
		tokens.get(3).setValue("-brown");
		tokens.get(2).setBioTag("B-CM");
		//produces "the" "quick" "methyl" "-brown" "fox" "jumps" "over" "the" "chlorinated" "dog"
		
		tokeniser.tidyHyphensAfterNEs(tokens);
		assertEquals(11, tokens.size());
		assertEquals("the", tokens.get(0).getValue());
		assertEquals("quick", tokens.get(1).getValue());
		assertEquals("methyl", tokens.get(2).getValue());
		assertEquals("-", tokens.get(3).getValue());
		assertEquals("brown", tokens.get(4).getValue());
		assertEquals("fox", tokens.get(5).getValue());
		assertEquals("jumps", tokens.get(6).getValue());
		assertEquals("over", tokens.get(7).getValue());
		assertEquals("the", tokens.get(8).getValue());
		assertEquals("chlorinated", tokens.get(9).getValue());
		assertEquals("dog", tokens.get(10).getValue());
	}
}
