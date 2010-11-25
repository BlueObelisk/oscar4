package uk.ac.cam.ch.wwmm.oscarMEMM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nu.xom.Document;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.IProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.interfaces.ChemicalEntityRecogniser;
import uk.ac.cam.ch.wwmm.oscar.scixml.TextToSciXML;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * @author lh359
 * 
 */
public class RegressionTestsforOscar3 {

	Logger LOG = Logger.getLogger(RegressionTestsforOscar3.class);

	@Test
	public void testConstructor() {
		Assert.assertNotNull(new MEMMRecogniser());
	}

	@Test
	@Ignore
	public void testSentence1() throws Exception {
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar3/test/regressionTest/");
		String sentence = rg.getString("Sentence1.txt");

		List<String> expectedSurfaceList = Arrays.asList(
				"5-ethoxymethylene-1,3-diaryl-2-thiobarbituric acids_1",
				"malononitrile_1", "A_1", "C_1", "acid_1", "hydrolysis_1",
				"C_2", "5-ethoxymethylene-1,3-diaryl-2-thiobarbituric acids_2",
				"malononitrile_2");
		List<Double> expectedProbList = Arrays.asList(0.9522672355736751,
				0.9956194686746215, 0.00, 0.30735471308029166, 0.00,
				0.9999484864253582, 0.34065781326024086, 0.9551279164821104,
				0.9956696660305651);
		List<String> expectedTypeList = Arrays.asList("CM", "CM", "ONT", "CM",
				"ONT", "RN", "CM", "CM", "CM");
		
		evaluateNamedEntities(sentence, expectedSurfaceList, expectedTypeList,
				expectedProbList);
	}

	@Test
	@Ignore
	public void testSentence2() throws Exception {
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar3/test/regressionTest/");
		String sentence = rg.getString("Sentence2.txt");
		List<String> expectedSurfaceList = Arrays.asList("N-methylaniline_1",
				"N,N-dimethylaniline_1", "calcium_1", "silica_1");
		List<Double> expectedProbList = Arrays.asList(0.9964313404393998,
				0.9995782016130931, 0.9860357829790156, 0.9603591420667275);
		List<String> expectedTypeList = Arrays.asList("CM", "CM", "CM", "CM");
		
		evaluateNamedEntities(sentence, expectedSurfaceList, expectedTypeList,
				expectedProbList);
	}

	@Test
	@Ignore
	public void testSentence3() throws Exception {
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar3/test/regressionTest/");
		String sentence = rg.getString("Sentence3.txt");
		List<String> expectedSurfaceList = Arrays.asList("dNTPs_1",
				"γ-phosphates_1", "dUTP_1", "ligand_1", "groups_1", "C5_1",
				"uracil_1", "dNTPs_2", "AMV_1", "size_1", "γ-phosphonate_1");
		List<Double> expectedProbList = Arrays.asList(0.39248159697863777,
				0.968218689388447, 0.5126968674819777, 0.00, 0.00,
				0.46708681080060027, 0.4541378222332686, 0.40645323363275354,
				0.31359322291086644, 0.00, 0.9951601406578985);
		List<String> expectedTypeList = Arrays.asList("CM", "CM", "CM", "ONT",
				"ONT", "CM", "CM", "CM", "CM", "ONT", "CM");

		
		evaluateNamedEntities(sentence, expectedSurfaceList, expectedTypeList,
				expectedProbList);
	}

	@Test

	@Ignore
	public void testSentence4() throws Exception {
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar3/test/regressionTest/");
		String sentence = rg.getString("Sentence4.txt");
		List<String> expectedSurfaceList = Arrays
				.asList("2′-deoxythymidine 5′-[β,γ-(methylphosphinyl)methyl-phosphonyl]-α-phosphate_1",
						"3′-azido-3′-deoxy_1", "AMV_1");
		List<Double> expectedProbList = Arrays.asList(0.9186146617851262,
				0.9964296971374825, 0.2998157263149458);
		List<String> expectedTypeList = Arrays.asList("CM", "CM", "CM");
		
		evaluateNamedEntities(sentence, expectedSurfaceList, expectedTypeList,
				expectedProbList);
	}

	@Test

	@Ignore
	public void testSentence5() throws Exception {
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar3/test/regressionTest/");
		String sentence = rg.getString("Sentence5.txt");
		List<String> expectedSurfaceList = Arrays.asList("A_1", "124D_1",
				"iron_1", "ammonium chloride_1", "EtOH_1", "H2O_1", "water_1",
				"dichloromethane_1", "Na2SO4_1", "124E_1");
		List<Double> expectedProbList = Arrays.asList(0.00,
				0.36483978870258776, 0.740647747313242, 0.984944130100973,
				0.8187057257195454, 0.5593710134101818, 0.9926313739994163,
				0.9984564769946481, 0.7821893198686901, 0.2642592235899056);
		List<String> expectedTypeList = Arrays.asList("ONT", "CM", "CM", "CM",
				"CM", "CM", "CM", "CM", "CM", "CM");
		
		evaluateNamedEntities(sentence, expectedSurfaceList, expectedTypeList,
				expectedProbList);
	}

	@Test
	@Ignore
	public void testSentence6() throws Exception {
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar3/test/regressionTest/");
		String sentence = rg.getString("Sentence6.txt");
		List<String> expectedSurfaceList = Arrays
				.asList("A_1",
						"8-hydroxyquinoline-7−carboxylic acid_1",
						"benzylamine_1",
						"1‐hydroxybenzotriazole_1",
						"1-(3-dimethylaminopropyl)-3-ethylcarbodiimide hydrochloride_1",
						"triethylamine_1", "DMF_1", "methanol_1", "C-18_1",
						"water_1", "acetonitrile_1", "TFA_1");
		List<Double> expectedProbList = Arrays.asList(0.00, 0.9763876481410927,
				0.877166645595111, 0.8070202675168012, 0.8771168559135208,
				0.9430003382163082, 0.7821893198686901, 0.9943684604393002,
				0.39805870405590443, 0.9777133455959804, 0.9469694587383923,
				0.5893981910757157);
		List<String> expectedTypeList = Arrays.asList("ONT", "CM", "CM", "CM",
				"CM", "CM", "CM", "CM", "CM", "CM", "CM", "CM");
		evaluateNamedEntities(sentence, expectedSurfaceList, expectedTypeList,
				expectedProbList);
	}

	/*
	 * Evaluates the NamedEntities produced by the MEMM recogniser against the
	 * surface, probability and type values found by OSCAR3's MEMM Recogniser
	 */
	private void evaluateNamedEntities(String sentence,
			List<String> expectedSurfaceList, List<String> expectedTypeList,
			List<Double> expectedProbList) throws Exception {

		// Check that Sentence is not empty
		Assert.assertTrue("Extracting String: ",
				sentence != null && sentence.length() > 0);

		Document doc = TextToSciXML.textToSciXML(sentence);
		IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance()
				.makeTokenisedDocument(Tokeniser.getInstance(), doc, false,
						false, false);
		// Check that ProcDoc is not empty
		Assert.assertTrue(procDoc != null);

		List<NamedEntity> neList;
		ChemicalEntityRecogniser cei = new MEMMRecogniser();
		neList = cei.findNamedEntities(procDoc.getTokenSequences());
		// Check that neList is not empty
		Assert.assertTrue(neList != null);

		List<String> actualSurfaceList = new ArrayList<String>();
		List<Object> actualProbList = new ArrayList<Object>();
		List<NamedEntityType> actualTypeList = new ArrayList<NamedEntityType>();
		for (NamedEntity namedEntity : neList) {
			// Using a count to differentiate between duplicates in a list
			int count = 1;
			String surface = namedEntity.getSurface();
			while (actualSurfaceList.contains(surface + "_"
					+ String.valueOf(count)))
				count++;

			surface = surface + "_" + String.valueOf(count);
			actualSurfaceList.add(surface);
			actualTypeList.add(namedEntity.getType());
			actualProbList.add(namedEntity.getConfidence());

			// Check if NamedEntity Surface is in the expectedSurfaceList
			Assert.assertTrue(surface+" is a false positive ",expectedSurfaceList.contains(surface));
			
			if (expectedSurfaceList.contains(surface)) {
				int index = expectedSurfaceList.indexOf(surface);
				Assert.assertEquals("Type for " + namedEntity.getSurface(),
						expectedTypeList.get(index), namedEntity.getType());
				if (!NamedEntityType.ONTOLOGY.isInstance(namedEntity.getType())) {
					Assert.assertEquals(
							"Probability for " + namedEntity.getSurface(),
							expectedProbList.get(index),
							(Double) namedEntity.getConfidence());
				}

			}

		}
		for (String string : expectedSurfaceList) {
			Assert.assertTrue(string + " is a false negative ",
					actualSurfaceList.contains(string));
		}

	}

}
