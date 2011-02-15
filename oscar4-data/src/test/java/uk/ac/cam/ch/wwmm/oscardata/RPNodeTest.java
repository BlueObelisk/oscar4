package uk.ac.cam.ch.wwmm.oscardata;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;

import jregex.Matcher;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Text;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;

public class RPNodeTest {

	@Test
	public void testConstructor() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscardata/dataNode.xml");
		Element dataNodeSource = new Builder().build(in).getRootElement();
		RParser parser = RParser.getInstance();
		RPNode rpNode = new RPNode(parser, dataNodeSource);
		
		assertEquals("testId", rpNode.getId());
		assertEquals("testValue", rpNode.getValue());
		assertEquals(42, rpNode.getParseGroup());
		assertEquals("testType", rpNode.getType());
		assertEquals("testValue", rpNode.getValue());
		assertFalse(rpNode.isSaf());
		assertTrue(rpNode.isUnique());
		assertTrue(rpNode.getrParser() == parser);
		Matcher matcher = rpNode.getPattern().matcher("testRegex");
		assertTrue(matcher.matches());
		assertEquals("testRegex", matcher.group(0));
		assertEquals(0, rpNode.getChildren().size());
	}
	
	@Test
	public void testConstructorRecursive() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscardata/dataNodeWithChild.xml");
		Element dataNodeSource = new Builder().build(in).getRootElement();
		RParser parser = RParser.getInstance();
		RPNode rpNode = new RPNode(parser, dataNodeSource);
		
		assertEquals("testId", rpNode.getId());
		assertEquals("testValue", rpNode.getValue());
		assertEquals(42, rpNode.getParseGroup());
		assertEquals("testType", rpNode.getType());
		assertEquals("testValue", rpNode.getValue());
		assertTrue(rpNode.isSaf());
		assertFalse(rpNode.isUnique());
		assertTrue(rpNode.getrParser() == parser);
		Matcher matcher = rpNode.getPattern().matcher("fookgbar");
		assertTrue(matcher.matches());
		assertEquals("fookgbar", matcher.group(0));
		assertEquals(0, rpNode.getChildren().size());
		//adding children is managed by RParser.readXML
	}
	
	
	@Test
	public void testParseXOMText() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscardata/dataNode.xml");
		Element dataNodeSource = new Builder().build(in).getRootElement();
		RParser parser = RParser.getInstance();
		RPNode topNode = new RPNode(parser);
		topNode.addChild(dataNodeSource);
		Text textNode = new Text("The quick brown testRegex jumps over the lazy dog");
		Element dummy = new Element("dummy"); 
		dummy.appendChild(textNode);
		
		assertEquals(0, dummy.getChildElements().size());
		topNode.parseXOMText(textNode);
		assertEquals(1, dummy.getChildElements().size());
		assertEquals("testRegex", dummy.getChildElements().get(0).getValue());
	}
	
	
	@Test
	public void testAnnotateData() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscardata/dataNode.xml");
		Element dataNodeSource = new Builder().build(in).getRootElement();
		RParser parser = RParser.getInstance();
		RPNode topNode = new RPNode(parser);
		topNode.addChild(dataNodeSource);
		TokenSequence tokSeq = new TokenSequence("The quick brown testRegex jumps over the lazy dog", 0, null, null);
		
		List <DataAnnotation> annotations = topNode.annotateData(tokSeq);
		assertEquals(1, annotations.size());
		assertEquals(16, annotations.get(0).getStart());
		assertEquals(25, annotations.get(0).getEnd());
		assertEquals("testRegex", annotations.get(0).getSurface());
		assertEquals("<testType type=\"testValue\">testRegex</testType>", annotations.get(0).getAnnotatedElement().toXML());
	}
	
	@Test
	public void testAnnotateDataWithChild() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscardata/dataNodeWithChild.xml");
		Element dataNodeSource = new Builder().build(in).getRootElement();
		RParser parser = RParser.getInstance();
		RPNode topNode = new RPNode(parser);
		topNode.addChild(dataNodeSource);
		TokenSequence tokSeq = new TokenSequence("The quick brown fookgbar jumps over the lazy dog", 0, null, null);
		
		List <DataAnnotation> annotations = topNode.annotateData(tokSeq);
		assertEquals(1, annotations.size());
		assertEquals(16, annotations.get(0).getStart());
		assertEquals(24, annotations.get(0).getEnd());
		assertEquals("fookgbar", annotations.get(0).getSurface());
		assertEquals("<testType type=\"testValue\" saf=\"yes\">foo<units>kg</units>bar</testType>", annotations.get(0).getAnnotatedElement().toXML());
	}
	
	
}
