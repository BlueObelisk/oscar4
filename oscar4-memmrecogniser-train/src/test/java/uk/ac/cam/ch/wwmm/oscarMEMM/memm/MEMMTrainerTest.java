package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.ont.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;
import uk.ac.cam.ch.wwmm.oscarMEMM.MEMMRecogniser;
import uk.ac.cam.ch.wwmm.oscarrecogniser.saf.StandoffResolver.ResolutionMode;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGramBuilder;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;
import ch.unibe.jexample.Given;
import ch.unibe.jexample.JExample;

@RunWith(JExample.class)
public class MEMMTrainerTest {

	@Test
	public MEMMTrainer testConstructor() {
		MEMMTrainer trainer = new MEMMTrainer(ChemNameDictRegistry.getDefaultInstance());
		assertNotNull(trainer);
		return trainer;
	}

	@Given("testConstructor")
	public String testUntrainedStatus(MEMMTrainer trainer) throws Exception {
		String xml = trainer.getModel().writeModel().toXML();
		assertEquals("<model />", xml);
		return xml;
	}

	@Given("testConstructor,testUntrainedStatus")
	public Element testLearning(MEMMTrainer trainer, String untrainedXML)
			throws Exception {
		List <Document> sourceDocs = new ArrayList<Document>();
		InputStream stream = this
				.getClass()
				.getClassLoader()
				.getResourceAsStream(
					"uk/ac/cam/ch/wwmm/oscarMEMM/memm/paper.xml");
		try {
			sourceDocs.add(new Builder().build(stream));	
		}
		finally {
			IOUtils.closeQuietly(stream);
		}
		trainer.trainOnDocs(sourceDocs);
		Element trainedModel = trainer.getModel().writeModel();
		assertNotSame(untrainedXML, trainedModel.toXML());

		assertEquals("model", trainedModel.getLocalName());
		assertEquals(1, trainedModel.getChildElements("etd").size());
		assertEquals(1, trainedModel.getChildElements("memm").size());

		Element memmModel = trainedModel.getFirstChildElement("memm");
		assertEquals("memm", memmModel.getLocalName());
		Elements elements = memmModel.getChildElements();
		for (int i = 0; i < elements.size(); i++)
			assertEquals("maxent", elements.get(i).getLocalName());

		
		return trainedModel;
	}

	
	@Test
	public void testRecognising() throws Exception {
		List<String> expectedSurfaceList = Arrays.asList("ether", "ether", "ether ketone", "ketone", "ketone", "nitrogen", "nitrogen", "bisphthalazinone", "sulfonated difluoride ketone", "difluoride ketone", "ketone", "potassium", "potassium carbonate", "DMSO", "toluene", "Nitrogen", "Nitrogen", "mixture", "mixture", "water", "toluene", "DMSO", "methanol", "water", "water", "polymer", "7a");
		List<String> expectedTypeList = Arrays.asList("ONT", "ONT", "CM", "CM", "ONT", "CM", "ONT", "CM", "CM", "CM", "ONT", "ONT", "CM", "CM", "CM", "CM", "ONT", "ONT", "ONT", "CM", "CM", "CM", "CM", "CM", "CM", "ONT", "CM");

		String sentence = "Preparation of Sulfonated Poly(phthalazinone ether ether ketone) 7a. To a 25 mL three-necked round-bottomed flask fitted with a Dean-stark trap, a condenser, a nitrogen inlet/outlet, and magnetic stirrer was added bisphthalazinone monomer 4 (0.6267 g, 1 mmol), sulfonated difluoride ketone 5 (0.4223 g, 1 mmol), anhydrous potassium carbonate (0.1935 g, 1.4 mmol), 5 mL of DMSO, and 6 mL of toluene. Nitrogen was purged through the reaction mixture with stirring for 10 min, and then the mixture was slowly heated to 140 \u00B0C and kept stirring for 2 h. After water generated was azoetroped off with toluene. The temperature was slowly increased to 175 \u00B0C. The temperature was maintained for 20 h, and the viscous solution was cooled to 100 \u00B0C followed by diluting with 2 mL of DMSO and, thereafter, precipitated into 100 mL of 1:  1 (v/v) methanol/water. The precipitates were filtered and washed with water for three times. The fibrous residues were collected and dried at 110 \u00B0C under vacuum for 24 h. A total of 0.9423 g of polymer 7a was obtained in high yield of 93%.";
		List<String> actualSurfaceList = new ArrayList<String>();
		List<String> actualTypeList = new ArrayList<String>();
		MEMMRecogniser memm = new MEMMRecogniser(
				trainModel(), OntologyTerms.getDefaultInstance(),
				new ChemNameDictRegistry(Locale.ENGLISH));
		ProcessingDocument procdoc = ProcessingDocumentFactory.getInstance()
				.makeTokenisedDocument(Tokeniser.getDefaultInstance(), sentence);
		List<NamedEntity> neList = memm.findNamedEntities(procdoc.getTokenSequences(), ResolutionMode.MARK_BLOCKED);
		assertEquals("Number of recognised entities: ", 27, neList.size());
		for (NamedEntity namedEntity : neList) {
			actualSurfaceList.add(namedEntity.getSurface());
			actualTypeList.add(namedEntity.getType().getName());
		}
		assertEquals("Chemical Names recognised",expectedSurfaceList,actualSurfaceList);
		assertEquals("Chemical Types recognised",expectedTypeList,actualTypeList);
	}
	
	@Given("testLearning")
	public void testExtractTrainingData(Element trainedModel) throws Exception{
		MEMMModel model = new MEMMModel(trainedModel);
		assertEquals("Number of Chemical words in ExtractedTrainingData size",453, model.getExtractedTrainingData().getChemicalWords().size());
		assertEquals("Number of non-chemical words in ExtractedTrainingData size",1175, model.getExtractedTrainingData().getNonChemicalWords().size());
	}
	
	
	@Test
	/**
	 * To check that the same model is always produced from the same input
	 */
	public void testDeterministicModelProduction() throws Exception {
		Element model1 = trainModel().writeModel();
		Element model2 = trainModel().writeModel();
		assertTrue(model1.toXML().equals(model2.toXML()));
		
		MEMMModel model = new MEMMModel(model1);
		//previously, reading a model has triggered a change in the
		//ExtractedTrainingData causing a different model to be produced

		Element model3 = trainModel().writeModel();
		
		assertTrue(model1.toXML().equals(model2.toXML()));
		assertTrue(model1.toXML().equals(model3.toXML()));
	}
	
	
	@Test
	public void testEventCollection() throws Exception {
		InputStream in = ClassLoader.getSystemResourceAsStream("uk/ac/cam/ch/wwmm/oscarMEMM/memm/eventCollectionTest.xml");
		List <Document> sourceDocs = new ArrayList<Document>();
		try {
			sourceDocs.add(new Builder().build(in));
		}
		finally {
			IOUtils.closeQuietly(in);
		}
		MEMMTrainer trainer = new MEMMTrainer(ChemNameDictRegistry.getDefaultInstance());
		
		trainer.trainOnDocs(sourceDocs);
		assertEquals(6, trainer.evsByPrev.keySet().size());
		assertTrue(trainer.evsByPrev.keySet().contains(BioType.fromString("B-CM")));
		assertFalse(trainer.evsByPrev.keySet().contains(BioType.fromString("I-CM")));
		assertTrue(trainer.evsByPrev.keySet().contains(BioType.fromString("B-RN")));
		assertTrue(trainer.evsByPrev.keySet().contains(BioType.fromString("I-RN")));
		assertTrue(trainer.evsByPrev.keySet().contains(BioType.fromString("B-ASE")));
		assertTrue(trainer.evsByPrev.keySet().contains(BioType.fromString("I-ASE")));
		assertTrue(trainer.evsByPrev.keySet().contains(BioType.fromString("O")));
	}
	

	@Test
	public void testModelReloading() throws Exception {
		MEMMModel trained = trainModel();
		Element serialised = trained.writeModel();
		MEMMModel reloaded = new MEMMModel();
		reloaded.readModel(serialised);
		reloaded.chemNameDictNames = Collections
				.unmodifiableSet(ChemNameDictRegistry.getDefaultInstance()
						.getAllNames());
		reloaded.nGram = NGramBuilder.buildOrDeserialiseModel(reloaded.etd, reloaded.chemNameDictNames);
		
		assertTrue(trained.nGram.compareTo(reloaded.nGram));
		
		String text1 = "Preparation of Sulfonated Poly(phthalazinone ether ether ketone) 7a. To a 25 mL three-necked round-bottomed flask fitted with a Dean-stark trap, a condenser, a nitrogen inlet/outlet, and magnetic stirrer was added bisphthalazinone monomer 4 (0.6267 g, 1 mmol), sulfonated difluoride ketone 5 (0.4223 g, 1 mmol), anhydrous potassium carbonate (0.1935 g, 1.4 mmol), 5 mL of DMSO, and 6 mL of toluene. Nitrogen was purged through the reaction mixture with stirring for 10 min, and then the mixture was slowly heated to 140 \u00B0C and kept stirring for 2 h. After water generated was azoetroped off with toluene. The temperature was slowly increased to 175 \u00B0C. The temperature was maintained for 20 h, and the viscous solution was cooled to 100 \u00B0C followed by diluting with 2 mL of DMSO and, thereafter, precipitated into 100 mL of 1:  1 (v/v) methanol/water. The precipitates were filtered and washed with water for three times. The fibrous residues were collected and dried at 110 \u00B0C under vacuum for 24 h. A total of 0.9423 g of polymer 7a was obtained in high yield of 93%.";
		String text2 = "The oxidation step itself is challenging as it involves the formal removal of four hydrogens from a tetrahydropyridine moiety to reach the fully aromatic species. The literature contains scattered reports of the use of oxidants for this transformation: 2,3-Dichloro-5,6-dicyano-1,4-benzoquinone (DDQ), ceric ammonium nitrate (CAN), nitrobenzene, elemental sulfur, palladium and manganese dioxide among others, all of them far from being ideally suited for these substrates.";
		String text3 = "The Ugi reaction of 2-substituted dihydrobenzoxazepines was found to proceed with unexpectedly good diastereoselectivitiy (diastereoisomeric ratios up to 9:1), despite the large distance between the pre-existing stereogenic centre and the newly generated one. This result represents the first good 1,4 asymmetric induction in an Ugi reaction as well as the first example of diastereoselective Ugi reaction of seven membered cyclic imines. It allows the diversity-oriented synthesis of various tetrahydro[f][1,4]benzoxazepines.";
		String text4 = "A practical approach to highly functionalized 4-hydroxypyridine derivatives with stereogenic side chains in the 2- and 6-positions is described. The presented two-step process utilizes a multicomponent reaction of alkoxyallenes, nitriles and carboxylic acids to provide β-methoxy-β-ketoenamides which are transformed into 4-hydroxypyridines in a subsequent cyclocondensation. The process shows broad substrate scope and leads to differentially substituted enantiopure pyridines in good to moderate yields. The preparation of diverse substituted lactic acid derived pyrid-4-yl nonaflates is described. Additional evidence for the postulated mechanism of the multicomponent reaction is presented.";
		String text5 = "While alkynic o-nitrotoluenesulfonamide 4b did not react at all with 0.5 mol % of [Au(NTf2)(L1)] at room temperature (Table 2, entry 1), this substrate underwent 7-exo-dig cyclization upon increasing catalyst loading to 2.5 mol % and heating at 80 °C, giving N-nosylazepine derivative 5b in 76% isolated yield (Table 2, entry 2). N-Benzyloxycarbonyl (Cbz) and N-acetylazepine derivatives 5c and 5d were obtained in low yields through the cyclization of substrates 4c and 4d (Table 2, entries 3 and 4). On the other hand, the reactions of the substrates bearing N-tert-butoxycarbonyl (Boc) or N-p-methoxybenzyl (PMB) groups (4e,f) did not give the desired products at all (Table 2, entries 5 and 6). It seems that the reactivity of the substrates is affected by the balance between nucleophilicity of the nitrogen atom and acidity of the N–H bond as well as a steric factor.";
		String text6 = "(2-Methoxymethoxynaphthalen-1-yl)propynoic acid naphthalen-2-yl ester (1c): To a stirred solution of 3-[2-(methoxymethoxy)-1-naphthalenyl]-2-propynoic acid [48] (0.256 g, 1.00 mmol), 2-naphthol (0.159 g, 1.10 mmol), and 4-dimethylaminopyridine (12.2 mg, 0.100 mmol) in CH2Cl2 (10 mL) was added a solution of dicyclohexylcarbodiimide (0.248 g, 1.20 mmol) in CH2Cl2 (3 mL) at 0 °C, and the mixture was stirred at 0 °C for 2 h and at room temperature for 18 h. The crude mixture was filtered with CH2Cl2. The filtrate was washed with brine, dried over Na2SO4, and concentrated. The residue was purified by a silica gel column chromatography (hexane/EtOAc = 10:1) to give 1c (0.222 g, 0.580 mmol, 58% yield).";
		
		compareModelResults(trained, reloaded, text1);
		compareModelResults(trained, reloaded, text2);
		compareModelResults(trained, reloaded, text3);
		compareModelResults(trained, reloaded, text4);
		compareModelResults(trained, reloaded, text5);
		compareModelResults(trained, reloaded, text6);
	}

	private void compareModelResults(MEMMModel trained, MEMMModel reloaded,
			String text) {

		TokenSequence tokSeq = Tokeniser.getDefaultInstance().tokenise(text);
		List <NamedEntity> trainedNes1 = trained.findNEs(tokSeq, 0.04);
		List <NamedEntity> trainedNes2 = trained.findNEs(tokSeq, 0.04);
		List <NamedEntity> reloadedNes1 = reloaded.findNEs(tokSeq, 0.04);
		List <NamedEntity> reloadedNes2 = reloaded.findNEs(tokSeq, 0.04);
		assertEquals(trainedNes1, trainedNes2);
		assertEquals(reloadedNes1, reloadedNes2);
		assertEquals(trainedNes1, reloadedNes1);
	}
	

	private MEMMModel trainModel() throws Exception {
		MEMMTrainer trainer = new MEMMTrainer(ChemNameDictRegistry.getDefaultInstance());
		List <Document> sourceDocs = new ArrayList<Document>();
		InputStream stream = this
				.getClass()
				.getClassLoader()
				.getResourceAsStream(
					"uk/ac/cam/ch/wwmm/oscarMEMM/memm/paper.xml");
		try {
			sourceDocs.add(new Builder().build(stream));
		} finally {
			IOUtils.closeQuietly(stream);
		}
		trainer.trainOnDocs(sourceDocs);
		return trainer.getModel();
	}
	
}
