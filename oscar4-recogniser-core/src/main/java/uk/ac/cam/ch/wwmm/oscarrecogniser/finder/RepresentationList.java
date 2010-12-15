package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Class provides a type-safe list of token representations (represented using strings)
 * @author Sam Adams
 */
public class RepresentationList implements Iterable<String> {
    
    private List<String> representations;

    public RepresentationList() {
        this.representations = new ArrayList<String>();
    }

    public RepresentationList(RepresentationList representationList) {
        this.representations = new ArrayList<String>(representationList.representations);
    }


    public int getRepresentationCount() {
        return this.representations.size();
    }

    public String getRepresentation(int i) {
        return this.representations.get(i);
    }

    public void addRepresentation(String representation) {
        this.representations.add(representation);
    }


    public void removeRepresentations(Collection<String> representations) {
        this.representations.removeAll(representations);
    }

    public void addRepresentations(Collection<String> representations) {
        this.representations.addAll(representations);
    }

    public Iterator<String> iterator() {
        return this.representations.iterator();
    }

    public String[] toArray() {
        String[] a = new String[this.representations.size()];
        return this.representations.toArray(a);
    }

    public List<String> toList() {
        return new ArrayList<String>(representations);
    }

    public boolean isEmpty() {
        return representations.isEmpty();
    }
}
