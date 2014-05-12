package uk.ac.cam.ch.wwmm.oscarpattern;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.exceptions.DataFormatException;
import uk.ac.cam.ch.wwmm.oscar.ont.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar.scixml.TextToSciXML;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata.ExtractedTrainingData;
import uk.ac.cam.ch.wwmm.oscarrecogniser.finder.TermMaps;
import uk.ac.cam.ch.wwmm.oscarrecogniser.saf.StandoffResolver.ResolutionMode;
import uk.ac.cam.ch.wwmm.oscartokeniser.TokenClassifier;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author egonw
 * @author dmj30
 */
public class PatternRecogniserTest {

	private static PatternRecogniser recogniser;

	@BeforeClass
	public static void setUp() {
		recogniser = new PatternRecogniser();
	}
	
	@AfterClass
	public static void cleanUp() {
		recogniser = null;
	}
	
	@Test public void testConstructor() {
		assertNotNull(recogniser);
	}

	@Test
	public void test_findNamedEntities() throws Exception
	{
		ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/oscar/test/testcard/");
		String s = rg.getString("testcard.txt");
		assertTrue("Have testcard string", s != null && s.length() > 0);
		Document doc = TextToSciXML.textToSciXML(s);

		IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
			Tokeniser.getDefaultInstance(), doc);
		assertTrue(procDoc != null);
		List<NamedEntity> neList;
		neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertTrue(neList != null);
		assertTrue(neList.size() > 0);
	}
	
	@Test
	public void testFindNamedEntitiesFromString() throws Exception {
		String text = "Hello acetone world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("acetone", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindMultipleTokenEntity() throws Exception {
		String text = "Hello ethyl acetate world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("ethyl acetate", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindNonDictionaryEntity() throws Exception {
		String text = "Hello 1-methyl-2-ethyl-3-propyl-4-butyl-5-pentyl-6-hexylbenzene world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("1-methyl-2-ethyl-3-propyl-4-butyl-5-pentyl-6-hexylbenzene", neList.get(0).getSurface());
	}
	
	@Test
	public void testFindNonDictionaryMultipleTokenEntity() throws Exception {
		String text = "Hello 1,2-difluoro-1-chloro-2-methyl-ethyl acetate world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences());
		assertEquals(1, neList.size());
		assertEquals("1,2-difluoro-1-chloro-2-methyl-ethyl acetate", neList.get(0).getSurface());
	}
	
	
	@Test
	public void testSetOntPseudoConfidence() {
		PatternRecogniser recogniserForCustomisation = new PatternRecogniser();
		assertEquals(0.2, recogniserForCustomisation.getOntPseudoConfidence(), 0.00001);
		recogniserForCustomisation.setOntPseudoConfidence(0.3);
		assertEquals(0.3, recogniserForCustomisation.getOntPseudoConfidence(), 0.00001);
	}
	
	@Test
	public void testSetCustPseudoConfidence() {
		PatternRecogniser recogniserForCustomisation = new PatternRecogniser();
		assertEquals(0.2, recogniserForCustomisation.getCustPseudoConfidence(), 0.00001);
		recogniserForCustomisation.setCustPseudoConfidence(0.3);
		assertEquals(0.3, recogniserForCustomisation.getCustPseudoConfidence(), 0.00001);
	}
	
	@Test
	public void testSetCprPseudoConfidence() {
		PatternRecogniser recogniserForCustomisation = new PatternRecogniser();
		assertEquals(0.2, recogniserForCustomisation.getCprPseudoConfidence(), 0.00001);
		recogniserForCustomisation.setCprPseudoConfidence(0.3);
		assertEquals(0.3, recogniserForCustomisation.getCprPseudoConfidence(), 0.00001);
	}
	
	@Test
	public void testSetPseudoConfidences() {
		PatternRecogniser recogniserForCustomisation = new PatternRecogniser();
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
	public void testSetNgramThreshold() {
		PatternRecogniser recogniserForCustomisation = new PatternRecogniser();
		assertEquals(-2, recogniserForCustomisation.getNgramThreshold(), 0.00001);
		recogniserForCustomisation.setNgramThreshold(42);
		assertEquals(42, recogniserForCustomisation.getNgramThreshold(), 0.00001);
	}

	
	@Test
	public void testWithCustomChemNameDictRegistry() throws URISyntaxException {
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		MutableChemNameDict dictionary = new MutableChemNameDict(new URI("http://www.example.org"), Locale.ENGLISH);
		dictionary.addChemical("registryName", "", "");
		registry.register(dictionary);
		PatternRecogniser recogniser = new PatternRecogniser(
				ExtractedTrainingData.getDefaultInstance(), TermMaps.getInstance().getNeTerms(),
				TokenClassifier.getDefaultInstance(), OntologyTerms.getDefaultInstance(),
				registry);
		assertEquals(1, recogniser.getRegistryNames().size());
		assertTrue(recogniser.getRegistryNames().contains("registryname"));
	}
	
	@Test (expected = UnsupportedOperationException.class)
	public void testImmutableChemNameDictRegistryNames() {
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		PatternRecogniser recogniser = new PatternRecogniser(
				ExtractedTrainingData.getDefaultInstance(), TermMaps.getInstance().getNeTerms(),
				TokenClassifier.getDefaultInstance(), OntologyTerms.getDefaultInstance(),
				registry);
		recogniser.getRegistryNames().clear();
	}
	
	@Test
	public void testWithCustomNeTerms() throws DataFormatException, IOException {
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), "benzene furanyl");
		List<NamedEntity> nes = recogniser.findNamedEntities(procDoc);
		assertEquals(2, nes.size());
		assertEquals("benzene", nes.get(0).getSurface());
		assertEquals("furanyl", nes.get(1).getSurface());
		
		Map<String, NamedEntityType> neTerms = new HashMap<String, NamedEntityType>();
		neTerms.put("$-ene $-yl", NamedEntityType.COMPOUND);
		PatternRecogniser customRecogniser = new PatternRecogniser(
				ExtractedTrainingData.getDefaultInstance(), neTerms,
				TokenClassifier.getDefaultInstance(), OntologyTerms.getDefaultInstance(),
				ChemNameDictRegistry.getDefaultInstance());
		List<NamedEntity> nes2 = customRecogniser.findNamedEntities(procDoc);
		assertEquals(1, nes2.size());
		assertEquals("benzene furanyl", nes2.get(0).getSurface());
	}
	
	
	@Test
	public void testWithCustomOntologyTerms() {
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
    	PatternRecogniser customRecogniser = new PatternRecogniser(
    			ExtractedTrainingData.getDefaultInstance(), TermMaps.getInstance().getNeTerms(),
    			TokenClassifier.getDefaultInstance(), new OntologyTerms(ontTerms),
    			ChemNameDictRegistry.getDefaultInstance());
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
	public void testFindNamedEntitiesIncludesAdHocAcronyms() {
		String defined = "the quick bromochlorodimethylhydantoin (BCDMH) fox jumps over the BCDMH dog.";
		String unDefined = "the quick BCDMH fox jumps over the BCDMH dog.";
		ProcessingDocument definedDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), defined);
		ProcessingDocument unDefinedDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), unDefined);
		
		assertEquals(0, recogniser.findNamedEntities(unDefinedDoc).size());
		List <NamedEntity> nes = recogniser.findNamedEntities(definedDoc);
		assertEquals(3, nes.size());
		
		assertEquals("bromochlorodimethylhydantoin", nes.get(0).getSurface());
		assertTrue(NamedEntityType.COMPOUND.isInstance(nes.get(0).getType()));
		assertEquals(10, nes.get(0).getStart());
		
		assertEquals("BCDMH", nes.get(1).getSurface());
		assertTrue(NamedEntityType.COMPOUND.isInstance(nes.get(1).getType()));
		assertEquals(40, nes.get(1).getStart());
		
		assertEquals("BCDMH", nes.get(2).getSurface());
		assertTrue(NamedEntityType.COMPOUND.isInstance(nes.get(2).getType()));
		assertEquals(66, nes.get(2).getStart());
	}
	
	
	@Test
	public void testMergeOntIdsAndCustTypes() {
		List<NamedEntity> nes = new ArrayList<NamedEntity>();
		NamedEntity ne1 = new NamedEntity("foo", 2, 4, NamedEntityType.COMPOUND);
		NamedEntity ne2 = new NamedEntity("foo", 2, 4, NamedEntityType.ONTOLOGY);
		ne2.setOntIds(new HashSet<String>(Arrays.asList(new String [] {"foo:001", "foo:002"})));
		NamedEntity ne3 = new NamedEntity("foo", 2, 4, NamedEntityType.ONTOLOGY);
		ne3.setOntIds(new HashSet<String>(Arrays.asList(new String [] {"foo:001", "foo:003"})));
		NamedEntity ne4 = new NamedEntity("food", 2, 5, NamedEntityType.COMPOUND);
		
		NamedEntity ne5 = new NamedEntity("bar", 6, 8, NamedEntityType.COMPOUND);
		NamedEntity ne6 = new NamedEntity("bar", 6, 8, NamedEntityType.CUSTOM);
		ne6.setCustTypes(new HashSet<String>(Arrays.asList(new String[] {"FOO"})));
		NamedEntity ne7 = new NamedEntity("bar", 6, 8, NamedEntityType.CUSTOM);
		ne7.setCustTypes(new HashSet<String>(Arrays.asList(new String[] {"BAR"})));
		NamedEntity ne8 = new NamedEntity("baritone", 6, 13, NamedEntityType.COMPOUND);
		
		nes.add(ne1);
		nes.add(ne2);
		nes.add(ne3);
		nes.add(ne4);
		nes.add(ne5);
		nes.add(ne6);
		nes.add(ne7);
		nes.add(ne8);
		
		PatternRecogniser.mergeOntIdsAndCustTypes(nes);
		
		assertEquals(3, nes.get(0).getOntIds().size());
		assertTrue(nes.get(0).getOntIds().contains("foo:001"));
		assertTrue(nes.get(0).getOntIds().contains("foo:002"));
		assertTrue(nes.get(0).getOntIds().contains("foo:003"));
		assertEquals(0, nes.get(0).getCustTypes().size());
		
		assertEquals(3, nes.get(1).getOntIds().size());
		assertTrue(nes.get(1).getOntIds().contains("foo:001"));
		assertTrue(nes.get(1).getOntIds().contains("foo:002"));
		assertTrue(nes.get(1).getOntIds().contains("foo:003"));
		assertEquals(0, nes.get(1).getCustTypes().size());
		
		assertEquals(3, nes.get(2).getOntIds().size());
		assertTrue(nes.get(2).getOntIds().contains("foo:001"));
		assertTrue(nes.get(2).getOntIds().contains("foo:002"));
		assertTrue(nes.get(2).getOntIds().contains("foo:003"));
		assertEquals(0, nes.get(2).getCustTypes().size());
		
		assertEquals(0, nes.get(3).getOntIds().size());
		assertEquals(0, nes.get(3).getCustTypes().size());
		
		assertEquals(2, nes.get(4).getCustTypes().size());
		assertTrue(nes.get(4).getCustTypes().contains("FOO"));
		assertTrue(nes.get(4).getCustTypes().contains("BAR"));
		assertEquals(0, nes.get(4).getOntIds().size());
		
		assertEquals(2, nes.get(5).getCustTypes().size());
		assertTrue(nes.get(5).getCustTypes().contains("FOO"));
		assertTrue(nes.get(5).getCustTypes().contains("BAR"));
		assertEquals(0, nes.get(5).getOntIds().size());

		assertEquals(2, nes.get(6).getCustTypes().size());
		assertTrue(nes.get(6).getCustTypes().contains("FOO"));
		assertTrue(nes.get(6).getCustTypes().contains("BAR"));
		assertEquals(0, nes.get(6).getOntIds().size());
		
		assertEquals(0, nes.get(7).getCustTypes().size());
		assertEquals(0, nes.get(7).getOntIds().size());
	}
	
	
	@Test
	public void testIdentifyAcronyms() {
		String source = "foo polystyrene (FOOBAR), more FOOBAR, ethylene diamine tetra acetate (EDTA) and more EDTA";
		List <TokenSequence> tokSeqList = new ArrayList<TokenSequence>();
		tokSeqList.add(Tokeniser.getDefaultInstance().tokenise(source));
		List <NamedEntity> nes = new ArrayList<NamedEntity>();
		nes.add(new NamedEntity("polystyrene", 4, 15, NamedEntityType.COMPOUND));
		nes.add(new NamedEntity("FOOBAR", 17, 23, NamedEntityType.POTENTIALACRONYM));
		nes.add(new NamedEntity("FOOBAR", 31, 37, NamedEntityType.POTENTIALACRONYM));
		nes.add(new NamedEntity("ethylene diamine tetra acetate", 39, 69, NamedEntityType.COMPOUND));
		nes.add(new NamedEntity("EDTA", 71, 75, NamedEntityType.POTENTIALACRONYM));
		nes.add(new NamedEntity("EDTA", 86, 90, NamedEntityType.POTENTIALACRONYM));		
		
		PatternRecogniser.handlePotentialAcronyms(tokSeqList, nes);
		assertEquals(4, nes.size());
		assertEquals("polystyrene", nes.get(0).getSurface());
		assertTrue(NamedEntityType.COMPOUND.equals(nes.get(0).getType()));
		
		assertEquals("ethylene diamine tetra acetate", nes.get(1).getSurface());
		assertTrue(NamedEntityType.COMPOUND.equals(nes.get(1).getType()));
		
		assertEquals("EDTA", nes.get(2).getSurface());
		assertTrue(NamedEntityType.COMPOUND.equals(nes.get(2).getType()));
		assertEquals(71, nes.get(2).getStart());
		
		assertEquals("EDTA", nes.get(3).getSurface());
		assertTrue(NamedEntityType.COMPOUND.equals(nes.get(3).getType()));
		assertEquals(86, nes.get(3).getStart());
	}
	
	
	@Test
	public void testRemoveStopwords() {
		List<NamedEntity> nes = new ArrayList<NamedEntity>();
		nes.add(new NamedEntity("", 0, 0, NamedEntityType.COMPOUND));
		nes.add(new NamedEntity("", 0, 0, NamedEntityType.REACTION));
		nes.add(new NamedEntity("", 0, 0, NamedEntityType.STOP));
		nes.add(new NamedEntity("", 0, 0, NamedEntityType.ONTOLOGY));
		nes.add(new NamedEntity("", 0, 0, NamedEntityType.ONTOLOGY));
		nes.add(new NamedEntity("", 0, 0, NamedEntityType.STOP));
		PatternRecogniser.removeStopwords(nes);
		assertEquals(4, nes.size());
		for (NamedEntity ne : nes) {
			assertFalse(NamedEntityType.STOP.equals(ne.getType()));
		}
	}
	
	
	@Test
	public void testFindEntitiesMarkBlocked() throws Exception {
		String text = "Hello 2-chloroethyl ethyl ether hydrolysis in dimethyl sulfoxide world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences(), ResolutionMode.MARK_BLOCKED);
		
		//this call finds blocked named entities as well as the ones we're expecting, so...
		assertTrue(neListContainsCorrectNe(neList, "2-", NamedEntityType.LOCANTPREFIX, true));
		assertTrue(neListContainsCorrectNe(neList, "2-chloroethyl", NamedEntityType.COMPOUND, true));
		assertTrue(neListContainsCorrectNe(neList, "ethyl ether", NamedEntityType.COMPOUND, true));
		assertTrue(neListContainsCorrectNe(neList, "2-chloroethyl ethyl ether", NamedEntityType.COMPOUND, false));
		
		assertTrue(neListContainsCorrectNe(neList, "hydrolysis", NamedEntityType.ONTOLOGY, false ));
		
		assertTrue(neListContainsCorrectNe(neList, "dimethyl", NamedEntityType.COMPOUND, true));
		assertTrue(neListContainsCorrectNe(neList, "sulfoxide", NamedEntityType.ONTOLOGY, true));
		assertTrue(neListContainsCorrectNe(neList, "sulfoxide", NamedEntityType.COMPOUND, true));
		assertTrue(neListContainsCorrectNe(neList, "dimethyl sulfoxide", NamedEntityType.COMPOUND, false));
	}
	
	@Test
	public void testFindEntitiesRemoveBlocked() throws Exception {
		String text = "Hello 2-chloroethyl ethyl ether hydrolysis in dimethyl sulfoxide world!";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		List<NamedEntity> neList = recogniser.findNamedEntities(procDoc.getTokenSequences(), ResolutionMode.REMOVE_BLOCKED);
		
		assertEquals(3, neList.size());
		assertEquals("2-chloroethyl ethyl ether", neList.get(0).getSurface());
		assertTrue(NamedEntityType.COMPOUND.isInstance(neList.get(0).getType()));
		assertFalse(neList.get(0).isBlocked());
		
		assertEquals("hydrolysis", neList.get(1).getSurface());
		assertTrue(NamedEntityType.ONTOLOGY.isInstance(neList.get(1).getType()));
		assertFalse(neList.get(1).isBlocked());
		
		assertEquals("dimethyl sulfoxide", neList.get(2).getSurface());
		assertTrue(NamedEntityType.COMPOUND.isInstance(neList.get(2).getType()));
		assertFalse(neList.get(2).isBlocked());
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
}



