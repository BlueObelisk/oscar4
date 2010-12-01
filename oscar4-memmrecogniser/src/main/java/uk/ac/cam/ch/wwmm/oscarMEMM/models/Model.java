package uk.ac.cam.ch.wwmm.oscarMEMM.models;

import nu.xom.Document;
import nu.xom.Element;

import org.apache.log4j.Logger;

import uk.ac.cam.ch.wwmm.oscar.tools.OscarProperties;
import uk.ac.cam.ch.wwmm.oscar.tools.ResourceGetter;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.MEMM;
import uk.ac.cam.ch.wwmm.oscarMEMM.memm.data.MEMMModel;
import uk.ac.cam.ch.wwmm.oscarrecogniser.etd.ExtractedTrainingData;

/**Routines to co-ordinate the holding of experimental training data, and 
 * models from the MEMM, MEMM rescorer and other modules.
 * 
 * @author ptc24
 *
 */
public class Model {
	
	private final static Logger logger = Logger.getLogger(Model.class);

    private ExtractedTrainingData extractedTrainingData;
    private MEMM memm;

    private static Model defaultInstance;

    public Model(Document modelDoc) {
        Element modelRoot = modelDoc.getRootElement();
		Element memmElem = modelRoot.getFirstChildElement("memm");
		if (memmElem != null) {
			MEMMModel model = new MEMMModel();
			try {
				model.readModel(modelDoc);
			} catch (Exception e) {
				throw new Error(e);
			}
            memm = new MEMM(model);
		} else {
			memm = null;
		}
		Element etdElem = modelRoot.getFirstChildElement("etd");
		if (etdElem != null) {
			this.extractedTrainingData = ExtractedTrainingData.reinitialise(etdElem);
		} else {
            this.extractedTrainingData = new ExtractedTrainingData();
		}
    }


    public ExtractedTrainingData getExtractedTrainingData() {
        return extractedTrainingData;
    }

    public MEMM getMemm() {
        return memm;
    }

    
    /**Examines the current MEMM, NESubtypes and ExtractTrainingData singletons,
	 * and produces an XML document from their contents.
	 * 
	 * @return The XML document.
	 * @throws Exception
	 */
	public static Document makeModel() throws Exception {
		MEMM memm = MEMM.getInstance();
		return new Document(memm.getModel().writeModel());
	}
	
	/**Examines an XML document produced with MakeModel, and uses the data
	 * to initialise the relevant singletons.
	 * 
	 * @param modelDoc The XML document.
	 * @throws Exception
	 */
	public static Model restoreModel(Document modelDoc) throws Exception {
        return new Model(modelDoc);
	}
	
	/**Loads a model file, with the given name (a .xml suffix will be added)
	 * from the resources path.
	 * 
	 * @param modelName The model to load.
	 */
	public static Model loadModelFromResources(String modelName) {
		try {
			Document modelDoc = new ResourceGetter(
					Model.class.getClassLoader(),
					"uk/ac/cam/ch/wwmm/oscarMEMM/models/"
				).getXMLDocument(modelName + ".xml");
			return restoreModel(modelDoc);
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
	public static Model loadModel(String modelName) {
		try {
			return loadModelFromResources(modelName);
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


    public static Model getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = loadDefaultInstance();
        }
        return defaultInstance;
    }

    private static Model loadDefaultInstance() {
        String modelName = OscarProperties.getData().model;
        return loadModel(modelName);
    }

}
