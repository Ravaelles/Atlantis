package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
The TechType (or Technology Type, also referred to as an Ability) represents a Unit's ability which can be researched with UnitInterface::research or used with UnitInterface::useTech. In order for a Unit to use its own specialized ability, it must first be available and researched. See also TechTypes
*/
/**
Expected type constructor. If the type is an invalid type, then it becomes Types::Unknown. A type is invalid if its value is less than 0 or greater than Types::Unknown. Parameters id The id that corresponds to this type. It is typically an integer value that corresponds to an internal Broodwar type. If the given id is invalid, then it becomes Types::Unknown.
*/
public class TechType {

    public String toString() {
        return toString_native(pointer);
    }

/**
Retrieves the race that is required to research or use the TechType. Note There is an exception where Infested Kerrigan can use Psionic Storm. This does not apply to the behavior of this function. Returns Race object indicating which race is designed to use this technology type.
*/
    public Race getRace() {
        return getRace_native(pointer);
    }

/**
Retrieves the mineral cost of researching this technology. Returns Amount of minerals needed in order to research this technology.
*/
    public int mineralPrice() {
        return mineralPrice_native(pointer);
    }

/**
Retrieves the vespene gas cost of researching this technology. Returns Amount of vespene gas needed in order to research this technology.
*/
    public int gasPrice() {
        return gasPrice_native(pointer);
    }

/**
Retrieves the number of frames needed to research the tech type. Returns The time, in frames, it will take for the research to complete. See also UnitInterface::getRemainingResearchTime
*/
    public int researchTime() {
        return researchTime_native(pointer);
    }

/**
Retrieves the amount of energy needed to use this TechType as an ability. Returns Energy cost of the ability. See also UnitInterface::getEnergy
*/
    public int energyCost() {
        return energyCost_native(pointer);
    }

/**
Retrieves the UnitType that can research this technology. Returns UnitType that is able to research the technology in the game. Return values UnitTypes::None If the technology/ability is either provided for free or never available.
*/
    public UnitType whatResearches() {
        return whatResearches_native(pointer);
    }

/**
Retrieves the Weapon that is attached to this tech type. A technology's WeaponType is used to indicate the range and behaviour of the ability when used by a Unit. Returns WeaponType containing information about the ability's behavior. Return values WeaponTypes::None If there is no corresponding WeaponType.
*/
    public WeaponType getWeapon() {
        return getWeapon_native(pointer);
    }

/**
Checks if this ability can be used on other units. Returns true if the ability can be used on other units, and false if it can not.
*/
    public boolean targetsUnit() {
        return targetsUnit_native(pointer);
    }

/**
Checks if this ability can be used on the terrain (ground). Returns true if the ability can be used on the terrain.
*/
    public boolean targetsPosition() {
        return targetsPosition_native(pointer);
    }

/**
Retrieves the Order that a Unit uses when using this ability. Returns Order representing the action a Unit uses to perform this ability
*/
    public Order getOrder() {
        return getOrder_native(pointer);
    }

/**
Retrieves the UnitType required to research this technology. The required unit type must be a completed unit owned by the player researching the technology. Returns UnitType that is needed to research this tech type. Return values UnitTypes::None if no unit is required to research this tech type. See also PlayerInterface::completedUnitCount Since 4.1.2
*/
    public UnitType requiredUnit() {
        return requiredUnit_native(pointer);
    }

    public static final TechType Stim_Packs = new TechType(0);

    public static final TechType Lockdown = new TechType(0);

    public static final TechType EMP_Shockwave = new TechType(0);

    public static final TechType Spider_Mines = new TechType(0);

    public static final TechType Scanner_Sweep = new TechType(0);

    public static final TechType Tank_Siege_Mode = new TechType(0);

    public static final TechType Defensive_Matrix = new TechType(0);

    public static final TechType Irradiate = new TechType(0);

    public static final TechType Yamato_Gun = new TechType(0);

    public static final TechType Cloaking_Field = new TechType(0);

    public static final TechType Personnel_Cloaking = new TechType(0);

    public static final TechType Restoration = new TechType(0);

    public static final TechType Optical_Flare = new TechType(0);

    public static final TechType Healing = new TechType(0);

    public static final TechType Nuclear_Strike = new TechType(0);

    public static final TechType Burrowing = new TechType(0);

    public static final TechType Infestation = new TechType(0);

    public static final TechType Spawn_Broodlings = new TechType(0);

    public static final TechType Dark_Swarm = new TechType(0);

    public static final TechType Plague = new TechType(0);

    public static final TechType Consume = new TechType(0);

    public static final TechType Ensnare = new TechType(0);

    public static final TechType Parasite = new TechType(0);

    public static final TechType Lurker_Aspect = new TechType(0);

    public static final TechType Psionic_Storm = new TechType(0);

    public static final TechType Hallucination = new TechType(0);

    public static final TechType Recall = new TechType(0);

    public static final TechType Stasis_Field = new TechType(0);

    public static final TechType Archon_Warp = new TechType(0);

    public static final TechType Disruption_Web = new TechType(0);

    public static final TechType Mind_Control = new TechType(0);

    public static final TechType Dark_Archon_Meld = new TechType(0);

    public static final TechType Feedback = new TechType(0);

    public static final TechType Maelstrom = new TechType(0);

    public static final TechType None = new TechType(0);

    public static final TechType Unknown = new TechType(0);


    private static Map<Long, TechType> instances = new HashMap<Long, TechType>();

    private TechType(long pointer) {
        this.pointer = pointer;
    }

    private static TechType get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        TechType instance = instances.get(pointer);
        if (instance == null ) {
            instance = new TechType(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native String toString_native(long pointer);

    private native Race getRace_native(long pointer);

    private native int mineralPrice_native(long pointer);

    private native int gasPrice_native(long pointer);

    private native int researchTime_native(long pointer);

    private native int energyCost_native(long pointer);

    private native UnitType whatResearches_native(long pointer);

    private native WeaponType getWeapon_native(long pointer);

    private native boolean targetsUnit_native(long pointer);

    private native boolean targetsPosition_native(long pointer);

    private native Order getOrder_native(long pointer);

    private native UnitType requiredUnit_native(long pointer);


}
