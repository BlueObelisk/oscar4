package uk.ac.cam.ch.wwmm.oscar.xmltools;

import static junit.framework.Assert.assertEquals;
import nu.xom.Builder;
import nu.xom.Element;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.xmltools.XMLSpanTagger;

/**
 * @author Peter Corbett
 */
public class XMLInserterTest {
	
	static Builder XMLBuilder = new Builder();

	@Test
	public void testIncorporateNewTags() throws Exception {
		Element baselineXML = XMLBuilder.build("<a> <b /><c /> <c /><b /> <b><c /></b> <c><b /></c> \n" +
				" <b><c>_</c></b> <b /><c>_</c> <c><b />_</c> <b>_<c /></b> <b>_</b><c /> \n" +
				" +++***+++ +++*<d>*</d>*+++ +++<d>***</d>+++ +++<d>**</d>*+++ +++*<d>**</d>+++ \n" +
				" +++*<d />**+++ +++*<d><e>*</e></d>*+++ </a>", "/localhost").getRootElement();
		
		Element baselineXMLCopy = new Element(baselineXML);
		Element newXML = XMLBuilder.build("<a>     \n" +
				" _ _ _ _ _ \n" +
				" +++<z>***</z>+++ +++***+++ +++<x>***</x>+++ +++***+++ +++***+++ \n" +
				" +++*<w>**+++</w> +++***+++ </a>", "/localhost").getRootElement();
		String expectedOut = ("<a> <b /><c /> <c /><b /> <b><c /></b> <c><b /></c> \n" +
				" <b><c>_</c></b> <b /><c>_</c> <c><b />_</c> <b>_<c /></b> <b>_</b><c /> \n" +
				" +++<z>***</z>+++ +++*<d>*</d>*+++ +++<d><x>***</x></d>+++ +++<d>**</d>*+++ +++*<d>**</d>+++ \n" +
				" +++*<d /><w>**+++</w> +++*<d><e>*</e></d>*+++ </a>");
		XMLInserter xi = new XMLInserter(baselineXMLCopy, "a", "c");
		XMLSpanTagger.tagUpDocument(newXML, "b");
		xi.incorporateElementsFromRetaggedDocument(newXML, "b");
		
		xi.deTagDocument();		
		assertEquals("Insertion goes in OK", expectedOut, baselineXMLCopy.toXML());
	}

}
