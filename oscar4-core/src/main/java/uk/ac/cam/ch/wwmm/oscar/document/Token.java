package uk.ac.cam.ch.wwmm.oscar.document;

import nu.xom.Element;
import uk.ac.cam.ch.wwmm.oscar.types.BioType;

/**A token - corresponding to a word, a number, a character of punctuation or
 * suchlike. Not whitespace.
 * 
 * @author ptc24
 *
 */
public final class Token implements IToken {	

    // TODO can we make this class immutable?

	/* Many of these are package visibility so Tokeniser can work on them
	 * easily.
	 */
	
	private String value;
	/**The character offset at the start of the token */
	private int start;
	/**The character offset (caret position) at the end of the token */
	private int end;
	private int id;
	private IProcessingDocument doc;
	private ITokenSequence tokenSequence;
	
	private Element neElem;
	
	/** The B/I/O tag, when inline annotation is digested */
	private BioType bioTag;

	public Token(String value, int start, int end, IProcessingDocument doc, BioType bioTag, Element neElem) {
		this.start = start;
		this.end = end;
		this.value = value;
		this.doc = doc;
		this.bioTag = bioTag;
		this.neElem = neElem;
	}
	

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IToken#getNAfter(int)
	 */
	public IToken getNAfter(int n) {
		int pos = n + id;
		if (tokenSequence == null){
			throw new RuntimeException();
		}
		else if(tokenSequence.getTokens().size() <= pos || pos < 0) {
			return null;
		}
		return tokenSequence.getTokens().get(pos);
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IToken#getValue()
	 */
	public String getValue() {
		return value;
	}

    /* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IToken#setValue(java.lang.String)
	 */
    public void setValue(String value) {
        this.value = value;
    }

    /* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IToken#getId()
	 */
	public int getId() {
		return id;
	}

    /* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IToken#setId(int)
	 */
    public void setId(int id) {
        this.id = id;
    }

	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IToken#getDoc()
	 */
	public IProcessingDocument getDoc() {
		return doc;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IToken#getStart()
	 */
	public int getStart() {
		return start;
	}

    
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IToken#getEnd()
	 */
	public int getEnd() {
		return end;
	}

    /* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IToken#setEnd(int)
	 */
    public void setEnd(int end) {
        this.end = end;
    }


	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IToken#getBioTag()
	 */
	public BioType getBioTag() {
		return bioTag;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IToken#setBioTag(java.lang.String)
	 */
	public void setBioTag(BioType bioTag) {
		this.bioTag = bioTag;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IToken#getTokenSequence()
	 */

	
	public ITokenSequence getTokenSequence() {
		return tokenSequence;
	}

    /* (non-Javadoc)
	 * @see uk.ac.cam.ch.wwmm.oscar.document.IToken#setTokenSequence(uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence)
	 */
    public void setTokenSequence(ITokenSequence tokenSequence) {
        this.tokenSequence = tokenSequence;
    }


    public Element getNeElem() {
        return neElem;
    }

    public void setNeElem(Element neElem) {
        this.neElem = neElem;
    }
}
