package uk.ac.cam.ch.wwmm.oscar;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import nu.xom.Element;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.ITokeniser;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.ont.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscarMEMM.MEMMRecogniser;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.ChemPapersModel;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.PubMedModel;
import uk.ac.cam.ch.wwmm.oscarpattern.PatternRecogniser;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

public class OscarTest {

	@Test
	public void testConstructor() {
		Oscar oscar = new Oscar();
		assertNotNull(oscar);
	}
	
	@Test
	public void testGetDictionaryRegistry() {
		Oscar oscar = new Oscar();
		assertNotNull(oscar.getDictionaryRegistry());
	}
	
	@Test
	public void testOscarLoadingOfDefaultDictionaries() {
		Oscar oscar = new Oscar();
		ChemNameDictRegistry registry = oscar.getDictionaryRegistry();
		// test loading of the ChEBI dictionary
		assertEquals(
			"InChI=1/CH4/h1H4",
			registry.getInChI("methane").iterator().next()
		);
		// test loading of the default dictionary
		assertEquals(
			"InChI=1/C4H6O3/c1-3(5)7-4(2)6/h1-2H3",
			registry.getInChI("Ac2O").iterator().next()
		);
	}

	@Test
	public void testGetNamedEntities() {
		Oscar oscar = new Oscar();
		List<NamedEntity> entities =
			oscar.getNamedEntities("Then we mix benzene with toluene.");
		assertEquals(2, entities.size());
		assertEquals("benzene", entities.get(0).getSurface());
		assertEquals("toluene", entities.get(1).getSurface());
	}

	@Test
	public void testGetResolvedEntities() {
		Oscar oscar = new Oscar();
		Map<NamedEntity,String> entities =
			oscar.getResolvedEntities("Then we mix benzene with toluene.");
		assertNotNull(entities);
		assertEquals(2, entities.size());
		for (NamedEntity ne : entities.keySet()) {
			if ("benzene".equals(ne.getSurface())) {
				assertEquals("InChI=1/C6H6/c1-2-4-6-5-3-1/h1-6H", entities.get(ne));
			}
			else if ("toluene".equals(ne.getSurface())) {
				assertEquals("InChI=1/C7H8/c1-7-5-3-2-4-6-7/h2-6H,1H3", entities.get(ne));
			}
			else {
				fail();
			}
		}
	}
	
	
	@Test (expected = IllegalArgumentException.class)
	public void testSetRecogniserRejectsNull() {
		Oscar oscar = new Oscar();
		oscar.setRecogniser(null);
	}
	
	@Test
	public void testSetRecogniser() {
		Oscar oscar = new Oscar();
		assertTrue(oscar.getRecogniser() instanceof MEMMRecogniser);
		oscar.setRecogniser(new PatternRecogniser());
		assertTrue(oscar.getRecogniser() instanceof PatternRecogniser);
	}
	
	@Test
	public void testSetRecogniserModel() {
		Oscar oscar = new Oscar();
		MEMMRecogniser chempapersRecogniser = new MEMMRecogniser(new ChemPapersModel(), OntologyTerms.getDefaultInstance());
		oscar.setRecogniser(chempapersRecogniser);
		assertTrue(((MEMMRecogniser)oscar.getRecogniser()).getModel() instanceof ChemPapersModel);

		MEMMRecogniser pubmedRecogniser = new MEMMRecogniser(new PubMedModel(), OntologyTerms.getDefaultInstance());
		oscar.setRecogniser(pubmedRecogniser);
		assertTrue(((MEMMRecogniser)oscar.getRecogniser()).getModel() instanceof PubMedModel);
	}
	
	@Test
	public void testTokenise() {
		Oscar oscar = new Oscar();
		List <ITokenSequence> tokSeqs = oscar.tokenise("Then we mix benzene with toluene.");
		assertEquals(1, tokSeqs.size());
		ITokenSequence tokSeq = tokSeqs.get(0);
		assertEquals(7, tokSeq.getTokens().size());
		assertEquals("Then", tokSeq.getTokens().get(0).getSurface());
		assertEquals("we", tokSeq.getTokens().get(1).getSurface());
		assertEquals("mix", tokSeq.getTokens().get(2).getSurface());
		assertEquals("benzene", tokSeq.getTokens().get(3).getSurface());
		assertEquals("with", tokSeq.getTokens().get(4).getSurface());
		assertEquals("toluene", tokSeq.getTokens().get(5).getSurface());
		assertEquals(".", tokSeq.getTokens().get(6).getSurface());
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testSetTokeniserRejectsNull() {
		Oscar oscar = new Oscar();
		oscar.setTokeniser(null);
	}
	
	@Test
	public void testSetTokeniser() {
		Oscar oscar = new Oscar();
		assertTrue(oscar.getTokeniser() == Tokeniser.getDefaultInstance());
		ITokeniser tokeniser = new TokeniserImpl();
		oscar.setTokeniser(tokeniser);
		assertTrue(oscar.getTokeniser() == tokeniser);
	}
	
	
	class TokeniserImpl implements ITokeniser {

		public ITokenSequence tokenise(String text,
				IProcessingDocument procDoc, int offset, Element element) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
