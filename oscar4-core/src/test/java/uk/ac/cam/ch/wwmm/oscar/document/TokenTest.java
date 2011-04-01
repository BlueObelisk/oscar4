package uk.ac.cam.ch.wwmm.oscar.document;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.types.BioTag;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

/**
 * 
 * @author egonw
 * @author dmj30
 *
 */
public class TokenTest {

	@Test
	public void testValue() {
		Token token = new Token(
			"alsoAToken", 0, 0, null, null, null
		);
		Assert.assertEquals("alsoAToken", token.getSurface());
		token.setSurface("token");
		Assert.assertEquals("token", token.getSurface());
	}

	@Test
	public void testId() {
		Token token = new Token(
			null, 0, 0, null, null, null
		);
		token.setIndex(777);
		Assert.assertEquals(777, token.getIndex());
	}

	@Test
	public void testStart() {
		Token token = new Token(
			null, 77, 0, null, null, null
		);
		Assert.assertEquals(77, token.getStart());
	}

	@Test
	public void testEnd() {
		Token token = new Token(
			null, 0, 77, null, null, null
		);
		Assert.assertEquals(77, token.getEnd());
		token.setEnd(88);
		Assert.assertEquals(88, token.getEnd());
	}

	@Test
	public void testBioTag() {
		Token token = new Token(
			null, 0, 77, null, null, null
		);
		token.setBioType(new BioType(BioTag.B, NamedEntityType.COMPOUND));
		Assert.assertEquals(
			new BioType(BioTag.B, NamedEntityType.COMPOUND),
			token.getBioType()
		);
	}
	
	@Test
	public void testGetNAfter() {
		Token t1 = new Token("foo", 0, 3, null, null, null);
		t1.setIndex(0);
		Token t2 = new Token("bar", 4, 7, null, null, null);
		t2.setIndex(1);
		
		List <Token> tokens = new ArrayList<Token>();
		tokens.add(t1);
		tokens.add(t2);
		TokenSequence tokSeq = new TokenSequence("foo bar", 0, null, tokens);
		t1.setTokenSequence(tokSeq);
		t2.setTokenSequence(tokSeq);
		
		assertNull(t1.getNAfter(-1));
		assertTrue(t1 == t1.getNAfter(0));
		assertTrue(t2 == t1.getNAfter(1));
		assertTrue(t1 == t2.getNAfter(-1));
		assertTrue(t2 == t2.getNAfter(0));
		assertNull(t2.getNAfter(1));
	}
}
