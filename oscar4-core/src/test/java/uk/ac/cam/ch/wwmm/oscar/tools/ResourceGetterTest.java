package uk.ac.cam.ch.wwmm.oscar.tools;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nu.xom.Document;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author egonw
 * @author Sam Adams
 * @author dmj30
 *
 */
public class ResourceGetterTest {

	@Test
	public void testLoadFromResourceGetterClasspath() throws Exception {
		InputStream stream = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar/terms/"
			).getStream("abbreviations.txt");
		assertNotNull(stream);
	}


    @Test
    public void testRemoveComments() {
        List<String> list = new ArrayList<String>();
        list.add("hello0");
        list.add("#bar");
        list.add("hello1#boo");
        list.add("hello2   #foo");

        assertEquals(Arrays.asList("hello0", "hello1", "hello2"), ResourceGetter.removeComments(list));
    }

    @Test
    public void testRemoveEmptyLines() {
        List<String> list = new ArrayList<String>();
        list.add("hello0");
        list.add("");
        list.add("hello1#boo");
        list.add("");

        assertEquals(
        	Arrays.asList("hello0", "hello1"),
        	ResourceGetter.removeComments(list)
        );
    }

    @Test
    public void testGetXMLDocument() throws Exception {
    	ResourceGetter rg = new ResourceGetter("/tools/");
    	Document doc = rg.getXMLDocument("simple.xml");
    	assertEquals("<foo><bar /></foo>", doc.getRootElement().toXML());
    }
    
    @Ignore
    //NYI
    @Test
    public void testGetXMLDocumentWithDtd() throws Exception {
    	ResourceGetter rg = new ResourceGetter("/tools/");
    	Document doc = rg.getXMLDocument("docWithDtd.xml");
    	assertEquals("<foo><bar /></foo>", doc.getRootElement().toXML());
    }
    
    @Test
    public void testGetStringsDefaultEncoding() throws Exception {
    	ResourceGetter rg = new ResourceGetter("/");
    	List <String> lines = rg.getStrings("utf-8.txt");
    	assertEquals(2, lines.size());
    	assertEquals("line 1", lines.get(0));
    	assertEquals("line \u03B2", lines.get(1));
    }
    
    @Test
    public void testGetStringsAsciiEncoding() throws Exception {
    	ResourceGetter rg = new ResourceGetter("/");
    	List <String> lines = rg.getStrings("utf-8.txt", "ASCII");
    	assertEquals(2, lines.size());
    	assertEquals("line 1", lines.get(0));
    	assertFalse("line \u03B2".equals(lines.get(1)));
    	assertTrue(lines.get(1).startsWith("line "));
    }
    
    @Test (expected = UnsupportedCharsetException.class)
    public void testGetStringsUnsupportedCharsetException() throws Exception {
    	ResourceGetter rg = new ResourceGetter("/");
    	rg.getStrings("utf-8.txt", "sdfsgsodighsdoig");
    }
    
    @Test (expected = FileNotFoundException.class)
    public void testGetStringsMissingFile() throws Exception {
    	ResourceGetter rg = new ResourceGetter("/");
    	rg.getStrings("skdfgsg", "UTF-8");
    }
}
