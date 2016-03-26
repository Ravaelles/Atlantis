package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
This object identifies a weapon type used by a unit to attack and deal damage. Some weapon types can be upgraded while others are used for special abilities. See also WeaponTypes
*/
/**
Expected type constructor. If the type is an invalid type, then it becomes Types::Unknown. A type is invalid if its value is less than 0 or greater than Types::Unknown. Parameters id The id that corresponds to this type. It is typically an integer value that corresponds to an internal Broodwar type. If the given id is invalid, then it becomes Types::Unknown.
*/
public class WeaponType {

    public String toString() {
        return toString_native(pointer);
    }

/**
Retrieves the technology type that must be researched before this weapon can be used. Returns TechType required by this weapon. Return values TechTypes::None if no tech type is required to use this weapon. See also TechType::getWeapon
*/
    public TechType getTech() {
        return getTech_native(pointer);
    }

/**
Retrieves the unit type that is intended to use this weapon type. Note There is a rare case where some hero unit types use the same weapon. Returns The UnitType that uses this weapon. See also UnitType::groundWeapon, UnitType::airWeapon
*/
    public UnitType whatUses() {
        return whatUses_native(pointer);
    }

/**
Retrieves the base amount of damage that this weapon can deal per attack. Note That this damage amount must go through a DamageType and UnitSizeType filter before it is applied to a unit. Returns Amount of base damage that this weapon deals.
*/
    public int damageAmount() {
        return damageAmount_native(pointer);
    }

/**
Determines the bonus amount of damage that this weapon type increases by for every upgrade to this type. See also upgradeType Returns Amount of damage added for every weapon upgrade.
*/
    public int damageBonus() {
        return damageBonus_native(pointer);
    }

/**
Retrieves the base amount of cooldown time between each attack, in frames. Returns The amount of base cooldown applied to the unit after an attack. See also UnitInterface::getGroundWeaponCooldown, UnitInterface::getAirWeaponCooldown
*/
    public int damageCooldown() {
        return damageCooldown_native(pointer);
    }

/**
Obtains the intended number of missiles/attacks that are used. This is used to multiply with the damage amount to obtain the full amount of damage for an attack. Returns The damage factor multiplied by the amount to obtain the total damage. See also damageAmount
*/
    public int damageFactor() {
        return damageFactor_native(pointer);
    }

/**
Retrieves the upgrade type that increases this weapon's damage output. Returns The UpgradeType used to upgrade this weapon's damage. See also damageBonus
*/
    public UpgradeType upgradeType() {
        return upgradeType_native(pointer);
    }

/**
Retrieves the damage type that this weapon applies to a unit type. Returns DamageType used for damage calculation. See also DamageType, UnitSizeType
*/
    public DamageType damageType() {
        return damageType_native(pointer);
    }

/**
Retrieves the explosion type that indicates how the weapon deals damage. Returns ExplosionType identifying how damage is applied to a target location.
*/
    public ExplosionType explosionType() {
        return explosionType_native(pointer);
    }

/**
Retrieves the minimum attack range of the weapon, measured in pixels. This value is 0 for almost all weapon types, except for WeaponTypes::Arclite_Shock_Cannon and WeaponTypes::Arclite_Shock_Cannon_Edmund_Duke. Returns Minimum attack range, in pixels.
*/
    public int minRange() {
        return minRange_native(pointer);
    }

/**
Retrieves the maximum attack range of the weapon, measured in pixels. Returns Maximum attack range, in pixels.
*/
    public int maxRange() {
        return maxRange_native(pointer);
    }

/**
Retrieves the inner radius used for splash damage calculations, in pixels. Returns Radius of the inner splash area, in pixels.
*/
    public int innerSplashRadius() {
        return innerSplashRadius_native(pointer);
    }

/**
Retrieves the middle radius used for splash damage calculations, in pixels. Returns Radius of the middle splash area, in pixels.
*/
    public int medianSplashRadius() {
        return medianSplashRadius_native(pointer);
    }

/**
Retrieves the outer radius used for splash damage calculations, in pixels. Returns Radius of the outer splash area, in pixels.
*/
    public int outerSplashRadius() {
        return outerSplashRadius_native(pointer);
    }

/**
Checks if this weapon type can target air units. Returns true if this weapon type can target air units, and false otherwise. See also UnitInterface::isFlying, UnitType::isFlyer
*/
    public boolean targetsAir() {
        return targetsAir_native(pointer);
    }

/**
Checks if this weapon type can target ground units. Returns true if this weapon type can target ground units, and false otherwise. See also UnitInterface::isFlying, UnitType::isFlyer
*/
    public boolean targetsGround() {
        return targetsGround_native(pointer);
    }

/**
Checks if this weapon type can only target mechanical units. Returns true if this weapon type can only target mechanical units, and false otherwise. See also targetsOrgOrMech, UnitType::isMechanical
*/
    public boolean targetsMechanical() {
        return targetsMechanical_native(pointer);
    }

/**
Checks if this weapon type can only target organic units. Returns true if this weapon type can only target organic units, and false otherwise. See also targetsOrgOrMech, UnitType::isOrganic
*/
    public boolean targetsOrganic() {
        return targetsOrganic_native(pointer);
    }

/**
Checks if this weapon type cannot target structures. Returns true if this weapon type cannot target buildings, and false if it can. See also UnitType::isBuilding
*/
    public boolean targetsNonBuilding() {
        return targetsNonBuilding_native(pointer);
    }

/**
Checks if this weapon type cannot target robotic units. Returns true if this weapon type cannot target robotic units, and false if it can. See also UnitType::isRobotic
*/
    public boolean targetsNonRobotic() {
        return targetsNonRobotic_native(pointer);
    }

/**
Checks if this weapon type can target the ground. Note This is more for attacks like Psionic Storm which can target a location, not to be confused with attack move. Returns true if this weapon type can target a location, and false otherwise.
*/
    public boolean targetsTerrain() {
        return targetsTerrain_native(pointer);
    }

/**
Checks if this weapon type can only target organic or mechanical units. Returns true if this weapon type can only target organic or mechanical units, and false otherwise. See also targetsOrganic, targetsMechanical, UnitType::isOrganic, UnitType::isMechanical
*/
    public boolean targetsOrgOrMech() {
        return targetsOrgOrMech_native(pointer);
    }

/**
Checks if this weapon type can only target units owned by the same player. This is used for WeaponTypes::Consume. Returns true if this weapon type can only target your own units, and false otherwise. See also UnitInterface::getPlayer
*/
    public boolean targetsOwn() {
        return targetsOwn_native(pointer);
    }

    public static final WeaponType Gauss_Rifle = new WeaponType(0);

    public static final WeaponType Gauss_Rifle_Jim_Raynor = new WeaponType(0);

    public static final WeaponType C_10_Canister_Rifle = new WeaponType(0);

    public static final WeaponType C_10_Canister_Rifle_Sarah_Kerrigan = new WeaponType(0);

    public static final WeaponType C_10_Canister_Rifle_Samir_Duran = new WeaponType(0);

    public static final WeaponType C_10_Canister_Rifle_Infested_Duran = new WeaponType(0);

    public static final WeaponType C_10_Canister_Rifle_Alexei_Stukov = new WeaponType(0);

    public static final WeaponType Fragmentation_Grenade = new WeaponType(0);

    public static final WeaponType Fragmentation_Grenade_Jim_Raynor = new WeaponType(0);

    public static final WeaponType Spider_Mines = new WeaponType(0);

    public static final WeaponType Twin_Autocannons = new WeaponType(0);

    public static final WeaponType Twin_Autocannons_Alan_Schezar = new WeaponType(0);

    public static final WeaponType Hellfire_Missile_Pack = new WeaponType(0);

    public static final WeaponType Hellfire_Missile_Pack_Alan_Schezar = new WeaponType(0);

    public static final WeaponType Arclite_Cannon = new WeaponType(0);

    public static final WeaponType Arclite_Cannon_Edmund_Duke = new WeaponType(0);

    public static final WeaponType Fusion_Cutter = new WeaponType(0);

    public static final WeaponType Gemini_Missiles = new WeaponType(0);

    public static final WeaponType Gemini_Missiles_Tom_Kazansky = new WeaponType(0);

    public static final WeaponType Burst_Lasers = new WeaponType(0);

    public static final WeaponType Burst_Lasers_Tom_Kazansky = new WeaponType(0);

    public static final WeaponType ATS_Laser_Battery = new WeaponType(0);

    public static final WeaponType ATS_Laser_Battery_Hero = new WeaponType(0);

    public static final WeaponType ATS_Laser_Battery_Hyperion = new WeaponType(0);

    public static final WeaponType ATA_Laser_Battery = new WeaponType(0);

    public static final WeaponType ATA_Laser_Battery_Hero = new WeaponType(0);

    public static final WeaponType ATA_Laser_Battery_Hyperion = new WeaponType(0);

    public static final WeaponType Flame_Thrower = new WeaponType(0);

    public static final WeaponType Flame_Thrower_Gui_Montag = new WeaponType(0);

    public static final WeaponType Arclite_Shock_Cannon = new WeaponType(0);

    public static final WeaponType Arclite_Shock_Cannon_Edmund_Duke = new WeaponType(0);

    public static final WeaponType Longbolt_Missile = new WeaponType(0);

    public static final WeaponType Claws = new WeaponType(0);

    public static final WeaponType Claws_Devouring_One = new WeaponType(0);

    public static final WeaponType Claws_Infested_Kerrigan = new WeaponType(0);

    public static final WeaponType Needle_Spines = new WeaponType(0);

    public static final WeaponType Needle_Spines_Hunter_Killer = new WeaponType(0);

    public static final WeaponType Kaiser_Blades = new WeaponType(0);

    public static final WeaponType Kaiser_Blades_Torrasque = new WeaponType(0);

    public static final WeaponType Toxic_Spores = new WeaponType(0);

    public static final WeaponType Spines = new WeaponType(0);

    public static final WeaponType Acid_Spore = new WeaponType(0);

    public static final WeaponType Acid_Spore_Kukulza = new WeaponType(0);

    public static final WeaponType Glave_Wurm = new WeaponType(0);

    public static final WeaponType Glave_Wurm_Kukulza = new WeaponType(0);

    public static final WeaponType Seeker_Spores = new WeaponType(0);

    public static final WeaponType Subterranean_Tentacle = new WeaponType(0);

    public static final WeaponType Suicide_Infested_Terran = new WeaponType(0);

    public static final WeaponType Suicide_Scourge = new WeaponType(0);

    public static final WeaponType Particle_Beam = new WeaponType(0);

    public static final WeaponType Psi_Blades = new WeaponType(0);

    public static final WeaponType Psi_Blades_Fenix = new WeaponType(0);

    public static final WeaponType Phase_Disruptor = new WeaponType(0);

    public static final WeaponType Phase_Disruptor_Fenix = new WeaponType(0);

    public static final WeaponType Psi_Assault = new WeaponType(0);

    public static final WeaponType Psionic_Shockwave = new WeaponType(0);

    public static final WeaponType Psionic_Shockwave_TZ_Archon = new WeaponType(0);

    public static final WeaponType Dual_Photon_Blasters = new WeaponType(0);

    public static final WeaponType Dual_Photon_Blasters_Mojo = new WeaponType(0);

    public static final WeaponType Dual_Photon_Blasters_Artanis = new WeaponType(0);

    public static final WeaponType Anti_Matter_Missiles = new WeaponType(0);

    public static final WeaponType Anti_Matter_Missiles_Mojo = new WeaponType(0);

    public static final WeaponType Anti_Matter_Missiles_Artanis = new WeaponType(0);

    public static final WeaponType Phase_Disruptor_Cannon = new WeaponType(0);

    public static final WeaponType Phase_Disruptor_Cannon_Danimoth = new WeaponType(0);

    public static final WeaponType Pulse_Cannon = new WeaponType(0);

    public static final WeaponType STS_Photon_Cannon = new WeaponType(0);

    public static final WeaponType STA_Photon_Cannon = new WeaponType(0);

    public static final WeaponType Scarab = new WeaponType(0);

    public static final WeaponType Neutron_Flare = new WeaponType(0);

    public static final WeaponType Halo_Rockets = new WeaponType(0);

    public static final WeaponType Corrosive_Acid = new WeaponType(0);

    public static final WeaponType Subterranean_Spines = new WeaponType(0);

    public static final WeaponType Warp_Blades = new WeaponType(0);

    public static final WeaponType Warp_Blades_Hero = new WeaponType(0);

    public static final WeaponType Warp_Blades_Zeratul = new WeaponType(0);

    public static final WeaponType Independant_Laser_Battery = new WeaponType(0);

    public static final WeaponType Twin_Autocannons_Floor_Trap = new WeaponType(0);

    public static final WeaponType Hellfire_Missile_Pack_Wall_Trap = new WeaponType(0);

    public static final WeaponType Flame_Thrower_Wall_Trap = new WeaponType(0);

    public static final WeaponType Hellfire_Missile_Pack_Floor_Trap = new WeaponType(0);

    public static final WeaponType Yamato_Gun = new WeaponType(0);

    public static final WeaponType Nuclear_Strike = new WeaponType(0);

    public static final WeaponType Lockdown = new WeaponType(0);

    public static final WeaponType EMP_Shockwave = new WeaponType(0);

    public static final WeaponType Irradiate = new WeaponType(0);

    public static final WeaponType Parasite = new WeaponType(0);

    public static final WeaponType Spawn_Broodlings = new WeaponType(0);

    public static final WeaponType Ensnare = new WeaponType(0);

    public static final WeaponType Dark_Swarm = new WeaponType(0);

    public static final WeaponType Plague = new WeaponType(0);

    public static final WeaponType Consume = new WeaponType(0);

    public static final WeaponType Stasis_Field = new WeaponType(0);

    public static final WeaponType Psionic_Storm = new WeaponType(0);

    public static final WeaponType Disruption_Web = new WeaponType(0);

    public static final WeaponType Restoration = new WeaponType(0);

    public static final WeaponType Mind_Control = new WeaponType(0);

    public static final WeaponType Feedback = new WeaponType(0);

    public static final WeaponType Optical_Flare = new WeaponType(0);

    public static final WeaponType Maelstrom = new WeaponType(0);

    public static final WeaponType None = new WeaponType(0);

    public static final WeaponType Unknown = new WeaponType(0);


    private static Map<Long, WeaponType> instances = new HashMap<Long, WeaponType>();

    private WeaponType(long pointer) {
        this.pointer = pointer;
    }

    private static WeaponType get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        WeaponType instance = instances.get(pointer);
        if (instance == null ) {
            instance = new WeaponType(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native String toString_native(long pointer);

    private native TechType getTech_native(long pointer);

    private native UnitType whatUses_native(long pointer);

    private native int damageAmount_native(long pointer);

    private native int damageBonus_native(long pointer);

    private native int damageCooldown_native(long pointer);

    private native int damageFactor_native(long pointer);

    private native UpgradeType upgradeType_native(long pointer);

    private native DamageType damageType_native(long pointer);

    private native ExplosionType explosionType_native(long pointer);

    private native int minRange_native(long pointer);

    private native int maxRange_native(long pointer);

    private native int innerSplashRadius_native(long pointer);

    private native int medianSplashRadius_native(long pointer);

    private native int outerSplashRadius_native(long pointer);

    private native boolean targetsAir_native(long pointer);

    private native boolean targetsGround_native(long pointer);

    private native boolean targetsMechanical_native(long pointer);

    private native boolean targetsOrganic_native(long pointer);

    private native boolean targetsNonBuilding_native(long pointer);

    private native boolean targetsNonRobotic_native(long pointer);

    private native boolean targetsTerrain_native(long pointer);

    private native boolean targetsOrgOrMech_native(long pointer);

    private native boolean targetsOwn_native(long pointer);

    // =========================================================
    // ===== Start of ATLANTIS CODE ============================
    // =========================================================
    
    public double getDamageNormalized() {
        if (this.equals(WeaponType.Psi_Blades)) {
            return 16;
        }
        else {
            return damageAmount()* damageFactor();
        }
    }

}
