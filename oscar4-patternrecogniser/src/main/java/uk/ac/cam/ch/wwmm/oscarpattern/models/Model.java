package uk.ac.cam.ch.wwmm.oscarpattern.models;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscarpattern.tools.FileTools;

/**Routines to co-ordinate the holding of experimental training data, and 
 * models from the MEMM, MEMM rescorer and other modules.
 * 
 * @author ptc24
 *
 */
public class Model {

	/**Examines the current MEMM, NESubtypes and ExtractTrainingData singletons,
	 * and produces an XML document from their contents.
	 * 
	 * @return The XML document.
	 * @throws Exception
	 */
	public static Document makeModel() throws Exception {
		Element modelRoot = new Element("model");
		modelRoot.appendChild(ExtractTrainingData.getInstance().toXML());
//		MEMM memm = MEMMSingleton.getInstance();
//		if(memm != null) modelRoot.appendChild(memm.writeModel());
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
//		if(memmElem != null) {
//			MEMMSingleton.load(memmElem);
//		} else {
//			MEMMSingleton.clear();
//		}
		Element etdElem = modelRoot.getFirstChildElement("etd");
		if(etdElem != null) {
			ExtractTrainingData.reinitialise(etdElem);
		} else {
			ExtractTrainingData.clear();
		}
		Element subtypeElem = modelRoot.getFirstChildElement("subtypes");
//		if(subtypeElem != null) {
//			NESubtypes.reinitialise(subtypeElem);
//		} else {
//			NESubtypes.clear();
//		}

	}
	
	/**Loads a model file, with the given name (a .xml suffix will be added)
	 * from the resources path.
	 * 
	 * @param modelName The model to load.
	 */
	public static void loadModelFromResources(String modelName) {
		try {			
			Document modelDoc = new ResourceGetter("uk/ac/cam/ch/wwmm/oscarpattern/models/").getXMLDocument(modelName + ".xml");
			restoreModel(modelDoc);
		} catch (Exception e) {
			throw new Error(e);
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
		Logger logger = Logger.getLogger(Model.class);
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
		makeModel(modelName, FileTools.getFilesFromDirectoryByName(new File(OscarProperties.getData().workspace, "scrapbook"), "scrapbook.xml"));
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
	 * saves it in the models directory.
	 * 
	 * @param modelName The name of the model file (".xml" will be appended to
	 * this)
	 */
	public static void makeModel(String modelName, List<File> files, boolean rescore) {
		try {
//			MEMMSingleton.train(files, rescore); // This also trains the ETD
//			NESubtypes.trainOnFiles(files);
			Document modelDoc = makeModel();
			if(OscarProperties.getData().workspace.equals("none")) {
				throw new Error("You can't train a model unless you have a workspace");
			}
			File trainDir = new File(OscarProperties.getData().workspace, "models");
			if(trainDir.exists() && !trainDir.isDirectory()) {
				throw new Error("You have a file called models in your workspace - it should be a directory!");
			}
			if(!trainDir.exists()) trainDir.mkdir();
			new Serializer(new FileOutputStream(new File(trainDir, modelName + ".xml"))).write(modelDoc);
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
