package uk.ac.cam.ch.wwmm.oscar.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class StringToolsTest {

	@Test
	public void testIsLackingOpenBracket() {
		assertTrue("blatant lack)", StringTools.isLackingOpenBracket("blatant lack)"));
		assertTrue("(blatant] lack)", StringTools.isLackingOpenBracket("(blatant] lack)"));
		assertTrue("blatant)( lack", StringTools.isLackingOpenBracket("blatant)( lack"));
		assertTrue("blatant] lack", StringTools.isLackingOpenBracket("blatant] lack"));
		assertTrue("blatant} lack", StringTools.isLackingOpenBracket("blatant} lack"));

		assertFalse("no lack", StringTools.isLackingOpenBracket("no lack"));
		assertFalse("(no lack)", StringTools.isLackingOpenBracket("(no lack)"));
		assertFalse("[no( )lack]", StringTools.isLackingOpenBracket("[no( )lack]"));
		assertFalse("(no lack", StringTools.isLackingOpenBracket("(no lack"));
		assertFalse("{no} [lack]", StringTools.isLackingOpenBracket("{no} [lack]"));
		assertFalse("[no][lack]", StringTools.isLackingOpenBracket("[no][lack]"));
	}

	@Test
	public void testIsLackingCloseBracket() {
		assertTrue("(blatant lack", StringTools.isLackingCloseBracket("(blatant lack"));
		assertTrue("(blatant[ lack)", StringTools.isLackingCloseBracket("(blatant[ lack)"));
		assertTrue("blatant)( lack", StringTools.isLackingCloseBracket("blatant)( lack"));
		assertTrue("blatant [lack", StringTools.isLackingCloseBracket("blatant [lack"));
		assertTrue("blatant {lack", StringTools.isLackingCloseBracket("blatant {lack"));

		assertFalse("no lack", StringTools.isLackingCloseBracket("no lack"));
		assertFalse("(no lack)", StringTools.isLackingCloseBracket("(no lack)"));
		assertFalse("[no( )lack]", StringTools.isLackingCloseBracket("[no( )lack]"));
		assertFalse("no lack)", StringTools.isLackingCloseBracket("no lack)"));
		assertFalse("{no} [lack]", StringTools.isLackingCloseBracket("{no} [lack]"));
		assertFalse("[no][lack]", StringTools.isLackingCloseBracket("[no][lack]"));
		
		//One from the wild
		assertTrue("3,3?-ethylenebis[2-(ethylsulfanyl)benzoselenazolium tetrafluoroborate", 
				StringTools.isLackingCloseBracket("3,3?-ethylenebis[2-(ethylsulfanyl)benzoselenazolium tetrafluoroborate")); 
	}
	
	@Test
	public void testIsBracketed() {
		assertTrue("(bracketed)", StringTools.isBracketed("(bracketed)"));
		assertTrue("(brac(ket)ed)", StringTools.isBracketed("(brac(ket)ed)"));
		assertTrue("(bracket(ed))", StringTools.isBracketed("(bracket(ed))"));
		assertTrue("[bracketed]", StringTools.isBracketed("[bracketed]"));
		assertTrue("{bracketed}", StringTools.isBracketed("{bracketed}"));

		assertFalse("not bracketed", StringTools.isBracketed("not bracketed"));
		assertFalse("[not][bracketed]", StringTools.isBracketed("[not][bracketed]"));
		assertFalse("not{ }bracketed", StringTools.isBracketed("not{ }bracketed"));
	}
	
	@Test
	public void testUrlEncodeLongString() {
		assertEquals("short string", "Hello", StringTools.urlEncodeLongString("Hello"));
		assertEquals("short string", "%22Hello%22", StringTools.urlEncodeLongString("\"Hello\""));
		assertEquals("longer string", "123456789%2C123456789.123456789+123456789%22123456789%5B\n123456789",
				StringTools.urlEncodeLongString("123456789,123456789.123456789 123456789\"123456789[123456789"));
		assertEquals("huge string", "123456789%2C123456789.123456789+123456789%22123456789%5B\n" +
				"123456789%2C123456789.123456789+123456789%22123456789%5B\n" +
				"123456789%2C123456789.123456789+123456789%22123456789%5B\n" +
				"123456789%2C123456789.123456789+123456789%22123456789%5B\n" +
				"123456789%2C123456789.123456789+123456789%22123456789%5B\n" +
				"123456789%2C123456789.123456789+123456789%22123456789%5B\n" +
				"123456789%2C123456789.123456789+123456789%22123456789%5B\n" +
				"123456789%2C123456789.123456789+123456789%22123456789%5B\n" +
				"123456789%2C123456789.123456789+123456789%22123456789%5B\n" +
				"123456789%2C123456789.123456789+123456789%22123456789%5B\n",
				StringTools.urlEncodeLongString("123456789,123456789.123456789 123456789\"123456789[" +
						"123456789,123456789.123456789 123456789\"123456789[" +
						"123456789,123456789.123456789 123456789\"123456789[" +
						"123456789,123456789.123456789 123456789\"123456789[" +
						"123456789,123456789.123456789 123456789\"123456789[" +
						"123456789,123456789.123456789 123456789\"123456789[" +
						"123456789,123456789.123456789 123456789\"123456789[" +
						"123456789,123456789.123456789 123456789\"123456789[" +
						"123456789,123456789.123456789 123456789\"123456789[" +
						"123456789,123456789.123456789 123456789\"123456789["));
	}
	
	@Test
	public void testTestForAcronym() {
		assertTrue(StringTools.testForAcronym("DHB", "6',7'-dihydroxybergamottin"));		
		assertTrue(StringTools.testForAcronym("ITZ", "itraconazole"));
		assertTrue(StringTools.testForAcronym("TG", "triglyceride"));
		assertTrue(StringTools.testForAcronym("NAC", "n-acetyl-l-cysteine"));
		assertTrue(StringTools.testForAcronym("DEDTC", "Diethyl dithiocarbamate"));
		assertTrue(StringTools.testForAcronym("ATRA", "all-trans-retinoic acid"));
		assertTrue(StringTools.testForAcronym("KC", "ketoconazole"));
		assertTrue(StringTools.testForAcronym("EDDP", "2-ethyl-1,5-dimethyl-3,3-diphenylpyrrolinium"));
		assertTrue(StringTools.testForAcronym("VBL", "[(3)h]vinblastine"));
		assertTrue(StringTools.testForAcronym("ALF", "alfentanil"));
		assertTrue(StringTools.testForAcronym("MDZ", "midazolam"));
		assertTrue(StringTools.testForAcronym("TAO", "troleandomycin"));
		assertTrue(StringTools.testForAcronym("PROP", "6-n-propylthiouracil"));
		assertTrue(StringTools.testForAcronym("MID", "midazolam"));
		assertTrue(StringTools.testForAcronym("NIF", "nifedipine"));
		assertTrue(StringTools.testForAcronym("FC", "furanocoumarin"));
		assertTrue(StringTools.testForAcronym("BT", "bergaptol"));
		assertTrue(StringTools.testForAcronym("NFP", "nifedipine"));
		assertTrue(StringTools.testForAcronym("AOM", "azoxymethane"));
		assertTrue(StringTools.testForAcronym("PCL", "paclitaxel"));
		assertTrue(StringTools.testForAcronym("INDI", "indinavir"));
		assertTrue(StringTools.testForAcronym("DPPH", "2,2-diphenyl-1-picrylhidracyl"));
		assertTrue(StringTools.testForAcronym("CAF", "caffeine"));

		assertFalse(StringTools.testForAcronym("TC", "cholesterol"));
		assertFalse(StringTools.testForAcronym("OTF", "fentanyl citrate"));
		assertFalse(StringTools.testForAcronym("INN", "rifampin"));
		assertFalse(StringTools.testForAcronym("SCN", "nucleus"));
		assertFalse(StringTools.testForAcronym("EROD", "o-dealkylase"));
		assertFalse(StringTools.testForAcronym("BROD", "benzyloxy resorufin"));
		assertFalse(StringTools.testForAcronym("HMACF", "ACF"));
		assertFalse(StringTools.testForAcronym("INN", "cyclosporine"));
	}

	@Test
	public void testSpaceSepListToSubLists() {
		assertEquals("[, 1]", StringTools.spaceSepListToSubLists("1").toString());
		assertEquals("[, 1, 2, 1 2]", StringTools.spaceSepListToSubLists("1 2").toString());
		assertEquals("[, 1, 2, 1 2, 3, 1 3, 2 3, 1 2 3]", StringTools.spaceSepListToSubLists("1 2 3").toString());
	}

	@Test
	public void testNormaliseName() {
		assertEquals("foobar", StringTools.normaliseName("foo\u00adbar"));
		assertEquals("foo bar", StringTools.normaliseName("foo   bar"));
		assertEquals("foo bar", StringTools.normaliseName("foo	bar"));
		assertEquals("fish", StringTools.normaliseName("fish"));
		assertEquals("fish", StringTools.normaliseName("Fish"));
		assertEquals("FISH", StringTools.normaliseName("FISH"));
		assertEquals("FiSH", StringTools.normaliseName("FiSH"));
		assertEquals("n3mm", StringTools.normaliseName("N3mm"));
		assertEquals("fish(andchips)", StringTools.normaliseName("FiSH(andchips)"));
		assertEquals("n-demethylation", StringTools.normaliseName("N-Demethylation"));
		assertEquals("n-demethylation", StringTools.normaliseName("N-demethylation"));
		assertEquals("se-demethylation", StringTools.normaliseName("Se-Demethylation"));
		assertEquals("camelcase", StringTools.normaliseName("CamelCase"));
		assertEquals("camelcase", StringTools.normaliseName("camelCase"));
	}
	
	@Test
	public void testNormaliseName2() {
		assertEquals("fish", StringTools.normaliseName2("fish"));
		assertEquals("fish", StringTools.normaliseName2("Fish"));
		assertEquals("FISH", StringTools.normaliseName2("FISH"));
		assertEquals("FiSH", StringTools.normaliseName2("FiSH"));
		assertEquals("N3mm", StringTools.normaliseName2("N3mm"));
		assertEquals("FiSH(andchips)", StringTools.normaliseName2("FiSH(andchips)"));
		assertEquals("N-demethylation", StringTools.normaliseName2("N-Demethylation"));
		assertEquals("N-demethylation", StringTools.normaliseName2("N-demethylation"));
		assertEquals("Se-demethylation", StringTools.normaliseName2("Se-Demethylation"));
		assertEquals("camelCase", StringTools.normaliseName2("CamelCase"));
		assertEquals("camelCase", StringTools.normaliseName2("camelCase"));
	}
	
	@Test
	public void testObjectListToString() {
		List<Integer> integers = new ArrayList<Integer>();
		for (int i = 1; i <= 4; i++) integers.add(i);
		assertEquals("1 2 3 4", StringTools.objectListToString(integers, " "));
		assertEquals("1234", StringTools.objectListToString(integers, ""));
		assertEquals("1 2 3 4", StringTools.objectListToString(Arrays.asList("1 2 3 4".split(" ")), " "));
	}
	
	@Test
	public void testUnicodeToLatin() {
		assertEquals("Hello, world!", StringTools.unicodeToLatin("Hello, world!"));
		assertEquals("Hello, alpha world!", StringTools.unicodeToLatin("Hello, \u03b1 world!"));
	}

	@Test
	public void testRemoveNoChars() {
		String testString = "foo";
		String removed = StringTools.removeCharFromString("z".charAt(0), testString);
		assertTrue(testString == removed);
	}
	
	@Test
	public void removeAllChars() {
		String testString = "xxx";
		String removed = StringTools.removeCharFromString("x".charAt(0), testString);
		assertEquals("", removed);
	}
	
	@Test
	public void testRemoveCharFromString() {
		String testString = "foo";
		char c = "f".charAt(0);
		assertEquals("oo", StringTools.removeCharFromString(c, testString));
		assertEquals("f", StringTools.removeCharFromString("o".charAt(0), testString));
	}
	
	@Test
	public void testRemoveCharFromSentence() {
		String testString = "the quick brown fox jumps over the lazy dog";
		String removed = StringTools.removeCharFromString("e".charAt(0), testString);
		assertEquals("th quick brown fox jumps ovr th lazy dog", removed);
		
		String removed2 = StringTools.removeCharFromString(" ".charAt(0), removed);
		assertEquals("thquickbrownfoxjumpsovrthlazydog", removed2);
		
		String removed3 = StringTools.removeCharFromString("o".charAt(0), removed2);
		assertEquals("thquickbrwnfxjumpsvrthlazydg", removed3);
	}
	
	@Test
	public void testRemoveCharCaseSensitive() {
		String testString = "Barack Obama";
		String removed1 = StringTools.removeCharFromString("B".charAt(0), testString);
		assertEquals("arack Obama", removed1);
		
		String removed2 = StringTools.removeCharFromString("b".charAt(0), testString);
		assertEquals("Barack Oama", removed2);
	}
	
	@Test
	public void testRemoveUnicodeCharacter() {
		String testString = "foo" + "\u00ad" + "bar";
		String removed = StringTools.removeCharFromString("\u00ad".charAt(0), testString);
		assertEquals("foobar", removed);
	}

	@Test
	public void testExpandRegex() {
		assertEquals("[]", StringTools.expandRegex(null).toString());
		assertEquals("[]", StringTools.expandRegex("").toString());
		assertEquals("[12345]", StringTools.expandRegex("12345").toString());
		assertEquals("[12345]", StringTools.expandRegex("(12345)?").toString());
		assertEquals("[1267, 1234567]", StringTools.expandRegex("12(345)?67").toString());
		assertEquals("[12 67 , 12 345 67 ]", StringTools.expandRegex("12 (345 )?67 ").toString());
		assertEquals("[34, 12, 1234]", StringTools.expandRegex("(12)?(34)?").toString());
		assertEquals("[34, 3456, 1234, 123456]", StringTools.expandRegex("(12)?34(56)?").toString());
		assertEquals("[034, 03456, 01234, 0123456]", StringTools.expandRegex("0(12)?34(56)?").toString());
		assertEquals("[347, 34567, 12347, 1234567]", StringTools.expandRegex("(12)?34(56)?7").toString());
		assertEquals("[127, 12347, 1234567]", StringTools.expandRegex("12(34(56)?)?7").toString());		
		assertEquals("[127, 12567, 1234567]", StringTools.expandRegex("12((34)?56)?7").toString());		
		assertEquals("[127, 1278, 12567, 125678, 1234567, 12345678]", StringTools.expandRegex("12((34)?56)?7(8)?").toString());		
		assertEquals("[12(34)+56]", StringTools.expandRegex("12(34)+56").toString());
		assertEquals("[12(34)*56]", StringTools.expandRegex("12(34)*56").toString());
	}
}
