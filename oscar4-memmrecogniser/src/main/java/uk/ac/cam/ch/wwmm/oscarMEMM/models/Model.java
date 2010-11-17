package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import java.io.File;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMM;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMSingleton;
import uk.ac.cam.ch.wwmm.oscarrecogniser.etd.ExtractedTrainingData;

/**Routines to co-ordinate the holding of experimental training data, and 
 * models from the MEMM, MEMM rescorer and other modules.
 * 
 * @author ptc24
 *
 */
public class Model {
	
	private final static Logger logger = Logger.getLogger(Model.class);

	/**Examines the current MEMM, NESubtypes and ExtractTrainingData singletons,
	 * and produces an XML document from their contents.
	 * 
	 * @return The XML document.
	 * @throws Exception
	 */
	public static Document makeModel() throws Exception {
		Element modelRoot = new Element("model");
		modelRoot.appendChild(ExtractedTrainingData.getInstance().toXML());
		MEMM memm = MEMMSingleton.getInstance();
		if(memm != null) modelRoot.appendChild(memm.writeModel());
//		NESubtypes subtypes = NESubtypes.getInstance();
//		if(subtypes.OK) modelRoot.appendChild(subtypes.toXML());
		return new Document(modelRoot);
	}
	
	/**Examines an XML document produced with MakeModel, and uses the data
	 * to initialise the relevant singletons.
	 * 
	 * @param modelDoc The XML document.
	 * @throws Exception
	 */
	public static void restoreModel(Document modelDoc) throws Exception {
		Element modelRoot = modelDoc.getRootElement();
		Element memmElem = modelRoot.getFirstChildElement("memm");
		if(memmElem != null) {
			MEMMSingleton.load(memmElem);
		} else {
			MEMMSingleton.clear();
		}
		Element etdElem = modelRoot.getFirstChildElement("etd");
		if(etdElem != null) {
			ExtractedTrainingData.reinitialise(etdElem);
		} else {
			ExtractedTrainingData.getInstance().clear();
		}
	}
	
	/**Loads a model file, with the given name (a .xml suffix will be added)
	 * from the resources path.
	 * 
	 * @param modelName The model to load.
	 */
	public static void loadModelFromResources(String modelName) {
		try {
			Document modelDoc = new ResourceGetter(
					Model.class.getClassLoader(),
					"uk/ac/cam/ch/wwmm/oscarMEMM/models/"
				).getXMLDocument(modelName + ".xml");
			restoreModel(modelDoc);
		} catch (Exception e) {
			throw new Error("Could not find model: " + modelName, e);
		}		
	}
	
	/**Loads a model file, with the given name (a .xml suffix will be added).
	 * This will look first for a "models" directory in the workspace, and
	 * then at the resource path.
	 * 
	 * @param modelName The model to load.
	 */
	public static void loadModel(String modelName) {
		try {			
			if(OscarProperties.getData().workspace.equals("none")) {
				loadModelFromResources(modelName);
				return;
			}
			File trainDir = new File(OscarProperties.getData().workspace, "models");
			if(!trainDir.exists() || !trainDir.isDirectory() || !new File(trainDir,modelName+".xml").exists()) {
				loadModelFromResources(modelName);
				return;
			}
			Document modelDoc = new Builder().build(new File(trainDir, modelName + ".xml"));
			restoreModel(modelDoc);
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	/**Loads the model file given by the "model" property.
	 * 
	 */
	public static void loadModel() {
		logger.debug("Loading model " + OscarProperties.getData().model + "...");
		loadModel(OscarProperties.getData().model);
		logger.debug("...model loaded OK!");
	}
	
	/**Compiles a model, based on the ScrapBook files in the workspace, and
	 * saves it in the models directory.
	 * 
	 * @param modelName The name of the model file (".xml" will be appended to
	 * this)
	 */
	public static void makeModel(String modelName) {
		System.err.println("Model.java line 123 : Need to load model but commented out coe");
		//makeModel(modelName, FileTools.getFilesFromDirectoryByName(new File(Oscar3Props.getInstance().workspace, "scrapbook"), "scrapbook.xml"));
	}

	/**Produces a hash value for the current model.
	 * 
	 * @return The hash value.
	 * @throws Exception
	 */
	public static int makeHash() throws Exception {
		return makeModel().toXML().hashCode();
	}	
}
