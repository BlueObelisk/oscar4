package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.dictionaries.PolymerDictionary;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.ont.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarrecogniser.tokenanalysis.NGram;
import uk.ac.cam.ch.wwmm.oscartokeniser.TokenClassifier;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author sea36
 * @author dmj30
 */
public class DFANEFinderTest {

	private static Set<String> defaultRegistryNames;
	private static DFANEFinder finder;
	private static Tokeniser tokeniser;
	
	private static final double DEFAULT_NGRAM_THRESHOLD = -2;

	@BeforeClass
	public static void setup() {
		defaultRegistryNames = Collections.unmodifiableSet(ChemNameDictRegistry.getDefaultInstance().getAllNames());
		finder = DFANEFinder.getDefaultInstance();
		tokeniser = Tokeniser.getDefaultInstance();
	}
	
	@AfterClass
	public static void cleanUp() {
		defaultRegistryNames = null;
		finder = null;
		tokeniser = null;
	}
	
    @Test
    public void testDFANEFinder() {
        // Synthesis of 4-Pyridone-3-sulfate and an improved synthesis of 3-Hydroxy-4-Pyridone
        // Edward J Behrman
        // Chemistry Central Journal 2009, 3:1
        // doi:10.1186/1752-153X-3-1
        String paragraph = "Following removal of 4-pyridone by extraction with methyl ethyl ketone as described above, the dried salt cake was re-extracted with about 250 mL of 95% ethanol for seven hours. Cooling the solution gave 2 g of crude material as off-white crystals (17%). It was recrystallized from 95% ethanol. It gradually decomposes between 250\u2013290\u00B0C without melting. Anal. calcd. for C5H4NO5SNa + 1.5H2O: C, 25.00%; H, 2.94%; N, 5.83%. Found: C, 25.17%, H, 2.70%; N, 5.87%. Hydrolysis of the ester in 0.2 M sulfuric acid for 30 min at 95\u00B0C followed by neutralization with NaOH and drying gives a residue from which 3-hydroxy-4-pyridone may be extracted quantitatively by trituration with boiling 95% ethanol and purification by crystallization from methanol as above.";

        TokenSequence ts = tokeniser.tokenise(paragraph);
        List<NamedEntity> namedEntityList = finder.findNamedEntities(ts, NGram.getInstance(), DEFAULT_NGRAM_THRESHOLD);
        String expected = "[NE:STOP:18:20:of], [NE:CPR:21:23:4-], [NE:CM:21:31:4-pyridone], [NE:CM:21:31:4-pyridone], [NE:STOP:32:34:by], [NE:STOP:46:50:with], [NE:CM:51:57:methyl], [NE:CM:51:57:methyl], [NE:CM:51:57:methyl], [NE:CM:58:63:ethyl], [NE:CM:58:63:ethyl], [NE:CM:58:63:ethyl], [NE:CM:51:70:methyl ethyl ketone], [NE:CM:58:70:ethyl ketone], [NE:CM:64:70:ketone], [NE:CM:64:70:ketone], [NE:ONT:64:70:ketone], [NE:STOP:71:73:as], [NE:STOP:84:89:above], [NE:STOP:91:94:the], [NE:ONT:101:105:salt], [NE:STOP:111:114:was], [NE:STOP:128:132:with], [NE:STOP:133:138:about], [NE:STOP:146:148:of], [NE:CM:153:160:ethanol], [NE:CM:153:160:ethanol], [NE:CM:153:160:ethanol], [NE:STOP:161:164:for], [NE:STOP:186:189:the], [NE:STOP:208:210:of], [NE:STOP:226:228:as], [NE:STOP:229:232:off], [NE:AHA:255:257:It], [NE:STOP:258:261:was], [NE:STOP:277:281:from], [NE:CM:286:293:ethanol], [NE:CM:286:293:ethanol], [NE:CM:286:293:ethanol], [NE:AHA:295:297:It], [NE:STOP:319:326:between], [NE:CPR:327:331:250\u2013], [NE:STOP:337:344:without], [NE:ONT:345:352:melting], [NE:CM:360:365:calcd], [NE:CM:360:365:calcd], [NE:STOP:367:370:for], [NE:CM:371:381:C5H4NO5SNa], [NE:CM:384:390:1.5H2O], [NE:AHA:392:393:C], [NE:CM:392:393:C], [NE:CM:392:393:C], [NE:AHA:403:404:H], [NE:CM:403:404:H], [NE:CM:403:404:H], [NE:AHA:413:414:N], [NE:CM:413:414:N], [NE:CM:413:414:N], [NE:AHA:430:431:C], [NE:CM:430:431:C], [NE:CM:430:431:C], [NE:AHA:441:442:H], [NE:CM:441:442:H], [NE:CM:441:442:H], [NE:AHA:451:452:N], [NE:CM:451:452:N], [NE:CM:451:452:N], [NE:ONT:461:471:Hydrolysis], [NE:STOP:472:474:of], [NE:STOP:475:478:the], [NE:ONT:479:484:ester], [NE:STOP:485:487:in], [NE:STOP:488:493:0.2 M], [NE:AHA:492:493:M], [NE:CM:494:507:sulfuric acid], [NE:STOP:503:507:acid], [NE:ONT:503:507:acid], [NE:STOP:508:511:for], [NE:STOP:519:521:at], [NE:STOP:536:538:by], [NE:STOP:554:558:with], [NE:AHA:559:563:NaOH], [NE:CM:559:563:NaOH], [NE:CM:559:563:NaOH], [NE:STOP:564:567:and], [NE:STOP:581:582:a], [NE:STOP:591:595:from], [NE:STOP:596:601:which], [NE:CPR:602:604:3-], [NE:CM:602:622:3-hydroxy-4-pyridone], [NE:CM:602:622:3-hydroxy-4-pyridone], [NE:STOP:623:626:may], [NE:STOP:627:629:be], [NE:STOP:655:657:by], [NE:STOP:670:674:with], [NE:CM:687:694:ethanol], [NE:CM:687:694:ethanol], [NE:CM:687:694:ethanol], [NE:STOP:695:698:and], [NE:STOP:712:714:by], [NE:ONT:715:730:crystallization], [NE:STOP:731:735:from], [NE:CM:736:744:methanol], [NE:CM:736:744:methanol], [NE:CM:736:744:methanol], [NE:STOP:745:747:as], [NE:STOP:748:753:above]";
//FIXME DFAFinder can find exactly the same entity multiple times if it is found for different reasons e.g. $INCND (is in chemnamedict), $-yl (ends in yl) etc.
        List<String> found = new ArrayList<String>();
        for (NamedEntity ne : namedEntityList) {
            StringBuilder sb = new StringBuilder("[NE:");
            NamedEntityType type = ne.getType();
            if (type.getParent() != null) {
                type = type.getParent();
            }
            sb.append(type).append(':').append(ne.getStart()).append(':').append(ne.getEnd()).append(':').append(ne.getSurface()).append(']');
            found.add(sb.toString());
        }

        assertEquals(Arrays.asList(expected.split(", ")), found);
    }
    
    @Test
    public void testGenerateTokenRepresentations() {
    	String paragraph = "The quick brown ethyl acetate jumps over the lazy adenosine triphosphate";
        TokenSequence ts = tokeniser.tokenise(paragraph);
        List<RepresentationList> repsLists = finder.generateTokenRepresentations(ts, NGram.getInstance(), DEFAULT_NGRAM_THRESHOLD);
        assertEquals(11, repsLists.size());
        for (int i = 0; i < 11; i++) {
        	if (i==3) {
        		assertTrue(repsLists.get(i).toList().contains("$GP"));
        		assertFalse(repsLists.get(i).toList().contains("$CM"));
        	}
        	else if (i==4 | i==9 | i==10) {
        		assertTrue(repsLists.get(i).toList().contains("$CM"));
        		assertFalse(repsLists.get(i).toList().contains("$GP"));
        	}
        	else {
        		assertFalse(repsLists.get(i).toList().contains("$CM"));
        		assertFalse(repsLists.get(i).toList().contains("$GP"));
        	}
        }
    }
    
    @Test
    public void testGenerateTokenRepresentationsObeysNgramThreshold() {
    	String paragraph = "The quick brown ethyl acetate jumps over the lazy adenosine triphosphate";
        TokenSequence ts = tokeniser.tokenise(paragraph);
        List<RepresentationList> repsLists = finder.generateTokenRepresentations(ts, NGram.getInstance(), 500);
        assertEquals(11, repsLists.size());
        for (int i = 0; i < 11; i++) {
    		assertFalse(repsLists.get(i).toList().contains("$CM"));
    		assertFalse(repsLists.get(i).toList().contains("$GP"));
        }
    }

    @Test
    public void testSulfuricAcid() {
        // derived from testDFANEFinder()
    	StringBuffer paragraph = new StringBuffer();
    	for (int i=1; i<47; i++) paragraph.append("          ");
    	paragraph.append("    ");
        paragraph.append(
        	"Hydrolysis of the ester in 0.2 M sulfuric acid for 30 min at 95\u00B0C."
        );

        TokenSequence ts = tokeniser.tokenise(paragraph.toString());
        List<NamedEntity> namedEntityList = finder.findNamedEntities(ts, NGram.getInstance(), DEFAULT_NGRAM_THRESHOLD);
        String expected = "[NE:ONT:464:474:Hydrolysis], [NE:STOP:475:477:of], [NE:STOP:478:481:the], [NE:ONT:482:487:ester], [NE:STOP:488:490:in], [NE:STOP:491:496:0.2 M], [NE:AHA:495:496:M], [NE:CM:497:510:sulfuric acid], [NE:STOP:506:510:acid], [NE:ONT:506:510:acid], [NE:STOP:511:514:for], [NE:STOP:522:524:at]";

        List<String> found = new ArrayList<String>();
        for (NamedEntity ne : namedEntityList) {
            StringBuilder sb = new StringBuilder("[NE:");
            NamedEntityType type = ne.getType();
            if (type.getParent() != null) {
                type = type.getParent();
            }
            sb.append(type).append(':').append(ne.getStart()).append(':').append(ne.getEnd()).append(':').append(ne.getSurface()).append(']');
            found.add(sb.toString());
        }

        assertEquals(Arrays.asList(expected.split(", ")), found);
    }

    @Test
    public void testHPEI() throws Exception {
        // derived from ChemicalTagger's ChemistryPOSTaggerTest.sentence1()
    	String paragraph = 
        	"The synthetic procedure for partially EA- or BA-modified HPEI is exemplified for HPEI25K.";
        TokenSequence ts = tokeniser.tokenise(paragraph);
        ChemNameDictRegistry registry = new ChemNameDictRegistry();
        registry.register(new PolymerDictionary());
        DFANEFinder finder = new DFANEFinder(TermMaps.getInstance().getNeTerms(),
        		TokenClassifier.getDefaultInstance(), OntologyTerms.getDefaultInstance(),
        		Collections.unmodifiableSet(registry.getAllNames()));
        List<NamedEntity> neList = finder.findNamedEntities(ts, NGram.getInstance(), DEFAULT_NGRAM_THRESHOLD);
        assertTrue(namedEntityListContainsNamedEntity(neList, NamedEntityType.COMPOUND, "HPEI25K", 81, 88));
    }
    
    @Test
    public void testFindOntIds() {
    	String source = "noble gas";
    	ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
    			Tokeniser.getDefaultInstance(), source);
    	
    	List<NamedEntity> nes = finder.findNamedEntities(
    			procDoc.getTokenSequences().get(0), NGram.getInstance(), DEFAULT_NGRAM_THRESHOLD);
    	assertEquals(1, nes.size());
    	assertTrue(NamedEntityType.ONTOLOGY.isInstance(nes.get(0).getType()));
    	assertEquals(1, nes.get(0).getOntIds().size());
    	assertTrue(nes.get(0).getOntIds().contains("CHEBI:33309"));
    }
    
    @Test
    public void testFindCustomOntTerms() {
    	ListMultimap<String, String> terms = ArrayListMultimap.create();
    	terms.put("jumps", "foo:001");
    	terms.put("jumps", "foo:002");
    	OntologyTerms ontologyTerms = new OntologyTerms(terms);
    	DFANEFinder finder = new DFANEFinder(
    			TermMaps.getInstance().getNeTerms(), TokenClassifier.getDefaultInstance(),
    			ontologyTerms, defaultRegistryNames);
    	String source = "The quick brown ethyl acetate jumps over the lazy acetone";
    	ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
    			Tokeniser.getDefaultInstance(), source);
    	
    	List<NamedEntity> nes = finder.findNamedEntities(
    			procDoc.getTokenSequences().get(0), NGram.getInstance(), DEFAULT_NGRAM_THRESHOLD);
    	boolean foundJumps = false;
    	for (NamedEntity ne : nes) {
			if ("jumps".equals(ne.getSurface())) {
				assertTrue(NamedEntityType.ONTOLOGY.isInstance(ne.getType()));
				assertEquals(2, ne.getOntIds().size());
				assertTrue(ne.getOntIds().contains("foo:001"));
				assertTrue(ne.getOntIds().contains("foo:002"));
				foundJumps = true;
			}
			else {
				assertEquals(0, ne.getOntIds().size());
			}
			if (NamedEntityType.ONTOLOGY.isInstance(ne.getType())) {
				assertEquals("jumps", ne.getSurface());
			}
		}
    	assertTrue(foundJumps);
    }
    
    @Test
    public void testFindCustomChemNameDictEntries() throws URISyntaxException {
    	Set <String> registryNames = new HashSet<String>();
    	registryNames.add("foobar");
    	DFANEFinder finder = new DFANEFinder(
    			TermMaps.getInstance().getNeTerms(), TokenClassifier.getDefaultInstance(),
    			OntologyTerms.getDefaultInstance(),	Collections.unmodifiableSet(registryNames));
    	String text = "The word 'foobar' in some English text";
    	ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
    			tokeniser, text);
    	TokenSequence tokSeq = procDoc.getTokenSequences().get(0);
    	List <NamedEntity> neList = finder.findNamedEntities(tokSeq, NGram.getInstance(), DEFAULT_NGRAM_THRESHOLD);
    	assertTrue(namedEntityListContainsNamedEntity(neList, NamedEntityType.COMPOUND, "foobar", 10, 16));
    }
    
    private boolean namedEntityListContainsNamedEntity(List <NamedEntity> nes, NamedEntityType type, String surface, int start, int end) {
    	for (NamedEntity ne : nes) {
			if (type.isInstance(ne.getType())) {
				if (surface.equals(ne.getSurface())) {
					if (start == ne.getStart()) {
						if (end == ne.getEnd()) {
							return true;
						}
					}
				}
			}
		}
    	return false;
    }
}
