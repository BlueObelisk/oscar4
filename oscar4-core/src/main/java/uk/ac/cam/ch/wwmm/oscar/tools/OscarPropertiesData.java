package uk.ac.cam.ch.wwmm.oscar.tools;

/**
 * Sets up and maintains a properties file for Oscar. Unlike the Oscar3Props
 * past, it does not take care of anything but properties. Therefore, it
 * does not deploy InChI libraries, or interactively set up workspace, or
 * servers. 
 * 
 * @author ptc24
 * @author jat45
 * @author egonw
 */
public class OscarPropertiesData {

	public boolean splitOnEnDash;
	public boolean useONT;
	public boolean useDSO;
	public boolean deprioritiseONT;
	public boolean useFormulaRegex;
	public boolean useWordShapeHeuristic;
	public boolean polymerMode;
		
	public double neThreshold;

	public String model;
	public String xmlStrings;

}
