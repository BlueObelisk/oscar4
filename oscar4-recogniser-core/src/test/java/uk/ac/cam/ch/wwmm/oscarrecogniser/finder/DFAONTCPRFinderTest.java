package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

/**
 * 
 * @author dmj30
 *
 */
public class DFAONTCPRFinderTest {

    @Test
    public void testFindNumericalChemicalPrefixes() {
    	String text = "The 1- and 2-foo bar";
    	ITokenSequence tokenSequence = Tokeniser.getInstance().tokenise(text);
    	List <NamedEntity> nes = DFAONTCPRFinder.getInstance().findNamedEntities(tokenSequence);
    	assertTrue(toSurfaceList(nes).contains("2-"));
    	assertTrue(toSurfaceList(nes).contains("1-"));
    	assertEquals(2, nes.size());
    }
    
    @Test
    public void testFindComplexNumericalChemicalPrefixes() {
    	String text = "The 1,2- and 2,3-foo bar";
    	ITokenSequence tokenSequence = Tokeniser.getInstance().tokenise(text);
    	List <NamedEntity> nes = DFAONTCPRFinder.getInstance().findNamedEntities(tokenSequence);
    	assertTrue(toSurfaceList(nes).contains("2,3-"));
    	assertTrue(toSurfaceList(nes).contains("1,2-"));
    	assertEquals(2, nes.size());
    }
    
    @Test
    public void testFindWordyChemicalPrefixes() {
    	String text = "The cis- and trans-foo bar";
    	ITokenSequence tokenSequence = Tokeniser.getInstance().tokenise(text);
    	List <NamedEntity> nes = DFAONTCPRFinder.getInstance().findNamedEntities(tokenSequence);
    	assertTrue(toSurfaceList(nes).contains("trans-"));
    	assertTrue(toSurfaceList(nes).contains("cis-"));
    	assertEquals(2, nes.size());
    }
    
    @Test
    public void testDontFindNonChemicalPrefixes() {
    	String text = "The foo- and bar-foobar";
    	ITokenSequence tokenSequence = Tokeniser.getInstance().tokenise(text);
    	List <NamedEntity> nes = DFAONTCPRFinder.getInstance().findNamedEntities(tokenSequence);
    	assertEquals(0, nes.size());
    }
    
    private List <String> toSurfaceList(List <NamedEntity> nes) {
    	List <String> surfaces = new ArrayList<String>();
    	for (NamedEntity ne : nes) {
			surfaces.add(ne.getSurface());
		}
    	return surfaces;
	}
	
    
    @Test
    public void testHandleTokenForNumericalPrefix() {
    	NECollector collector = new NECollector();
    	assertEquals(0, collector.getNes().size());
    	
    	Token t = new Token("1,2-", 0, 4, null, null, null);
    	List <IToken> tokens = new ArrayList<IToken>();
    	tokens.add(t);
    	TokenSequence tokSeq = new TokenSequence("1,2-", 0, null, tokens);
    	t.setTokenSequence(tokSeq);
    	
    	DFAONTCPRFinder.getInstance().handleTokenForPrefix(t, collector);
    	List <NamedEntity> nes = collector.getNes(); 
    	assertEquals(1, nes.size());
    	assertEquals("1,2-", nes.get(0).getSurface());
    	assertTrue(NamedEntityType.LOCANTPREFIX == nes.get(0).getType());
    }
    
    @Test
    public void testHandleTokenForEmbeddedNumericalPrefix() {
    	NECollector collector = new NECollector();
    	assertEquals(0, collector.getNes().size());
    	
    	Token t = new Token("2,3-substituted", 0, 15, null, null, null);
    	List <IToken> tokens = new ArrayList<IToken>();
    	tokens.add(t);
    	TokenSequence tokSeq = new TokenSequence("2,3-substituted", 0, null, tokens);
    	t.setTokenSequence(tokSeq);
    	
    	DFAONTCPRFinder.getInstance().handleTokenForPrefix(t, collector);
    	List <NamedEntity> nes = collector.getNes(); 
    	assertEquals(1, nes.size());
    	assertEquals("2,3-", nes.get(0).getSurface());
    	assertTrue(NamedEntityType.LOCANTPREFIX == nes.get(0).getType());
    }
    
    @Test
    public void testHandleTokenForTokenisedNumericalPrefix() {
    	NECollector collector = new NECollector();
    	assertEquals(0, collector.getNes().size());
    	
    	Token t1 = new Token("2,3", 0, 3, null, null, null);
    	t1.setId(0);
    	Token t2 = new Token("-", 3, 4, null, null, null);
    	t2.setId(1);
    	List <IToken> tokens = new ArrayList<IToken>();
    	tokens.add(t1);
    	tokens.add(t2);
    	TokenSequence tokSeq = new TokenSequence("2,3-", 0, null, tokens);
    	t1.setTokenSequence(tokSeq);
    	t2.setTokenSequence(tokSeq);
    	
    	DFAONTCPRFinder.getInstance().handleTokenForPrefix(t1, collector);
    	assertEquals(0, collector.getNes().size());
    	
    	DFAONTCPRFinder.getInstance().handleTokenForPrefix(t2, collector);
    	List <NamedEntity> nes = collector.getNes(); 
    	assertEquals(1, nes.size());
    	assertEquals("2,3-", nes.get(0).getSurface());
    	assertTrue(NamedEntityType.LOCANTPREFIX == nes.get(0).getType());
    }
    
    @Test
    public void testHandleTokenForWordyPrefix() {
    	NECollector collector = new NECollector();
    	assertEquals(0, collector.getNes().size());
    	
    	Token t = new Token("endo-", 0, 5, null, null, null);
    	List <IToken> tokens = new ArrayList<IToken>();
    	tokens.add(t);
    	TokenSequence tokSeq = new TokenSequence("endo-", 0, null, tokens);
    	t.setTokenSequence(tokSeq);
    	
    	DFAONTCPRFinder.getInstance().handleTokenForPrefix(t, collector);
    	List <NamedEntity> nes = collector.getNes(); 
    	assertEquals(1, nes.size());
    	assertEquals("endo-", nes.get(0).getSurface());
    	assertTrue(NamedEntityType.LOCANTPREFIX == nes.get(0).getType());
    }
    
    @Test
    public void testHandleTokenForEmbeddedWordyPrefix() {
    	NECollector collector = new NECollector();
    	assertEquals(0, collector.getNes().size());
    	
    	Token t = new Token("trans-isomer", 0, 12, null, null, null);
    	List <IToken> tokens = new ArrayList<IToken>();
    	tokens.add(t);
    	TokenSequence tokSeq = new TokenSequence("trans-isomer", 0, null, tokens);
    	t.setTokenSequence(tokSeq);
    	
    	DFAONTCPRFinder.getInstance().handleTokenForPrefix(t, collector);
    	List <NamedEntity> nes = collector.getNes(); 
    	assertEquals(1, nes.size());
    	assertEquals("trans-", nes.get(0).getSurface());
    	assertTrue(NamedEntityType.LOCANTPREFIX == nes.get(0).getType());
    }
    
    @Test
    public void testHandleTokenForTokenisedWordyPrefix() {
    	NECollector collector = new NECollector();
    	assertEquals(0, collector.getNes().size());
    	
    	Token t1 = new Token("exo", 0, 3, null, null, null);
    	t1.setId(0);
    	Token t2 = new Token("-", 3, 4, null, null, null);
    	t2.setId(1);
    	List <IToken> tokens = new ArrayList<IToken>();
    	tokens.add(t1);
    	tokens.add(t2);
    	TokenSequence tokSeq = new TokenSequence("exo-", 0, null, tokens);
    	t1.setTokenSequence(tokSeq);
    	t2.setTokenSequence(tokSeq);
    	
    	DFAONTCPRFinder.getInstance().handleTokenForPrefix(t1, collector);
    	assertEquals(0, collector.getNes().size());
    	
    	DFAONTCPRFinder.getInstance().handleTokenForPrefix(t2, collector);
    	List <NamedEntity> nes = collector.getNes(); 
    	assertEquals(1, nes.size());
    	assertEquals("exo-", nes.get(0).getSurface());
    	assertTrue(NamedEntityType.LOCANTPREFIX == nes.get(0).getType());
    }
}
