package uk.ac.cam.ch.wwmm.oscartokeniser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;

/**
 * @author egonw
 */
public final class TokeniserTest {

	@Test
	public void testConstructor() {
		Assert.assertNotNull(new Tokeniser());
	}

	@Test
	public void testGettingInstance() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		Assert.assertNotNull(getClass().getClassLoader().loadClass(
				"uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser").newInstance());
	}

	@Test
	public void testAbbreviations() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "(ca. 30 mL)";
		TokenSequence tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(tokseq.getTokens().size(), 5);
		checkTokens(tokseq.getTokens(), "( ca. 30 mL )");
	}

	@Test
	public void testHyphens() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "EA- or BA-modified HPEI";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(tokseq.getTokens().size(), 4);
		checkTokens(tokseq.getTokens(), "EA- or BA-modified HPEI");
	}
	@Test
	public void testSlashes() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "methanol/water";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(tokseq.getTokens().size(), 3);
		checkTokens(tokseq.getTokens(), "methanol / water");
	}

	private void checkTokens(List<Token> tokens, String expectedTokens) {
		List<String> expectedList = new ArrayList<String>();
		for (String item : expectedTokens.split(" ")) {
			expectedList.add(item);
		}
		for (int i = 0; i < expectedList.size(); i++) {
			Assert.assertEquals(expectedList.get(i), tokens.get(i).getValue());
		}

	}
}
