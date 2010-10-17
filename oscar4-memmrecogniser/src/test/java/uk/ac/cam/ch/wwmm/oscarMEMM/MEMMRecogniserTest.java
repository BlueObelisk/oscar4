package uk.ac.cam.ch.wwmm.oscarMEMM;

import java.util.List;

import nu.xom.Document;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.scixml.TextToSciXML;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * @author egonw
 * @author j_robinson
 */
public class MEMMRecogniserTest {

	@Test public void testConstructor() {
		Assert.assertNotNull(new MEMMRecogniser());
	}

	@Test
	public void testFindNamedEntities() throws Exception {
		ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar3/test/testcard/resources/");
		String s = rg.getString("testcard.txt");
		Assert.assertTrue("Have testcard string", s != null && s.length() > 0);
		Document doc = TextToSciXML.textToSciXML(s);
		
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().
			makeTokenisedDocument(Tokeniser.getInstance(), doc, false, false, false);
		Assert.assertTrue(procDoc != null);
		List<NamedEntity> neList;
		ChemicalEntityRecogniser cei = new MEMMRecogniser();
		neList = cei.findNamedEntities(procDoc.getTokenSequences());
		Assert.assertTrue(neList != null);
		Assert.assertTrue(neList.size() > 0);
	}
}
