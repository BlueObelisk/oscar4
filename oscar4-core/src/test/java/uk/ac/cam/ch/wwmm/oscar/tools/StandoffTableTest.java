package uk.ac.cam.ch.wwmm.oscar.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nu.xom.Builder;
import nu.xom.Element;

import org.junit.Ignore;
import org.junit.Test;

public class StandoffTableTest {

	static Builder XMLBuilder = new Builder();

	@Test
	public void testStandoffTable() throws Exception {
		Element testXML = XMLBuilder.build("<a> <b /><c /> <c /><b /> <b><c /></b> <c><b /></c> \n" +
				" <b><c>_</c></b> <b /><c>_</c> <c><b />_</c> <b>_<c /></b> <b>_</b><c /> \n" +
				" +++***+++ +++*<d>*</d>*+++ +++<d>***</d>+++ +++<d>**</d>*+++ +++*<d>**</d>+++ \n" +
				" +++*<d />**+++ +++*<d><e>*</e></d>*+++ </a>", "/localhost").getRootElement();
		StandoffTable st = new StandoffTable(testXML);
		st.getElemAtOffset(0);
		assertTrue("No exception thown yet!", true);
	}

	@Test
	public void testGetElemAtOffset() throws Exception {
		Element testXML = XMLBuilder.build("<a>012345678<b>9" +
				"0</b>1234<c>5</c>6789" +
				"<d>0<e>1</e>2</d>3<d><e>45</e>6</d>789" +
				"01<f><g>23</g></f>456<i/>789</a>", "/localhost").getRootElement();
		StandoffTable st = new StandoffTable(testXML);
		assertTrue("No exception thown yet!", true);
		assertEquals("Root element at pos 0", "a", st.getElemAtOffset(0).getLocalName());
		assertEquals("b element at pos 10", "b", st.getElemAtOffset(10).getLocalName());
		assertEquals("Root element at pos 14", "a", st.getElemAtOffset(14).getLocalName());
		assertEquals("c element at pos 15", "c", st.getElemAtOffset(15).getLocalName());
		assertEquals("Root element at pos 16", "a", st.getElemAtOffset(16).getLocalName());
		assertEquals("d element at pos 20", "d", st.getElemAtOffset(20).getLocalName());
		assertEquals("e element at pos 21", "e", st.getElemAtOffset(21).getLocalName());
		assertEquals("d element at pos 22", "d", st.getElemAtOffset(22).getLocalName());
		assertEquals("Root element at pos 23", "a", st.getElemAtOffset(23).getLocalName());		
		assertEquals("e element at pos 25", "e", st.getElemAtOffset(25).getLocalName());
		assertEquals("d element at pos 26", "d", st.getElemAtOffset(26).getLocalName());
		assertEquals("Root element at pos 31", "a", st.getElemAtOffset(31).getLocalName());
		assertEquals("g element at pos 32", "g", st.getElemAtOffset(32).getLocalName());
		assertEquals("g element at pos 33", "g", st.getElemAtOffset(33).getLocalName());
		assertEquals("Root element at pos 34", "a", st.getElemAtOffset(34).getLocalName());
		assertEquals("Root element at pos 36", "a", st.getElemAtOffset(36).getLocalName());
		assertEquals("Root element at pos 37", "a", st.getElemAtOffset(37).getLocalName());
		assertEquals("Root element at pos 39", "a", st.getElemAtOffset(39).getLocalName());
		try {
			st.getElemAtOffset(40);
			assertTrue("Should have caught an Exception by now", false);
		} catch (Exception e) {
			assertTrue("Caught an exception: no position 40", true);
		}
	}
	
	@Test
	public void testGetLeftPointAtOffset() throws Exception {
		Element testXML = XMLBuilder.build("<a>012345678<b>9" +
				"0</b>1234<c>5</c>6789" +
				"<d>0<e>1</e>2</d>3<d><e>45</e>6</d>789" +
				"01<f><g>23</g></f>456<i/>789</a>", "/localhost").getRootElement();
		StandoffTable st = new StandoffTable(testXML);
		assertTrue("No exception thown yet!", true);
		assertEquals("At pos 0", "/1/1.0", st.getLeftPointAtOffset(0));
		assertEquals("At pos 1", "/1/1.1", st.getLeftPointAtOffset(1));
		assertEquals("At pos 8", "/1/1.8", st.getLeftPointAtOffset(8));
		assertEquals("At pos 9", "/1/2/1.0", st.getLeftPointAtOffset(9));
		assertEquals("At pos 10", "/1/2/1.1", st.getLeftPointAtOffset(10));
		assertEquals("At pos 11", "/1/3.0", st.getLeftPointAtOffset(11));
		assertEquals("At pos 19", "/1/5.3", st.getLeftPointAtOffset(19));
		assertEquals("At pos 20", "/1/6/1.0", st.getLeftPointAtOffset(20));
		assertEquals("At pos 21", "/1/6/2/1.0", st.getLeftPointAtOffset(21));
		assertEquals("At pos 22", "/1/6/3.0", st.getLeftPointAtOffset(22));
		assertEquals("At pos 23", "/1/7.0", st.getLeftPointAtOffset(23));
		assertEquals("At pos 24", "/1/8/1/1.0", st.getLeftPointAtOffset(24));
		assertEquals("At pos 25", "/1/8/1/1.1", st.getLeftPointAtOffset(25));
		assertEquals("At pos 26", "/1/8/2.0", st.getLeftPointAtOffset(26));

	}

	@Test
	public void testGetRightPointAtOffset() throws Exception {
		Element testXML = XMLBuilder.build("<a>012345678<b>9" +
				"0</b>1234<c>5</c>6789" +
				"<d>0<e>1</e>2</d>3<d><e>45</e>6</d>789" +
				"01<f><g>23</g></f>456<i/>789</a>", "/localhost").getRootElement();
		StandoffTable st = new StandoffTable(testXML);
		assertTrue("No exception thown yet!", true);
		try {
			assertEquals("At pos 0", "/1.0", st.getRightPointAtOffset(0));
			assertTrue("This should throw!", false);
		} catch (Exception e) {
			assertTrue("Throws exception OK!", true);
		}
		assertEquals("At pos 1", "/1/1.1", st.getRightPointAtOffset(1));
		assertEquals("At pos 8", "/1/1.8", st.getRightPointAtOffset(8));
		assertEquals("At pos 9", "/1/1.9", st.getRightPointAtOffset(9));
		assertEquals("At pos 10", "/1/2/1.1", st.getRightPointAtOffset(10));
		assertEquals("At pos 11", "/1/2/1.2", st.getRightPointAtOffset(11));
		assertEquals("At pos 19", "/1/5.3", st.getRightPointAtOffset(19));
		assertEquals("At pos 20", "/1/5.4", st.getRightPointAtOffset(20));
		assertEquals("At pos 21", "/1/6/1.1", st.getRightPointAtOffset(21));
		assertEquals("At pos 22", "/1/6/2/1.1", st.getRightPointAtOffset(22));
		assertEquals("At pos 23", "/1/6/3.1", st.getRightPointAtOffset(23));
		assertEquals("At pos 24", "/1/7.1", st.getRightPointAtOffset(24));
		assertEquals("At pos 25", "/1/8/1/1.1", st.getRightPointAtOffset(25));
		assertEquals("At pos 26", "/1/8/1/1.2", st.getRightPointAtOffset(26));
	}
	
	@Ignore("Ignore until XMLSpanTagger is ported.")
	public void testGetOffsetAtXPoint() throws Exception {
		Element testXML = XMLBuilder.build("<a>012345678<b>9" +
				"0</b>1234<c>5</c>6789" +
				"<d>0<e>1</e>2</d>3<d><e>45</e>6</d>789" +
				"01<f><g>23</g></f>456<i/>789</a>", "/localhost").getRootElement();
//		XMLSpanTagger.tagUpDocument(testXML, "a");
		StandoffTable st = new StandoffTable(testXML);
        assertEquals("At /1/1.1", 1, st.getOffsetAtXPoint("/1/1.1"));
        assertEquals("At /1/1.8", 8, st.getOffsetAtXPoint("/1/1.8"));
        assertEquals("At /1/1.9", 9, st.getOffsetAtXPoint("/1/1.9"));
        assertEquals("At /1/2/1.1", 10, st.getOffsetAtXPoint("/1/2/1.1"));
        assertEquals("At /1/2/1.2", 11, st.getOffsetAtXPoint("/1/2/1.2"));
        assertEquals("At /1/5.3", 19, st.getOffsetAtXPoint("/1/5.3"));
        assertEquals("At /1/5.4", 20, st.getOffsetAtXPoint("/1/5.4"));
        assertEquals("At /1/6/1.1", 21, st.getOffsetAtXPoint("/1/6/1.1"));
        assertEquals("At /1/6/2/1.1", 22, st.getOffsetAtXPoint("/1/6/2/1.1"));
        assertEquals("At /1/6/3.1", 23, st.getOffsetAtXPoint("/1/6/3.1"));
        assertEquals("At /1/7.1", 24, st.getOffsetAtXPoint("/1/7.1"));
        assertEquals("At /1/8/1/1.1", 25, st.getOffsetAtXPoint("/1/8/1/1.1"));
        assertEquals("At /1/8/1/1.2", 26, st.getOffsetAtXPoint("/1/8/1/1.2"));
                
        assertEquals("At /1/1.0", 0, st.getOffsetAtXPoint("/1/1.0"));
        assertEquals("At /1/1.1", 1, st.getOffsetAtXPoint("/1/1.1"));
        assertEquals("At /1/1.8", 8, st.getOffsetAtXPoint("/1/1.8"));
        assertEquals("At /1/2/1.0", 9, st.getOffsetAtXPoint("/1/2/1.0"));
        assertEquals("At /1/2/1.1", 10, st.getOffsetAtXPoint("/1/2/1.1"));
        assertEquals("At /1/3.0", 11, st.getOffsetAtXPoint("/1/3.0"));
        assertEquals("At /1/5.3", 19, st.getOffsetAtXPoint("/1/5.3"));
        assertEquals("At /1/6/1.0", 20, st.getOffsetAtXPoint("/1/6/1.0"));
        assertEquals("At /1/6/2/1.0", 21, st.getOffsetAtXPoint("/1/6/2/1.0"));
        assertEquals("At /1/6/3.0", 22, st.getOffsetAtXPoint("/1/6/3.0"));
        assertEquals("At /1/7.0", 23, st.getOffsetAtXPoint("/1/7.0"));
        assertEquals("At /1/8/1/1.0", 24, st.getOffsetAtXPoint("/1/8/1/1.0"));
        assertEquals("At /1/8/1/1.1", 25, st.getOffsetAtXPoint("/1/8/1/1.1"));
        assertEquals("At /1/8/2.0", 26, st.getOffsetAtXPoint("/1/8/2.0"));
	}

}
