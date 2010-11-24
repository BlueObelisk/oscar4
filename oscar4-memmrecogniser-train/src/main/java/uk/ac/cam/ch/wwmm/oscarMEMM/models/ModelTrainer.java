package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMTrainer;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMMTrainerSingleton;

/**Routines to co-ordinate the holding of experimental training data, and 
 * models from the MEMM, MEMM rescorer and other modules.
 * 
 * @author ptc24
 *
 */
public class ModelTrainer {
	
	private final static Logger logger = Logger.getLogger(ModelTrainer.class);

	/**Examines the current MEMM, NESubtypes and ExtractTrainingData singletons,
	 * and produces an XML document from their contents.
	 * 
	 * @return The XML document.
	 * @throws Exception
	 */
	public static Document makeModel() throws Exception {
		Element modelRoot = new Element("model");
		modelRoot.appendChild(ExtractedTrainingData.getInstance().toXML());
		MEMMTrainer memm = MEMMTrainerSingleton.getInstance();
		if(memm != null) modelRoot.appendChild(memm.writeModel());
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
			MEMMTrainerSingleton.load(memmElem);
		} else {
			MEMMTrainerSingleton.clear();
		}
		Element etdElem = modelRoot.getFirstChildElement("etd");
		if(etdElem != null) {
			ExtractedTrainingData.reinitialise(etdElem);
		} else {
			ExtractedTrainingData.clear();
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
			loadModelFromResources(modelName);
			return;
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

	/**Compiles a model, based on the given set of ScrapBook files, and
	 * saves it in the models directory.
	 * 
	 * @param modelName The name of the model file (".xml" will be appended to
	 * this)
	 */
	public static void makeModel(String modelName, List<File> files) {
		makeModel(modelName, files, true);
	}
	
	/**Compiles a model, based on the given set of ScrapBook files, and
	 * saves it into a temporary file.
	 * 
	 * @param modelName The name of the model file (".xml" will be appended to
	 * this)
	 */
	public static void makeModel(String modelName, List<File> files, boolean rescore) {
		try {
			MEMMTrainerSingleton.train(files, rescore); // This also trains the ETD
			//NESubtypes.trainOnFiles(files); commented it out on 19 jan 2010
			Document modelDoc = makeModel();
			new Serializer(
				new FileOutputStream(File.createTempFile(modelName, ".xml"))
			).write(modelDoc);
		} catch (Exception e) {
			throw new Error(e);
		}
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
