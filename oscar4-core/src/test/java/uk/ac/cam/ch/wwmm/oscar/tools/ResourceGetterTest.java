package uk.ac.cam.ch.wwmm.oscar.tools;

import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

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

}
