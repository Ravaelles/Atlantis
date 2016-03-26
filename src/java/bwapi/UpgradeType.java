package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
The upgrade type represents a passive upgrade that can be obtained with UnitInterface::upgrade. See also UpgradeTypes
*/
/**
Expected type constructor. If the type is an invalid type, then it becomes Types::Unknown. A type is invalid if its value is less than 0 or greater than Types::Unknown. Parameters id The id that corresponds to this type. It is typically an integer value that corresponds to an internal Broodwar type. If the given id is invalid, then it becomes Types::Unknown.
*/
public class UpgradeType {

    public String toString() {
        return toString_native(pointer);
    }

/**
Retrieves the race the upgrade is for. For example, UpgradeTypes::Terran_Infantry_Armor.getRace() will return Races::Terran. Returns Race that this upgrade belongs to.
*/
    public Race getRace() {
        return getRace_native(pointer);
    }

/**
Returns the mineral price for the upgrade. Parameters level (optional) The next upgrade level. Note Upgrades start at level 0. Returns The mineral cost of the upgrade for the given level.
*/
    public int mineralPrice() {
        return mineralPrice_native(pointer);
    }

    public int mineralPrice(int level) {
        return mineralPrice_native(pointer, level);
    }

/**
The amount that the mineral price increases for each additional upgrade. Returns The mineral cost added to the upgrade after each level.
*/
    public int mineralPriceFactor() {
        return mineralPriceFactor_native(pointer);
    }

/**
Returns the vespene gas price for the first upgrade. Parameters level (optional) The next upgrade level. Note Upgrades start at level 0. Returns The gas cost of the upgrade for the given level.
*/
    public int gasPrice() {
        return gasPrice_native(pointer);
    }

    public int gasPrice(int level) {
        return gasPrice_native(pointer, level);
    }

/**
Returns the amount that the vespene gas price increases for each additional upgrade. Returns The gas cost added to the upgrade after each level.
*/
    public int gasPriceFactor() {
        return gasPriceFactor_native(pointer);
    }

/**
Returns the number of frames needed to research the first upgrade. Parameters level (optional) The next upgrade level. Note Upgrades start at level 0. Returns The time cost of the upgrade for the given level.
*/
    public int upgradeTime() {
        return upgradeTime_native(pointer);
    }

    public int upgradeTime(int level) {
        return upgradeTime_native(pointer, level);
    }

/**
Returns the number of frames that the upgrade time increases for each additional upgrade. Returns The time cost added to the upgrade after each level.
*/
    public int upgradeTimeFactor() {
        return upgradeTimeFactor_native(pointer);
    }

/**
Returns the maximum number of times the upgrade can be researched. Returns Maximum number of times this upgrade can be upgraded.
*/
    public int maxRepeats() {
        return maxRepeats_native(pointer);
    }

/**
Returns the type of unit that researches the upgrade. Returns The UnitType that is used to upgrade this type.
*/
    public UnitType whatUpgrades() {
        return whatUpgrades_native(pointer);
    }

/**
Returns the type of unit that is required for the upgrade. The player must have at least one of these units completed in order to start upgrading this upgrade. Parameters level (optional) The next upgrade level. Note Upgrades start at level 0. Returns UnitType required to obtain this upgrade.
*/
    public UnitType whatsRequired() {
        return whatsRequired_native(pointer);
    }

    public UnitType whatsRequired(int level) {
        return whatsRequired_native(pointer, level);
    }

    public static final UpgradeType Terran_Infantry_Armor = new UpgradeType(0);

    public static final UpgradeType Terran_Vehicle_Plating = new UpgradeType(0);

    public static final UpgradeType Terran_Ship_Plating = new UpgradeType(0);

    public static final UpgradeType Terran_Infantry_Weapons = new UpgradeType(0);

    public static final UpgradeType Terran_Vehicle_Weapons = new UpgradeType(0);

    public static final UpgradeType Terran_Ship_Weapons = new UpgradeType(0);

    public static final UpgradeType U_238_Shells = new UpgradeType(0);

    public static final UpgradeType Ion_Thrusters = new UpgradeType(0);

    public static final UpgradeType Titan_Reactor = new UpgradeType(0);

    public static final UpgradeType Ocular_Implants = new UpgradeType(0);

    public static final UpgradeType Moebius_Reactor = new UpgradeType(0);

    public static final UpgradeType Apollo_Reactor = new UpgradeType(0);

    public static final UpgradeType Colossus_Reactor = new UpgradeType(0);

    public static final UpgradeType Caduceus_Reactor = new UpgradeType(0);

    public static final UpgradeType Charon_Boosters = new UpgradeType(0);

    public static final UpgradeType Zerg_Carapace = new UpgradeType(0);

    public static final UpgradeType Zerg_Flyer_Carapace = new UpgradeType(0);

    public static final UpgradeType Zerg_Melee_Attacks = new UpgradeType(0);

    public static final UpgradeType Zerg_Missile_Attacks = new UpgradeType(0);

    public static final UpgradeType Zerg_Flyer_Attacks = new UpgradeType(0);

    public static final UpgradeType Ventral_Sacs = new UpgradeType(0);

    public static final UpgradeType Antennae = new UpgradeType(0);

    public static final UpgradeType Pneumatized_Carapace = new UpgradeType(0);

    public static final UpgradeType Metabolic_Boost = new UpgradeType(0);

    public static final UpgradeType Adrenal_Glands = new UpgradeType(0);

    public static final UpgradeType Muscular_Augments = new UpgradeType(0);

    public static final UpgradeType Grooved_Spines = new UpgradeType(0);

    public static final UpgradeType Gamete_Meiosis = new UpgradeType(0);

    public static final UpgradeType Metasynaptic_Node = new UpgradeType(0);

    public static final UpgradeType Chitinous_Plating = new UpgradeType(0);

    public static final UpgradeType Anabolic_Synthesis = new UpgradeType(0);

    public static final UpgradeType Protoss_Ground_Armor = new UpgradeType(0);

    public static final UpgradeType Protoss_Air_Armor = new UpgradeType(0);

    public static final UpgradeType Protoss_Ground_Weapons = new UpgradeType(0);

    public static final UpgradeType Protoss_Air_Weapons = new UpgradeType(0);

    public static final UpgradeType Protoss_Plasma_Shields = new UpgradeType(0);

    public static final UpgradeType Singularity_Charge = new UpgradeType(0);

    public static final UpgradeType Leg_Enhancements = new UpgradeType(0);

    public static final UpgradeType Scarab_Damage = new UpgradeType(0);

    public static final UpgradeType Reaver_Capacity = new UpgradeType(0);

    public static final UpgradeType Gravitic_Drive = new UpgradeType(0);

    public static final UpgradeType Sensor_Array = new UpgradeType(0);

    public static final UpgradeType Gravitic_Boosters = new UpgradeType(0);

    public static final UpgradeType Khaydarin_Amulet = new UpgradeType(0);

    public static final UpgradeType Apial_Sensors = new UpgradeType(0);

    public static final UpgradeType Gravitic_Thrusters = new UpgradeType(0);

    public static final UpgradeType Carrier_Capacity = new UpgradeType(0);

    public static final UpgradeType Khaydarin_Core = new UpgradeType(0);

    public static final UpgradeType Argus_Jewel = new UpgradeType(0);

    public static final UpgradeType Argus_Talisman = new UpgradeType(0);

    public static final UpgradeType Upgrade_60 = new UpgradeType(0);

    public static final UpgradeType None = new UpgradeType(0);

    public static final UpgradeType Unknown = new UpgradeType(0);


    private static Map<Long, UpgradeType> instances = new HashMap<Long, UpgradeType>();

    private UpgradeType(long pointer) {
        this.pointer = pointer;
    }

    private static UpgradeType get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        UpgradeType instance = instances.get(pointer);
        if (instance == null ) {
            instance = new UpgradeType(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native String toString_native(long pointer);

    private native Race getRace_native(long pointer);

    private native int mineralPrice_native(long pointer);

    private native int mineralPrice_native(long pointer, int level);

    private native int mineralPriceFactor_native(long pointer);

    private native int gasPrice_native(long pointer);

    private native int gasPrice_native(long pointer, int level);

    private native int gasPriceFactor_native(long pointer);

    private native int upgradeTime_native(long pointer);

    private native int upgradeTime_native(long pointer, int level);

    private native int upgradeTimeFactor_native(long pointer);

    private native int maxRepeats_native(long pointer);

    private native UnitType whatUpgrades_native(long pointer);

    private native UnitType whatsRequired_native(long pointer);

    private native UnitType whatsRequired_native(long pointer, int level);


}
