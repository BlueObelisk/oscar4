package uk.ac.cam.ch.wwmm.oscardata;

import static org.junit.Assert.*;

import java.io.InputStream;

import jregex.Matcher;

import nu.xom.Builder;
import nu.xom.Element;

import org.junit.Test;

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
	
}
