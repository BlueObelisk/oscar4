package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscarrecogniser.extractedtrainingdata.ExtractedTrainingData;


public class NGramBuilderTest {

	private static final int EXPECTED_DATA_LENTH = 7311616;
	
	/** the NGram models are used repeatedly in the tests, so to 
	 *  save on set up time we share one copy of each a	cross the tests 
	 */
	private static Set<String> defaultRegistryNames;
	private static NGram vanillaNGram;
	private static NGram pubmedNGram;
	private static NGram chempapersNGram;

	
	@BeforeClass
	public static void buildSharedNGrams() {
		defaultRegistryNames = Collections.unmodifiableSet(ChemNameDictRegistry
				.getDefaultInstance().getAllNames());
		vanillaNGram = NGramBuilder.buildModel();
		pubmedNGram = NGramBuilder.buildModel(
				ExtractedTrainingData.loadExtractedTrainingData("pubmed"),
				defaultRegistryNames);
		chempapersNGram = NGramBuilder.buildModel(
				ExtractedTrainingData.loadExtractedTrainingData("chempapers"),
				defaultRegistryNames);
	}

	@AfterClass
	public static void releaseMemory() {
		defaultRegistryNames = null;
		vanillaNGram = null;
		pubmedNGram = null;
		chempapersNGram = null;
	}
	
	@Test
	public void testConstructor() {
		NGramBuilder builder = new NGramBuilder();
		assertNotNull(builder.getEnglishWords());
		assertNotNull(builder.getChemicalWords());
		assertTrue(builder.getEnglishWords().contains("cactus"));
		assertFalse(builder.getEnglishWords().contains("foo"));
		assertTrue(builder.getChemicalWords().contains("pyridine"));
		assertFalse(builder.getChemicalWords().contains("bar"));
	}
	
	@Test
	public void testCustomConstructor() throws URISyntaxException {
		ExtractedTrainingData annotations = mock(ExtractedTrainingData.class);
		Set <String> english = new HashSet<String>();
		Set <String> chemical = new HashSet<String>();
		english.add("foo");
		chemical.add("bar");
		stub(annotations.getNonChemicalWords()).toReturn(english);
		stub(annotations.getChemicalWords()).toReturn(chemical);
		
		Set <String> registryNames = new HashSet<String>();
		registryNames.add("registryname");
//		MutableChemNameDict dictionary = new MutableChemNameDict(
//				new URI("http://www.example.org"), Locale.ENGLISH);
//		dictionary.addChemical("registryname", "", "");
//		ChemNameDictRegistry registry = new ChemNameDictRegistry(Locale.ENGLISH);
//		registry.register(dictionary);
		
		NGramBuilder builder = new NGramBuilder(annotations, registryNames);
		assertNotNull(builder.getEnglishWords());
		assertNotNull(builder.getChemicalWords());
		assertTrue(builder.getEnglishWords().contains("cactus"));
		assertTrue(builder.getEnglishWords().contains("foo"));
		assertTrue(builder.getChemicalWords().contains("registryname"));
	}
	
	
	@Test
	public void testBuildModels() {
		NGram vanillaNGram2 = NGramBuilder.buildModel();
		assertFalse(vanillaNGram == vanillaNGram2);
		
		ExtractedTrainingData etd = ExtractedTrainingData.loadExtractedTrainingData("chempapers");
		NGram customisedNGram = NGramBuilder.buildModel(
				etd, defaultRegistryNames);
		
		short [] data1 = vanillaNGram.getData();
		short [] data2 = vanillaNGram2.getData();
		short [] data3 = customisedNGram.getData();
		
		assertEquals(EXPECTED_DATA_LENTH, data1.length);
		assertEquals(EXPECTED_DATA_LENTH, data2.length);
		assertEquals(EXPECTED_DATA_LENTH, data3.length);
		
		assertTrue(vanillaNGram.compareTo(vanillaNGram2));
		assertFalse(vanillaNGram.compareTo(customisedNGram));
		assertFalse(vanillaNGram2.compareTo(customisedNGram));
	}
	
	@Test
	public void testCalculatePubmedEmptyChemnamedictFingerprint() {
		ExtractedTrainingData annotations = ExtractedTrainingData.loadExtractedTrainingData("pubmed");
		NGramBuilder builder = new NGramBuilder(annotations,
				Collections.unmodifiableSet(Collections.<String> emptySet()));
		assertFalse("-412073498_-1815304182".equals(builder.calculateSourceDataFingerprint()));
	}
	
	
	/**
	 * check that the serialised vanilla model can be read and is consistent
	 * with the current training data
	 */
	@Test
	public void testDeserialiseVanillaModel() throws IOException {
		NGramBuilder builder = new NGramBuilder();
		NGram deserialised = null;
		try{
			deserialised = NGramBuilder.deserialiseModel(builder.calculateSourceDataFingerprint());
		}
		catch (IOException e) {
			e.printStackTrace();
			fail("Deserialisation of vanilla model failed, has NGramBuilder's main method been used to create a new serialisation?");
		}
		short [] deserialisedData = deserialised.getData();
		short [] builtData = vanillaNGram.getData();
		
		double expectedLength = Math.pow(NGramBuilder.ALPHABET.length(), 4);
		assertEquals(expectedLength, deserialisedData.length, .001);
		assertEquals(deserialisedData.length, builtData.length);
		assertTrue(deserialised.compareTo(vanillaNGram));
	}
	
	/**
	 * check that the serialised chempapers model can be read and is consistent
	 * with the current training data
	 */
	@Test
	public void testDeserialiseChempapersModel() throws IOException {
		ExtractedTrainingData annotations = ExtractedTrainingData.loadExtractedTrainingData("chempapers");
		NGramBuilder builder = new NGramBuilder(annotations, defaultRegistryNames);
		NGram deserialised = null;
		try{
			deserialised = NGramBuilder.deserialiseModel(builder.calculateSourceDataFingerprint());
		}
		catch (IOException e) {
			e.printStackTrace();
			fail("Deserialisation of chempapers model failed, has NGramBuilder's main method been used to create a new serialisation?");
		}
		short [] deserialisedData = deserialised.getData();
		short [] builtData = chempapersNGram.getData();
		
		assertEquals(EXPECTED_DATA_LENTH, deserialisedData.length);
		assertEquals(EXPECTED_DATA_LENTH, builtData.length);
		assertTrue(deserialised.compareTo(chempapersNGram));
	}

	/**
	 * check that the serialised pubmed model can be read and is consistent
	 * with the current training data
	 */
	@Test
	public void testDeserialisePubmedModel() throws IOException {
		ExtractedTrainingData annotations = ExtractedTrainingData.loadExtractedTrainingData("pubmed");
		NGramBuilder builder = new NGramBuilder(annotations, defaultRegistryNames);
		NGram deserialised = null;
		try{
			deserialised = NGramBuilder.deserialiseModel(builder.calculateSourceDataFingerprint());
		}
		catch (IOException e) {
			e.printStackTrace();
			fail("Deserialisation of pubmed model failed, has NGramBuilder's main method been used to create a new serialisation?");
		}
		short [] deserialisedData = deserialised.getData();
		short [] builtData = pubmedNGram.getData();
		
		assertEquals(EXPECTED_DATA_LENTH, deserialisedData.length);
		assertEquals(EXPECTED_DATA_LENTH, builtData.length);
		assertTrue(deserialised.compareTo(pubmedNGram));
	}
	
	@Test (expected = FileNotFoundException.class)
	public void testDeserialiseNonExistentModel() throws IOException {
		NGramBuilder.deserialiseModel("foobar");
	}
	
	
	@Test
	public void testBuildOrDeserialiseVanillaModel() {
		NGram vanilla = NGramBuilder.buildOrDeserialiseModel();
		assertNotNull(vanilla);
		assertEquals(EXPECTED_DATA_LENTH, vanilla.getData().length);
		assertTrue(vanillaNGram.compareTo(vanilla));
	}
	
	@Test
	public void testBuildOrDeserialisePubmedModel() {
		ExtractedTrainingData annotations = ExtractedTrainingData.loadExtractedTrainingData("pubmed");
		NGram pubmedModel = NGramBuilder.buildOrDeserialiseModel(
				annotations, defaultRegistryNames);
		assertNotNull(pubmedModel);
		assertEquals(EXPECTED_DATA_LENTH, pubmedModel.getData().length);
		assertTrue(pubmedModel.compareTo(pubmedNGram));
	}
	
	@Test
	public void testBuildOrDeserialiseChempapersModel() {
		ExtractedTrainingData annotations = ExtractedTrainingData.loadExtractedTrainingData("chempapers");
		NGram chempapersModel = NGramBuilder.buildOrDeserialiseModel(
				annotations, defaultRegistryNames);
		assertNotNull(chempapersModel);
		assertEquals(EXPECTED_DATA_LENTH, chempapersModel.getData().length);
		assertTrue(chempapersModel.compareTo(chempapersNGram));
	}
	
}
