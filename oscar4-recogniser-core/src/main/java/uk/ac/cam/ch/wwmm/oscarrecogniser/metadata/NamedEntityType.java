package uk.ac.cam.ch.wwmm.oscarrecogniser.metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sea36
 */
public class NamedEntityType {

    /**
     * The known named entity types registered with {@link #register(String, String)},
     * retrievable using {@link #valueOf(String)}.<br>
     * Keep the underscore for the ordering.
     */
    private static volatile Map<String, NamedEntityType> _types = null;

    public static final NamedEntityType COMPOUND = register("CM", "Compound");
	public static final NamedEntityType COMPOUNDS = register("CMS", "Compounds");
	public static final NamedEntityType GROUP = register("GP", "Group");
	public static final NamedEntityType REACTION = register("RN", "Reaction");
	public static final NamedEntityType ADJECTIVE = register("CJ", "Adjective");
	public static final NamedEntityType LOCANTPREFIX = register("CPR", "Locant Prefix");
	public static final NamedEntityType POTENTIALACRONYM = register("AHA", "Potential Acronym");
	public static final NamedEntityType ASE = register("ASE", "Ase");
	public static final NamedEntityType ASES = register("ASES", "Ases");
	public static final NamedEntityType PROPERNOUN = register("PN", "Proper Noun");
	public static final NamedEntityType ONTOLOGY = register("ONT", "Ontology Term");
	public static final NamedEntityType CUSTOM = register("CUST", "Custom");
	public static final NamedEntityType STOP = register("STOP", "Stop Word");
	public static final NamedEntityType POLYMER = register("PM", "Polymer");

    private final String name;
    private final String description;


    private static Map<String, NamedEntityType> getTypes() {
        if (_types == null) {
            _types = new HashMap<String, NamedEntityType>();
        }
        return _types;
    }

    public static synchronized NamedEntityType register(String id, String name) {
        if (!getTypes().containsKey(name)) {
            final NamedEntityType type = new NamedEntityType(id, name);
            getTypes().put(name, type);
        }
        return getTypes().get(name);
    }

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
        this.name = name;
        this.description = description;
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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
