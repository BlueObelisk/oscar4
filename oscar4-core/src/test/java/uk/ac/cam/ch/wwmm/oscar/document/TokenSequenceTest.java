package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

public class TokenSequenceTest {

	@Test
	public void testConstructor() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 0, null, new ArrayList<IToken>()
		);
		Assert.assertNotNull(seq);
	}

	@Test
	public void testGetSurface() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 0, null, new ArrayList<IToken>()
		);
		Assert.assertEquals(
			"This is a token sequence.", 
			seq.getSurface()
		);
	}

	@Test
	public void testGetOffset() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 5, null, new ArrayList<IToken>()
		);
		Assert.assertEquals(
			5, seq.getOffset()
		);
	}

	@SuppressWarnings("serial")
	@Test
	public void testGetTokens() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 5, null,
			new ArrayList<IToken>() {{
				add(new Token("This", 0, 3, null, null, null));
				add(new Token("is", 5, 6, null, null, null));
				add(new Token("a", 8, 8, null, null, null));
				add(new Token("token", 10, 14, null, null, null));
				add(new Token("sequence", 16, 23, null, null, null));
				add(new Token(".", 24, 24, null, null, null));
			}}
		);
		List<IToken> tokens = seq.getTokens();
		Assert.assertEquals(
			6, tokens.size()
		);
		Assert.assertEquals("This", tokens.get(0).getValue());
		Assert.assertEquals(".", tokens.get(5).getValue());
	}

	@SuppressWarnings("serial")
	@Test
	public void testGetTokensStringList() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 5, null,
			new ArrayList<IToken>() {{
				add(new Token("This", 0, 3, null, null, null));
				add(new Token("is", 5, 6, null, null, null));
				add(new Token("a", 8, 8, null, null, null));
				add(new Token("token", 10, 14, null, null, null));
				add(new Token("sequence", 16, 23, null, null, null));
				add(new Token(".", 24, 24, null, null, null));
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
			"This is a token sequence.", 5, null,
			new ArrayList<IToken>() {{
				add(new Token("This", 0, 3, null, null, null));
				add(new Token("is", 5, 6, null, null, null));
				add(new Token("a", 8, 8, null, null, null));
				add(new Token("token", 10, 14, null, null, null));
				add(new Token("sequence", 16, 23, null, null, null));
				add(new Token(".", 24, 24, null, null, null));
			}}
		);
		Assert.assertEquals(6, seq.size());
	}

	@SuppressWarnings("serial")
	@Test
	public void testToken_int() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 5, null,
			new ArrayList<IToken>() {{
				add(new Token("This", 0, 3, null, null, null));
				add(new Token("is", 5, 6, null, null, null));
				add(new Token("a", 8, 8, null, null, null));
				add(new Token("token", 10, 14, null, null, null));
				add(new Token("sequence", 16, 23, null, null, null));
				add(new Token(".", 24, 24, null, null, null));
			}}
		);
		Assert.assertEquals("is", seq.getToken(1).getValue());
		Assert.assertEquals(".", seq.getToken(5).getValue());
	}

	@SuppressWarnings("serial")
	@Ignore
	public void testSubstring() {
		TokenSequence seq = new TokenSequence(
			"This is a token sequence.", 5, null,
			new ArrayList<IToken>() {{
				add(new Token("This", 0, 3, null, null, null));
				add(new Token("is", 5, 6, null, null, null));
				add(new Token("a", 8, 8, null, null, null));
				add(new Token("token", 10, 14, null, null, null));
				add(new Token("sequence", 16, 23, null, null, null));
				add(new Token(".", 24, 24, null, null, null));
			}}
		);
		Assert.assertEquals(
			"is a token", seq.getSubstring(2, 4)
		);
	}
}
