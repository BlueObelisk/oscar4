package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class SentenceTest {

	@Test
	public void testConstructor() {
		Sentence sentence = new Sentence();
		Assert.assertNotNull(sentence);
		Assert.assertNotNull(sentence.getTokens());
		Assert.assertEquals(0, sentence.getTokens().size());
		Assert.assertTrue(sentence.isEmpty());
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testConstructor_List() {
		Sentence sentence = new Sentence(new ArrayList<IToken>() {{
			add(new Token("foo", 0, 2, null, null, null));
		}});
		Assert.assertNotNull(sentence);
		Assert.assertNotNull(sentence.getTokens());
		Assert.assertFalse(sentence.isEmpty());
		Assert.assertEquals(1, sentence.getTokens().size());
	}

	@Test
	public void testAddToken() {
		Sentence sentence = new Sentence(new ArrayList<IToken>());
		Assert.assertTrue(sentence.isEmpty());
		sentence.addToken(new Token("foo", 0, 2, null, null, null));
		Assert.assertFalse(sentence.isEmpty());
		Assert.assertEquals(1, sentence.getTokens().size());
	}
}
