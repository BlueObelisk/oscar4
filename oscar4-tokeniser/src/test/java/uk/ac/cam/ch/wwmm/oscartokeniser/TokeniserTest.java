package uk.ac.cam.ch.wwmm.oscartokeniser;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscartokeniser.TokenClassifier.TokenClass;

/**
 * @author egonw
 * @author dmj30
 */
public final class TokeniserTest {

	@Test
	public void testConstructor() {
		assertNotNull(new Tokeniser(TokenClassifier.getDefaultInstance()));
	}
	
	@Ignore
	@Test
	//TODO I can't actually think of any strings that would cause this pass to test
	//     If there are none, then the Tokeniser doesn't need to check terms against
	//     TokenClassifier - dmj30
	public void testCustomisation() {
		String bond = "TMS-boc";
		Tokeniser defaultTokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		assertEquals(1, defaultTokeniser.tokenise(bond).getTokens().size());
		
		Map<String, TokenClass> tlrs = new HashMap<String, TokenClassifier.TokenClass>();
		tlrs.put("bondRegex", new TokenClass(null, "foo", null));
		Tokeniser customTokeniser = new Tokeniser(new TokenClassifier(tlrs));
		assertEquals(3, customTokeniser.tokenise(bond).getTokens().size());
	}

	@Test
	public void testAbbreviations() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "(ca. 30 mL)";
		ITokenSequence tokseq = tokeniser.tokenise(s);
		assertEquals(5, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "( ca. 30 mL )");
	}

	@Test
	public void testHyphens() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "EA- or BA-modified HPEI";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(7, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "EA - or BA - modified HPEI");
	}
	
	@Test
	public void testSlashes() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "methanol/water";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(3, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "methanol / water");
	}

	@Test
	public void test1Butanol() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "1-butanol";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testButan1ol() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "butan-1-ol";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testTransButene() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "trans-but-2-ene";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testPhosphinin2One() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "phosphinin-2(1H)-one";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testIronIII() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "Fe(III)";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testIronIIILowercase() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "Fe(iii)";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testIronThreePlus() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "Fe(3+)";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testIronNought() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "Fe(0)";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testWrappedInBrackets() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "Tetrahydro furan (THF)";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(5, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "Tetrahydro furan ( THF )");
	}
	
	@Test
	public void testSAlanine() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "(S)-alanine";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testDGlucose() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "D-glucose";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "D-glucose");
	}
	
	@Test
	public void testSpiroSystem() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "spiro[4.5]decane";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testLambdaConvention1() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "lambda5-phosphane";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	@Test
	public void testLambdaConvention2() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "\u03BB5-phosphane";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Ignore
	public void testRingAssembly() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "2,2':6',2\"-Terphenyl-1,1',1\"-triol";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testBetaDGlucose() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "\u03b2-D-Glucose";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testPeptide() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "L-alanyl-L-glutaminyl-L-arginyl-O-phosphono-L-seryl-L-alanyl-L-proline";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testChargedAluminium() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "aluminium(3+)";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testPolymer() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "poly(2,2'-diamino-5-hexadecylbiphenyl-3,3'-diyl)";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testMethylideneCyclohexane() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "1-methyl-2-methylidene-cyclohexane";
		ITokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "1-methyl-2-methylidene-cyclohexane");
	}

	@Test
	public void testTrademark() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
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
			assertEquals(expectedList.get(i), tokens.get(i).getSurface());
		}
	}
	
	@Test
	public void testSimpleTokenSetup() {
		String source = "The quick brown fox jumps over the lazy dog";
		ITokenSequence ts = Tokeniser.getDefaultInstance().tokenise(source);
		assertEquals(9, ts.getTokens().size());
		
		assertEquals("The", ts.getToken(0).getSurface());
		assertEquals(0, ts.getToken(0).getStart());
		assertEquals(3, ts.getToken(0).getEnd());
		
		assertEquals("quick", ts.getToken(1).getSurface());
		assertEquals(4, ts.getToken(1).getStart());
		assertEquals(9, ts.getToken(1).getEnd());
		
		assertEquals("brown", ts.getToken(2).getSurface());
		assertEquals(10, ts.getToken(2).getStart());
		assertEquals(15, ts.getToken(2).getEnd());
		
		assertEquals("fox", ts.getToken(3).getSurface());
		assertEquals(16, ts.getToken(3).getStart());
		assertEquals(19, ts.getToken(3).getEnd());
		
		assertEquals("jumps", ts.getToken(4).getSurface());
		assertEquals(20, ts.getToken(4).getStart());
		assertEquals(25, ts.getToken(4).getEnd());
		
		assertEquals("over", ts.getToken(5).getSurface());
		assertEquals(26, ts.getToken(5).getStart());
		assertEquals(30, ts.getToken(5).getEnd());
		
		assertEquals("the", ts.getToken(6).getSurface());
		assertEquals(31, ts.getToken(6).getStart());
		assertEquals(34, ts.getToken(6).getEnd());
		
		assertEquals("lazy", ts.getToken(7).getSurface());
		assertEquals(35, ts.getToken(7).getStart());
		assertEquals(39, ts.getToken(7).getEnd());
		
		assertEquals("dog", ts.getToken(8).getSurface());
		assertEquals(40, ts.getToken(8).getStart());
		assertEquals(43, ts.getToken(8).getEnd());
	}
	
	@Test
	public void testTokenSequenceSurface() {
		String source = "The quick brown fox jumps over the lazy dog";
		ITokenSequence ts = Tokeniser.getDefaultInstance().tokenise(source);
		assertEquals(source, ts.getSurface());
	}
}
