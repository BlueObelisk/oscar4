package uk.ac.cam.ch.wwmm.oscar.document;

import static org.junit.Assert.*;

import java.io.InputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

public class XOMBasedProcessingDocumentFactoryTest {

	@Test
	public void testMakeTokenSequenceStandard() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/document/learningSpecificTokenisation.xml");
		Document sourceDoc = new Builder().build(in);
		Tokeniser tokeniser = Tokeniser.getInstance();
		XOMBasedProcessingDocument procDoc = XOMBasedProcessingDocumentFactory.getInstance().makeDocument(sourceDoc);
		Element e = (Element) sourceDoc.query("//P").get(0);
		String text = "the quick methylbrown ethyl acetate jumps over thechlorinated dog";
		
		ITokenSequence tokSeq = XOMBasedProcessingDocumentFactory.getInstance().makeTokenSequence(tokeniser, false, false, null, procDoc, e, text, 0);
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
	
}
