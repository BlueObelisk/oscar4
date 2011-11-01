package uk.ac.cam.ch.wwmm.oscartokeniser;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.document.Token;
import uk.ac.cam.ch.wwmm.oscar.document.TokenSequence;

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
		
		Map<String, TokenClass> tlrs = new HashMap<String, TokenClass>();
		tlrs.put("bondRegex", new TokenClass(null, "foo", null));
		Tokeniser customTokeniser = new Tokeniser(new TokenClassifier(tlrs));
		assertEquals(3, customTokeniser.tokenise(bond).getTokens().size());
	}

	@Test
	public void testAbbreviations() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "(ca. 30 mL)";
		TokenSequence tokseq = tokeniser.tokenise(s);
		assertEquals(5, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "( ca. 30 mL )");
	}
	
	@Test
	public void testBracketedState() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		TokenSequence tokseq = tokeniser.tokenise("NaOH(aq)");
		assertEquals(4, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "NaOH ( aq )");
		
		tokseq = tokeniser.tokenise("HCl(g)");
		assertEquals(4, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "HCl ( g )");
		
		//counter example
		tokseq = tokeniser.tokenise("5(g)");
		assertEquals(1, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "5(g)");
	}

	@Test
	public void testHyphens() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "EA- or BA-modified HPEI";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(7, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "EA - or BA - modified HPEI");
	}
	
	@Test
	public void testSlashes() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "methanol/water";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(3, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "methanol / water");
	}

	@Test
	public void test1Butanol() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "1-butanol";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testButan1ol() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "butan-1-ol";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testTransButene() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "trans-but-2-ene";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testPhosphinin2One() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "phosphinin-2(1H)-one";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testIronIII() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "Fe(III)";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testIronIIILowercase() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "Fe(iii)";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testIronThreePlus() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "Fe(3+)";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testIronNought() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "Fe(0)";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testWrappedInBrackets() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "Tetrahydro furan (THF)";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(5, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "Tetrahydro furan ( THF )");
	}
	
	@Test
	public void testSAlanine() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "(S)-alanine";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testDGlucose() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "D-glucose";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "D-glucose");
	}
	
	@Test
	public void testSpiroSystem() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "spiro[4.5]decane";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testLambdaConvention1() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "lambda5-phosphane";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testLambdaConvention2() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "\u03BB5-phosphane";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testStandardColonUsage() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		assertEquals(3, tokeniser.tokenise("ethanol:water").getTokens().size());
		assertEquals(3, tokeniser.tokenise("1:2").getTokens().size());
		assertEquals(7, tokeniser.tokenise("(foo):(bar)").getTokens().size());
		assertEquals(5, tokeniser.tokenise("foo):(bar").getTokens().size());
	}
	
	@Test
	public void testRingAssembly() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "2,2':6',2''-Terphenyl-1,1',1''-triol";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testFusedRingSystem1() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "phenothiazino[3',4':5,6][1,4]oxazino[2,3-i]benzo[5,6][1,4]thiazino[3,2-c]phenoxazine";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testFusedRingSystem2() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "phenanthro[4,5-bcd:1,2-c']difuran";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}

	@Test
	public void testBetaDGlucose() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "\u03b2-D-Glucose";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testPeptide() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "L-alanyl-L-glutaminyl-L-arginyl-O-phosphono-L-seryl-L-alanyl-L-proline";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testChargedAluminium() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "aluminium(3+)";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testSaccharide() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "beta-D-Glucopyranosyl-(1->4)-D-glucose";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testPolymer() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "poly(2,2'-diamino-5-hexadecylbiphenyl-3,3'-diyl)";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
	}
	
	@Test
	public void testMethylideneCyclohexane() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "1-methyl-2-methylidene-cyclohexane";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(1, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "1-methyl-2-methylidene-cyclohexane");
	}

	@Test
	public void testSplittingOnCommonMathematicalOperators() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		assertEquals(3, tokeniser.tokenise("J=8.8").getTokens().size());
		assertEquals(1, tokeniser.tokenise("CH2=CH2").getTokens().size());
		assertEquals(3, tokeniser.tokenise("mL\u00D73").getTokens().size());//multiplication
		assertEquals(2, tokeniser.tokenise("3\u00D7").getTokens().size());
		assertEquals(2, tokeniser.tokenise("\u00D73").getTokens().size());
		assertEquals(3, tokeniser.tokenise("15\u00F73").getTokens().size());//division
		assertEquals(3, tokeniser.tokenise("5+3").getTokens().size());//addition
		assertEquals(1, tokeniser.tokenise("ESI+").getTokens().size());
	}
	
	@Test
	public void testLightRotation() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		assertEquals(1, tokeniser.tokenise("(+)-chiraline").getTokens().size());
		assertEquals(1, tokeniser.tokenise("(-)-chiraline").getTokens().size());
		assertEquals(1, tokeniser.tokenise("(+-)-chiraline").getTokens().size());
		assertEquals(1, tokeniser.tokenise("(+)-chiraline").getTokens().size());
		assertEquals(1, tokeniser.tokenise("(\u00B1)-chiraline").getTokens().size());
	}

	@Test
	public void testTrademark() {
		Tokeniser tokeniser = new Tokeniser(TokenClassifier.getDefaultInstance());
		String s = "CML(TM)";
		TokenSequence  tokseq = tokeniser.tokenise(s);
		assertEquals(4, tokseq.getTokens().size());
		checkTokens(tokseq.getTokens(), "CML ( TM )");
	}
	
	private void checkTokens(List<Token> tokens, String expectedTokens) {
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
		TokenSequence ts = Tokeniser.getDefaultInstance().tokenise(source);
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
		TokenSequence ts = Tokeniser.getDefaultInstance().tokenise(source);
		assertEquals(source, ts.getSurface());
	}
}
