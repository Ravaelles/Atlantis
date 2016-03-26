package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
Damage types are used in Broodwar to determine the amount of damage that will be done to a unit. This corresponds with UnitSizeType to determine the damage done to a unit. See also WeaponType, DamageTypes, UnitSizeType View on Liquipedia View on Starcraft Campendium (Official Website) View on Starcraft Wikia
*/
/**
Expected type constructor. If the type is an invalid type, then it becomes Types::Unknown. A type is invalid if its value is less than 0 or greater than Types::Unknown. Parameters id The id that corresponds to this type. It is typically an integer value that corresponds to an internal Broodwar type. If the given id is invalid, then it becomes Types::Unknown.
*/
public class DamageType {

    public String toString() {
        return toString_native(pointer);
    }

    public static final DamageType Independent = new DamageType(0);

    public static final DamageType Explosive = new DamageType(0);

    public static final DamageType Concussive = new DamageType(0);

    public static final DamageType Normal = new DamageType(0);

    public static final DamageType Ignore_Armor = new DamageType(0);

    public static final DamageType None = new DamageType(0);

    public static final DamageType Unknown = new DamageType(0);


    private static Map<Long, DamageType> instances = new HashMap<Long, DamageType>();

    private DamageType(long pointer) {
        this.pointer = pointer;
    }

    private static DamageType get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        DamageType instance = instances.get(pointer);
        if (instance == null ) {
            instance = new DamageType(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native String toString_native(long pointer);


}
