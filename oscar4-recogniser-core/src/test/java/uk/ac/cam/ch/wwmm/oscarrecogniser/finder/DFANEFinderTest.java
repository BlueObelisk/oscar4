package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.chemnamedict.ChemNameDictRegistry;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.core.PolymerDictionary;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * @author sea36
 */
public class DFANEFinderTest {

	private static DFANEFinder finder;
	private static Tokeniser tokeniser;

	@BeforeClass
	public static void setup() {
		long ms = System.currentTimeMillis();
		finder = DFANEFinder.getInstance();
		System.out.println("Boot 1: " + (System.currentTimeMillis() - ms));
		ms = System.currentTimeMillis();
		tokeniser = Tokeniser.getInstance();
		System.out.println("Boot 2: " + (System.currentTimeMillis() - ms));
	}
	
    @Test
    public void testDFANEFinder() {
        // Synthesis of 4-Pyridone-3-sulfate and an improved synthesis of 3-Hydroxy-4-Pyridone
        // Edward J Behrman
        // Chemistry Central Journal 2009, 3:1
        // doi:10.1186/1752-153X-3-1
        String paragraph = "Following removal of 4-pyridone by extraction with methyl ethyl ketone as described above, the dried salt cake was re-extracted with about 250 mL of 95% ethanol for seven hours. Cooling the solution gave 2 g of crude material as off-white crystals (17%). It was recrystallized from 95% ethanol. It gradually decomposes between 250â€“290Â°C without melting. Anal. calcd. for C5H4NO5SNa + 1.5H2O: C, 25.00%; H, 2.94%; N, 5.83%. Found: C, 25.17%, H, 2.70%; N, 5.87%. Hydrolysis of the ester in 0.2 M sulfuric acid for 30 min at 95Â°C followed by neutralization with NaOH and drying gives a residue from which 3-hydroxy-4-pyridone may be extracted quantitatively by trituration with boiling 95% ethanol and purification by crystallization from methanol as above.";

        finder = DFANEFinder.getInstance();
        ITokenSequence ts = tokeniser.tokenise(paragraph);
        List<NamedEntity> namedEntityList = finder.findNamedEntities(ts);
        String expected = "[NE:STOP:18:20:of], [NE:CPR:21:23:4-], [NE:CM:21:31:4-pyridone], [NE:CM:21:31:4-pyridone], [NE:STOP:32:34:by], [NE:STOP:46:50:with], [NE:CM:51:57:methyl], [NE:CM:51:57:methyl], [NE:CM:51:57:methyl], [NE:CM:58:63:ethyl], [NE:CM:58:63:ethyl], [NE:ONT:58:63:ethyl], [NE:CM:51:70:methyl ethyl ketone], [NE:CM:58:70:ethyl ketone], [NE:CM:64:70:ketone], [NE:CM:64:70:ketone], [NE:ONT:64:70:ketone], [NE:STOP:71:73:as], [NE:STOP:84:89:above], [NE:STOP:91:94:the], [NE:ONT:101:105:salt], [NE:STOP:111:114:was], [NE:STOP:128:132:with], [NE:STOP:133:138:about], [NE:STOP:146:148:of], [NE:CM:153:160:ethanol], [NE:CM:153:160:ethanol], [NE:CM:153:160:ethanol], [NE:STOP:161:164:for], [NE:STOP:186:189:the], [NE:STOP:208:210:of], [NE:STOP:226:228:as], [NE:AHA:255:257:It], [NE:STOP:258:261:was], [NE:STOP:277:281:from], [NE:CM:286:293:ethanol], [NE:CM:286:293:ethanol], [NE:CM:286:293:ethanol], [NE:AHA:295:297:It], [NE:STOP:319:326:between], [NE:STOP:340:347:without], [NE:ONT:348:355:melting], [NE:CM:363:368:calcd], [NE:CM:363:368:calcd], [NE:STOP:370:373:for], [NE:CM:374:384:C5H4NO5SNa], [NE:CM:387:393:1.5H2O], [NE:AHA:395:396:C], [NE:CM:395:396:C], [NE:CM:395:396:C], [NE:AHA:406:407:H], [NE:CM:406:407:H], [NE:CM:406:407:H], [NE:AHA:416:417:N], [NE:CM:416:417:N], [NE:CM:416:417:N], [NE:AHA:433:434:C], [NE:CM:433:434:C], [NE:CM:433:434:C], [NE:AHA:444:445:H], [NE:CM:444:445:H], [NE:CM:444:445:H], [NE:AHA:454:455:N], [NE:CM:454:455:N], [NE:CM:454:455:N], [NE:ONT:464:474:Hydrolysis], [NE:STOP:475:477:of], [NE:STOP:478:481:the], [NE:ONT:482:487:ester], [NE:STOP:488:490:in], [NE:STOP:491:496:0.2 M], [NE:AHA:495:496:M], [NE:CM:497:510:sulfuric acid], [NE:STOP:506:510:acid], [NE:ONT:506:510:acid], [NE:STOP:511:514:for], [NE:STOP:522:524:at], [NE:STOP:540:542:by], [NE:STOP:558:562:with], [NE:AHA:563:567:NaOH], [NE:CM:563:567:NaOH], [NE:CM:563:567:NaOH], [NE:STOP:568:571:and], [NE:STOP:585:586:a], [NE:STOP:595:599:from], [NE:STOP:600:605:which], [NE:CPR:606:608:3-], [NE:CM:606:626:3-hydroxy-4-pyridone], [NE:CM:606:626:3-hydroxy-4-pyridone], [NE:STOP:627:630:may], [NE:STOP:631:633:be], [NE:STOP:659:661:by], [NE:STOP:674:678:with], [NE:CM:691:698:ethanol], [NE:CM:691:698:ethanol], [NE:CM:691:698:ethanol], [NE:STOP:699:702:and], [NE:STOP:716:718:by], [NE:ONT:719:734:crystallization], [NE:STOP:735:739:from], [NE:CM:740:748:methanol], [NE:CM:740:748:methanol], [NE:CM:740:748:methanol], [NE:STOP:749:751:as], [NE:STOP:752:757:above]";

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
    public void testSulfuricAcid() {
        // derived from testDFANEFinder()
    	StringBuffer paragraph = new StringBuffer();
    	for (int i=1; i<47; i++) paragraph.append("          ");
    	paragraph.append("    ");
        paragraph.append(
        	"Hydrolysis of the ester in 0.2 M sulfuric acid for 30 min at 95°C."
        );

        ITokenSequence ts = tokeniser.tokenise(paragraph.toString());
        List<NamedEntity> namedEntityList = finder.findNamedEntities(ts);
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
    	ChemNameDictRegistry.getInstance().register(
    		new PolymerDictionary()
    	);
        ITokenSequence ts = tokeniser.tokenise(paragraph);
        List<NamedEntity> namedEntityList = finder.findNamedEntities(ts);
        for (NamedEntity ne : namedEntityList) {
        	System.out.println(ne.toString());
        }
    }
}
