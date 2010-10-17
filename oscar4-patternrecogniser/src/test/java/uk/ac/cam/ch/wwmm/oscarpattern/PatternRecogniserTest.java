package uk.ac.cam.ch.wwmm.oscarpattern;

import static org.junit.Assert.assertTrue;

import java.util.List;

import nu.xom.Document;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscar.scixml.TextToSciXML;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * @author egonw
 */
public class PatternRecogniserTest {

	@Test public void testConstructor() {
		Assert.assertNotNull(new PatternRecogniser());
	}

	@Test
	public void test_findNamedEntities() throws Exception
	{
		ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar/test/testcard/");
		String s = rg.getString("testcard.txt");
		assertTrue("Have testcard string", s != null && s.length() > 0);
		Document doc = TextToSciXML.textToSciXML(s);

		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
			Tokeniser.getInstance(), doc, false, false, false);
		assertTrue(procDoc != null);
		List<NamedEntity> neList;
		ChemicalEntityRecogniser cei = new PatternRecogniser();
		neList = cei.findNamedEntities(procDoc);
		assertTrue(neList != null);
		assertTrue(neList.size() > 0);
	}
}
