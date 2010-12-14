package uk.ac.cam.ch.wwmm.oscar.document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sam Adams
 */
public class Sentence {

    private List<IToken> tokens;

    public Sentence(List<IToken> tokens) {
        this.tokens = tokens;
    }

    public Sentence() {
        this.tokens = new ArrayList<IToken>();
    }

    public List<IToken> getTokens() {
        return tokens;
    }

    public boolean isEmpty() {
        return tokens.isEmpty();
    }

    public void addToken(IToken token) {
        tokens.add(token);
    }

}
