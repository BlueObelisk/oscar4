package uk.ac.cam.ch.wwmm.oscartokeniser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.Token;

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
		ITokenSequence tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(tokseq.getTokens().size(), 5);
		checkTokens(tokseq.getTokens(), "( ca. 30 mL )");
	}

	@Test
	/**
	 * @dmj30: I'm not convinced that this test is mandating desired behaviour
	 */
	public void testHyphens() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "EA- or BA-modified HPEI";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(tokseq.getTokens().size(), 4);
		checkTokens(tokseq.getTokens(), "EA- or BA-modified HPEI");
	}
	
	@Test
	public void testSlashes() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "methanol/water";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(tokseq.getTokens().size(), 3);
		checkTokens(tokseq.getTokens(), "methanol / water");
	}

	@Test
	public void test1Butanol() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "1-butanol";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testButan1ol() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "butan-1-ol";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testTransButene() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "trans-but-2-ene";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testPhosphinin2One() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "phosphinin-2(1H)-one";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	/**
	 * TODO This appears to be incorrect behaviour according the annotation guidelines
	 */
	public void testIronIII() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "Fe(III)";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(2, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "Fe (III)");
	}

	@Test
	public void testSAlanine() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "(S)-alanine";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testDGlucose() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "D-glucose";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testSpiroSystem() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "spiro[4.5]decane";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testLambdaConvention1() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "lambda5-phosphane";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}
	@Test
	public void testLambdaConvention2() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "\u03BB5-phosphane";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}

	@Ignore
	public void testRingAssembly() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "2,2':6',2\"-Terphenyl-1,1',1\"-triol";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testBetaDGlucose() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "\u03b2-D-Glucose";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testPeptide() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "L-alanyl-L-glutaminyl-L-arginyl-O-phosphono-L-seryl-L-alanyl-L-proline";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testChargedAluminium() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "aluminium(3+)";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testPolymer() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "poly(2,2'-diamino-5-hexadecylbiphenyl-3,3'-diyl)";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testMethylideneCyclohexane() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "1-methyl-2-methylidene-cyclohexane";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testTrademark() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "CML(TM)";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		Assert.assertEquals(4, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "CML ( TM )");
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
