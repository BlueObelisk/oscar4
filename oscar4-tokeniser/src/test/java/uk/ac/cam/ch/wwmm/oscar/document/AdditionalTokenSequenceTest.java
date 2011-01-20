package uk.ac.cam.ch.wwmm.oscar.document;

import static org.junit.Assert.*;

import java.io.InputStream;

import nu.xom.Builder;
import nu.xom.Document;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;


/**
 * 
 * Further tests for the TokenSequence class that rely on the Tokeniser implementation.
 * 
 * @author dmj30
 *
 */
public class AdditionalTokenSequenceTest {

	//empty
	private static ITokenSequence header;
	//The quick brown fox jumps over the lazy dog.
	private static ITokenSequence para1;
	//The slow green turtle sneaks under the watchful cat.
	private static ITokenSequence para2;
	
	@BeforeClass 
	public static void setUp() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/document/testDoc.xml");
		Document doc = new Builder().build(in);
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(Tokeniser.getInstance(), doc);
		header = procDoc.getTokenSequences().get(0);
		para1 = procDoc.getTokenSequences().get(1);
		para2 = procDoc.getTokenSequences().get(2);
		
		assertEquals(0, header.getTokens().size());
		assertEquals(10, para1.getTokens().size());
		assertEquals(10, para2.getTokens().size());
		assertEquals(44, para1.getSurface().length());
		assertEquals(52, para2.getSurface().length());
	}
	
	@Test
	public void testGetTokenByStartIndex() {
		assertEquals("The", para1.getTokenByStartIndex(0).getValue());
		assertEquals("quick", para1.getTokenByStartIndex(4).getValue());
		assertEquals("brown", para1.getTokenByStartIndex(10).getValue());
		assertEquals("fox", para1.getTokenByStartIndex(16).getValue());
		assertEquals("jumps", para1.getTokenByStartIndex(20).getValue());
		assertEquals("over", para1.getTokenByStartIndex(26).getValue());
		assertEquals("the", para1.getTokenByStartIndex(31).getValue());
		assertEquals("lazy", para1.getTokenByStartIndex(35).getValue());
		assertEquals("dog", para1.getTokenByStartIndex(40).getValue());
		assertEquals(".", para1.getTokenByStartIndex(43).getValue());
		
		assertEquals("The", para2.getTokenByStartIndex(44).getValue());
		assertEquals("slow", para2.getTokenByStartIndex(48).getValue());
		assertEquals("green", para2.getTokenByStartIndex(53).getValue());
		assertEquals("turtle", para2.getTokenByStartIndex(59).getValue());
		assertEquals("sneaks", para2.getTokenByStartIndex(66).getValue());
		assertEquals("under", para2.getTokenByStartIndex(73).getValue());
		assertEquals("the", para2.getTokenByStartIndex(79).getValue());
		assertEquals("watchful", para2.getTokenByStartIndex(83).getValue());
		assertEquals("cat", para2.getTokenByStartIndex(92).getValue());
		assertEquals(".", para2.getTokenByStartIndex(95).getValue());
	}

	
	@Test
	public void testGetTokenByStartIndexNoSuchToken() {
		assertNull(para1.getTokenByStartIndex(1));
		assertNull(para1.getTokenByStartIndex(8));
		assertNull(para1.getTokenByStartIndex(17));
		assertNull(para1.getTokenByStartIndex(38));
	}
	
	@Test (expected = ArrayIndexOutOfBoundsException.class)
	public void testGetTokenByStartIndexOutOfRangeAtStart() {
		para2.getTokenByStartIndex(43);
	}
	
	@Test (expected = ArrayIndexOutOfBoundsException.class)
	public void testGetTokenByStartIndexOutOfRangeAtEnd() {
		para1.getTokenByStartIndex(45);
	}
	
	
	@Test
	public void testGetTokenByEndIndex() {
		assertEquals("The", para1.getTokenByEndIndex(3).getValue());
		assertEquals("quick", para1.getTokenByEndIndex(9).getValue());
		assertEquals("brown", para1.getTokenByEndIndex(15).getValue());
		assertEquals("fox", para1.getTokenByEndIndex(19).getValue());
		assertEquals("jumps", para1.getTokenByEndIndex(25).getValue());
		assertEquals("over", para1.getTokenByEndIndex(30).getValue());
		assertEquals("the", para1.getTokenByEndIndex(34).getValue());
		assertEquals("lazy", para1.getTokenByEndIndex(39).getValue());
		assertEquals("dog", para1.getTokenByEndIndex(43).getValue());
		assertEquals(".", para1.getTokenByEndIndex(44).getValue());
		
		assertEquals("The", para2.getTokenByEndIndex(47).getValue());
		assertEquals("slow", para2.getTokenByEndIndex(52).getValue());
		assertEquals("green", para2.getTokenByEndIndex(58).getValue());
		assertEquals("turtle", para2.getTokenByEndIndex(65).getValue());
		assertEquals("sneaks", para2.getTokenByEndIndex(72).getValue());
		assertEquals("under", para2.getTokenByEndIndex(78).getValue());
		assertEquals("the", para2.getTokenByEndIndex(82).getValue());
		assertEquals("watchful", para2.getTokenByEndIndex(91).getValue());
		assertEquals("cat", para2.getTokenByEndIndex(95).getValue());
		assertEquals(".", para2.getTokenByEndIndex(96).getValue());
	}
	
	@Test
	public void testGetTokenByEndIndexNoSuchToken() {
		assertNull(para1.getTokenByEndIndex(4));
		assertNull(para1.getTokenByEndIndex(8));
		assertNull(para1.getTokenByEndIndex(11));
		assertNull(para1.getTokenByEndIndex(27));
	}
	
	@Test (expected = ArrayIndexOutOfBoundsException.class)
	public void testGetTokenByEndIndexOutOfRangeAtStart() {
		para2.getTokenByEndIndex(43);
	}
	
	@Test (expected = ArrayIndexOutOfBoundsException.class)
	public void testGetTokenByEndIndexOutOfRangeAtEnd() {
		para1.getTokenByEndIndex(45);
	}
}
