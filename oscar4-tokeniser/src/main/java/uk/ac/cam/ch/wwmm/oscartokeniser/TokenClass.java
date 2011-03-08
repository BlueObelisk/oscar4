package uk.ac.cam.ch.wwmm.oscartokeniser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.cam.ch.wwmm.oscar.types.NamedEntityType;

/**
 *  A regular expression used to classify individual Tokens.
 *
 * @author ptc24
 * @author dmj30
 *
*/

public class TokenClass {

   private final NamedEntityType type;
   private final Pattern pattern;
   private final String name;

   public TokenClass (NamedEntityType type, String regex, String name) {
   	this.type = type;
   	this.pattern = Pattern.compile(regex, Pattern.COMMENTS);
   	this.name = name;
   }

   public boolean isMatch(String s) {
       Matcher m = pattern.matcher(s);
       return m.matches();
   }

   public NamedEntityType getType() {
       return type;
   }

   public String getName() {
       return name;
   }

}
