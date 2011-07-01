package uk.ac.cam.ch.wwmm.oscar.types;

import java.util.HashMap;
import java.util.Map;

/**
 * The type of a named entity. A {@link NamedEntityType} has a;
 * <ul><li>name - a unique name for the NamedEntityType, e.g. "CM"
 * <li>description - an expanded description of what the NamedEntityType
 * represents, e.g. "Compound"
 * <li>priority - the priority of the NamedEntityType, for determining
 * which of two named entities should be discarded where they precisely
 * share a surface string</ul>
 * 
 * @author sea36
 * @author dmj30
 */
public class NamedEntityType {

    /**
     * The known named entity types registered with {@link #register(String, String)},
     * retrievable using {@link #valueOf(String)}.<br>
     * Keep the underscore for the ordering.
     */
    private static volatile Map<String, NamedEntityType> _types = null;

    public static final NamedEntityType COMPOUND = register("CM", "Compound", 6);
	public static final NamedEntityType COMPOUNDS = register("CMS", "Compounds");
	public static final NamedEntityType GROUP = register("GP", "Group");
	public static final NamedEntityType REACTION = register("RN", "Reaction", 8);
	public static final NamedEntityType ADJECTIVE = register("CJ", "Adjective", 9);
	public static final NamedEntityType LOCANTPREFIX = register("CPR", "Locant Prefix", 10);
	public static final NamedEntityType POTENTIALACRONYM = register("AHA", "Potential Acronym", 4);
	public static final NamedEntityType ASE = register("ASE", "Ase", 5);
	public static final NamedEntityType ASES = register("ASES", "Ases");
	public static final NamedEntityType PROPERNOUN = register("PN", "Proper Noun");
	public static final NamedEntityType ONTOLOGY = register("ONT", "Ontology Term", 2);
	public static final NamedEntityType CUSTOM = register("CUST", "Custom", 3);
	public static final NamedEntityType STOP = register("STOP", "Stop Word", 11);
	public static final NamedEntityType POLYMER = register("PM", "Polymer", 7);
	public static final NamedEntityType DATA = register("DATA", "Data");
	
    private final String name;
    private final String description;
    private final int priority;


    private static Map<String, NamedEntityType> getTypes() {
        if (_types == null) {
            _types = new HashMap<String, NamedEntityType>();
        }
        return _types;
    }

    /**
     * Returns a registered named entity with the given name and description
     * and a priority of 0. Does not overwrite an existing registered
     * NamedEntityType of the same name.
     * 
     * @param name a unique name for the NamedEntityType, e.g. "CM"
     * @param description an expanded description of what the NamedEntityType
     * represents, e.g. "Compound"
     */
    public static synchronized NamedEntityType register(String name, String description) {
        return register(name, description, 0);
    }

    /**
     * Returns a registered named entity with the given name, description
     * and priority. Does not overwrite an existing registered NamedEntityType
     * of the same name.
     * 
     * @param name a unique name for the NamedEntityType, e.g. "CM"
     * @param description an expanded description of what the NamedEntityType
     * represents, e.g. "Compound"
     * @param priority the priority of the NamedEntityType, for determining
     * which of two named entities should be discarded where they precisely
     * share a surface string
     */
    public static synchronized NamedEntityType register(String name, String description, int priority) {
        if (!getTypes().containsKey(name)) {
            final NamedEntityType type = new NamedEntityType(name, description, priority);
            getTypes().put(name, type);
        }
        return getTypes().get(name);
    }

    /**
     * Returns a {@link NamedEntityType} with the given name. If the name
     * has been registered, the registered object will be returned.
     */
    public static NamedEntityType valueOf(String name) {
        NamedEntityType result = null;
        if ((name != null) && !name.equals("")) {
            result = getTypes().get(name);
            if (result == null) {
                result = new NamedEntityType(name);
            }
        }
        return result;
    }


    private NamedEntityType(String name) {
        this(name, null);
    }

    private NamedEntityType(String name, String description) {
        this(name, description, 0);
    }

    private NamedEntityType(String name, String description, int priority) {
        this.name = name;
        this.description = description;
        this.priority = priority;
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }
    

    /**
     * Returns the parent {@link NamedEntityType} of the current
     * {@link NamedEntityType}, or null if it has no parent.
     */
    public NamedEntityType getParent() {
        int i = getName().lastIndexOf('-');
        return i == -1 ? null : valueOf(getName().substring(0, i));
    }

    /**
     * Tests if the given {@link NamedEntityType} is of the same
     * type or a subtype of the current {@link NamedEntityType}
     */
    public boolean isInstance(NamedEntityType type) {
        return getName().equals(type.getName())
                || type.getName().startsWith(getName()+'-');
    }


    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof NamedEntityType) {
            final NamedEntityType that = (NamedEntityType) obj;
            return getName().equals(that.getName());
        }
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }

}
