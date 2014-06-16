package uk.ac.cam.ch.wwmm.oscar;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;

import nu.xom.Element;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.FormatType;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ResolvedNamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ITokeniser;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.ont.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscarMEMM.MEMMRecogniser;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMModel;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.ChemPapersModel;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.PubMedModel;
import uk.ac.cam.ch.wwmm.oscarpattern.PatternRecogniser;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

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
			"InChI=1S/CH4/h1H4",
			registry.getStdInchis("methane").iterator().next()
			);
		assertEquals(
			"VNWKTOKETHGBQD-UHFFFAOYSA-N",
			registry.getStdInchiKeys("methane").iterator().next()
			);
		// test loading of the default dictionary
		assertEquals(
			"InChI=1S/C4H6O3/c1-3(5)7-4(2)6/h1-2H3",
			registry.getStdInchis("Ac2O").iterator().next()
			);
		assertEquals(
			"WFDIJRYMOXRFFG-UHFFFAOYSA-N",
			registry.getStdInchiKeys("Ac2O").iterator().next()
			);
	}

	@Test
	public void testFindNamedEntities() {
		Oscar oscar = new Oscar();
		List<NamedEntity> entities =
			oscar.findNamedEntities("Then we mix benzene with toluene.");
		assertEquals(2, entities.size());
		assertEquals("benzene", entities.get(0).getSurface());
		assertEquals("toluene", entities.get(1).getSurface());
	}

	@Test
	public void testFindResolvableEntities() {
		Oscar oscar = new Oscar();
		oscar.setDictionaryRegistry(ChemNameDictRegistry.getDefaultInstance());
		List <ResolvedNamedEntity> entities =
			oscar.findResolvableEntities("Then we mix benzene with napthyridine and toluene.");
		assertEquals(2, entities.size());
		
		assertEquals("benzene", entities.get(0).getNamedEntity().getSurface());
		assertEquals("c1ccccc1", entities.get(0).getFirstChemicalStructure(FormatType.SMILES).getValue());
		assertEquals("InChI=1S/C6H6/c1-2-4-6-5-3-1/h1-6H", entities.get(0).getFirstChemicalStructure(FormatType.STD_INCHI).getValue());
		assertNull(entities.get(0).getFirstChemicalStructure(FormatType.CML));
		
		assertEquals("toluene", entities.get(1).getNamedEntity().getSurface());
		assertEquals("Cc1ccccc1", entities.get(1).getFirstChemicalStructure(FormatType.SMILES).getValue());
		assertEquals("InChI=1S/C7H8/c1-7-5-3-2-4-6-7/h2-6H,1H3", entities.get(1).getFirstChemicalStructure(FormatType.STD_INCHI).getValue());
		assertNull(entities.get(1).getFirstChemicalStructure(FormatType.CML));
	}

    @Test
	public void testFindAndResolveNamedEntities() {
		Oscar oscar = new Oscar();
		oscar.setDictionaryRegistry(ChemNameDictRegistry.getDefaultInstance());
		List <ResolvedNamedEntity> entities =
			oscar.findAndResolveNamedEntities("Then we mix benzene with napthyridine and toluene.");
		assertEquals(3, entities.size());

		assertEquals("benzene", entities.get(0).getNamedEntity().getSurface());
		assertEquals("c1ccccc1", entities.get(0).getFirstChemicalStructure(FormatType.SMILES).getValue());
		assertEquals("InChI=1S/C6H6/c1-2-4-6-5-3-1/h1-6H", entities.get(0).getFirstChemicalStructure(FormatType.STD_INCHI).getValue());
		assertNull(entities.get(0).getFirstChemicalStructure(FormatType.CML));

        assertEquals("napthyridine", entities.get(1).getNamedEntity().getSurface());
        assertNull(entities.get(1).getFirstChemicalStructure(FormatType.SMILES));
        assertNull(entities.get(1).getFirstChemicalStructure(FormatType.STD_INCHI));
		assertNull(entities.get(1).getFirstChemicalStructure(FormatType.CML));

		assertEquals("toluene", entities.get(2).getNamedEntity().getSurface());
		assertEquals("Cc1ccccc1", entities.get(2).getFirstChemicalStructure(FormatType.SMILES).getValue());
		assertEquals("InChI=1S/C7H8/c1-7-5-3-2-4-6-7/h2-6H,1H3", entities.get(2).getFirstChemicalStructure(FormatType.STD_INCHI).getValue());
		assertNull(entities.get(2).getFirstChemicalStructure(FormatType.CML));
	}
	
	@Test
	public void testFindResolvableNonDictionaryEntities() {
		//TODO fix reliance on inchi library
		String testName = "methylethane";
		Oscar oscar = new Oscar();
		assertFalse(oscar.getDictionaryRegistry().hasName(testName));
		List <ResolvedNamedEntity> entities =
			oscar.findResolvableEntities("before adding the " + testName);
		assertEquals(1, entities.size());
		assertEquals(testName, entities.get(0).getNamedEntity().getSurface());
		assertEquals("CCC", entities.get(0).getFirstChemicalStructure(FormatType.SMILES).getValue());
		assertEquals("InChI=1S/C3H8/c1-3-2/h3H2,1-2H3", entities.get(0).getFirstChemicalStructure(FormatType.STD_INCHI).getValue());
		assertNotNull(entities.get(0).getFirstChemicalStructure(FormatType.CML));
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
		MEMMRecogniser chempapersRecogniser = new MEMMRecogniser(
				new ChemPapersModel(), OntologyTerms.getDefaultInstance(),
				new ChemNameDictRegistry(Locale.ENGLISH));
		oscar.setRecogniser(chempapersRecogniser);
		assertTrue(((MEMMRecogniser)oscar.getRecogniser()).getModel() instanceof ChemPapersModel);

		MEMMRecogniser pubmedRecogniser = new MEMMRecogniser(
				new PubMedModel(), OntologyTerms.getDefaultInstance(),
				new ChemNameDictRegistry(Locale.ENGLISH));
		oscar.setRecogniser(pubmedRecogniser);
		assertTrue(((MEMMRecogniser)oscar.getRecogniser()).getModel() instanceof PubMedModel);
	}
	
	@Test
	public void testTokenise() {
		Oscar oscar = new Oscar();
		List <TokenSequence> tokSeqs = oscar.tokenise("Then we mix benzene with toluene.");
		assertEquals(1, tokSeqs.size());
		TokenSequence tokSeq = tokSeqs.get(0);
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
	
	
	@Test
	public void setOntologyTerms() {
		Oscar oscar = new Oscar();
		assertTrue(oscar.getOntologyTerms() == OntologyTerms.getDefaultInstance());
		ListMultimap<String, String> terms = ArrayListMultimap.create();
		OntologyTerms ontologyTerms = new OntologyTerms(terms);
		oscar.setOntologyTerms(ontologyTerms);
		assertTrue(oscar.getOntologyTerms() == ontologyTerms);
	}
	
	
	@Test
	public void testSetMemmModel() {
		Oscar oscar = new Oscar();
		assertTrue(oscar.getMemmModel() instanceof ChemPapersModel);
		oscar.setMemmModel(new PubMedModel());
		assertTrue(oscar.getMemmModel() instanceof PubMedModel);
	}
	
	
	@Test
	public void testSetDictionaryRegistry() {
		Oscar oscar = new Oscar();
		ChemNameDictRegistry registry1 = oscar.getDictionaryRegistry();
		assertNotNull(registry1);
		
		ChemNameDictRegistry registry2 = new ChemNameDictRegistry(Locale.FRENCH);
		assertFalse(registry1 == registry2);
		
		oscar.setDictionaryRegistry(registry2);
		assertTrue(oscar.getDictionaryRegistry() == registry2);
	}
	
	@Test
	public void testMemmModelSetup() {
		ListMultimap<String, String> terms = ArrayListMultimap.create();
		OntologyTerms ontologyTerms = new OntologyTerms(terms);
		MEMMModel model = new ChemPapersModel();
		
		Oscar oscar = new Oscar();
		oscar.setOntologyTerms(ontologyTerms);
		oscar.setMemmModel(model);
		MEMMRecogniser recogniser = (MEMMRecogniser) oscar.getRecogniser();
		assertTrue(recogniser.getModel() == model);
		assertTrue(recogniser.getOntologyAndPrefixTermFinder().getOntologyTerms() == ontologyTerms);
	}


    @Test
    public void testMemmThreadSafety() throws InterruptedException {
        final Oscar oscar = new Oscar();
        runThreadSafetyTest(oscar);
    }

    @Test
    public void testPatternThreadSafety() throws InterruptedException {
        final Oscar oscar = new Oscar();
        oscar.setRecogniser(new PatternRecogniser());
        runThreadSafetyTest(oscar);
    }

    private void runThreadSafetyTest(Oscar oscar) throws InterruptedException {
        OscarThread[] threads = new OscarThread[10];
        for (int i = 0; i < threads.length; i++) {
            OscarThread thread = new OscarThread(oscar);
            threads[i] = thread;
            thread.start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        for (int i = 0; i < threads.length; i++) {
            if (threads[i].getError() != null) {
                fail(threads[i].getError());
            }
        }
    }

    static class OscarThread extends Thread {

        private Oscar oscar;
        private String error;

        OscarThread(Oscar oscar) {
            this.oscar = oscar;
        }

        @Override
        public void run() {
            String s = "Then we mix benzene with toluene.";
		    for (int i = 0; i < 20; i++) {
                List<NamedEntity> entities = oscar.findNamedEntities(s);
                if (entities.size() != 2) {
                    error = "Expected 2 entities; found "+entities.size();
                    break;
                }
                if (!"benzene".equals(entities.get(0).getSurface())) {
                    error = "Expected 'benzene'; found "+entities.get(0).getSurface();
                    break;
                }
                if (!"toluene".equals(entities.get(1).getSurface())) {
                    error = "Expected 'toluene'; found "+entities.get(1).getSurface();
                    break;
                }
            }
        }

        public String getError() {
            return error;
        }

    }

	
	class TokeniserImpl implements ITokeniser {

		public TokenSequence tokenise(String text,
				IProcessingDocument procDoc, int offset, Element element) {
			return null;
		}
		
	}
}
