package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocument;
import uk.ac.cam.ch.wwmm.oscar.document.ProcessingDocumentFactory;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;
import uk.ac.cam.ch.wwmm.oscar.ont.OntologyTerms;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * 
 * @author dmj30
 *
 */
public class DFAONTCPRFinderTest {

    @Test
    public void testFindNumericalChemicalPrefixes()  {
    	String text = "The 1- and 2-foo bar";
    	TokenSequence tokenSequence = Tokeniser.getDefaultInstance().tokenise(text);
    	List <NamedEntity> nes = DFAONTCPRFinder.getDefaultInstance().findNamedEntities(tokenSequence);
    	assertTrue(toSurfaceList(nes).contains("2-"));
    	assertTrue(toSurfaceList(nes).contains("1-"));
    	assertEquals(2, nes.size());
    }
    
    @Test
    public void testFindComplexNumericalChemicalPrefixes() {
    	String text = "The 1,2- and 2,3-foo bar";
    	TokenSequence tokenSequence = Tokeniser.getDefaultInstance().tokenise(text);
    	List <NamedEntity> nes = DFAONTCPRFinder.getDefaultInstance().findNamedEntities(tokenSequence);
    	assertTrue(toSurfaceList(nes).contains("2,3-"));
    	assertTrue(toSurfaceList(nes).contains("1,2-"));
    	assertEquals(2, nes.size());
    }
    
    @Test
    public void testFindWordyChemicalPrefixes() {
    	String text = "The cis- and trans-foo bar";
    	TokenSequence tokenSequence = Tokeniser.getDefaultInstance().tokenise(text);
    	List <NamedEntity> nes = DFAONTCPRFinder.getDefaultInstance().findNamedEntities(tokenSequence);
    	assertTrue(toSurfaceList(nes).contains("trans-"));
    	assertTrue(toSurfaceList(nes).contains("cis-"));
    	assertEquals(2, nes.size());
    }
    
    @Test
    public void testDontFindNonChemicalPrefixes() {
    	String text = "The foo- and bar-foobar";
    	TokenSequence tokenSequence = Tokeniser.getDefaultInstance().tokenise(text);
    	List <NamedEntity> nes = DFAONTCPRFinder.getDefaultInstance().findNamedEntities(tokenSequence);
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
    	List <Token> tokens = new ArrayList<Token>();
    	tokens.add(t);
    	TokenSequence tokSeq = new TokenSequence("1,2-", 0, null, tokens);
    	t.setTokenSequence(tokSeq);
    	
    	DFAONTCPRFinder.getDefaultInstance().handleTokenForPrefix(t, collector);
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
    	List <Token> tokens = new ArrayList<Token>();
    	tokens.add(t);
    	TokenSequence tokSeq = new TokenSequence("2,3-substituted", 0, null, tokens);
    	t.setTokenSequence(tokSeq);
    	
    	DFAONTCPRFinder.getDefaultInstance().handleTokenForPrefix(t, collector);
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
    	t1.setIndex(0);
    	Token t2 = new Token("-", 3, 4, null, null, null);
    	t2.setIndex(1);
    	List <Token> tokens = new ArrayList<Token>();
    	tokens.add(t1);
    	tokens.add(t2);
    	TokenSequence tokSeq = new TokenSequence("2,3-", 0, null, tokens);
    	t1.setTokenSequence(tokSeq);
    	t2.setTokenSequence(tokSeq);
    	
    	DFAONTCPRFinder.getDefaultInstance().handleTokenForPrefix(t1, collector);
    	assertEquals(0, collector.getNes().size());
    	
    	DFAONTCPRFinder.getDefaultInstance().handleTokenForPrefix(t2, collector);
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
    	List <Token> tokens = new ArrayList<Token>();
    	tokens.add(t);
    	TokenSequence tokSeq = new TokenSequence("endo-", 0, null, tokens);
    	t.setTokenSequence(tokSeq);
    	
    	DFAONTCPRFinder.getDefaultInstance().handleTokenForPrefix(t, collector);
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
    	List <Token> tokens = new ArrayList<Token>();
    	tokens.add(t);
    	TokenSequence tokSeq = new TokenSequence("trans-isomer", 0, null, tokens);
    	t.setTokenSequence(tokSeq);
    	
    	DFAONTCPRFinder.getDefaultInstance().handleTokenForPrefix(t, collector);
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
    	t1.setIndex(0);
    	Token t2 = new Token("-", 3, 4, null, null, null);
    	t2.setIndex(1);
    	List <Token> tokens = new ArrayList<Token>();
    	tokens.add(t1);
    	tokens.add(t2);
    	TokenSequence tokSeq = new TokenSequence("exo-", 0, null, tokens);
    	t1.setTokenSequence(tokSeq);
    	t2.setTokenSequence(tokSeq);
    	
    	DFAONTCPRFinder.getDefaultInstance().handleTokenForPrefix(t1, collector);
    	assertEquals(0, collector.getNes().size());
    	
    	DFAONTCPRFinder.getDefaultInstance().handleTokenForPrefix(t2, collector);
    	List <NamedEntity> nes = collector.getNes(); 
    	assertEquals(1, nes.size());
    	assertEquals("exo-", nes.get(0).getSurface());
    	assertTrue(NamedEntityType.LOCANTPREFIX == nes.get(0).getType());
    }
    
    
    @Test
    public void testFindOntIds() {
    	String source = "noble gas";
    	ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
    			Tokeniser.getDefaultInstance(), source);
    	
    	List<NamedEntity> nes = DFAONTCPRFinder.getDefaultInstance().findNamedEntities(
    			procDoc.getTokenSequences().get(0));
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
    	DFAONTCPRFinder finder = new DFAONTCPRFinder(ontologyTerms);
    	String source = "The quick brown ethyl acetate jumps over the lazy acetone";
    	ProcessingDocument procDoc = ProcessingDocumentFactory.getInstance().makeTokenisedDocument(
    			Tokeniser.getDefaultInstance(), source);
    	
    	List<NamedEntity> nes = finder.findNamedEntities(
    			procDoc.getTokenSequences().get(0));
    	assertEquals(1, nes.size());
    	assertTrue(NamedEntityType.ONTOLOGY.isInstance(nes.get(0).getType()));
		assertEquals(2, nes.get(0).getOntIds().size());
		assertTrue(nes.get(0).getOntIds().contains("foo:001"));
		assertTrue(nes.get(0).getOntIds().contains("foo:002"));
    }
}
