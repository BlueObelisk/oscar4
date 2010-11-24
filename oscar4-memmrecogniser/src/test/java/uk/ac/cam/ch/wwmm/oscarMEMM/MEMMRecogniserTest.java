package uk.ac.cam.ch.wwmm.oscarMEMM;

import static org.junit.Assert.*;

import java.util.List;

import nu.xom.Document;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscar.scixml.TextToSciXML;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * @author egonw
 * @author j_robinson
 */
public class MEMMRecogniserTest {

	@Test public void testConstructor() {
		assertNotNull(new MEMMRecogniser());
	}

	@Test
	public void testFindNamedEntities() throws Exception {
		ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar3/test/testcard/resources/");
		String s = rg.getString("testcard.txt");
		assertTrue("Have testcard string", s != null && s.length() > 0);
		Document doc = TextToSciXML.textToSciXML(s);
		
		IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().
			makeTokenisedDocument(Tokeniser.getInstance(), doc, false, false, false);
		assertTrue(procDoc != null);
		List<NamedEntity> neList;
		ChemicalEntityRecogniser cei = new MEMMRecogniser();
		neList = cei.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neList != null);
		assertEquals("Only acetone should be recognized", 1, neList.size());
		assertEquals("acetone", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindNamedEntitiesFromString() throws Exception {
		String source = "Hello acetone world!";
		ProcessingDocument procDoc = new ProcessingDocumentFactory().makeTokenisedDocument(Tokeniser.getInstance(), source);
		List <NamedEntity> neList = new MEMMRecogniser().findNamedEntities(procDoc);
		assertEquals(1, neList.size());
		assertEquals("acetone", neList.get(0).getSurface());
	}
}
