package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
A representation of a weapon's explosion type. This indicates how the weapon behaves, such as if it deals splash damage or causes an effect to occur. See also ExplosionTypes
*/
/**
Expected type constructor. If the type is an invalid type, then it becomes Types::Unknown. A type is invalid if its value is less than 0 or greater than Types::Unknown. Parameters id The id that corresponds to this type. It is typically an integer value that corresponds to an internal Broodwar type. If the given id is invalid, then it becomes Types::Unknown.
*/
public class ExplosionType {

    public String toString() {
        return toString_native(pointer);
    }

    public static final ExplosionType None = new ExplosionType(0);

    public static final ExplosionType Normal = new ExplosionType(0);

    public static final ExplosionType Radial_Splash = new ExplosionType(0);

    public static final ExplosionType Enemy_Splash = new ExplosionType(0);

    public static final ExplosionType Lockdown = new ExplosionType(0);

    public static final ExplosionType Nuclear_Missile = new ExplosionType(0);

    public static final ExplosionType Parasite = new ExplosionType(0);

    public static final ExplosionType Broodlings = new ExplosionType(0);

    public static final ExplosionType EMP_Shockwave = new ExplosionType(0);

    public static final ExplosionType Irradiate = new ExplosionType(0);

    public static final ExplosionType Ensnare = new ExplosionType(0);

    public static final ExplosionType Plague = new ExplosionType(0);

    public static final ExplosionType Stasis_Field = new ExplosionType(0);

    public static final ExplosionType Dark_Swarm = new ExplosionType(0);

    public static final ExplosionType Consume = new ExplosionType(0);

    public static final ExplosionType Yamato_Gun = new ExplosionType(0);

    public static final ExplosionType Restoration = new ExplosionType(0);

    public static final ExplosionType Disruption_Web = new ExplosionType(0);

    public static final ExplosionType Corrosive_Acid = new ExplosionType(0);

    public static final ExplosionType Mind_Control = new ExplosionType(0);

    public static final ExplosionType Feedback = new ExplosionType(0);

    public static final ExplosionType Optical_Flare = new ExplosionType(0);

    public static final ExplosionType Maelstrom = new ExplosionType(0);

    public static final ExplosionType Air_Splash = new ExplosionType(0);

    public static final ExplosionType Unknown = new ExplosionType(0);


    private static Map<Long, ExplosionType> instances = new HashMap<Long, ExplosionType>();

    private ExplosionType(long pointer) {
        this.pointer = pointer;
    }

    private static ExplosionType get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        ExplosionType instance = instances.get(pointer);
        if (instance == null ) {
            instance = new ExplosionType(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native String toString_native(long pointer);


}
