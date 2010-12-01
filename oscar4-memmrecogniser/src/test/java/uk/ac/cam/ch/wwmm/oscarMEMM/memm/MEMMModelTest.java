package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.util.Set;

import nu.xom.Document;
import nu.xom.Element;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscarMEMM.models.Model;

public class MEMMModelTest {

	@Test
	public void testConstructor() {
		MEMMModel model = new MEMMModel();
		// this test ensures that no Exceptions and Errors are thrown
		Assert.assertNotNull(model);
		// but no rescorer is defined
		Assert.assertNull(model.getRescorer());
	}

	@Test
	public void testGetTagSet() {
		Set<String> set = new MEMMModel().getTagSet();
		Assert.assertNotNull(set);
		// a new instance should have no data
		Assert.assertEquals(0, set.size());
	}

	@Test
	public void testGetNamedEntityTypes() {
		Set<NamedEntityType> set = new MEMMModel().getNamedEntityTypes();
		Assert.assertNotNull(set);
		// a new instance should have no data
		Assert.assertEquals(0, set.size());
	}

	@Test
	public void testReadModel() throws Exception {
		Document modelDoc = new ResourceGetter(
			Model.class.getClassLoader(),
			"uk/ac/cam/ch/wwmm/oscarMEMM/models/"
		).getXMLDocument("chempapers.xml");
		Element modelRoot = modelDoc.getRootElement();
		Assert.assertNotNull(modelRoot);
		Element memmElem = modelRoot.getFirstChildElement("memm");
		Assert.assertNotNull(memmElem);
		MEMMModel model = new MEMMModel();
		model.readModel(memmElem);
		Assert.assertNotSame(0, model.getNamedEntityTypes().size());
		Assert.assertNotNull(model.getRescorer());
	}
}
