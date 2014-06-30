package uk.ac.cam.ch.wwmm.oscar.document;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

public class NamedEntitityTest {

	@Test
	public void testOntIdsNotNull() {
		TokenSequence tokSeq = new TokenSequence("", 0, null, new ArrayList<Token>());
		Token token = new Token("", 0, 0, null, null, null);
		token.setTokenSequence(tokSeq);
		List <Token> tokenList = new ArrayList<Token>();
		tokenList.add(token);
		
		NamedEntity ne1 = new NamedEntity(tokenList, "", NamedEntityType.COMPOUND);
		NamedEntity ne2 = new NamedEntity("", 0, 0, NamedEntityType.COMPOUND);
		NamedEntity ne3 = NamedEntity.forPrefix(token ,"");
		
		assertNotNull(ne1.getOntIds());
		assertNotNull(ne2.getOntIds());
		assertNotNull(ne3.getOntIds());
	}
	
	@Test
	public void testCustTypesNotNull() {
		TokenSequence tokSeq = new TokenSequence("", 0, null, new ArrayList<Token>());
		Token token = new Token("", 0, 0, null, null, null);
		token.setTokenSequence(tokSeq);
		List <Token> tokenList = new ArrayList<Token>();
		tokenList.add(token);
		
		NamedEntity ne1 = new NamedEntity(tokenList, "", NamedEntityType.COMPOUND);
		NamedEntity ne2 = new NamedEntity("", 0, 0, NamedEntityType.COMPOUND);
		NamedEntity ne3 = NamedEntity.forPrefix(token ,"");
		
		assertNotNull(ne1.getCustTypes());
		assertNotNull(ne2.getCustTypes());
		assertNotNull(ne3.getCustTypes());
	}
}
