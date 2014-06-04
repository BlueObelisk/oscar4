package uk.ac.cam.ch.wwmm.oscar.opsin;

import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.junit.Assert;
import org.junit.Test;

public class OpsinDictionaryTest {

	@Test
	public void testMethaneInChI() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertEquals(
			"InChI=1/CH4/h1H4",
			dict.getInchis("methane").iterator().next()
		);
	}
	
	@Test
	public void testMethaneStdInChI() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertEquals(
			"InChI=1S/CH4/h1H4",
			dict.getStdInchis("methane").iterator().next()
		);
	}
	
	@Test
	public void testMethaneStdInChIKey() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertEquals(
			"VNWKTOKETHGBQD-UHFFFAOYSA-N",
			dict.getStdInchiKeys("methane").iterator().next()
		);
	}
	
	@Test
	public void testMethaneCML() {
		OpsinDictionary dict = new OpsinDictionary();
		Element cml = dict.getCML("methane").iterator().next();
		Nodes atoms = cml.query("//cml:atom", new XPathContext("cml", "http://www.xml-cml.org/schema"));
		Assert.assertEquals(5, atoms.size());
		Nodes bonds = cml.query("//cml:bond", new XPathContext("cml", "http://www.xml-cml.org/schema"));
		Assert.assertEquals(4, bonds.size());
	}
	
	@Test
	public void testMethaneSmiles() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertEquals("C", dict.getAllSmiles("methane").iterator().next());
	}

	@Test
	public void testBenzeneInChI() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertEquals(
			"InChI=1/C6H6/c1-2-4-6-5-3-1/h1-6H",
			dict.getInchis("benzene").iterator().next()
		);
	}
	
	@Test
	public void testBenzeneStdInChI() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertEquals(
			"InChI=1S/C6H6/c1-2-4-6-5-3-1/h1-6H",
			dict.getStdInchis("benzene").iterator().next()
		);
	}
	
	@Test
	public void testBenzeneStdInChIKey() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertEquals(
			"UHOVQNZJYSORNB-UHFFFAOYSA-N",
			dict.getStdInchiKeys("benzene").iterator().next()
		);
	}
	
	@Test
	public void testBenzeneCML(){
		OpsinDictionary dict = new OpsinDictionary();
		Element cml = dict.getCML("benzene").iterator().next();
		Nodes atoms = cml.query("//cml:atom", new XPathContext("cml", "http://www.xml-cml.org/schema"));
		Assert.assertEquals(12, atoms.size());
		Nodes bonds = cml.query("//cml:bond", new XPathContext("cml", "http://www.xml-cml.org/schema"));
		Assert.assertEquals(12, bonds.size());
	}

	@Test
	public void testBenzeneSmiles() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertEquals("C1=CC=CC=C1", dict.getAllSmiles("benzene").iterator().next());
	}
	
	@Test
	public void testNoInChIAvailable() {
		OpsinDictionary dict = new OpsinDictionary();
		Assert.assertEquals("Name has a SMILES representation", 1, dict.getAllSmiles("poly(ethylene)").size());
		Assert.assertEquals("Name does not have an InChI representation", 0, dict.getInchis("poly(ethylene)").size());
		Assert.assertEquals("Name does not have an StdInChI representation", 0, dict.getStdInchis("poly(ethylene)").size());
		Assert.assertEquals("Name does not have an StdInChIKey representation", 0, dict.getStdInchiKeys("poly(ethylene)").size());
	}
}
