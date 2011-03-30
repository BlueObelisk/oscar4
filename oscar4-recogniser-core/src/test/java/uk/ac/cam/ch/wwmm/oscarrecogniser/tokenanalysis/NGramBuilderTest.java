package uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections.set.UnmodifiableSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.data.MutableChemNameDict;
import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscarrecogniser.manualAnnotations.ManualAnnotations;


public class NGramBuilderTest {

	private static final int EXPECTED_DATA_LENTH = 7311616;
	
	/** the NGram models are used repeatedly in the tests, so to 
	 *  save on set up time we share one copy of each across the tests 
	 */
	private static UnmodifiableSet defaultRegistryNames;
	private static NGram vanillaNGram;
	private static NGram pubmedNGram;
	private static NGram chempapersNGram;

	
	@BeforeClass
	public static void buildSharedNGrams() {
		defaultRegistryNames = (UnmodifiableSet) UnmodifiableSet.decorate(
				ChemNameDictRegistry.getDefaultInstance().getAllNames());
		vanillaNGram = NGramBuilder.buildModel();
		pubmedNGram = NGramBuilder.buildModel(
				ManualAnnotations.loadManualAnnotations("pubmed"),
				defaultRegistryNames);
		chempapersNGram = NGramBuilder.buildModel(
				ManualAnnotations.loadManualAnnotations("chempapers"),
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
		ManualAnnotations annotations = mock(ManualAnnotations.class);
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
		
		NGramBuilder builder = new NGramBuilder(
				annotations, (UnmodifiableSet) UnmodifiableSet.decorate(registryNames));
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
		
		ManualAnnotations etd = ManualAnnotations.loadManualAnnotations("chempapers");
		NGram customisedNGram = NGramBuilder.buildModel(
				etd, defaultRegistryNames);
		
		short [] data1 = vanillaNGram.getData();
		short [] data2 = vanillaNGram2.getData();
		short [] data3 = customisedNGram.getData();
		
		assertEquals(EXPECTED_DATA_LENTH, data1.length);
		assertEquals(data1.length, data2.length);
		assertEquals(data1.length, data3.length);
		
		boolean identicalData3 = true;
		for (int i = 0; i < data1.length; i++) {
			assertEquals(data1[i], data2[i]);
			if (data1[i] != data3[i]) {
				identicalData3 = false;
				break;
			}
		}
		assertFalse(identicalData3);
	}
	
	
	@Test
	public void testCalculateVanillaSourceDataFingerprint() {
		NGramBuilder builder = new NGramBuilder();
		assertEquals("430379104_-969383385", builder.calculateSourceDataFingerprint());
	}
	
	@Test
	public void testCalculateChempapersSourceDataFingerprint() {
		ManualAnnotations annotations = ManualAnnotations.loadManualAnnotations("chempapers");
		NGramBuilder builder = new NGramBuilder(
				annotations, defaultRegistryNames);
		assertEquals("1662272140_-167370350", builder.calculateSourceDataFingerprint());
	}
	
	@Test
	public void testCalculatePubmedSourceDataFingerprint() {
		ManualAnnotations annotations = ManualAnnotations.loadManualAnnotations("pubmed");
		NGramBuilder builder = new NGramBuilder(
				annotations, defaultRegistryNames);
		assertEquals("-412073498_-1815304182", builder.calculateSourceDataFingerprint());
	}
	
	@Test
	public void testCalculatePubmedEmptyChemnamedictFingerprint() {
		ManualAnnotations annotations = ManualAnnotations.loadManualAnnotations("pubmed");
		NGramBuilder builder = new NGramBuilder(annotations,
				(UnmodifiableSet) UnmodifiableSet.decorate(Collections.emptySet()));
		assertFalse("-412073498_-1815304182".equals(builder.calculateSourceDataFingerprint()));
	}
	
	
	/**
	 * check that the serialised vanilla model can be read and is consistent
	 * with the current training data
	 */
	@Test
	public void testDeserialiseVanillaModel() throws IOException {
		NGram deserialised = NGramBuilder.deserialiseModel("430379104_-969383385");
		short [] deserialisedData = deserialised.getData();
		short [] builtData = vanillaNGram.getData();
		
		double expectedLength = Math.pow(NGramBuilder.ALPHABET.length(), 4);
		assertEquals(expectedLength, deserialisedData.length, .001);
		assertEquals(deserialisedData.length, builtData.length);
		for (int i = 0; i < builtData.length; i++) {
			assertEquals(builtData[i], deserialisedData[i]);
		}
	}
	
	/**
	 * check that the serialised pubmed model can be read and is consistent
	 * with the current training data
	 */
	@Test
	public void testDeserialisePubmedModel() throws IOException {
		NGram deserialised = NGramBuilder.deserialiseModel("-412073498_-1815304182");
		short [] deserialisedData = deserialised.getData();
		short [] builtData = pubmedNGram.getData();
		
		assertEquals(EXPECTED_DATA_LENTH, deserialisedData.length);
		assertEquals(EXPECTED_DATA_LENTH, builtData.length);
		for (int i = 0; i < builtData.length; i++) {
			assertEquals(builtData[i], deserialisedData[i]);
		}
	}
	
	/**
	 * check that the serialised chempapers model can be read and is consistent
	 * with the current training data
	 */
	@Test
	public void testDeserialiseChempapersModel() throws IOException {
		NGram deserialised = NGramBuilder.deserialiseModel("1662272140_-167370350");
		short [] deserialisedData = deserialised.getData();
		short [] builtData = chempapersNGram.getData();
		
		assertEquals(EXPECTED_DATA_LENTH, deserialisedData.length);
		assertEquals(EXPECTED_DATA_LENTH, builtData.length);
		for (int i = 0; i < builtData.length; i++) {
			assertEquals(builtData[i], deserialisedData[i]);
		}
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
		for (int i = 0; i < EXPECTED_DATA_LENTH; i++) {
			assertEquals(vanillaNGram.getData()[i], vanilla.getData()[i]);
		}
	}
	
	@Test
	public void testBuildOrDeserialisePubmedModel() {
		ManualAnnotations annotations = ManualAnnotations.loadManualAnnotations("pubmed");
		NGram pubmedModel = NGramBuilder.buildOrDeserialiseModel(
				annotations, defaultRegistryNames);
		assertNotNull(pubmedModel);
		assertEquals(EXPECTED_DATA_LENTH, pubmedModel.getData().length);
		for (int i = 0; i < EXPECTED_DATA_LENTH; i++) {
			assertEquals(pubmedNGram.getData()[i], pubmedModel.getData()[i]);
		}
	}
	
	@Test
	public void testBuildOrDeserialiseChempapersModel() {
		ManualAnnotations annotations = ManualAnnotations.loadManualAnnotations("chempapers");
		NGram chempapersModel = NGramBuilder.buildOrDeserialiseModel(
				annotations, defaultRegistryNames);
		assertNotNull(chempapersModel);
		assertEquals(EXPECTED_DATA_LENTH, chempapersModel.getData().length);
		for (int i = 0; i < EXPECTED_DATA_LENTH; i++) {
			assertEquals(chempapersNGram.getData()[i], chempapersModel.getData()[i]);
		}
	}
	
}
