package uk.ac.cam.ch.wwmm.oscarrecogniser.finder;

import org.junit.Test;
import uk.ac.cam.ch.wwmm.oscar.document.IToken;
import uk.ac.cam.ch.wwmm.oscar.document.ITokenSequence;
import uk.ac.cam.ch.wwmm.oscar.document.NamedEntity;
import uk.ac.cam.ch.wwmm.oscar.tools.StringTools;
import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;
import uk.ac.cam.ch.wwmm.oscartokeniser.Tokeniser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Sam Adams
 */
public class DFAFinderTest {

    @Test
    public void testFindSimpleTerms() {
        NamedEntityType ANIMAL = NamedEntityType.valueOf("ANIMAL");
        Map<String,NamedEntityType> terms = new HashMap<String, NamedEntityType>();
        terms.put("fox", ANIMAL);
        terms.put("dog", ANIMAL);
        Finder finder = new Finder(terms);
        String s = "The quick brown fox jumps over the lazy dog.";
        List<NamedEntity> neList = finder.findNamedEntities(s);
        assertEquals(2, neList.size());
        assertTrue(neList.contains(new NamedEntity("fox", 16, 19, ANIMAL)));
        assertTrue(neList.contains(new NamedEntity("dog", 40, 43, ANIMAL)));
    }


    @Test
    public void testFindSimpleTermsDifferentCase() {
        NamedEntityType ANIMAL = NamedEntityType.valueOf("ANIMAL");
        Map<String,NamedEntityType> terms = new HashMap<String, NamedEntityType>();
        terms.put("fox", ANIMAL);
        terms.put("dog", ANIMAL);
        Finder finder = new Finder(terms);
        String s = "The quick brown Fox jumps over the lazy Dog.";
        List<NamedEntity> neList = finder.findNamedEntities(s);
        assertEquals(2, neList.size());
        assertTrue(neList.contains(new NamedEntity("Fox", 16, 19, ANIMAL)));
        assertTrue(neList.contains(new NamedEntity("Dog", 40, 43, ANIMAL)));
    }

    
    static class Finder extends DFAFinder {

        private Map<String, NamedEntityType> terms;

        Finder(Map<String,NamedEntityType> terms) {
            this.terms = terms;
            init();
        }

        @Override
        protected void loadTerms() {
            for (Map.Entry<String, NamedEntityType> term : terms.entrySet()) {
                addNamedEntity(term.getKey(), term.getValue(), true);
            }
        }

        public List<NamedEntity> findNamedEntities(String s) {
            ITokenSequence t = Tokeniser.getInstance().tokenise(s);
            NECollector nec = new NECollector();
            List<RepresentationList> repsList = generateTokenRepresentations(t);
            findItems(t, repsList, nec);
            return nec.getNes();
        }

        private List<RepresentationList> generateTokenRepresentations(ITokenSequence t) {
            List<RepresentationList> repsList = new ArrayList<RepresentationList>();
            for(IToken token : t.getTokens()) {
                repsList.add(generateTokenRepresentations(token));
            }
            return repsList;
        }

        protected RepresentationList generateTokenRepresentations(IToken token) {
            RepresentationList tokenRepresentations = new RepresentationList();
            String value = token.getValue();
            tokenRepresentations.addRepresentation(value);
            String normalisedValue = StringTools.normaliseName(value);
            if (!normalisedValue.equals(value)) {
                tokenRepresentations.addRepresentation(normalisedValue);
            }
            if (value.length() == 1) {
                if (StringTools.isHyphen(value)) {
                    tokenRepresentations.addRepresentation("$HYPH");
                } else if (StringTools.isMidElipsis(value)) {
                    tokenRepresentations.addRepresentation("$DOTS");
                }
            }

            return tokenRepresentations;
        }

    }

}
