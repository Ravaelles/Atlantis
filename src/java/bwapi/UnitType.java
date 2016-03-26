package bwapi;

import bwapi.*;
import java.lang.reflect.Field;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
The UnitType is used to get information about a particular type of unit, such as its cost, build time, weapon, hit points, abilities, etc. See also UnitInterface::getType, UnitType
*/
/**
Expected type constructor. If the type is an invalid type, then it becomes Types::Unknown. A type is invalid if its value is less than 0 or greater than Types::Unknown. Parameters id The id that corresponds to this type. It is typically an integer value that corresponds to an internal Broodwar type. If the given id is invalid, then it becomes Types::Unknown.
*/
//public class UnitType {
public class UnitType implements Comparable<UnitType> {

    public String toString() {
        return toString_native(pointer);
    }

/**
Retrieves the Race that the unit type belongs to. Returns Race indicating the race that owns this unit type. Return values Race::None indicating that the unit type does not belong to any particular race (a critter for example).
*/
    public Race getRace() {
        return getRace_native(pointer);
    }

/**
Obtains the source unit type that is used to build or train this unit type, as well as the amount of them that are required. Returns std::pair in which the first value is the UnitType that builds this unit type, and the second value is the number of those types that are required (this value is 2 for Archons, and 1 for all other types). Return values pair(UnitType::None,0) If this unit type cannot be made by the player.
*/
    public Pair<UnitType, Integer> whatBuilds() {
        return whatBuilds_native(pointer);
    }

/**
Retrieves the immediate technology tree requirements to make this unit type. Returns std::map containing a UnitType to number mapping of UnitType required.
*/
    public Map<UnitType, Integer> requiredUnits() {
        return requiredUnits_native(pointer);
    }

/**
Identifies the required TechType in order to create certain units. Note The only unit that requires a technology is the Lurker, which needs Lurker Aspect. Returns TechType indicating the technology that must be researched in order to create this unit type. Return values TechTypes::None If creating this unit type does not require a technology to be researched.
*/
    public TechType requiredTech() {
        return requiredTech_native(pointer);
    }

/**
Retrieves the cloaking technology associated with certain units. Returns TechType referring to the cloaking technology that this unit type uses as an ability. Return values TechTypes::None If this unit type does not have an active cloak ability.
*/
    public TechType cloakingTech() {
        return cloakingTech_native(pointer);
    }

/**
Retrieves the set of abilities that this unit can use, provided it is available to you in the game. Returns Set of TechTypes containing ability information.
*/
    public List<TechType> abilities() {
        return abilities_native(pointer);
    }

/**
Retrieves the set of upgrades that this unit can use to enhance its fighting ability. Returns Set of UpgradeTypes containing upgrade types that will impact this unit type.
*/
    public List<UpgradeType> upgrades() {
        return upgrades_native(pointer);
    }

/**
Retrieves the upgrade type used to increase the armor of this unit type. For each upgrade, this unit type gains +1 additional armor. Returns UpgradeType indicating the upgrade that increases this unit type's armor amount.
*/
    public UpgradeType armorUpgrade() {
        return armorUpgrade_native(pointer);
    }

/**
Retrieves the default maximum amount of hit points that this unit type can have. Note This value may not necessarily match the value seen in the Use Map Settings game type. Returns Integer indicating the maximum amount of hit points for this unit type.
*/
    public int maxHitPoints() {
        return maxHitPoints_native(pointer);
    }

/**
Retrieves the default maximum amount of shield points that this unit type can have. Note This value may not necessarily match the value seen in the Use Map Settings game type. Returns Integer indicating the maximum amount of shield points for this unit type. Return values 0 If this unit type does not have shields.
*/
    public int maxShields() {
        return maxShields_native(pointer);
    }

/**
Retrieves the maximum amount of energy this unit type can have by default. Returns Integer indicating the maximum amount of energy for this unit type. Return values 0 If this unit does not gain energy for abilities.
*/
    public int maxEnergy() {
        return maxEnergy_native(pointer);
    }

/**
Retrieves the default amount of armor that the unit type starts with, excluding upgrades. Note This value may not necessarily match the value seen in the Use Map Settings game type. Returns The amount of armor the unit type has.
*/
    public int armor() {
        return armor_native(pointer);
    }

/**
Retrieves the default mineral price of purchasing the unit. Note This value may not necessarily match the value seen in the Use Map Settings game type. Returns Mineral cost of the unit.
*/
    public int mineralPrice() {
        return mineralPrice_native(pointer);
    }

/**
Retrieves the default vespene gas price of purchasing the unit. Note This value may not necessarily match the value seen in the Use Map Settings game type. Returns Vespene gas cost of the unit.
*/
    public int gasPrice() {
        return gasPrice_native(pointer);
    }

/**
Retrieves the default time, in frames, needed to train, morph, or build the unit. Note This value may not necessarily match the value seen in the Use Map Settings game type. Returns Number of frames needed in order to build the unit. See also UnitInterface::getRemainingBuildTime
*/
    public int buildTime() {
        return buildTime_native(pointer);
    }

/**
Retrieves the amount of supply that this unit type will use when created. It will use the supply pool that is appropriate for its Race. Note In Starcraft programming, the managed supply values are double than what they appear in the game. The reason for this is because Zerglings use 0.5 visible supply. Returns Integer containing the supply required to build this unit. See also supplyProvided, PlayerInterface::supplyTotal, PlayerInterface::supplyUsed
*/
    public int supplyRequired() {
        return supplyRequired_native(pointer);
    }

/**
Retrieves the amount of supply that this unit type produces for its appropriate Race's supply pool. Note In Starcraft programming, the managed supply values are double than what they appear in the game. The reason for this is because Zerglings use 0.5 visible supply. See also supplyRequired, PlayerInterface::supplyTotal, PlayerInterface::supplyUsed
*/
    public int supplyProvided() {
        return supplyProvided_native(pointer);
    }

/**
Retrieves the amount of space required by this unit type to fit inside a Bunker or Transport(Dropship, Shuttle, Overlord ). Returns Amount of space required by this unit type for transport. Return values 255 If this unit type can not be transported. See also spaceProvided
*/
    public int spaceRequired() {
        return spaceRequired_native(pointer);
    }

/**
Retrieves the amount of space provided by this Bunker or Transport(Dropship, Shuttle, Overlord ) for unit transportation. Returns The number of slots provided by this unit type. See also spaceRequired
*/
    public int spaceProvided() {
        return spaceProvided_native(pointer);
    }

/**
Retrieves the amount of score points awarded for constructing this unit type. This value is used for calculating scores in the post-game score screen. Returns Number of points awarded for constructing this unit type. See also destroyScore
*/
    public int buildScore() {
        return buildScore_native(pointer);
    }

/**
Retrieves the amount of score points awarded for killing this unit type. This value is used for calculating scores in the post-game score screen. Returns Number of points awarded for killing this unit type. See also buildScore
*/
    public int destroyScore() {
        return destroyScore_native(pointer);
    }

/**
Retrieves the UnitSizeType of this unit, which is used in calculations along with weapon damage types to determine the amount of damage that will be dealt to this type. Returns UnitSizeType indicating the conceptual size of the unit type. See also WeaponType::damageType
*/
    public UnitSizeType size() {
        return size_native(pointer);
    }

/**
Retrieves the width of this unit type, in tiles. Used for determining the tile size of structures. Returns Width of this unit type, in tiles.
*/
    public int tileWidth() {
        return tileWidth_native(pointer);
    }

/**
Retrieves the height of this unit type, in tiles. Used for determining the tile size of structures. Returns Height of this unit type, in tiles.
*/
    public int tileHeight() {
        return tileHeight_native(pointer);
    }

/**
Retrieves the tile size of this unit type. Used for determining the tile size of structures. Returns TilePosition containing the width (x) and height (y) of the unit type, in tiles.
*/
    public TilePosition tileSize() {
        return tileSize_native(pointer);
    }

/**
Retrieves the distance from the center of the unit type to its left edge. Returns Distance to this unit type's left edge from its center, in pixels.
*/
    public int dimensionLeft() {
        return dimensionLeft_native(pointer);
    }

/**
Retrieves the distance from the center of the unit type to its top edge. Returns Distance to this unit type's top edge from its center, in pixels.
*/
    public int dimensionUp() {
        return dimensionUp_native(pointer);
    }

/**
Retrieves the distance from the center of the unit type to its right edge. Returns Distance to this unit type's right edge from its center, in pixels.
*/
    public int dimensionRight() {
        return dimensionRight_native(pointer);
    }

/**
Retrieves the distance from the center of the unit type to its bottom edge. Returns Distance to this unit type's bottom edge from its center, in pixels.
*/
    public int dimensionDown() {
        return dimensionDown_native(pointer);
    }

/**
A macro for retrieving the width of the unit type, which is calculated using dimensionLeft + dimensionRight + 1. Returns Width of the unit, in pixels.
*/
    public int width() {
        return width_native(pointer);
    }

/**
A macro for retrieving the height of the unit type, which is calculated using dimensionUp + dimensionDown + 1. Returns Height of the unit, in pixels.
*/
    public int height() {
        return height_native(pointer);
    }

/**
Retrieves the range at which this unit type will start targeting enemy units. Returns Distance at which this unit type begins to seek out enemy units, in pixels.
*/
    public int seekRange() {
        return seekRange_native(pointer);
    }

/**
Retrieves the sight range of this unit type. Returns Sight range of this unit type, measured in pixels.
*/
    public int sightRange() {
        return sightRange_native(pointer);
    }

/**
Retrieves this unit type's weapon type used when attacking targets on the ground. Returns WeaponType used as this unit type's ground weapon. See also maxGroundHits, airWeapon
*/
    public WeaponType groundWeapon() {
        return groundWeapon_native(pointer);
    }

/**
Retrieves the maximum number of hits this unit can deal to a ground target using its ground weapon. This value is multiplied by the ground weapon's damage to calculate the unit type's damage potential. Returns Maximum number of hits given to ground targets. See also groundWeapon, maxAirHits
*/
    public int maxGroundHits() {
        return maxGroundHits_native(pointer);
    }

/**
Retrieves this unit type's weapon type used when attacking targets in the air. Returns WeaponType used as this unit type's air weapon. See also maxAirHits, groundWeapon
*/
    public WeaponType airWeapon() {
        return airWeapon_native(pointer);
    }

/**
Retrieves the maximum number of hits this unit can deal to a flying target using its air weapon. This value is multiplied by the air weapon's damage to calculate the unit type's damage potential. Returns Maximum number of hits given to air targets. See also airWeapon, maxGroundHits
*/
    public int maxAirHits() {
        return maxAirHits_native(pointer);
    }

/**
Retrieves this unit type's top movement speed with no upgrades. Note That some units have inconsistent movement and this value is sometimes an approximation. Returns The approximate top speed, in pixels per frame, as a double. For liftable Terran structures, this function returns their movement speed while lifted.
*/
    public double topSpeed() {
        return topSpeed_native(pointer);
    }

/**
Retrieves the unit's acceleration amount. Returns How fast the unit can accelerate to its top speed.
*/
    public int acceleration() {
        return acceleration_native(pointer);
    }

/**
Retrieves the unit's halting distance. This determines how fast a unit can stop moving. Returns A halting distance value.
*/
    public int haltDistance() {
        return haltDistance_native(pointer);
    }

/**
Retrieves a unit's turning radius. This determines how fast a unit can turn. Returns A turn radius value.
*/
    public int turnRadius() {
        return turnRadius_native(pointer);
    }

/**
Determines if a unit can train other units. For example, UnitType::Terran_Barracks.canProduce() will return true, while UnitType::Terran_Marine.canProduce() will return false. This is also true for two non-structures: Carrier (can produce interceptors) and Reaver (can produce scarabs). Returns true if this unit type can have a production queue, and false otherwise.
*/
    public boolean canProduce() {
        return canProduce_native(pointer);
    }

/**
Checks if this unit is capable of attacking. Note This function returns false for units that can only inflict damage via special abilities, such as the High Templar. Returns true if this unit type is capable of damaging other units with a standard attack, and false otherwise.
*/
    public boolean canAttack() {
        return canAttack_native(pointer);
    }

/**
Checks if this unit type is capable of movement. Note Buildings will return false, including Terran liftable buildings which are capable of moving when lifted. Returns true if this unit can use a movement command, and false if they cannot move.
*/
    public boolean canMove() {
        return canMove_native(pointer);
    }

/**
Checks if this unit type is a flying unit. Flying units ignore ground pathing and collisions. Returns true if this unit type is in the air by default, and false otherwise.
*/
    public boolean isFlyer() {
        return isFlyer_native(pointer);
    }

/**
Checks if this unit type can regenerate hit points. This generally applies to Zerg units. Returns true if this unit type regenerates its hit points, and false otherwise.
*/
    public boolean regeneratesHP() {
        return regeneratesHP_native(pointer);
    }

/**
Checks if this unit type has the capacity to store energy and use it for special abilities. Returns true if this unit type generates energy, and false if it does not have an energy pool.
*/
    public boolean isSpellcaster() {
        return isSpellcaster_native(pointer);
    }

/**
Checks if this unit type is permanently cloaked. This means the unit type is always cloaked and requires a detector in order to see it. Returns true if this unit type is permanently cloaked, and false otherwise.
*/
    public boolean hasPermanentCloak() {
        return hasPermanentCloak_native(pointer);
    }

/**
Checks if this unit type is invincible by default. Invincible units cannot take damage. Returns true if this unit type is invincible, and false if it is vulnerable to attacks.
*/
    public boolean isInvincible() {
        return isInvincible_native(pointer);
    }

/**
Checks if this unit is an organic unit. The organic property is required for some abilities such as Heal. Returns true if this unit type has the organic property, and false otherwise.
*/
    public boolean isOrganic() {
        return isOrganic_native(pointer);
    }

/**
Checks if this unit is mechanical. The mechanical property is required for some actions such as Repair. Returns true if this unit type has the mechanical property, and false otherwise.
*/
    public boolean isMechanical() {
        return isMechanical_native(pointer);
    }

/**
Checks if this unit is robotic. The robotic property is applied to robotic units such as the Probe which prevents them from taking damage from Irradiate. Returns true if this unit type has the robotic property, and false otherwise.
*/
    public boolean isRobotic() {
        return isRobotic_native(pointer);
    }

/**
Checks if this unit type is capable of detecting units that are cloaked or burrowed. Returns true if this unit type is a detector by default, false if it does not have this property
*/
    public boolean isDetector() {
        return isDetector_native(pointer);
    }

/**
Checks if this unit type is capable of storing resources such as Mineral Fields. Resources are harvested from resource containers. Returns true if this unit type may contain resources that can be harvested, false otherwise.
*/
    public boolean isResourceContainer() {
        return isResourceContainer_native(pointer);
    }

/**
Checks if this unit type is a resource depot. Resource depots must be placed a certain distance from resources. Resource depots are typically the main building for any particular race. Workers will return resources to the nearest resource depot. Example: if ( BWAPI::Broodwar->self() ) { BWAPI::Unitset myUnits = BWAPI::Broodwar->self()->getUnits(); for ( auto u : myUnits ) { if ( u->isIdle() && u->getType().isResourceDepot() ) u->train( u->getType().getRace().getWorker() ); } } Returns true if the unit type is a resource depot, false if it is not.
*/
    public boolean isResourceDepot() {
        return isResourceDepot_native(pointer);
    }

/**
Checks if this unit type is a refinery. A refinery is a structure that is placed on top of a Vespene Geyser . Refinery types are Refinery , Extractor , and Assimilator. Example: if ( BWAPI::Broodwar->self() ) { BWAPI::Unitset myUnits = BWAPI::Broodwar->self()->getUnits(); for ( auto u : myUnits ) { if ( u->getType().isRefinery() ) { int nWorkersAssigned = u->getClientInfo<int>('work'); if ( nWorkersAssigned < 3 ) { Unit pClosestIdleWorker = u->getClosestUnit(BWAPI::Filter::IsWorker && BWAPI::Filter::IsIdle); if ( pClosestIdleWorker ) { // gather from the refinery (and check if successful) if ( pClosestIdleWorker->gather(u) ) { // set a back reference for when the unit is killed or re-assigned (code not provided) pClosestIdleWorker->setClientInfo(u, 'ref'); // Increment the number of workers assigned and associate it with the refinery ++nWorkersAssigned; u->setClientInfo(nWorkersAssigned, 'work'); } } } // workers < 3 } // isRefinery } // for } Returns true if this unit type is a refinery, and false if it is not.
*/
    public boolean isRefinery() {
        return isRefinery_native(pointer);
    }

/**
Checks if this unit type is a worker unit. Worker units can harvest resources and build structures. Worker unit types include the SCV , Probe, and Drone. Returns true if this unit type is a worker, and false if it is not.
*/
    public boolean isWorker() {
        return isWorker_native(pointer);
    }

/**
Checks if this structure is powered by a psi field. Structures powered by psi can only be placed near a Pylon. If the Pylon is destroyed, then this unit will lose power. Returns true if this unit type can only be placed in a psi field, false otherwise. Note If this function returns a successful state, then the following function calls will also return a successful state: isBuilding(), getRace() == Races::Protoss
*/
    public boolean requiresPsi() {
        return requiresPsi_native(pointer);
    }

/**
Checks if this structure must be placed on Zerg creep. Returns true if this unit type requires creep, false otherwise. Note If this function returns a successful state, then the following function calls will also return a successful state: isBuilding(), getRace() == Races::Zerg
*/
    public boolean requiresCreep() {
        return requiresCreep_native(pointer);
    }

/**
Checks if this unit type spawns two units when being hatched from an Egg. This is only applicable to Zerglings and Scourges. Returns true if morphing this unit type will spawn two of them, and false if only one is spawned.
*/
    public boolean isTwoUnitsInOneEgg() {
        return isTwoUnitsInOneEgg_native(pointer);
    }

/**
Checks if this unit type has the capability to use the Burrow technology when it is researched. Note The Lurker can burrow even without researching the ability. See also TechTypes::Burrow Returns true if this unit can use the Burrow ability, and false otherwise. Note If this function returns a successful state, then the following function calls will also return a successful state: getRace() == Races::Zerg, !isBuilding(), canMove()
*/
    public boolean isBurrowable() {
        return isBurrowable_native(pointer);
    }

/**
Checks if this unit type has the capability to use a cloaking ability when it is researched. This applies only to Wraiths and Ghosts, and does not include units which are permanently cloaked. Returns true if this unit has a cloaking ability, false otherwise. See also hasPermanentCloak, TechTypes::Cloaking_Field, TechTypes::Personnel_Cloaking
*/
    public boolean isCloakable() {
        return isCloakable_native(pointer);
    }

/**
Checks if this unit is a structure. This includes Mineral Fields and Vespene Geysers. Returns true if this unit is a building, and false otherwise.
*/
    public boolean isBuilding() {
        return isBuilding_native(pointer);
    }

/**
Checks if this unit is an add-on. Add-ons are attachments used by some Terran structures such as the Comsat Station. Returns true if this unit is an add-on, and false otherwise. Note If this function returns a successful state, then the following function calls will also return a successful state: getRace() == Races::Terran, isBuilding()
*/
    public boolean isAddon() {
        return isAddon_native(pointer);
    }

/**
Checks if this structure has the capability to use the lift-off command. Returns true if this unit type is a flyable building, false otherwise. Note If this function returns a successful state, then the following function calls will also return a successful state: isBuilding()
*/
    public boolean isFlyingBuilding() {
        return isFlyingBuilding_native(pointer);
    }

/**
Checks if this unit type is a neutral type, such as critters and resources. Returns true if this unit is intended to be neutral, and false otherwise.
*/
    public boolean isNeutral() {
        return isNeutral_native(pointer);
    }

/**
Checks if this unit type is a hero. Heroes are types that the player cannot obtain normally, and are identified by the white border around their icon when selected with a group. Note There are two non-hero units included in this set, the Civilian and Dark Templar Hero. Returns true if this unit type is a hero type, and false otherwise.
*/
    public boolean isHero() {
        return isHero_native(pointer);
    }

/**
Checks if this unit type is a powerup. Powerups can be picked up and carried by workers. They are usually only seen in campaign maps and Capture the Flag. Returns true if this unit type is a powerup type, and false otherwise.
*/
    public boolean isPowerup() {
        return isPowerup_native(pointer);
    }

/**
Checks if this unit type is a beacon. Each race has exactly one beacon each. They are UnitType::Special_Zerg_Beacon, UnitType::Special_Terran_Beacon, and UnitType::Special_Protoss_Beacon. See also isFlagBeacon Returns true if this unit type is one of the three race beacons, and false otherwise.
*/
    public boolean isBeacon() {
        return isBeacon_native(pointer);
    }

/**
Checks if this unit type is a flag beacon. Each race has exactly one flag beacon each. They are UnitType::Special_Zerg_Flag_Beacon, UnitType::Special_Terran_Flag_Beacon, and UnitType::Special_Protoss_Flag_Beacon. Flag beacons spawn a Flag after some ARBITRARY I FORGOT AMOUNT OF FRAMES. See also isBeacon Returns true if this unit type is one of the three race flag beacons, and false otherwise.
*/
    public boolean isFlagBeacon() {
        return isFlagBeacon_native(pointer);
    }

/**
Checks if this structure is special and cannot be obtained normally within the game. Returns true if this structure is a special building, and false otherwise. Note If this function returns a successful state, then the following function calls will also return a successful state: isBuilding()
*/
    public boolean isSpecialBuilding() {
        return isSpecialBuilding_native(pointer);
    }

/**
Identifies if this unit type is used to complement some abilities. These include UnitType::Spell_Dark_Swarm, UnitType::Spell_Disruption_Web, and UnitType::Spell_Scanner_Sweep, which correspond to TechTypes::Dark_Swarm, TechTypes::Disruption_Web, and TechTypes::Scanner_Sweep respectively. Returns true if this unit type is used for an ability, and false otherwise.
*/
    public boolean isSpell() {
        return isSpell_native(pointer);
    }

/**
Checks if this structure type produces creep. That is, the unit type spreads creep over a wide area so that Zerg structures can be placed on it. Returns true if this unit type spreads creep. Note If this function returns a successful state, then the following function calls will also return a successful state: getRace() == Races::Zerg, isBuilding() Since 4.1.2
*/
    public boolean producesCreep() {
        return producesCreep_native(pointer);
    }

/**
Checks if this unit type produces larva. This is essentially used to check if the unit type is a Hatchery, Lair, or Hive. Returns true if this unit type produces larva. Note If this function returns a successful state, then the following function calls will also return a successful state: getRace() == Races::Zerg, isBuilding()
*/
    public boolean producesLarva() {
        return producesLarva_native(pointer);
    }

/**
Checks if this unit type is a mineral field and contains a resource amount. This indicates that the unit type is either UnitType::Resource_Mineral_Field, UnitType::Resource_Mineral_Field_Type_2, or UnitType::Resource_Mineral_Field_Type_3. Returns true if this unit type is a mineral field resource.
*/
    public boolean isMineralField() {
        return isMineralField_native(pointer);
    }

/**
Checks if this unit type is a neutral critter. Returns true if this unit type is a critter, and false otherwise. Example usage: BWAPI::Position myBasePosition( BWAPI::Broodwar->self()->getStartLocation() ); BWAPI::UnitSet unitsAroundTheBase = BWAPI::Broodwar->getUnitsInRadius(myBasePosition, 1024, !BWAPI::Filter::IsOwned && !BWAPI::Filter::IsParasited); for ( auto u : unitsAroundTheBase ) { if ( u->getType().isCritter() && !u->isInvincible() ) { BWAPI::Unit myQueen = u->getClosestUnit(BWAPI::Filter::GetType == BWAPI::UnitType::Zerg_Queen && BWAPI::Filter::IsOwned); if ( myQueen ) myQueen->useTech(BWAPI::TechTypes::Parasite, u); } }
*/
    public boolean isCritter() {
        return isCritter_native(pointer);
    }

/**
Checks if this unit type is capable of constructing an add-on. An add-on is an extension or attachment for Terran structures, specifically the Command Center, Factory, Starport, and Science Facility. Returns true if this unit type can construct an add-on, and false if it can not. See also isAddon
*/
    public boolean canBuildAddon() {
        return canBuildAddon_native(pointer);
    }

/**
Retrieves the set of technologies that this unit type is capable of researching. Note Some maps have special parameters that disable certain technologies. Use PlayerInterface::isResearchAvailable to determine if a technology is actually available in the current game for a specific player. Returns TechType::set containing the technology types that can be researched. See also PlayerInterface::isResearchAvailable Since 4.1.2
*/
    public List<TechType> researchesWhat() {
        return researchesWhat_native(pointer);
    }

/**
Retrieves the set of upgrades that this unit type is capable of upgrading. Note Some maps have special upgrade limitations. Use PlayerInterface::getMaxUpgradeLevel to check if an upgrade is available. Returns UpgradeType::set containing the upgrade types that can be upgraded. See also PlayerInterface::getMaxUpgradeLevel Since 4.1.2
*/
    public List<UpgradeType> upgradesWhat() {
        return upgradesWhat_native(pointer);
    }

    public static final UnitType Terran_Firebat = new UnitType(0);

    public static final UnitType Terran_Ghost = new UnitType(0);

    public static final UnitType Terran_Goliath = new UnitType(0);

    public static final UnitType Terran_Marine = new UnitType(0);

    public static final UnitType Terran_Medic = new UnitType(0);

    public static final UnitType Terran_SCV = new UnitType(0);

    public static final UnitType Terran_Siege_Tank_Siege_Mode = new UnitType(0);

    public static final UnitType Terran_Siege_Tank_Tank_Mode = new UnitType(0);

    public static final UnitType Terran_Vulture = new UnitType(0);

    public static final UnitType Terran_Vulture_Spider_Mine = new UnitType(0);

    public static final UnitType Terran_Battlecruiser = new UnitType(0);

    public static final UnitType Terran_Dropship = new UnitType(0);

    public static final UnitType Terran_Nuclear_Missile = new UnitType(0);

    public static final UnitType Terran_Science_Vessel = new UnitType(0);

    public static final UnitType Terran_Valkyrie = new UnitType(0);

    public static final UnitType Terran_Wraith = new UnitType(0);

    public static final UnitType Hero_Alan_Schezar = new UnitType(0);

    public static final UnitType Hero_Alexei_Stukov = new UnitType(0);

    public static final UnitType Hero_Arcturus_Mengsk = new UnitType(0);

    public static final UnitType Hero_Edmund_Duke_Tank_Mode = new UnitType(0);

    public static final UnitType Hero_Edmund_Duke_Siege_Mode = new UnitType(0);

    public static final UnitType Hero_Gerard_DuGalle = new UnitType(0);

    public static final UnitType Hero_Gui_Montag = new UnitType(0);

    public static final UnitType Hero_Hyperion = new UnitType(0);

    public static final UnitType Hero_Jim_Raynor_Marine = new UnitType(0);

    public static final UnitType Hero_Jim_Raynor_Vulture = new UnitType(0);

    public static final UnitType Hero_Magellan = new UnitType(0);

    public static final UnitType Hero_Norad_II = new UnitType(0);

    public static final UnitType Hero_Samir_Duran = new UnitType(0);

    public static final UnitType Hero_Sarah_Kerrigan = new UnitType(0);

    public static final UnitType Hero_Tom_Kazansky = new UnitType(0);

    public static final UnitType Terran_Civilian = new UnitType(0);

    public static final UnitType Terran_Academy = new UnitType(0);

    public static final UnitType Terran_Armory = new UnitType(0);

    public static final UnitType Terran_Barracks = new UnitType(0);

    public static final UnitType Terran_Bunker = new UnitType(0);

    public static final UnitType Terran_Command_Center = new UnitType(0);

    public static final UnitType Terran_Engineering_Bay = new UnitType(0);

    public static final UnitType Terran_Factory = new UnitType(0);

    public static final UnitType Terran_Missile_Turret = new UnitType(0);

    public static final UnitType Terran_Refinery = new UnitType(0);

    public static final UnitType Terran_Science_Facility = new UnitType(0);

    public static final UnitType Terran_Starport = new UnitType(0);

    public static final UnitType Terran_Supply_Depot = new UnitType(0);

    public static final UnitType Terran_Comsat_Station = new UnitType(0);

    public static final UnitType Terran_Control_Tower = new UnitType(0);

    public static final UnitType Terran_Covert_Ops = new UnitType(0);

    public static final UnitType Terran_Machine_Shop = new UnitType(0);

    public static final UnitType Terran_Nuclear_Silo = new UnitType(0);

    public static final UnitType Terran_Physics_Lab = new UnitType(0);

    public static final UnitType Special_Crashed_Norad_II = new UnitType(0);

    public static final UnitType Special_Ion_Cannon = new UnitType(0);

    public static final UnitType Special_Power_Generator = new UnitType(0);

    public static final UnitType Special_Psi_Disrupter = new UnitType(0);

    public static final UnitType Protoss_Archon = new UnitType(0);

    public static final UnitType Protoss_Dark_Archon = new UnitType(0);

    public static final UnitType Protoss_Dark_Templar = new UnitType(0);

    public static final UnitType Protoss_Dragoon = new UnitType(0);

    public static final UnitType Protoss_High_Templar = new UnitType(0);

    public static final UnitType Protoss_Probe = new UnitType(0);

    public static final UnitType Protoss_Reaver = new UnitType(0);

    public static final UnitType Protoss_Scarab = new UnitType(0);

    public static final UnitType Protoss_Zealot = new UnitType(0);

    public static final UnitType Protoss_Arbiter = new UnitType(0);

    public static final UnitType Protoss_Carrier = new UnitType(0);

    public static final UnitType Protoss_Corsair = new UnitType(0);

    public static final UnitType Protoss_Interceptor = new UnitType(0);

    public static final UnitType Protoss_Observer = new UnitType(0);

    public static final UnitType Protoss_Scout = new UnitType(0);

    public static final UnitType Protoss_Shuttle = new UnitType(0);

    public static final UnitType Hero_Aldaris = new UnitType(0);

    public static final UnitType Hero_Artanis = new UnitType(0);

    public static final UnitType Hero_Danimoth = new UnitType(0);

    public static final UnitType Hero_Dark_Templar = new UnitType(0);

    public static final UnitType Hero_Fenix_Dragoon = new UnitType(0);

    public static final UnitType Hero_Fenix_Zealot = new UnitType(0);

    public static final UnitType Hero_Gantrithor = new UnitType(0);

    public static final UnitType Hero_Mojo = new UnitType(0);

    public static final UnitType Hero_Raszagal = new UnitType(0);

    public static final UnitType Hero_Tassadar = new UnitType(0);

    public static final UnitType Hero_Tassadar_Zeratul_Archon = new UnitType(0);

    public static final UnitType Hero_Warbringer = new UnitType(0);

    public static final UnitType Hero_Zeratul = new UnitType(0);

    public static final UnitType Protoss_Arbiter_Tribunal = new UnitType(0);

    public static final UnitType Protoss_Assimilator = new UnitType(0);

    public static final UnitType Protoss_Citadel_of_Adun = new UnitType(0);

    public static final UnitType Protoss_Cybernetics_Core = new UnitType(0);

    public static final UnitType Protoss_Fleet_Beacon = new UnitType(0);

    public static final UnitType Protoss_Forge = new UnitType(0);

    public static final UnitType Protoss_Gateway = new UnitType(0);

    public static final UnitType Protoss_Nexus = new UnitType(0);

    public static final UnitType Protoss_Observatory = new UnitType(0);

    public static final UnitType Protoss_Photon_Cannon = new UnitType(0);

    public static final UnitType Protoss_Pylon = new UnitType(0);

    public static final UnitType Protoss_Robotics_Facility = new UnitType(0);

    public static final UnitType Protoss_Robotics_Support_Bay = new UnitType(0);

    public static final UnitType Protoss_Shield_Battery = new UnitType(0);

    public static final UnitType Protoss_Stargate = new UnitType(0);

    public static final UnitType Protoss_Templar_Archives = new UnitType(0);

    public static final UnitType Special_Khaydarin_Crystal_Form = new UnitType(0);

    public static final UnitType Special_Protoss_Temple = new UnitType(0);

    public static final UnitType Special_Stasis_Cell_Prison = new UnitType(0);

    public static final UnitType Special_Warp_Gate = new UnitType(0);

    public static final UnitType Special_XelNaga_Temple = new UnitType(0);

    public static final UnitType Zerg_Broodling = new UnitType(0);

    public static final UnitType Zerg_Defiler = new UnitType(0);

    public static final UnitType Zerg_Drone = new UnitType(0);

    public static final UnitType Zerg_Egg = new UnitType(0);

    public static final UnitType Zerg_Hydralisk = new UnitType(0);

    public static final UnitType Zerg_Infested_Terran = new UnitType(0);

    public static final UnitType Zerg_Larva = new UnitType(0);

    public static final UnitType Zerg_Lurker = new UnitType(0);

    public static final UnitType Zerg_Lurker_Egg = new UnitType(0);

    public static final UnitType Zerg_Ultralisk = new UnitType(0);

    public static final UnitType Zerg_Zergling = new UnitType(0);

    public static final UnitType Zerg_Cocoon = new UnitType(0);

    public static final UnitType Zerg_Devourer = new UnitType(0);

    public static final UnitType Zerg_Guardian = new UnitType(0);

    public static final UnitType Zerg_Mutalisk = new UnitType(0);

    public static final UnitType Zerg_Overlord = new UnitType(0);

    public static final UnitType Zerg_Queen = new UnitType(0);

    public static final UnitType Zerg_Scourge = new UnitType(0);

    public static final UnitType Hero_Devouring_One = new UnitType(0);

    public static final UnitType Hero_Hunter_Killer = new UnitType(0);

    public static final UnitType Hero_Infested_Duran = new UnitType(0);

    public static final UnitType Hero_Infested_Kerrigan = new UnitType(0);

    public static final UnitType Hero_Kukulza_Guardian = new UnitType(0);

    public static final UnitType Hero_Kukulza_Mutalisk = new UnitType(0);

    public static final UnitType Hero_Matriarch = new UnitType(0);

    public static final UnitType Hero_Torrasque = new UnitType(0);

    public static final UnitType Hero_Unclean_One = new UnitType(0);

    public static final UnitType Hero_Yggdrasill = new UnitType(0);

    public static final UnitType Zerg_Creep_Colony = new UnitType(0);

    public static final UnitType Zerg_Defiler_Mound = new UnitType(0);

    public static final UnitType Zerg_Evolution_Chamber = new UnitType(0);

    public static final UnitType Zerg_Extractor = new UnitType(0);

    public static final UnitType Zerg_Greater_Spire = new UnitType(0);

    public static final UnitType Zerg_Hatchery = new UnitType(0);

    public static final UnitType Zerg_Hive = new UnitType(0);

    public static final UnitType Zerg_Hydralisk_Den = new UnitType(0);

    public static final UnitType Zerg_Infested_Command_Center = new UnitType(0);

    public static final UnitType Zerg_Lair = new UnitType(0);

    public static final UnitType Zerg_Nydus_Canal = new UnitType(0);

    public static final UnitType Zerg_Queens_Nest = new UnitType(0);

    public static final UnitType Zerg_Spawning_Pool = new UnitType(0);

    public static final UnitType Zerg_Spire = new UnitType(0);

    public static final UnitType Zerg_Spore_Colony = new UnitType(0);

    public static final UnitType Zerg_Sunken_Colony = new UnitType(0);

    public static final UnitType Zerg_Ultralisk_Cavern = new UnitType(0);

    public static final UnitType Special_Cerebrate = new UnitType(0);

    public static final UnitType Special_Cerebrate_Daggoth = new UnitType(0);

    public static final UnitType Special_Mature_Chrysalis = new UnitType(0);

    public static final UnitType Special_Overmind = new UnitType(0);

    public static final UnitType Special_Overmind_Cocoon = new UnitType(0);

    public static final UnitType Special_Overmind_With_Shell = new UnitType(0);

    public static final UnitType Critter_Bengalaas = new UnitType(0);

    public static final UnitType Critter_Kakaru = new UnitType(0);

    public static final UnitType Critter_Ragnasaur = new UnitType(0);

    public static final UnitType Critter_Rhynadon = new UnitType(0);

    public static final UnitType Critter_Scantid = new UnitType(0);

    public static final UnitType Critter_Ursadon = new UnitType(0);

    public static final UnitType Resource_Mineral_Field = new UnitType(0);

    public static final UnitType Resource_Mineral_Field_Type_2 = new UnitType(0);

    public static final UnitType Resource_Mineral_Field_Type_3 = new UnitType(0);

    public static final UnitType Resource_Vespene_Geyser = new UnitType(0);

    public static final UnitType Spell_Dark_Swarm = new UnitType(0);

    public static final UnitType Spell_Disruption_Web = new UnitType(0);

    public static final UnitType Spell_Scanner_Sweep = new UnitType(0);

    public static final UnitType Special_Protoss_Beacon = new UnitType(0);

    public static final UnitType Special_Protoss_Flag_Beacon = new UnitType(0);

    public static final UnitType Special_Terran_Beacon = new UnitType(0);

    public static final UnitType Special_Terran_Flag_Beacon = new UnitType(0);

    public static final UnitType Special_Zerg_Beacon = new UnitType(0);

    public static final UnitType Special_Zerg_Flag_Beacon = new UnitType(0);

    public static final UnitType Powerup_Data_Disk = new UnitType(0);

    public static final UnitType Powerup_Flag = new UnitType(0);

    public static final UnitType Powerup_Khalis_Crystal = new UnitType(0);

    public static final UnitType Powerup_Khaydarin_Crystal = new UnitType(0);

    public static final UnitType Powerup_Mineral_Cluster_Type_1 = new UnitType(0);

    public static final UnitType Powerup_Mineral_Cluster_Type_2 = new UnitType(0);

    public static final UnitType Powerup_Protoss_Gas_Orb_Type_1 = new UnitType(0);

    public static final UnitType Powerup_Protoss_Gas_Orb_Type_2 = new UnitType(0);

    public static final UnitType Powerup_Psi_Emitter = new UnitType(0);

    public static final UnitType Powerup_Terran_Gas_Tank_Type_1 = new UnitType(0);

    public static final UnitType Powerup_Terran_Gas_Tank_Type_2 = new UnitType(0);

    public static final UnitType Powerup_Uraj_Crystal = new UnitType(0);

    public static final UnitType Powerup_Young_Chrysalis = new UnitType(0);

    public static final UnitType Powerup_Zerg_Gas_Sac_Type_1 = new UnitType(0);

    public static final UnitType Powerup_Zerg_Gas_Sac_Type_2 = new UnitType(0);

    public static final UnitType Special_Floor_Gun_Trap = new UnitType(0);

    public static final UnitType Special_Floor_Missile_Trap = new UnitType(0);

    public static final UnitType Special_Right_Wall_Flame_Trap = new UnitType(0);

    public static final UnitType Special_Right_Wall_Missile_Trap = new UnitType(0);

    public static final UnitType Special_Wall_Flame_Trap = new UnitType(0);

    public static final UnitType Special_Wall_Missile_Trap = new UnitType(0);

    public static final UnitType Special_Pit_Door = new UnitType(0);

    public static final UnitType Special_Right_Pit_Door = new UnitType(0);

    public static final UnitType Special_Right_Upper_Level_Door = new UnitType(0);

    public static final UnitType Special_Upper_Level_Door = new UnitType(0);

    public static final UnitType Special_Cargo_Ship = new UnitType(0);

    public static final UnitType Special_Floor_Hatch = new UnitType(0);

    public static final UnitType Special_Independant_Starport = new UnitType(0);

    public static final UnitType Special_Map_Revealer = new UnitType(0);

    public static final UnitType Special_Mercenary_Gunship = new UnitType(0);

    public static final UnitType Special_Start_Location = new UnitType(0);

    public static final UnitType None = new UnitType(0);

    public static final UnitType AllUnits = new UnitType(0);

    public static final UnitType Men = new UnitType(0);

    public static final UnitType Buildings = new UnitType(0);

    public static final UnitType Factories = new UnitType(0);

    public static final UnitType Unknown = new UnitType(0);


    private static Map<Long, UnitType> instances = new HashMap<Long, UnitType>();

    private UnitType(long pointer) {
        this.pointer = pointer;
        atlantisInit(); // @AtlantisChange
    }

    private static UnitType get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        UnitType instance = instances.get(pointer);
        if (instance == null ) {
            instance = new UnitType(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native String toString_native(long pointer);

    private native Race getRace_native(long pointer);

    private native Pair<UnitType, Integer> whatBuilds_native(long pointer);

    private native Map<UnitType, Integer> requiredUnits_native(long pointer);

    private native TechType requiredTech_native(long pointer);

    private native TechType cloakingTech_native(long pointer);

    private native List<TechType> abilities_native(long pointer);

    private native List<UpgradeType> upgrades_native(long pointer);

    private native UpgradeType armorUpgrade_native(long pointer);

    private native int maxHitPoints_native(long pointer);

    private native int maxShields_native(long pointer);

    private native int maxEnergy_native(long pointer);

    private native int armor_native(long pointer);

    private native int mineralPrice_native(long pointer);

    private native int gasPrice_native(long pointer);

    private native int buildTime_native(long pointer);

    private native int supplyRequired_native(long pointer);

    private native int supplyProvided_native(long pointer);

    private native int spaceRequired_native(long pointer);

    private native int spaceProvided_native(long pointer);

    private native int buildScore_native(long pointer);

    private native int destroyScore_native(long pointer);

    private native UnitSizeType size_native(long pointer);

    private native int tileWidth_native(long pointer);

    private native int tileHeight_native(long pointer);

    private native TilePosition tileSize_native(long pointer);

    private native int dimensionLeft_native(long pointer);

    private native int dimensionUp_native(long pointer);

    private native int dimensionRight_native(long pointer);

    private native int dimensionDown_native(long pointer);

    private native int width_native(long pointer);

    private native int height_native(long pointer);

    private native int seekRange_native(long pointer);

    private native int sightRange_native(long pointer);

    private native WeaponType groundWeapon_native(long pointer);

    private native int maxGroundHits_native(long pointer);

    private native WeaponType airWeapon_native(long pointer);

    private native int maxAirHits_native(long pointer);

    private native double topSpeed_native(long pointer);

    private native int acceleration_native(long pointer);

    private native int haltDistance_native(long pointer);

    private native int turnRadius_native(long pointer);

    private native boolean canProduce_native(long pointer);

    private native boolean canAttack_native(long pointer);

    private native boolean canMove_native(long pointer);

    private native boolean isFlyer_native(long pointer);

    private native boolean regeneratesHP_native(long pointer);

    private native boolean isSpellcaster_native(long pointer);

    private native boolean hasPermanentCloak_native(long pointer);

    private native boolean isInvincible_native(long pointer);

    private native boolean isOrganic_native(long pointer);

    private native boolean isMechanical_native(long pointer);

    private native boolean isRobotic_native(long pointer);

    private native boolean isDetector_native(long pointer);

    private native boolean isResourceContainer_native(long pointer);

    private native boolean isResourceDepot_native(long pointer);

    private native boolean isRefinery_native(long pointer);

    private native boolean isWorker_native(long pointer);

    private native boolean requiresPsi_native(long pointer);

    private native boolean requiresCreep_native(long pointer);

    private native boolean isTwoUnitsInOneEgg_native(long pointer);

    private native boolean isBurrowable_native(long pointer);

    private native boolean isCloakable_native(long pointer);

    private native boolean isBuilding_native(long pointer);

    private native boolean isAddon_native(long pointer);

    private native boolean isFlyingBuilding_native(long pointer);

    private native boolean isNeutral_native(long pointer);

    private native boolean isHero_native(long pointer);

    private native boolean isPowerup_native(long pointer);

    private native boolean isBeacon_native(long pointer);

    private native boolean isFlagBeacon_native(long pointer);

    private native boolean isSpecialBuilding_native(long pointer);

    private native boolean isSpell_native(long pointer);

    private native boolean producesCreep_native(long pointer);

    private native boolean producesLarva_native(long pointer);

    private native boolean isMineralField_native(long pointer);

    private native boolean isCritter_native(long pointer);

    private native boolean canBuildAddon_native(long pointer);

    private native List<TechType> researchesWhat_native(long pointer);

    private native List<UpgradeType> upgradesWhat_native(long pointer);

    // =========================================================
    // ===== Start of ATLANTIS CODE ============================
    // =========================================================
    
    private static int firstFreeID = 1;
    private int ID;
    
    private String _name = null;
    private String _shortName = null;
    public static boolean disableErrorReporting = false;

    // =========================================================
    
    private void atlantisInit() {
        this.ID = UnitType.firstFreeID++;
    }
    
    // =========================================================
    
    public static Collection<UnitType> getAllUnitTypes() {
        return instances.values();
    }
    
    /**
     * You can "Terran_Marine" or "Terran Marine" or even "Marine".
     */
    public static UnitType getByName(String string) {
        string = string.replace(" ", "_").toLowerCase()
                .replace("terran_", "").replace("protoss_", "").replace("zerg_", "");

        for (Field field : UnitType.class.getFields()) {
            String otherTypeName = field.getName().toLowerCase()
                    .replace("terran_", "").replace("protoss_", "").replace("zerg_", "");
            if (!otherTypeName.startsWith("Hero") && otherTypeName.equals(string)) {
                try {
                    UnitType unitType = (UnitType) UnitType.class.getField(field.getName()).get(null);
                    return unitType;
                } catch (Exception e) {
                    if (!disableErrorReporting) {
                        System.err.println("error trying to find UnitType for: '" + string + "'\n" + e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns true if given type equals to one of types passed as parameter.
     */
    public boolean isType(UnitType... types) {
        for (UnitType otherType : types) {
            if (equals(otherType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if given unit is considered to be "melee" unit (not ranged).
     */
    private boolean _checkedIfIsMelee = false;
    private boolean _isMelee = false;

    public boolean isMeleeUnit() {
        if (!_checkedIfIsMelee) {
            _checkedIfIsMelee = true;
            _isMelee = isType(
                    // Terran
                    UnitType.Terran_SCV,
                    UnitType.Terran_SCV,
                    UnitType.Terran_Firebat,
                    // Protoss
                    UnitType.Protoss_Probe,
                    UnitType.Protoss_Zealot,
                    UnitType.Protoss_Dark_Templar,
                    // Zerg
                    UnitType.Zerg_Drone,
                    UnitType.Zerg_Zergling,
                    UnitType.Zerg_Broodling
            );
        }
        return _isMelee;
    }

    /**
     * Returns true if given unit is considered to be "ranged" unit (not melee).
     */
    public boolean isRangedUnit() {
        return !isMeleeUnit();
    }

    /**
     * Returns total sum of minerals and gas this unit is worth.
     */
    public int getTotalResources() {
        int total = gasPrice() + mineralPrice();
        if (this.equals(UnitType.Zerg_Zergling)) {
            total /= 2;
        }
        return total;
    }

    /**
     * Returns  name for of unit type like e.g. "Zerg Zergling", "Terran Marine", "Protoss Gateway".
     */
    public String getName() {
        if (_name == null) {
            try {
                for (Field field : UnitType.class.getDeclaredFields()) {
                    UnitType type = (UnitType) field.get(this);
                    if (type.equals(this)) {
                        _name = field.getName().replace("_", " ");
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println("Can't define name for unit type: " + this);
                return "error"; 
            }
        }
        return _name;
    }
    
    // =========================================================
    // Auxiliary methods
    
    /**
     * Returns short name for of unit type like e.g. "Zergling", "Marine", "Mutalisk", "Barracks".
     */
    public String getShortName() {
        String name = getName();
        if (_shortName == null) {
            _shortName = name.replace("Terran_", "").replace("Protoss_", "").replace("Zerg_", "")
                .replace("Hero_", "").replace("Special_", "").replace("Powerup_", "").replace("_", " ")
                .replace("Terran ", "").replace("Protoss ", "").replace("Zerg ", "")
                .replace("Hero ", "").replace("Special ", "").replace("Powerup ", "");
        }
        
        return _shortName;
    }

    // =========================================================
    // Override
    
    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UnitType other = (UnitType) obj;
        if (ID != other.ID) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(UnitType o) {
        return Integer.compare(this.ID, o.ID);
    }

    // =========================================================
    // Type comparison methods
    public boolean isBase() {
        return isType(UnitType.Terran_Command_Center, UnitType.Protoss_Nexus, UnitType.Zerg_Hatchery,
                UnitType.Zerg_Lair, UnitType.Zerg_Hive);
    }

    public boolean isInfantry() {
        return isOrganic();
    }

    public boolean isVehicle() {
        return isMechanical();
    }

    public boolean isTerranInfantry() {
        return isType(UnitType.Terran_Marine, UnitType.Terran_Medic, UnitType.Terran_Firebat, UnitType.Terran_Ghost);
    }

    public boolean isMedic() {
        return isType(UnitType.Terran_Medic);
    }

    public boolean isGasBuilding() {
        return isType(UnitType.Terran_Refinery, UnitType.Protoss_Assimilator, UnitType.Zerg_Extractor);
    }
    
    public boolean isLarva() {
        return this.equals(UnitType.Zerg_Larva);
    }

    public boolean isAirUnit() {
        return isFlyer();
    }

    public boolean isGroundUnit() {
        return !isFlyer();
    }

    // =========================================================
    // Auxiliary
    
    public WeaponType getGroundWeapon() {
        return groundWeapon();
    }
    
    public WeaponType getAirWeapon() {
        return airWeapon();
    }

}
