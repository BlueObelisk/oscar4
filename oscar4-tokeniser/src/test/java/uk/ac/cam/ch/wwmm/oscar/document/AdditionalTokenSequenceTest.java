package uk.ac.cam.ch.wwmm.oscar.document;

import static org.junit.Assert.*;

import java.io.InputStream;

import nu.xom.Builder;
import nu.xom.Document;

import org.junit.AfterClass;
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
	private static TokenSequence header;
	//The quick brown fox jumps over the lazy dog.
	private static TokenSequence para1;
	//The slow green turtle sneaks under the watchful cat.
	private static TokenSequence para2;
	
	@BeforeClass 
	public static void setUp() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscar/document/testDoc.xml");
		Document doc = new Builder().build(in);
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(Tokeniser.getDefaultInstance(), doc);
		header = procDoc.getTokenSequences().get(0);
		para1 = procDoc.getTokenSequences().get(1);
		para2 = procDoc.getTokenSequences().get(2);
		
		assertEquals(0, header.getTokens().size());
		assertEquals(10, para1.getTokens().size());
		assertEquals(10, para2.getTokens().size());
		assertEquals(44, para1.getSurface().length());
		assertEquals(52, para2.getSurface().length());
	}
	
	@AfterClass
	public static void cleanUp() {
		header = null;
		para1 = null;
		para2 = null;
	}
	
	@Test
	public void testGetTokenByStartIndex() {
		assertEquals("The", para1.getTokenByStartIndex(0).getSurface());
		assertEquals("quick", para1.getTokenByStartIndex(4).getSurface());
		assertEquals("brown", para1.getTokenByStartIndex(10).getSurface());
		assertEquals("fox", para1.getTokenByStartIndex(16).getSurface());
		assertEquals("jumps", para1.getTokenByStartIndex(20).getSurface());
		assertEquals("over", para1.getTokenByStartIndex(26).getSurface());
		assertEquals("the", para1.getTokenByStartIndex(31).getSurface());
		assertEquals("lazy", para1.getTokenByStartIndex(35).getSurface());
		assertEquals("dog", para1.getTokenByStartIndex(40).getSurface());
		assertEquals(".", para1.getTokenByStartIndex(43).getSurface());
		
		assertEquals("The", para2.getTokenByStartIndex(44).getSurface());
		assertEquals("slow", para2.getTokenByStartIndex(48).getSurface());
		assertEquals("green", para2.getTokenByStartIndex(53).getSurface());
		assertEquals("turtle", para2.getTokenByStartIndex(59).getSurface());
		assertEquals("sneaks", para2.getTokenByStartIndex(66).getSurface());
		assertEquals("under", para2.getTokenByStartIndex(73).getSurface());
		assertEquals("the", para2.getTokenByStartIndex(79).getSurface());
		assertEquals("watchful", para2.getTokenByStartIndex(83).getSurface());
		assertEquals("cat", para2.getTokenByStartIndex(92).getSurface());
		assertEquals(".", para2.getTokenByStartIndex(95).getSurface());
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
		assertEquals("The", para1.getTokenByEndIndex(3).getSurface());
		assertEquals("quick", para1.getTokenByEndIndex(9).getSurface());
		assertEquals("brown", para1.getTokenByEndIndex(15).getSurface());
		assertEquals("fox", para1.getTokenByEndIndex(19).getSurface());
		assertEquals("jumps", para1.getTokenByEndIndex(25).getSurface());
		assertEquals("over", para1.getTokenByEndIndex(30).getSurface());
		assertEquals("the", para1.getTokenByEndIndex(34).getSurface());
		assertEquals("lazy", para1.getTokenByEndIndex(39).getSurface());
		assertEquals("dog", para1.getTokenByEndIndex(43).getSurface());
		assertEquals(".", para1.getTokenByEndIndex(44).getSurface());
		
		assertEquals("The", para2.getTokenByEndIndex(47).getSurface());
		assertEquals("slow", para2.getTokenByEndIndex(52).getSurface());
		assertEquals("green", para2.getTokenByEndIndex(58).getSurface());
		assertEquals("turtle", para2.getTokenByEndIndex(65).getSurface());
		assertEquals("sneaks", para2.getTokenByEndIndex(72).getSurface());
		assertEquals("under", para2.getTokenByEndIndex(78).getSurface());
		assertEquals("the", para2.getTokenByEndIndex(82).getSurface());
		assertEquals("watchful", para2.getTokenByEndIndex(91).getSurface());
		assertEquals("cat", para2.getTokenByEndIndex(95).getSurface());
		assertEquals(".", para2.getTokenByEndIndex(96).getSurface());
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
