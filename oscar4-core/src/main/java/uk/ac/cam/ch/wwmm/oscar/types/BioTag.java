package uk.ac.cam.ch.wwmm.oscar.types;

/**
 * @author Sam Adams
 */
public enum BioTag {

    /**
     * The first token in a multiword entity
     */
    B,

    /**
     * A non-first token in a multiword entity
     */
    I,

    /**
     * Unknown whether the token is first/non-first
     */
    O

}
