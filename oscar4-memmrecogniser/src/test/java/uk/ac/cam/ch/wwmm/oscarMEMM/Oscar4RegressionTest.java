package uk.ac.cam.ch.wwmm.oscarMEMM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nu.xom.Document;

import org.junit.Assert;
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
 * Test failures here may indicate changes to OSCAR's features as
 * opposed to bugs.
 * 
 * @author lh359
 * @author dmj30
 */
public class Oscar4RegressionTest {

	@Test
	public void testConstructor() {
		Assert.assertNotNull(new MEMMRecogniser());
	}

	@Test
	public void testSentence1() throws Exception {
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar3/test/regressionTest/");
		String sentence = rg.getString("Sentence1.txt");

		List<String> expectedSurfaceList = Arrays.asList("5-_1",
				"5-ethoxymethylene-1,3-diaryl-2-thiobarbituric acids_1",
				"acids_1", "malononitrile_1", "C_1", "acid_1", "hydrolysis_1",
				"hydrolysis_2", "C_2", "5-_2",
				"5-ethoxymethylene-1,3-diaryl-2-thiobarbituric acids_2",
				"acids_2", "malononitrile_2");
		List<Double> expectedProbList = Arrays.asList(0.00, 0.9362979800888434,
				0.00, 0.9956194686746215, 0.30735471308029166, 0.00,
				0.9999484864253582, 0.00, 0.34065781326024086, 0.00,
				0.9400554713275171, 0.00, 0.9956696660305651);
		List<String> expectedTypeList = Arrays.asList("CPR", "CM", "ONT", "CM",
				"CM", "ONT", "RN", "ONT", "CM", "CPR", "CM", "ONT", "CM");
		evaluateNamedEntities(sentence, expectedSurfaceList, expectedTypeList,
				expectedProbList);
	}

	@Test
	public void testSentence2() throws Exception {
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar3/test/regressionTest/");
		String sentence = rg.getString("Sentence2.txt");
		List<String> expectedSurfaceList = Arrays.asList("N-_1","N-methylaniline_1","N,N-_1","N,N-dimethylaniline_1","calcium_1","calcium_2","silica_1");
		List<Double> expectedProbList = Arrays.asList(0.00, 0.9964313404393998,0.00, 0.9995782016130931,0.9860357829790156,0.00,0.9603591420667275);
		List<String> expectedTypeList = Arrays.asList("CPR","CM","CPR","CM","CM","ONT","CM");

		evaluateNamedEntities(sentence, expectedSurfaceList, expectedTypeList,
				expectedProbList);
	}

	@Test
	public void testSentence3() throws Exception {
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar3/test/regressionTest/");
		String sentence = rg.getString("Sentence3.txt");
		List<String> expectedSurfaceList = Arrays.asList("dNTPs_1","\u03b1-_1", "\u03b2-_1", "\u03B3-_1","\u03B3-phosphates_1","dUTP_1","ligand_1","groups_1","C5_1","uracil_1","\u03B3-_2","dNTPs_2","AMV_1","size_1","\u03B3-_3","\u03B3-phosphonate_1");
		List<Double> expectedProbList = Arrays.asList(0.39248159697863777, 0.00, 0.00, 0.00, 0.968218689388447,0.5126968674819777,0.00, 0.00, 0.46708681080060027,0.4541378222332686,0.00, 0.40645323363275354,0.29253811092128085,0.00, 0.00, 0.9917673063361496);
		List<String> expectedTypeList = Arrays.asList("CM","CPR","CPR","CPR","CM","CM","ONT","ONT","CM","CM","CPR","CM","CM","ONT","CPR","CM");
		evaluateNamedEntities(sentence, expectedSurfaceList, expectedTypeList,
				expectedProbList);
	}

	@Test
	public void testSentence4() throws Exception {
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar3/test/regressionTest/");
		String sentence = rg.getString("Sentence4.txt");
		List<String> expectedSurfaceList = Arrays.asList("2\u2032-_1","2\u2032-deoxythymidine 5\u2032-[\u03b2,\u03B3-(methylphosphinyl)methyl-phosphonyl]-\u03b1-phosphate_1","5\u2032-_1","3\u2032-_1","3\u2032-azido-3\u2032-deoxy_1","AMV_1");
		List<Double> expectedProbList = Arrays.asList(0.00, 0.9186146617851262,0.00, 0.00, 0.9969339157756932,0.29253811092128085);
		List<String> expectedTypeList = Arrays.asList("CPR","CM","CPR","CPR","CM","CM");

		evaluateNamedEntities(sentence, expectedSurfaceList, expectedTypeList,
				expectedProbList);
	}

	@Test
	public void testSentence5() throws Exception {
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar3/test/regressionTest/");
		String sentence = rg.getString("Sentence5.txt");
		List<String> expectedSurfaceList = Arrays.asList("124D_1","iron_1","ammonium chloride_1","EtOH_1","H2O_1","water_1","dichloromethane_1","Na2SO4_1","124E_1");
		List<Double> expectedProbList = Arrays.asList(0.36483978870258776,0.740647747313242,0.984944130100973,0.8187057257195454,0.5593710134101818,0.9923715644431383,0.9984564769946481,0.7821893198686901,0.2642592235899056);
		List<String> expectedTypeList = Arrays.asList("CM","CM","CM","CM","CM","CM","CM","CM","CM");
		evaluateNamedEntities(sentence, expectedSurfaceList, expectedTypeList,
				expectedProbList);
	}

	@Test
	public void testSentence6() throws Exception {
		ResourceGetter rg = new ResourceGetter(
				"uk/ac/cam/ch/wwmm/oscar3/test/regressionTest/");
		String sentence = rg.getString("Sentence6.txt");
		List<String> expectedSurfaceList = Arrays.asList("8-_1", "8-hydroxyquinoline-7-carboxylic acid_1","acid_1","benzylamine_1","1\u2010_1","1\u2010hydroxybenzotriazole_1","1-_1","1-(3-dimethylaminopropyl)-3-ethylcarbodiimide hydrochloride_1","hydrochloride_1","triethylamine_1","DMF_1","methanol_1","C-_1","C-18_1","water_1","acetonitrile_1","TFA_1");
		List<Double> expectedProbList = Arrays.asList(0.00, 0.9763876481410927, 0.00, 0.877166645595111, 0.00, 0.8070202675168012, 0.00, 0.8771168559135208, 0.00, 0.9430003382163082, 0.7821893198686901, 0.9943684604393002, 0.00, 0.39805870405590443, 0.9726097731082048, 0.9469694587383923, 0.5893981910757157);
		List<String> expectedTypeList = Arrays.asList("CPR","CM","ONT","CM","CPR","CM","CPR","CM","ONT","CM","CM","CM","CPR","CM","CM","CM","CM");
		evaluateNamedEntities(sentence, expectedSurfaceList, expectedTypeList,
				expectedProbList);
	}

	/**
	 * Evaluates the NamedEntities produced by the MEMM recogniser against the
	 * previously generated surface, probability and type values found by
	 *  OSCAR4's MEMM Recogniser
	 */
    private void evaluateNamedEntities(String sentence,
			List<String> expectedSurfaceList, List<String> expectedTypeNames,
 			List<Double> expectedProbList) throws Exception {

        List<NamedEntityType> expectedTypeList = new ArrayList<NamedEntityType>();
        for (String name : expectedTypeNames) {
            expectedTypeList.add(NamedEntityType.valueOf(name));
        }

		// Check that Sentence is not empty
		Assert.assertTrue("Extracting String: ",
				sentence != null && sentence.length() > 0);

		Document doc = TextToSciXML.textToSciXML(sentence);
		IProcessingDocument procDoc = ProcessingDocumentFactory.getInstance()
				.makeTokenisedDocument(Tokeniser.getInstance(), doc);
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
			Assert.assertTrue(surface + " is a false positive ",
					expectedSurfaceList.contains(surface));

			if (expectedSurfaceList.contains(surface)) {
				int index = expectedSurfaceList.indexOf(surface);
				Assert.assertEquals("Type for " + namedEntity.getSurface(),
						expectedTypeList.get(index), namedEntity.getType());
				if (!NamedEntityType.ONTOLOGY.isInstance(namedEntity.getType())
                        && !NamedEntityType.LOCANTPREFIX.isInstance(namedEntity.getType())) {
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
