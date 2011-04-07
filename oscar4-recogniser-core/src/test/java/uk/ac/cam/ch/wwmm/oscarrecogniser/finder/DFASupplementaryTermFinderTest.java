package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.MutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class DFASupplementaryTermFinderTest {

	@Test
	public void testConstructor() {
		ListMultimap<NamedEntityType, String> supplementaryTerms = ArrayListMultimap.create();
		DFASupplementaryTermFinder finder = new DFASupplementaryTermFinder(supplementaryTerms);
		assertTrue(supplementaryTerms == finder.getTerms());
	}
	
	@Test
	public void testRegistryConstructor() throws URISyntaxException {
		MutableChemNameDict dictionary = new MutableChemNameDict(new URI("http://www.example.org"), Locale.ENGLISH);
		dictionary.addName("foo");
		dictionary.addName("bar");
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		registry.register(dictionary);
		
		DFASupplementaryTermFinder finder = new DFASupplementaryTermFinder(registry);
		ListMultimap<NamedEntityType, String> supplementaryTerms = finder.getTerms();
		assertEquals(2, supplementaryTerms.size());
		assertTrue(supplementaryTerms.get(NamedEntityType.COMPOUND).contains("foo"));
		assertTrue(supplementaryTerms.get(NamedEntityType.COMPOUND).contains("bar"));
	}
	
	@Test
	public void testFindNamedEntities() {
		ListMultimap<NamedEntityType, String> supplementaryTerms = ArrayListMultimap.create();
		supplementaryTerms.put(NamedEntityType.COMPOUND, "quick");
		supplementaryTerms.put(NamedEntityType.COMPOUND, "brown");
		supplementaryTerms.put(NamedEntityType.REACTION, "lazy dog");
		DFASupplementaryTermFinder finder = new DFASupplementaryTermFinder(supplementaryTerms);
		
		String text = "The quick brown fox jumps over the lazy dog.";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		
		List <NamedEntity> neList = finder.findNamedEntities(procDoc.getTokenSequences().get(0));
		assertEquals(3, neList.size());
		
		assertEquals("quick", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
		assertEquals(4, neList.get(0).getStart());
		assertEquals(9, neList.get(0).getEnd());
		
		assertEquals("brown", neList.get(1).getSurface());
		assertTrue(neList.get(1).getType().isInstance(NamedEntityType.COMPOUND));
		assertEquals(10, neList.get(1).getStart());
		assertEquals(15, neList.get(1).getEnd());
		
		assertEquals("lazy dog", neList.get(2).getSurface());
		assertTrue(neList.get(2).getType().isInstance(NamedEntityType.REACTION));
		assertEquals(35, neList.get(2).getStart());
		assertEquals(43, neList.get(2).getEnd());
	}
	
	@Test
	public void testFindOverlappingNamedEntities() {
		ListMultimap<NamedEntityType, String> supplementaryTerms = ArrayListMultimap.create();
		supplementaryTerms.put(NamedEntityType.COMPOUND, "quick brown");
		supplementaryTerms.put(NamedEntityType.COMPOUND, "brown fox");
		DFASupplementaryTermFinder finder = new DFASupplementaryTermFinder(supplementaryTerms);
		
		String text = "The quick brown fox jumps over the lazy dog.";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		
		List <NamedEntity> neList = finder.findNamedEntities(procDoc.getTokenSequences().get(0));
		assertEquals(2, neList.size());
		
		assertEquals("quick brown", neList.get(0).getSurface());
		assertTrue(neList.get(0).getType().isInstance(NamedEntityType.COMPOUND));
		assertEquals(4, neList.get(0).getStart());
		assertEquals(15, neList.get(0).getEnd());
		
		assertEquals("brown fox", neList.get(1).getSurface());
		assertTrue(neList.get(1).getType().isInstance(NamedEntityType.COMPOUND));
		assertEquals(10, neList.get(1).getStart());
		assertEquals(19, neList.get(1).getEnd());
	}
	
	@Test
	public void testFindNamedEntitiesFromDictRegistry() throws URISyntaxException {
		MutableChemNameDict dictionary = new MutableChemNameDict(new URI("http://www.example.org"), Locale.ENGLISH);
		dictionary.addName("fox");
		dictionary.addName("dog");
		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
		registry.register(dictionary);
		
		DFASupplementaryTermFinder finder = new DFASupplementaryTermFinder(registry);
		String text = "The quick brown fox jumps over the lazy dog.";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		
		List <NamedEntity> neList = finder.findNamedEntities(procDoc.getTokenSequences().get(0));
		assertEquals(2, neList.size());
		assertEquals("fox", neList.get(0).getSurface());
		assertEquals("dog", neList.get(1).getSurface());
	}
	
	
	@Test
	@Ignore
	//TODO refactor the class such that this test can run in a acceptable amount of time
	public void testFindSingleTokenTerms() {
		ListMultimap<NamedEntityType, String> supplementaryTerms = ArrayListMultimap.create();
		supplementaryTerms.put(NamedEntityType.COMPOUND, "fox");
		Random random = new Random();
		for (int i = 0; i < 10000; i++) {
			StringBuilder builder = new StringBuilder(10);
			for (int j = 0; j < 8; j++) {
				builder.append(random.nextInt(9));
			}
			assertEquals(8, builder.length());
			supplementaryTerms.put(NamedEntityType.COMPOUND, builder.toString());
		}
		DFASupplementaryTermFinder finder = new DFASupplementaryTermFinder(supplementaryTerms);
		
		String text = "The quick brown fox jumps over the lazy dog.";
		ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
				Tokeniser.getDefaultInstance(), text);
		
		List <NamedEntity> neList = finder.findNamedEntities(procDoc.getTokenSequences().get(0));
		assertEquals(1, neList.size());
		assertEquals("fox", neList.get(0).getSurface());
	}
}
