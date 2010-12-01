package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.io.InputStream;

import nu.xom.Element;
import nu.xom.Elements;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.unibe.jexample.Given;
import ch.unibe.jexample.JExample;

@RunWith(JExample.class)
public class MEMMTrainerTest {

	@Test
	public MEMMTrainer testConstructor() throws Exception {
		MEMMTrainer trainer = new MEMMTrainer();
		Assert.assertNotNull(trainer);
		return trainer;
	}

	@Given("testConstructor")
	public String testUntrainedStatus(MEMMTrainer trainer) throws Exception {
		String xml = trainer.getModel().writeModel().toXML();
		Assert.assertEquals("<model />", xml);
		return xml;
	}

	@Given("testConstructor,testUntrainedStatus")
	public void testLearning(MEMMTrainer trainer, String untrainedXML) throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream(
			"uk/ac/cam/ch/wwmm/oscarMEMM/memm/paper.xml"
		);
		Assert.assertNotNull(stream);
		trainer.trainOnStream(stream);
		trainer.finishTraining();
		Element trainedModel = trainer.getModel().writeModel();
		Assert.assertNotSame(untrainedXML, trainedModel.toXML());

		Assert.assertEquals("model", trainedModel.getLocalName());
		Assert.assertEquals(1, trainedModel.getChildElements("etd").size());
		Assert.assertEquals(1, trainedModel.getChildElements("memm").size());

		Element memmModel = trainedModel.getFirstChildElement("memm");
		Assert.assertEquals("memm", memmModel.getLocalName());
		Elements elements = memmModel.getChildElements();
		for (int i=0; i<elements.size(); i++)
			Assert.assertEquals("maxent", elements.get(i).getLocalName());
	}
}
