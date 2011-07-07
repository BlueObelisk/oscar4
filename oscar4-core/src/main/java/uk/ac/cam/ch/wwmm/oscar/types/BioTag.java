package uk.ac.cam.ch.wwmm.oscar.types;

/**
 * A tag to define whether a token occurs inside or outside a named entity
 * 
 * @author Sam Adams
 */
public enum BioTag {

    /**
     * The first ("begin") token in a named entity
     */
    B,

    /**
     * A non-first ("inside") token in a named entity
     */
    I,

    /**
     * A token that is not in ("outside") a named entity
     */
    O

}
