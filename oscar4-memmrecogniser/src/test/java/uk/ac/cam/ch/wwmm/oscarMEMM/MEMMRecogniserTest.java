package uk.ac.cam.ch.wwmm.oscarMEMM;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nu.xom.Document;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.MutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.ont.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar.scixml.TextToSciXML;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.ChemPapersModel;
import uk.ac.cam.ch.wwmm.oscarrecogniser.saf.StandoffResolver.ResolutionMode;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author egonw
 * @author j_robinson
 * @author dmj30
 */
public class MEMMRecogniserTest {

	private static MEMMRecogniser recogniser;

	@BeforeClass
	public static void setUp() {
		recogniser = new MEMMRecogniser();
	}
	
	@AfterClass
	public static void cleanUp() {
		recogniser = null;
	}
	
	@Test public void testConstructor() {
		assertNotNull(recogniser);
	}

	@Test
	public void testFindNamedEntities() throws Exception {
		ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar3/test/testcard/resources/");
		String s = rg.getString("testcard.txt");
		assertTrue("Have testcard string", s != null && s.length() > 0);
		Document doc = TextToSciXML.textToSciXML(s);
		
		IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().
			makeTokenisedDocument(Tokeniser.getDefaultInstance(), doc);
		assertTrue(procDoc != null);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neList != null);
		assertEquals("Only acetone should be recognised", 1, neList.size());
		assertEquals("acetone", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindNamedEntitiesFromString() {
		String source = "Hello acetone world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(Tokeniser.getDefaultInstance(), source);
		List <NamedEntity> neList = recogniser.findNamedEntities(procDoc);
		assertEquals(1, neList.size());
		assertEquals("acetone", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}
	
	@Test
	public void testFindMultipleTokenEntity() throws Exception {
		String text = "Hello ethyl acetate world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		
		assertEquals(1, neList.size());
		assertEquals("ethyl acetate", neList.get(0).getSurface());
		assertTrue(NamedEntityType.COMPOUND.isInstance(neList.get(0).getType()));
	}
	
	@Test
	public void testFindNonDictionaryEntity() throws Exception {
		String text = "Hello 1-methyl-2-ethyl-3-propyl-4-butyl-5-pentyl-6-hexylbenzene world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("1-methyl-2-ethyl-3-propyl-4-butyl-5-pentyl-6-hexylbenzene", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}
	
	@Test
	public void testFindNonDictionaryMultipleTokenEntity() throws Exception {
		String text = "Hello 1,2-difluoro-1-chloro-2-methyl-ethyl acetate world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("1,2-difluoro-1-chloro-2-methyl-ethyl acetate", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}
	
	
	@Test
	public void testFindFeThree() {
		String text = "The quick brown Fe(III) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Fe(III)", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}

	private boolean neListContainsCorrectNe(List<NamedEntity> neList, String desiredNe,
			NamedEntityType desiredType, boolean blocked) {
		for (NamedEntity namedEntity : neList) {
			if (desiredNe.equals(namedEntity.getSurface())) {
				if (namedEntity.getType().isInstance(desiredType)) {
					if (namedEntity.isBlocked() == blocked) {
						return true;		
					}
				}
			}
		}
		return false;
	}
	
	@Test
	public void testFindFeThreeLowercase() {
		String text = "The quick brown Fe(iii) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Fe(iii)", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}
	
	@Test
	public void testFindFeThreePlus() {
		String text = "The quick brown Fe(3+) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Fe(3+)", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}
	
	@Test
	public void testFindFeNought() {
		String text = "The quick brown Fe(0) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Fe(0)", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}
	
	@Test
	public void testFindIronThree() {
		String text = "The quick brown Iron(III) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Iron(III)", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}
	
	@Test
	public void testFindIronThreeLowercase() {
		String text = "The quick brown Iron(iii) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Iron(iii)", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}
	
	@Test
	public void testFindIronThreePlus() {
		String text = "The quick brown Iron(3+) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Iron(3+)", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}
	
	@Test
	public void testFindIronNought() {
		String text = "The quick brown Iron(0) jumps over the lazy ligands";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Iron(0)", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}
	
	@Test
	public void testCuTwoCompound() {
		String text = "was added dropwise to Cu(II) nitrate hexahydrate (1.00 mmol ) .";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Cu(II) nitrate hexahydrate", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}
	
	@Test
	public void testCuLowercaseTwoCompound() {
		String text = "was added dropwise to Cu(ii) hydroxide (1.00 mmol ) .";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Cu(ii) hydroxide", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}

	@Test
	public void testCuTwoPlusCompound() {
		String text = "was added dropwise to Cu(2+) chloride (1.00 mmol ) .";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Cu(2+) chloride", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}

	@Test
	public void testCopperTwoCompound() {
		String text = "was added dropwise to Copper(II) acetate (1.00 mmol ) .";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Copper(II) acetate", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}

	@Test
	public void testCopperTwoLowercaseCompound() {
		String text = "was added dropwise to Copper(ii) sulfate pentahydrate (1.00 mmol ) .";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Copper(ii) sulfate pentahydrate", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}

	@Test
	public void testCopperTwoPlusCompound() {
		String text = "was added dropwise to Copper(2+) triflate (1.00 mmol ) .";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("Copper(2+) triflate", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
	}

	
	@Test
	public void testSetOntPseudoConfidence() {
		MEMMRecogniser recogniserForCustomisation = new MEMMRecogniser();
		assertEquals(0.2, recogniserForCustomisation.getOntPseudoConfidence(), 0.00001);
		recogniserForCustomisation.setOntPseudoConfidence(0.3);
		assertEquals(0.3, recogniserForCustomisation.getOntPseudoConfidence(), 0.00001);
	}
	
	@Test
	public void testSetCustPseudoConfidence() {
		MEMMRecogniser recogniserForCustomisation = new MEMMRecogniser();
		assertEquals(0.2, recogniserForCustomisation.getCustPseudoConfidence(), 0.00001);
		recogniserForCustomisation.setCustPseudoConfidence(0.3);
		assertEquals(0.3, recogniserForCustomisation.getCustPseudoConfidence(), 0.00001);
	}
	
	@Test
	public void testSetCprPseudoConfidence() {
		MEMMRecogniser recogniserForCustomisation = new MEMMRecogniser();
		assertEquals(0.2, recogniserForCustomisation.getCprPseudoConfidence(), 0.00001);
		recogniserForCustomisation.setCprPseudoConfidence(0.3);
		assertEquals(0.3, recogniserForCustomisation.getCprPseudoConfidence(), 0.00001);
	}
	
	@Test
	public void testSetPseudoConfidences() {
		MEMMRecogniser recogniserForCustomisation = new MEMMRecogniser();
		List <NamedEntity> nes = new ArrayList<NamedEntity>();
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument
				(Tokeniser.getDefaultInstance(), "foo");
		List <Token> tokens = procDoc.getTokenSequences().get(0).getTokens();
		nes.add(new NamedEntity(tokens, "", NamedEntityType.COMPOUND));
		nes.add(new NamedEntity(tokens, "", NamedEntityType.ONTOLOGY));
		nes.add(new NamedEntity(tokens, "", NamedEntityType.CUSTOM));
		nes.add(new NamedEntity(tokens, "", NamedEntityType.LOCANTPREFIX));
		
		recogniserForCustomisation.setOntPseudoConfidence(0.4);
		recogniserForCustomisation.setCustPseudoConfidence(0.5);
		recogniserForCustomisation.setCprPseudoConfidence(0.6);
		recogniserForCustomisation.setPseudoConfidences(nes);
		assertEquals(Double.NaN, nes.get(0).getPseudoConfidence(), 0.00001);
		assertEquals(0.4, nes.get(1).getPseudoConfidence(), 0.00001);
		assertEquals(0.5, nes.get(2).getPseudoConfidence(), 0.00001);
		assertEquals(0.6, nes.get(3).getPseudoConfidence(), 0.00001);
	}
	
	
	@Test
	public void testDeprioritiseOnts() {
		MEMMRecogniser recogniserForCustomisation = new MEMMRecogniser();
		String text = "4-hydroxybenzyl group";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument
				(Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> nes = recogniserForCustomisation.findNamedEntities(procDoc.getTokenSequences(), ResolutionMode.REMOVE_BLOCKED);
		assertEquals(1, nes.size());//"4-hydroxybenzyl group" is an ont term and due to its length takes precedence unless deprioritise onts is set
		for (NamedEntity ne : nes) {
			assertFalse(ne.getDeprioritiseOnt());
		}
		
		recogniserForCustomisation.setDeprioritiseOnts(true);
		nes = recogniserForCustomisation.findNamedEntities(procDoc.getTokenSequences(), ResolutionMode.REMOVE_BLOCKED);

		assertEquals(2, nes.size());
		for (NamedEntity ne : nes) {
			assertTrue(ne.getDeprioritiseOnt());
		}
	}
	
	
	@Test
	public void testFindsOntologyTerms() {
		String source = "The quick brown ethyl acetate jumps over the lazy acetone";
    	ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
    			Tokeniser.getDefaultInstance(), source);
    	List <NamedEntity> nes = recogniser.findNamedEntities(procDoc);
    	assertEquals(2, nes.size());
    	assertEquals("ethyl acetate", nes.get(0).getSurface());
    	assertTrue(NamedEntityType.COMPOUND.isInstance(nes.get(0).getType()));
    	assertEquals(0, nes.get(0).getOntIds().size());
    	assertEquals("acetone", nes.get(1).getSurface());
    	assertTrue(NamedEntityType.COMPOUND.isInstance(nes.get(1).getType()));
    	assertEquals(0, nes.get(1).getOntIds().size());

    	ListMultimap<String, String> ontTerms = ArrayListMultimap.create();
    	ontTerms.put("jumps", "foo:001");
    	ontTerms.put("jumps", "foo:002");
    	MEMMRecogniser customRecogniser = new MEMMRecogniser(
    			new ChemPapersModel(), new OntologyTerms(ontTerms),
    			new ChemNameDictRegistry(Locale.ENGLISH));
    	List <NamedEntity> customNes = customRecogniser.findNamedEntities(procDoc);
    	assertEquals(3, customNes.size());
    	assertEquals("ethyl acetate", customNes.get(0).getSurface());
    	assertTrue(NamedEntityType.COMPOUND.isInstance(customNes.get(0).getType()));
    	assertEquals(0, customNes.get(0).getOntIds().size());
    	assertEquals("jumps", customNes.get(1).getSurface());
    	assertTrue(NamedEntityType.ONTOLOGY.isInstance(customNes.get(1).getType()));
    	assertEquals(2, customNes.get(1).getOntIds().size());
    	assertTrue(customNes.get(1).getOntIds().contains("foo:001"));
    	assertTrue(customNes.get(1).getOntIds().contains("foo:002"));
    	assertEquals("acetone", customNes.get(2).getSurface());
    	assertTrue(NamedEntityType.COMPOUND.isInstance(customNes.get(2).getType()));
    	assertEquals(0, customNes.get(2).getOntIds().size());
	}
	
	@Test
	public void testCmCanHaveOntIds() {
		String text = "isoporphyrin";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List <NamedEntity> nes = recogniser.findNamedEntities(procDoc);
		assertEquals(1, nes.size());
		assertTrue(NamedEntityType.COMPOUND.isInstance(nes.get(0).getType()));
		assertEquals(1, nes.get(0).getOntIds().size());
		assertTrue(nes.get(0).getOntIds().contains("CHEBI:52538"));
	}

	@Test
	public void testRescorerModifiesConfidence() {
		String text = "THF (50 ml). THF (100 ml)";
		MEMMRecogniser recog = new MEMMRecogniser();
		recog.setUseRescorer(false);
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List <NamedEntity> nes = recog.findNamedEntities(procDoc);
		assertEquals(2, nes.size());
		recog.setUseRescorer(true);
		List <NamedEntity> rescoredNes = recog.findNamedEntities(procDoc);
		assertEquals(2, rescoredNes.size());
		assertTrue(rescoredNes.get(0).getConfidence() > nes.get(0).getConfidence());
		assertTrue(rescoredNes.get(1).getConfidence() > nes.get(1).getConfidence());
	}
	
	@Test
	public void testFindEntitiesMarkBlocked() throws Exception {
		String text = "2-methyl butan-1-ol underwent hydrolysis";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences(), ResolutionMode.MARK_BLOCKED);
		
		//the memmRecogniser may find multiple entity types for an entity with the non-preferred interpretations being marked as blocked
		assertEquals(5, neList.size());
		assertTrue(neListContainsCorrectNe(neList, "2-", NamedEntityType.LOCANTPREFIX, true));
		assertTrue(neListContainsCorrectNe(neList, "2-methyl butan-1-ol", NamedEntityType.COMPOUND, false));
		assertTrue(neListContainsCorrectNe(neList, "butan-1-ol", NamedEntityType.COMPOUND, true));
		assertTrue(neListContainsCorrectNe(neList, "hydrolysis", NamedEntityType.REACTION, false));
		assertTrue(neListContainsCorrectNe(neList, "hydrolysis", NamedEntityType.ONTOLOGY, true ));
	}
	
	@Test
	public void testFindEntitiesRemoveBlocked() throws Exception {
		String text = "Hello 2-methyl butan-1-ol hydrolysis in ethyl acetate world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences(), ResolutionMode.REMOVE_BLOCKED);
		
		assertEquals(3, neList.size());
		assertEquals("2-methyl butan-1-ol", neList.get(0).getSurface());
		assertTrue(NamedEntityType.COMPOUND.isInstance(neList.get(0).getType()));
		
		assertEquals("hydrolysis", neList.get(1).getSurface());
		assertTrue(NamedEntityType.REACTION.isInstance(neList.get(1).getType()));
		
		assertEquals("ethyl acetate", neList.get(2).getSurface());
		assertTrue(NamedEntityType.COMPOUND.isInstance(neList.get(2).getType()));
	}
	
	
	@Test
	public void testFindAdditionalNamedEntities() throws URISyntaxException {
		MutableChemNameDict dictionary = new MutableChemNameDict(new URI("http://www.example.org"), Locale.ENGLISH);
		dictionary.addName("additionalchemname");
		dictionary.addName("additional chemical name");
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		registry.register(dictionary);
		MEMMRecogniser recogniser = new MEMMRecogniser(
				new ChemPapersModel(), OntologyTerms.getDefaultInstance(), registry);
		
		ProcessingDocument procDoc1 = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), "benzene and an additionalchemname");
		List <NamedEntity> nes1 = recogniser.findNamedEntities(procDoc1);
		assertTrue(neListContainsCorrectNe(nes1, "benzene", NamedEntityType.COMPOUND, false));
		assertTrue(neListContainsCorrectNe(nes1, "additionalchemname", NamedEntityType.COMPOUND, false));
		
		ProcessingDocument procDoc2 = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), "benzene and an additional chemical name");
		List <NamedEntity> nes2 = recogniser.findNamedEntities(procDoc2);
		assertTrue(neListContainsCorrectNe(nes2, "benzene", NamedEntityType.COMPOUND, false));
		assertTrue(neListContainsCorrectNe(nes2, "additional chemical name", NamedEntityType.COMPOUND, false));
	}
}
