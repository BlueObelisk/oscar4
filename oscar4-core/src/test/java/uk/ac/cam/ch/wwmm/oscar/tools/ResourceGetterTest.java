package uk.ac.cam.ch.wwmm.oscar.tools;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nu.xom.Document;

import org.junit.Ignore;
import org.junit.Test;

public class ResourceGetterTest {

	@Test
	public void testLoadFromResourceGetterClasspath() throws Exception {
		InputStream stream = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar/tools/"
			).getStream("DefaultProperties.dat");
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
    public void testGetXMLDocument() {
    	ResourceGetter rg = new ResourceGetter("/tools/");
    	Document doc = rg.getXMLDocument("simple.xml");
    	assertEquals("<foo><bar /></foo>", doc.getRootElement().toXML());
    }
    
    @Ignore
    //NYI
    @Test
    public void testGetXMLDocumentWithDtd() {
    	ResourceGetter rg = new ResourceGetter("/tools/");
    	Document doc = rg.getXMLDocument("docWithDtd.xml");
    	assertEquals("<foo><bar /></foo>", doc.getRootElement().toXML());
    }
}
