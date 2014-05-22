package uk.ac.cam.ch.wwmm.oscardata;

import nu.xom.Element;

import uk.ac.cam.ch.wwmm.oscar.document.Annotation;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

public class DataAnnotation implements Annotation {

	private int endPos;
	private int startPos;
	private String surface;
	private Element internalMarkup;

	public DataAnnotation(int startPos, int endPos, String surface) {
		this.endPos = endPos;
		this.startPos = startPos;
		this.surface = surface;
	}
	
	
	public int getEnd() {
		return endPos;
	}

	public int getStart() {
		return startPos;
	}

	public String getSurface() {
		return surface;
	}

	public NamedEntityType getType() {
		return NamedEntityType.DATA;
	}


	public Element getAnnotatedElement() {
		return (Element) internalMarkup.copy();
	}


	void setInternalMarkup(Element internalMarkup) {
		this.internalMarkup = (Element) internalMarkup.copy();
	}

}
