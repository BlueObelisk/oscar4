package uk.ac.cam.ch.wwmm.oscar.tools;

import nu.xom.Document;

import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ResourceGetterTest {

	@Test
	public void testLoadFromResourceGetterClasspath() throws Exception {
		URL url = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar/tools/"
			).getURL("DefaultProperties.dat");
		assertNotNull(url);
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
    public void testGetXMLDocument() {
    	ResourceGetter rg = new ResourceGetter("/tools/");
    	Document doc = rg.getXMLDocument("simple.xml");
    	assertEquals("<foo><bar /></foo>", doc.getRootElement().toXML());
    }
    
    @Test
    public void testGetXMLDocumentWithDtd() {
    	System.out.println("--");
    	ResourceGetter rg = new ResourceGetter("/tools/");
    	Document doc = rg.getXMLDocument("docWithDtd.xml");
    	assertEquals("<foo><bar /></foo>", doc.getRootElement().toXML());
    }
}
