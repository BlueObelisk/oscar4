package uk.ac.cam.ch.wwmm.oscar.types;

/**
 * A concatenation of the {@link BioTag} and
 * {@link NamedEntityType} to which a token belongs.
 * 
 * @author sea36
 */
public class BioType {

    private final BioTag bioTag;
    private final NamedEntityType type;

    public BioType(BioTag bioTag, NamedEntityType type) {
        if (bioTag == null || type == null) {
            throw new IllegalArgumentException("Null argument: "+bioTag+"-"+type);
        }
        this.bioTag = bioTag;
        this.type = type;
    }

    public BioType(NamedEntityType type) {
        if (type == null) {
            throw new IllegalArgumentException("Null argument: "+type);
        }
        this.type = type;
        this.bioTag = null;
    }

    public BioType(BioTag bioTag) {
        if (bioTag == null) {
            throw new IllegalArgumentException("Null argument: "+bioTag);
        }
        this.bioTag = bioTag;
        this.type = null;
    }

    public BioTag getBio() {
        return bioTag;
    }

    public NamedEntityType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return (type != null ? type.hashCode() * 31 : 0)
        	   + (bioTag != null ? bioTag.hashCode() : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BioType) {
            final BioType that = (BioType) obj;
            if (getType() == null && that.getType() == null &&
            	getBio().equals(that.getBio())) return true;
            if (getBio() == null && that.getBio() == null &&
            	getType().equals(that.getType())) return true;
            return getType().equals(that.getType()) && getBio().equals(that.getBio());
        }
        return false;
    }

    @Override
    public String toString() {
    	if (getBio() == null) return "" + getType();
    	if (getType() == null) return "" + getBio();
        return getBio() + "-" + getType();
    }
    
    public static BioType fromString(String string) {
    	if (string.contains("-")) {
    		String bioTagString = string.substring(0, string.indexOf("-"));
    		BioTag tag = BioTag.valueOf(bioTagString);

    		String neTypeString = string.substring(string.indexOf("-")+1);
    		NamedEntityType neType = NamedEntityType.valueOf(neTypeString);

    		return new BioType(tag, neType);
    	}
    	try {
    		BioTag tag = BioTag.valueOf(string);
    		if (tag != null) return new BioType(tag);
    	} catch (IllegalArgumentException exception) {
    		// this happens when the string is not a BioTag, so we
    		// try the NamedEntityType next...
    	}
    	NamedEntityType neType = NamedEntityType.valueOf(string);
    	if (neType != null) return new BioType(neType);

    	throw new IllegalArgumentException(
    		"Unrecognised BioType: " + string
    	);
    }

}
