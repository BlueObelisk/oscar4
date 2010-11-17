package uk.ac.cam.ch.wwmm.oscar.xmltools;

import nu.xom.Element;
import nu.xom.Text;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.xmltools.XOMTools;

public class XOMToolsTest {

	@Test
	public void testGetNextSibling() {
		Element root = new Element("root");
		Element elem1 = new Element("elem1");
		Element elem2 = new Element("elem2");
		root.appendChild(elem1);
		root.appendChild(elem2);
		Assert.assertEquals(elem2, XOMTools.getNextSibling(elem1));
		Assert.assertNull(XOMTools.getNextSibling(elem2));
	}

	@Test
	public void testGetNextSibling_String() {
		Element root = new Element("root");
		Element elem1 = new Element("elem1");
		Element elem2 = new Element("elem2");
		root.appendChild(elem1);
		root.appendChild(elem2);
		Assert.assertNull(XOMTools.getNextSibling(elem1,"elem1"));
		Assert.assertEquals(elem2, XOMTools.getNextSibling(elem1, "elem2"));
	}

	@Test
	public void testGetPreviousSibling() {
		Element root = new Element("root");
		Element elem1 = new Element("elem1");
		Element elem2 = new Element("elem2");
		root.appendChild(elem1);
		root.appendChild(elem2);
		Assert.assertEquals(elem1, XOMTools.getPreviousSibling(elem2));
		Assert.assertNull(XOMTools.getPreviousSibling(elem1));
	}

	@Test
	public void testGetPreviousSibling_String() {
		Element root = new Element("root");
		Element elem1 = new Element("elem1");
		Element elem2 = new Element("elem2");
		root.appendChild(elem1);
		root.appendChild(elem2);
		Assert.assertNull(XOMTools.getPreviousSibling(elem1,"elem1"));
		Assert.assertNull(XOMTools.getPreviousSibling(elem2,"elem2"));
		Assert.assertEquals(elem1, XOMTools.getPreviousSibling(elem2, "elem1"));
	}

	@Test
	public void testInsertBefore() {
		Element root = new Element("root");
		Element elem1 = new Element("elem1");
		Element elem2 = new Element("elem2");
		Element elem3 = new Element("elem3");
		root.appendChild(elem1);
		root.appendChild(elem2);
		Assert.assertEquals(elem2, XOMTools.getNextSibling(elem1));
		XOMTools.insertBefore(elem2, elem3);
		Assert.assertEquals(elem3, XOMTools.getNextSibling(elem1));
	}

	@Test
	public void testInsertAfter() {
		Element root = new Element("root");
		Element elem1 = new Element("elem1");
		Element elem2 = new Element("elem2");
		Element elem3 = new Element("elem3");
		root.appendChild(elem1);
		root.appendChild(elem2);
		Assert.assertEquals(elem2, XOMTools.getNextSibling(elem1));
		XOMTools.insertAfter(elem1, elem3);
		Assert.assertEquals(elem3, XOMTools.getNextSibling(elem1));
	}

	@Test
	public void testNormalise() {
		Element root = new Element("root");
		root.appendChild(new Text("elem1"));
		root.appendChild(new Text(" "));
		root.appendChild(new Text("elem2"));
		Assert.assertEquals(3, root.getChildCount());
		XOMTools.normalise(root);
		Assert.assertEquals(1, root.getChildCount());
		Assert.assertEquals("elem1 elem2", root.getChild(0).getValue());
	}
}
