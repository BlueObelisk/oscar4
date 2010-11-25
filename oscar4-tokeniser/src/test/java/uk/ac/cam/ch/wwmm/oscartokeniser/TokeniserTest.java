package uk.ac.cam.ch.wwmm.oscartokeniser;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;

/**
 * @author egonw
 * @author dmj30
 */
public final class TokeniserTest {

	@Test
	public void testConstructor() {
		assertNotNull(new Tokeniser());
	}

	@Test
	public void testGettingInstance() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		assertNotNull(getClass().getClassLoader().loadClass(
				"uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser").newInstance());
	}

	@Test
	public void testAbbreviations() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "(ca. 30 mL)";
		ITokenSequence tokseq = tokeniser.tokenise(s);
		assertEquals(tokseq.getTokens().size(), 5);
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
		assertEquals(tokseq.getTokens().size(), 4);
		checkTokens(tokseq.getTokens(), "EA- or BA-modified HPEI");
	}
	
	@Test
	public void testSlashes() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "methanol/water";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(tokseq.getTokens().size(), 3);
		checkTokens(tokseq.getTokens(), "methanol / water");
	}

	@Test
	public void test1Butanol() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "1-butanol";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testButan1ol() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "butan-1-ol";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testTransButene() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "trans-but-2-ene";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testPhosphinin2One() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "phosphinin-2(1H)-one";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	/**
	 * TODO This appears to be incorrect behaviour according the annotation guidelines
	 */
	public void testIronIII() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "Fe(III)";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(2, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "Fe (III)");
	}

	@Test
	public void testSAlanine() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "(S)-alanine";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testDGlucose() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "D-glucose";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testSpiroSystem() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "spiro[4.5]decane";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testLambdaConvention1() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "lambda5-phosphane";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	@Test
	public void testLambdaConvention2() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "\u03BB5-phosphane";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Ignore
	public void testRingAssembly() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "2,2':6',2\"-Terphenyl-1,1',1\"-triol";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testBetaDGlucose() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "\u03b2-D-Glucose";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testPeptide() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "L-alanyl-L-glutaminyl-L-arginyl-O-phosphono-L-seryl-L-alanyl-L-proline";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testChargedAluminium() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "aluminium(3+)";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testPolymer() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "poly(2,2'-diamino-5-hexadecylbiphenyl-3,3'-diyl)";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testMethylideneCyclohexane() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "1-methyl-2-methylidene-cyclohexane";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testTrademark() {
		Tokeniser tokeniser = new Tokeniser();
		String s = "CML(TM)";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(4, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "CML ( TM )");
	}

	private void checkTokens(List<IToken> tokens, String expectedTokens) {
		List<String> expectedList = new ArrayList<String>();
		for (String item : expectedTokens.split(" ")) {
			expectedList.add(item);
		}
		for (int i = 0; i < expectedList.size(); i++) {
			assertEquals(expectedList.get(i), tokens.get(i).getValue());
		}
	}
	
	@Test
	public void testSimpleTokenSetup() {
		String source = "The quick brown fox jumps over the lazy dog";
		ITokenSequence ts = Tokeniser.getInstance().tokenise(source);
		assertEquals(9, ts.getTokens().size());
		
		assertEquals("The", ts.getToken(0).getValue());
		assertEquals(0, ts.getToken(0).getStart());
		assertEquals(3, ts.getToken(0).getEnd());
		
		assertEquals("quick", ts.getToken(1).getValue());
		assertEquals(4, ts.getToken(1).getStart());
		assertEquals(9, ts.getToken(1).getEnd());
		
		assertEquals("brown", ts.getToken(2).getValue());
		assertEquals(10, ts.getToken(2).getStart());
		assertEquals(15, ts.getToken(2).getEnd());
		
		assertEquals("fox", ts.getToken(3).getValue());
		assertEquals(16, ts.getToken(3).getStart());
		assertEquals(19, ts.getToken(3).getEnd());
		
		assertEquals("jumps", ts.getToken(4).getValue());
		assertEquals(20, ts.getToken(4).getStart());
		assertEquals(25, ts.getToken(4).getEnd());
		
		assertEquals("over", ts.getToken(5).getValue());
		assertEquals(26, ts.getToken(5).getStart());
		assertEquals(30, ts.getToken(5).getEnd());
		
		assertEquals("the", ts.getToken(6).getValue());
		assertEquals(31, ts.getToken(6).getStart());
		assertEquals(34, ts.getToken(6).getEnd());
		
		assertEquals("lazy", ts.getToken(7).getValue());
		assertEquals(35, ts.getToken(7).getStart());
		assertEquals(39, ts.getToken(7).getEnd());
		
		assertEquals("dog", ts.getToken(8).getValue());
		assertEquals(40, ts.getToken(8).getStart());
		assertEquals(43, ts.getToken(8).getEnd());
	}
	
	@Test
	public void testTokenSequenceSurface() {
		String source = "The quick brown fox jumps over the lazy dog";
		ITokenSequence ts = Tokeniser.getInstance().tokenise(source);
		assertEquals(source, ts.getSurface());
	}
}
