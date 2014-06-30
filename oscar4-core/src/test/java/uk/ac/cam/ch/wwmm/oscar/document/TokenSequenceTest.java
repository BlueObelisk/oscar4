package uk.ac.cam.ch.wwmm.oscar.document;

/**
 * 
 * @author egonw
 * @author dmj30
 */
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;

public class TokenSequenceTest {

	@Test
	public void testConstructor() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 0, null, new ArrayList<Token>()
		);
		Assert.assertNotNull(seq);
	}

	@Test
	public void testGetSurface() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 0, null, new ArrayList<Token>()
		);
		Assert.assertEquals(
			"This is a token sequence.", 
			seq.getSurface()
		);
	}

	@Test
	public void testGetOffset() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 5, null, new ArrayList<Token>()
		);
		Assert.assertEquals(
			5, seq.getOffset()
		);
	}

	@SuppressWarnings("serial")
	@Test
	public void testGetTokens() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 0, null,
			new ArrayList<Token>() {{
				add(new Token("This", 0, 4, null, null, null));
				add(new Token("is", 5, 7, null, null, null));
				add(new Token("a", 8, 9, null, null, null));
				add(new Token("token", 10, 15, null, null, null));
				add(new Token("sequence", 16, 24, null, null, null));
				add(new Token(".", 24, 25, null, null, null));
			}}
		);
		List<Token> tokens = seq.getTokens();
		Assert.assertEquals(
			6, tokens.size()
		);
		Assert.assertEquals("This", tokens.get(0).getSurface());
		Assert.assertEquals(".", tokens.get(5).getSurface());
	}

	@SuppressWarnings("serial")
	@Test
	public void testGetTokensStringList() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 0, null,
			new ArrayList<Token>() {{
				add(new Token("This", 0, 4, null, null, null));
				add(new Token("is", 5, 7, null, null, null));
				add(new Token("a", 8, 9, null, null, null));
				add(new Token("token", 10, 15, null, null, null));
				add(new Token("sequence", 16, 24, null, null, null));
				add(new Token(".", 24, 25, null, null, null));
			}}
		);
		List<String> tokens = seq.getTokenStringList();
		Assert.assertEquals(
			6, tokens.size()
		);
		Assert.assertEquals("This", tokens.get(0));
		Assert.assertEquals(".", tokens.get(5));
	}

	@SuppressWarnings("serial")
	@Test
	public void testSize() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 0, null,
			new ArrayList<Token>() {{
				add(new Token("This", 0, 4, null, null, null));
				add(new Token("is", 5, 7, null, null, null));
				add(new Token("a", 8, 9, null, null, null));
				add(new Token("token", 10, 15, null, null, null));
				add(new Token("sequence", 16, 24, null, null, null));
				add(new Token(".", 24, 25, null, null, null));
			}}
		);
		Assert.assertEquals(6, seq.getSize());
	}

	@SuppressWarnings("serial")
	@Test
	public void testToken_int() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 0, null,
			new ArrayList<Token>() {{
				add(new Token("This", 0, 4, null, null, null));
				add(new Token("is", 5, 7, null, null, null));
				add(new Token("a", 8, 9, null, null, null));
				add(new Token("token", 10, 15, null, null, null));
				add(new Token("sequence", 16, 24, null, null, null));
				add(new Token(".", 24, 25, null, null, null));
			}}
		);
		Assert.assertEquals("is", seq.getToken(1).getSurface());
		Assert.assertEquals(".", seq.getToken(5).getSurface());
	}

	@SuppressWarnings("serial")
	@Test
	public void testSubstring() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 0, null,
			new ArrayList<Token>() {{
				add(new Token("This", 0, 4, null, null, null));
				add(new Token("is", 5, 7, null, null, null));
				add(new Token("a", 8, 9, null, null, null));
				add(new Token("token", 10, 15, null, null, null));
				add(new Token("sequence", 16, 24, null, null, null));
				add(new Token(".", 24, 25, null, null, null));
			}}
		);
		Assert.assertEquals(
			"is a token", seq.getSubstring(1, 3)
		);
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testSubstringWithOffset() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 5, null,
			new ArrayList<Token>() {{
				add(new Token("This", 5, 9, null, null, null));
				add(new Token("is", 10, 12, null, null, null));
				add(new Token("a", 13, 14, null, null, null));
				add(new Token("token", 15, 20, null, null, null));
				add(new Token("sequence", 21, 29, null, null, null));
				add(new Token(".", 29, 30, null, null, null));
			}}
		);
		Assert.assertEquals(
			"is a token", seq.getSubstring(1, 3)
		);
	}
}
