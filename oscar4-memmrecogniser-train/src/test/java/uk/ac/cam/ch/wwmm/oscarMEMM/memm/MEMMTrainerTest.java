package uk.ac.cam.ch.wwmm.oscarMEMM.memm;

import java.io.InputStream;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.ac.cam.ch.wwmm.oscarMEMM.models.Model;

import ch.unibe.jexample.Given;
import ch.unibe.jexample.Injection;
import ch.unibe.jexample.InjectionPolicy;
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
		String xml = trainer.writeModel().toXML();
		Assert.assertEquals("<memm />", xml);
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
		Element trainedModel = trainer.writeModel();
		Assert.assertNotSame(untrainedXML, trainedModel.toXML());

		Assert.assertEquals("memm", trainedModel.getLocalName());
		Elements elements = trainedModel.getChildElements();
		for (int i=0; i<elements.size(); i++)
			Assert.assertEquals("maxent", elements.get(i).getLocalName());

		Element fullModel = Model.makeModel().getRootElement();
		Assert.assertEquals("model", fullModel.getLocalName());
		Assert.assertEquals(1, fullModel.getChildElements("etd").size());
		Assert.assertEquals(1, fullModel.getChildElements("memm").size());
	}
}
