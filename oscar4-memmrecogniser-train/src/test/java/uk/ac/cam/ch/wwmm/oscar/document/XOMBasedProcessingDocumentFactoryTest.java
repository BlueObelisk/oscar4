package uk.ac.cam.ch.wwmm.oscar.document;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

public class XOMBasedProcessingDocumentFactoryTest {

	@Test
	public void testTokeniseOnAnnotationBoundaries() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/document/sciXmlPaper.xml");
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
		XOMBasedProcessingDocumentFactory.getInstance().tokeniseOnAnnotationBoundaries(tokeniser, sourceString, procDoc, 0, procDoc.getDoc().getRootElement(), tokens);
		
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
		IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(tokeniser, source);
		List <IToken> tokens = procDoc.getTokenSequences().get(0).getTokens();
		assertEquals(4, tokens.size());
		assertEquals("foo", tokens.get(0).getValue());
		assertEquals("ethyl", tokens.get(1).getValue());
		assertEquals("acetate", tokens.get(2).getValue());
		assertEquals("bar", tokens.get(3).getValue());
		
		//FIXME manually setting the biotag is necessary as this is done by
		//XOMBasedProcessingDocumentFactory.tokeniseOnAnnotationBoundaries
		tokens.get(0).setBioTag("O");
		tokens.get(1).setBioTag("B-CM");
		tokens.get(2).setBioTag("I-CM");
		tokens.get(3).setBioTag("O");
		XOMBasedProcessingDocumentFactory.getInstance().mergeNeTokens(tokens, source, 0);
		
		assertEquals(3, tokens.size());
		assertEquals("foo", tokens.get(0).getValue());
		assertEquals("ethyl acetate", tokens.get(1).getValue());
		assertEquals("bar", tokens.get(2).getValue());
	}
	
	@Test
	public void testTidyHyphensAfterNE() throws Exception{
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/document/sciXmlPaper.xml");
		Document sourceDoc = new Builder().build(in);
		Tokeniser tokeniser = Tokeniser.getInstance();
		XOMBasedProcessingDocument procDoc = (XOMBasedProcessingDocument) XOMBasedProcessingDocumentFactory.getInstance().makeTokenisedDocument(tokeniser, sourceDoc, false, false, false);
		List <IToken> tokens = procDoc.getTokenSequences().get(1).getTokens(); 
		assertEquals(8, tokens.size());
		
		String sourceString = "the quick methylbrown fox jumps over thechlorinated dog";
		XOMBasedProcessingDocumentFactory.getInstance().tokeniseOnAnnotationBoundaries(tokeniser, sourceString, procDoc, 0, procDoc.getDoc().getRootElement(), tokens);
		assertEquals(10, tokens.size());
		
		//produces "the" "quick" "methyl" "brown" "fox" "jumps" "over" "the" "chlorinated" "dog"
		tokens.get(3).setValue("-brown");
		tokens.get(2).setBioTag("B-CM");
		//produces "the" "quick" "methyl" "-brown" "fox" "jumps" "over" "the" "chlorinated" "dog"
		
		XOMBasedProcessingDocumentFactory.getInstance().tidyHyphensAfterNEs(tokeniser, tokens);
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
	
	
	@Test
	public void testMakeTokenSequenceStandard() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/document/learningSpecificTokenisation.xml");
		Document sourceDoc = new Builder().build(in);
		Tokeniser tokeniser = Tokeniser.getInstance();
		XOMBasedProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeDocument(sourceDoc);
		Element para = (Element) procDoc.getDoc().query("//P").get(0);
//		String text = "the quick methylbrown ethyl acetate jumps over thechlorinated dog";
		String text = para.getValue();
		int offset = Integer.parseInt(para.getAttributeValue("xtspanstart"));
		
		ITokenSequence tokSeq = XOMBasedProcessingDocumentFactory.getInstance().makeTokenSequence(tokeniser, false, false, null, procDoc, para, text, offset);
		assertEquals(9, tokSeq.getTokens().size());
		assertEquals("the", tokSeq.getTokens().get(0).getValue());
		assertEquals("quick", tokSeq.getTokens().get(1).getValue());
		assertEquals("methylbrown", tokSeq.getTokens().get(2).getValue());
		assertEquals("ethyl", tokSeq.getTokens().get(3).getValue());
		assertEquals("acetate", tokSeq.getTokens().get(4).getValue());
		assertEquals("jumps", tokSeq.getTokens().get(5).getValue());
		assertEquals("over", tokSeq.getTokens().get(6).getValue());
		assertEquals("thechlorinated", tokSeq.getTokens().get(7).getValue());
		assertEquals("dog", tokSeq.getTokens().get(8).getValue());
	}
	
	@Test
	public void testMakeTokenSequenceTokeniseForNes() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/document/learningSpecificTokenisation.xml");
		Document sourceDoc = new Builder().build(in);
		Tokeniser tokeniser = Tokeniser.getInstance();
		XOMBasedProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeDocument(sourceDoc);
		Element e = (Element) procDoc.getDoc().getRootElement();
		Element para = (Element) procDoc.getDoc().query("//P").get(0); 
		String text = para.getValue();
		int offset = Integer.parseInt(para.getAttributeValue("xtspanstart"));
		
		ITokenSequence tokSeq = XOMBasedProcessingDocumentFactory.getInstance().makeTokenSequence(tokeniser, true, false, null, procDoc, e, text, offset);
		assertEquals(11, tokSeq.getTokens().size());
		assertEquals("the", tokSeq.getTokens().get(0).getValue());
		assertEquals("quick", tokSeq.getTokens().get(1).getValue());
		assertEquals("methyl", tokSeq.getTokens().get(2).getValue());
		assertEquals("brown", tokSeq.getTokens().get(3).getValue());
		assertEquals("ethyl", tokSeq.getTokens().get(4).getValue());
		assertEquals("acetate", tokSeq.getTokens().get(5).getValue());
		assertEquals("jumps", tokSeq.getTokens().get(6).getValue());
		assertEquals("over", tokSeq.getTokens().get(7).getValue());
		assertEquals("the", tokSeq.getTokens().get(8).getValue());
		assertEquals("chlorinated", tokSeq.getTokens().get(9).getValue());
		assertEquals("dog", tokSeq.getTokens().get(10).getValue());
	}
	
	@Test
	public void testMakeTokenSequenceTokeniseForAndMergeNes() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/document/learningSpecificTokenisation.xml");
		Document sourceDoc = new Builder().build(in);
		Tokeniser tokeniser = Tokeniser.getInstance();
		XOMBasedProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeDocument(sourceDoc);
		Element e = (Element) procDoc.getDoc().getRootElement();
		Element para = (Element) procDoc.getDoc().query("//P").get(0); 
		String text = para.getValue();
		int offset = Integer.parseInt(para.getAttributeValue("xtspanstart"));
		
		ITokenSequence tokSeq = XOMBasedProcessingDocumentFactory.getInstance().makeTokenSequence(tokeniser, true, true, null, procDoc, e, text, offset);
		assertEquals(10, tokSeq.getTokens().size());
		assertEquals("the", tokSeq.getTokens().get(0).getValue());
		assertEquals("quick", tokSeq.getTokens().get(1).getValue());
		assertEquals("methyl", tokSeq.getTokens().get(2).getValue());
		assertEquals("brown", tokSeq.getTokens().get(3).getValue());
		assertEquals("ethyl acetate", tokSeq.getTokens().get(4).getValue());
		assertEquals("jumps", tokSeq.getTokens().get(5).getValue());
		assertEquals("over", tokSeq.getTokens().get(6).getValue());
		assertEquals("the", tokSeq.getTokens().get(7).getValue());
		assertEquals("chlorinated", tokSeq.getTokens().get(8).getValue());
		assertEquals("dog", tokSeq.getTokens().get(9).getValue());
	}
	
	
	@Test
	public void testTokenIds() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/document/learningSpecificTokenisation.xml");
		Document sourceDoc = new Builder().build(in);
		Tokeniser tokeniser = Tokeniser.getInstance();
		XOMBasedProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeDocument(sourceDoc);
		Element e = (Element) procDoc.getDoc().getRootElement();
		Element para = (Element) procDoc.getDoc().query("//P").get(0); 
		String text = para.getValue();
		int offset = Integer.parseInt(para.getAttributeValue("xtspanstart"));
		
		ITokenSequence tokSeq = XOMBasedProcessingDocumentFactory.getInstance().makeTokenSequence(tokeniser, false, false, null, procDoc, e, text, offset);
		assertEquals(9, tokSeq.getTokens().size());
		for (int i = 0; i < tokSeq.size(); i++) {
			assertEquals(i, tokSeq.getTokens().get(i).getId());
		}
		
		tokSeq = XOMBasedProcessingDocumentFactory.getInstance().makeTokenSequence(tokeniser, true, false, null, procDoc, e, text, offset);
		assertEquals(11, tokSeq.getTokens().size());
		for (int i = 0; i < tokSeq.size(); i++) {
			assertEquals(i, tokSeq.getTokens().get(i).getId());
		}
		
		tokSeq = XOMBasedProcessingDocumentFactory.getInstance().makeTokenSequence(tokeniser, true, true, null, procDoc, e, text, offset);
		assertEquals(10, tokSeq.getTokens().size());
		for (int i = 0; i < tokSeq.size(); i++) {
			assertEquals(i, tokSeq.getTokens().get(i).getId());
		}
	}
	
	@Test
	public void testTokenIndex() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/document/learningSpecificTokenisation.xml");
		Document sourceDoc = new Builder().build(in);
		Tokeniser tokeniser = Tokeniser.getInstance();
		XOMBasedProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeDocument(sourceDoc);
		//fields aren't initialised by above call
		procDoc.tokensByStart = new HashMap<Integer,IToken>();
		procDoc.tokensByEnd = new HashMap<Integer,IToken>();
		Element e = (Element) procDoc.getDoc().getRootElement();
		Element para = (Element) procDoc.getDoc().query("//P").get(0); 
		String text = para.getValue();
		int offset = Integer.parseInt(para.getAttributeValue("xtspanstart"));
		
		List <IToken> tokens = XOMBasedProcessingDocumentFactory.getInstance().makeTokenSequence(tokeniser, false, false, null, procDoc, e, text, offset).getTokens();
		assertEquals(9, procDoc.getTokensByStart().size());
		assertEquals(9, procDoc.getTokensByEnd().size());
		for (IToken iToken : tokens) {
			assertNotNull(iToken);
			assertTrue(iToken == procDoc.getTokensByStart().get(iToken.getStart()));
			assertTrue(iToken == procDoc.getTokensByEnd().get(iToken.getEnd()));
		}
		
		tokens = XOMBasedProcessingDocumentFactory.getInstance().makeTokenSequence(tokeniser, true, false, null, procDoc, e, text, offset).getTokens();
		assertEquals(11, procDoc.getTokensByStart().size());
		assertEquals(11, procDoc.getTokensByEnd().size());
		for (IToken iToken : tokens) {
			assertNotNull(iToken);
			assertTrue(iToken == procDoc.getTokensByStart().get(iToken.getStart()));
			assertTrue(iToken == procDoc.getTokensByEnd().get(iToken.getEnd()));
		}
		
		tokens = XOMBasedProcessingDocumentFactory.getInstance().makeTokenSequence(tokeniser, true, true, null, procDoc, e, text, offset).getTokens();
		assertEquals(10, procDoc.getTokensByStart().size());
		assertEquals(10, procDoc.getTokensByEnd().size());
		for (IToken iToken : tokens) {
			assertNotNull(iToken);
			assertTrue(iToken == procDoc.getTokensByStart().get(iToken.getStart()));
			assertTrue(iToken == procDoc.getTokensByEnd().get(iToken.getEnd()));
		}
	}
	
	@Test
	public void testTokenSequenceAssignments() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/document/learningSpecificTokenisation.xml");
		Document sourceDoc = new Builder().build(in);
		Tokeniser tokeniser = Tokeniser.getInstance();
		XOMBasedProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeDocument(sourceDoc);
		//FIXME fields aren't initialised by above call... yet
		procDoc.tokensByStart = new HashMap<Integer,IToken>();
		procDoc.tokensByEnd = new HashMap<Integer,IToken>();
		Element e = (Element) procDoc.getDoc().getRootElement();
		Element para = (Element) procDoc.getDoc().query("//P").get(0); 
		String text = para.getValue();
		int offset = Integer.parseInt(para.getAttributeValue("xtspanstart"));
		
		ITokenSequence ts1 = XOMBasedProcessingDocumentFactory.getInstance().makeTokenSequence(tokeniser, false, false, null, procDoc, e, text, offset);
		for (IToken token : ts1.getTokens()) {
			assertTrue(ts1 == token.getTokenSequence());
		}
		assertTrue(e == ((TokenSequence)ts1).getElem());
		
		ITokenSequence ts2 = XOMBasedProcessingDocumentFactory.getInstance().makeTokenSequence(tokeniser, true, false, null, procDoc, e, text, offset);
		assertNotSame(ts2, ts1);
		for (IToken token : ts2.getTokens()) {
			assertTrue(ts2 == token.getTokenSequence());
		}
		assertTrue(e == ((TokenSequence)ts2).getElem());
		
		ITokenSequence ts3 = XOMBasedProcessingDocumentFactory.getInstance().makeTokenSequence(tokeniser, true, true, null, procDoc, e, text, offset);
		assertNotSame(ts3, ts2);
		assertNotSame(ts3, ts1);
		for (IToken token : ts3.getTokens()) {
			assertTrue(ts3 == token.getTokenSequence());
		}
		assertTrue(e == ((TokenSequence)ts3).getElem());
	}
	
}
