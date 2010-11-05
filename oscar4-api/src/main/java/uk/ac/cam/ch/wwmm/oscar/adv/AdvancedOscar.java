package uk.ac.cam.ch.wwmm.oscar.adv;

import java.lang.reflect.InvocationTargetException;

import uk.ac.cam.ch.wwmm.oscar.Oscar;

public class AdvancedOscar extends Oscar {

	public AdvancedOscar(ClassLoader classLoader) throws Exception {
		super();
		this.classLoader = classLoader;
	}

	public void setTokeniser(String tokeniser)
	throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		tokenizerInstance = loadTokeniser(tokeniser);
	}

	public String getTokeniser() {
		return tokenizerInstance.getClass().getName();
	}

	public void setRecogiser(String recogiser)
	throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		recogniserInstance = loadRecogiser(recogiser);
	}

	public String getRecogiser() {
		return recogniserInstance.getClass().getName();
	}

}
