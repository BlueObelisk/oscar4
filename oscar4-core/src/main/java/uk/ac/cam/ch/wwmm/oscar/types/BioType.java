package uk.ac.cam.ch.wwmm.oscar.types;

/**
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

    public BioTag getBio() {
        return bioTag;
    }

    public NamedEntityType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return getType().hashCode() * 31 + bioTag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BioType) {
            final BioType that = (BioType) obj;
            return getType().equals(that.getType()) && getBio().equals(that.getBio());
        }
        return false;
    }

    @Override
    public String toString() {
        return getBio()+"-"+getType();
    }

}
