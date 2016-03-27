package bwapi;

import atlantis.AtlantisGame;
import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
The Player represents a unique controller in the game. Each player in a match will have his or her own player instance. There is also a neutral player which owns all the neutral units (such as mineral patches and vespene geysers). See also Playerset, PlayerType, Race
*/
public class Player {

/**
Retrieves a unique ID that represents the player. Returns An integer representing the ID of the player.
*/
    public int getID() {
        return getID_native(pointer);
    }

/**
Retrieves the name of the player. Returns A std::string object containing the player's name. Note Don't forget to use std::string::c_str() when passing this parameter to Game::sendText and other variadic functions. Example usage: BWAPI::Player myEnemy = BWAPI::Broodwar->enemy(); if ( myEnemy != nullptr ) // Make sure there is an enemy! BWAPI::Broodwar->sendText("Prepare to be crushed, %s!", myEnemy->getName().c_str());
*/
    public String getName() {
        return getName_native(pointer);
    }

/**
Retrieves the set of all units that the player owns. This also includes incomplete units. Returns Reference to a Unitset containing the units. Note This does not include units that are loaded into transports, Bunkers, Refineries, Assimilators, or Extractors. Example usage: Unitset myUnits = BWAPI::Broodwar->self()->getUnits(); for ( auto u = myUnits.begin(); u != myUnits.end(); ++u ) { // Do something with your units }
*/
    public List<Unit> getUnits() {
        return getUnits_native(pointer);
    }

/**
Retrieves the race of the player. This allows you to change strategies against different races, or generalize some commands for yourself. Return values Races::Unknown If the player chose Races::Random when the game started and they have not been seen. Returns The Race that the player is using. Example usage: if ( BWAPI::Broodwar->enemy() ) { BWAPI::Race enemyRace = BWAPI::Broodwar->enemy()->getRace(); if ( enemyRace == Races::Zerg ) BWAPI::Broodwar->sendText("Do you really think you can beat me with a zergling rush?"); }
*/
    public Race getRace() {
        return getRace_native(pointer);
    }

/**
Retrieves the player's controller type. This allows you to distinguish betweeen computer and human players. Returns The PlayerType that identifies who is controlling a player. Note Other players using BWAPI will be treated as a human player and return PlayerTypes::Player. if ( BWAPI::Broodwar->enemy() ) { if ( BWAPI::Broodwar->enemy()->getType() == PlayerTypes::Computer ) BWAPI::Broodwar << "Looks like something I can abuse!" << std::endl; }
*/
    public PlayerType getType() {
        return getType_native(pointer);
    }

/**
Retrieves the player's force. A force is the team that the player is playing on. Returns The Force object that the player is part of.
*/
    public Force getForce() {
        return getForce_native(pointer);
    }

/**
Checks if this player is allied to the specified player. Parameters player The player to check alliance with. Return values true if this player is allied with player . false if this player is not allied with player. Note This function will also return false if this player is neutral or an observer, or if player is neutral or an observer. See also isEnemy
*/
    public boolean isAlly(Player player) {
        return isAlly_native(pointer, player);
    }

/**
Checks if this player is unallied to the specified player. Parameters player The player to check alliance with. Return values true if this player is allied with player . false if this player is not allied with player . Note This function will also return false if this player is neutral or an observer, or if player is neutral or an observer. See also isAlly
*/
//    public boolean isEnemy(Player player) {
//        return isEnemy_native(pointer, player);
//    }

/**
Checks if this player is the neutral player. Return values true if this player is the neutral player. false if this player is any other player.
*/
    public boolean isNeutral() {
        return isNeutral_native(pointer);
    }

/**
Retrieve's the player's starting location. Returns A TilePosition containing the position of the start location. Return values TilePositions::None if the player does not have a start location. TilePositions::Unknown if an error occured while trying to retrieve the start location. See also Game::getStartLocations, Game::getLastError
*/
    public TilePosition getStartLocation() {
        return getStartLocation_native(pointer);
    }

/**
Checks if the player has achieved victory. Returns true if this player has achieved victory, otherwise false
*/
    public boolean isVictorious() {
        return isVictorious_native(pointer);
    }

/**
Checks if the player has been defeated. Returns true if the player is defeated, otherwise false
*/
    public boolean isDefeated() {
        return isDefeated_native(pointer);
    }

/**
Checks if the player has left the game. Returns true if the player has left the game, otherwise false
*/
    public boolean leftGame() {
        return leftGame_native(pointer);
    }

/**
Retrieves the current amount of minerals/ore that this player has. Note This function will return 0 if the player is inaccessible. Returns Amount of minerals that the player currently has for spending.
*/
    public int minerals() {
        return minerals_native(pointer);
    }

/**
Retrieves the current amount of vespene gas that this player has. Note This function will return 0 if the player is inaccessible. Returns Amount of gas that the player currently has for spending.
*/
    public int gas() {
        return gas_native(pointer);
    }

/**
Retrieves the cumulative amount of minerals/ore that this player has gathered since the beginning of the game, including the amount that the player starts the game with (if any). Note This function will return 0 if the player is inaccessible. Returns Cumulative amount of minerals that the player has gathered.
*/
    public int gatheredMinerals() {
        return gatheredMinerals_native(pointer);
    }

/**
Retrieves the cumulative amount of vespene gas that this player has gathered since the beginning of the game, including the amount that the player starts the game with (if any). Note This function will return 0 if the player is inaccessible. Returns Cumulative amount of gas that the player has gathered.
*/
    public int gatheredGas() {
        return gatheredGas_native(pointer);
    }

/**
Retrieves the cumulative amount of minerals/ore that this player has spent on repairing units since the beginning of the game. This function only applies to Terran players. Note This function will return 0 if the player is inaccessible. Returns Cumulative amount of minerals that the player has spent repairing.
*/
    public int repairedMinerals() {
        return repairedMinerals_native(pointer);
    }

/**
Retrieves the cumulative amount of vespene gas that this player has spent on repairing units since the beginning of the game. This function only applies to Terran players. Note This function will return 0 if the player is inaccessible. Returns Cumulative amount of gas that the player has spent repairing.
*/
    public int repairedGas() {
        return repairedGas_native(pointer);
    }

/**
Retrieves the cumulative amount of minerals/ore that this player has gained from refunding (cancelling) units and structures. Note This function will return 0 if the player is inaccessible. Returns Cumulative amount of minerals that the player has received from refunds.
*/
    public int refundedMinerals() {
        return refundedMinerals_native(pointer);
    }

/**
Retrieves the cumulative amount of vespene gas that this player has gained from refunding (cancelling) units and structures. Note This function will return 0 if the player is inaccessible. Returns Cumulative amount of gas that the player has received from refunds.
*/
    public int refundedGas() {
        return refundedGas_native(pointer);
    }

/**
Retrieves the cumulative amount of minerals/ore that this player has spent, excluding repairs. Note This function will return 0 if the player is inaccessible. Returns Cumulative amount of minerals that the player has spent.
*/
    public int spentMinerals() {
        return spentMinerals_native(pointer);
    }

/**
Retrieves the cumulative amount of vespene gas that this player has spent, excluding repairs. Note This function will return 0 if the player is inaccessible. Returns Cumulative amount of gas that the player has spent.
*/
    public int spentGas() {
        return spentGas_native(pointer);
    }

/**
Retrieves the total amount of supply the player has available for unit control. Note In Starcraft programming, the managed supply values are double than what they appear in the game. The reason for this is because Zerglings use 0.5 visible supply. In Starcraft, the supply for each race is separate. Having a Pylon and an Overlord will not give you 32 supply. It will instead give you 16 Protoss supply and 16 Zerg supply. Parameters race (optional) The race to query the total supply for. If this is omitted, then the player's current race will be used. Returns The total supply available for this player and the given race. Example usage: if ( BWAPI::Broodwar->self()->supplyUsed() + 8 >= BWAPI::Broodwar->self()->supplyTotal() ) { // Construct pylons, supply depots, or overlords } See also supplyUsed
*/
    public int supplyTotal() {
        return supplyTotal_native(pointer);
    }

    public int supplyTotal(Race race) {
        return supplyTotal_native(pointer, race);
    }

/**
Retrieves the current amount of supply that the player is using for unit control. Parameters race (optional) The race to query the used supply for. If this is omitted, then the player's current race will be used. Returns The supply that is in use for this player and the given race. See also supplyTotal
*/
    public int supplyUsed() {
        return supplyUsed_native(pointer);
    }

    public int supplyUsed(Race race) {
        return supplyUsed_native(pointer, race);
    }

/**
Retrieves the total number of units that the player has. If the information about the player is limited, then this function will only return the number of visible units. Parameters unit (optional) The unit type to query. UnitType macros are accepted. If this parameter is omitted, then it will use UnitTypes::AllUnits by default. Returns The total number of units of the given type that the player owns. See also visibleUnitCount, completedUnitCount, incompleteUnitCount
*/
    public int allUnitCount() {
        return allUnitCount_native(pointer);
    }

    public int allUnitCount(UnitType unit) {
        return allUnitCount_native(pointer, unit);
    }

/**
Retrieves the total number of strictly visible units that the player has, even if information on the player is unrestricted. Parameters unit (optional) The unit type to query. UnitType macros are accepted. If this parameter is omitted, then it will use UnitTypes::AllUnits by default. Returns The total number of units of the given type that the player owns, and is visible to the BWAPI player. See also allUnitCount, completedUnitCount, incompleteUnitCount
*/
    public int visibleUnitCount() {
        return visibleUnitCount_native(pointer);
    }

    public int visibleUnitCount(UnitType unit) {
        return visibleUnitCount_native(pointer, unit);
    }

/**
Retrieves the number of completed units that the player has. If the information about the player is limited, then this function will only return the number of visible completed units. Parameters unit (optional) The unit type to query. UnitType macros are accepted. If this parameter is omitted, then it will use UnitTypes::AllUnits by default. Returns The number of completed units of the given type that the player owns. Example usage: bool obtainNextUpgrade(BWAPI::UpgradeType upgType) { BWAPI::Player self = BWAPI::Broodwar->self(); int maxLvl = self->getMaxUpgradeLevel(upgType); int currentLvl = self->getUpgradeLevel(upgType); if ( !self->isUpgrading(upgType) && currentLvl < maxLvl && self->completedUnitCount(upgType.whatsRequired(currentLvl+1)) > 0 && self->completedUnitCount(upgType.whatUpgrades()) > 0 ) return self->getUnits().upgrade(upgType); return false; } See also allUnitCount, visibleUnitCount, incompleteUnitCount
*/
    public int completedUnitCount() {
        return completedUnitCount_native(pointer);
    }

    public int completedUnitCount(UnitType unit) {
        return completedUnitCount_native(pointer, unit);
    }

/**
Retrieves the number of incomplete units that the player has. If the information about the player is limited, then this function will only return the number of visible incomplete units. Note This function is a macro for allUnitCount() - completedUnitCount(). Parameters unit (optional) The unit type to query. UnitType macros are accepted. If this parameter is omitted, then it will use UnitTypes::AllUnits by default. Returns The number of incomplete units of the given type that the player owns. See also allUnitCount, visibleUnitCount, completedUnitCount
*/
    public int incompleteUnitCount() {
        return incompleteUnitCount_native(pointer);
    }

    public int incompleteUnitCount(UnitType unit) {
        return incompleteUnitCount_native(pointer, unit);
    }

/**
Retrieves the number units that have died for this player. Parameters unit (optional) The unit type to query. UnitType macros are accepted. If this parameter is omitted, then it will use UnitTypes::AllUnits by default. Returns The total number of units that have died throughout the game.
*/
    public int deadUnitCount() {
        return deadUnitCount_native(pointer);
    }

    public int deadUnitCount(UnitType unit) {
        return deadUnitCount_native(pointer, unit);
    }

/**
Retrieves the number units that the player has killed. Parameters unit (optional) The unit type to query. UnitType macros are accepted. If this parameter is omitted, then it will use UnitTypes::AllUnits by default. Returns The total number of units that the player has killed throughout the game.
*/
    public int killedUnitCount() {
        return killedUnitCount_native(pointer);
    }

    public int killedUnitCount(UnitType unit) {
        return killedUnitCount_native(pointer, unit);
    }

/**
Retrieves the current upgrade level that the player has attained for a given upgrade type. Parameters upgrade The UpgradeType to query. Returns The number of levels that the upgrade has been upgraded for this player. Example usage: bool obtainNextUpgrade(BWAPI::UpgradeType upgType) { BWAPI::Player self = BWAPI::Broodwar->self(); int maxLvl = self->getMaxUpgradeLevel(upgType); int currentLvl = self->getUpgradeLevel(upgType); if ( !self->isUpgrading(upgType) && currentLvl < maxLvl && self->completedUnitCount(upgType.whatsRequired(currentLvl+1)) > 0 && self->completedUnitCount(upgType.whatUpgrades()) > 0 ) return self->getUnits().upgrade(upgType); return false; } See also UnitInterface::upgrade, getMaxUpgradeLevel
*/
    public int getUpgradeLevel(UpgradeType upgrade) {
        return getUpgradeLevel_native(pointer, upgrade);
    }

/**
Checks if the player has already researched a given technology. Parameters tech The TechType to query. Returns true if the player has obtained the given tech, or false if they have not See also isResearching, UnitInterface::research, isResearchAvailable
*/
    public boolean hasResearched(TechType tech) {
        return hasResearched_native(pointer, tech);
    }

/**
Checks if the player is researching a given technology type. Parameters tech The TechType to query. Returns true if the player is currently researching the tech, or false otherwise See also UnitInterface::research, hasResearched
*/
    public boolean isResearching(TechType tech) {
        return isResearching_native(pointer, tech);
    }

/**
Checks if the player is upgrading a given upgrade type. Parameters upgrade The upgrade type to query. Returns true if the player is currently upgrading the given upgrade, false otherwise Example usage: bool obtainNextUpgrade(BWAPI::UpgradeType upgType) { BWAPI::Player self = BWAPI::Broodwar->self(); int maxLvl = self->getMaxUpgradeLevel(upgType); int currentLvl = self->getUpgradeLevel(upgType); if ( !self->isUpgrading(upgType) && currentLvl < maxLvl && self->completedUnitCount(upgType.whatsRequired(currentLvl+1)) > 0 && self->completedUnitCount(upgType.whatUpgrades()) > 0 ) return self->getUnits().upgrade(upgType); return false; } See also UnitInterface::upgrade
*/
    public boolean isUpgrading(UpgradeType upgrade) {
        return isUpgrading_native(pointer, upgrade);
    }

/**
Retrieves the color value of the current player. Returns Color object that represents the color of the current player.
*/
    public Color getColor() {
        return getColor_native(pointer);
    }

/**
Retrieves the control code character that changes the color of text messages to represent this player. Returns character code to use for text in Broodwar.
*/
    public char getTextColor() {
        return getTextColor_native(pointer);
    }

/**
Retrieves the maximum amount of energy that a unit type will have, taking the player's energy upgrades into consideration. Parameters unit The UnitType to retrieve the maximum energy for. Returns Maximum amount of energy that the given unit type can have.
*/
    public int maxEnergy(UnitType unit) {
        return maxEnergy_native(pointer, unit);
    }

/**
Retrieves the top speed of a unit type, taking the player's speed upgrades into consideration. Parameters unit The UnitType to retrieve the top speed for. Returns Top speed of the provided unit type for this player.
*/
    public double topSpeed(UnitType unit) {
        return topSpeed_native(pointer, unit);
    }

/**
Retrieves the maximum weapon range of a weapon type, taking the player's weapon upgrades into consideration. Parameters weapon The WeaponType to retrieve the maximum range for. Returns Maximum range of the given weapon type for units owned by this player.
*/
    public int weaponMaxRange(WeaponType weapon) {
        return weaponMaxRange_native(pointer, weapon);
    }

/**
Retrieves the sight range of a unit type, taking the player's sight range upgrades into consideration. Parameters unit The UnitType to retrieve the sight range for. Returns Sight range of the provided unit type for this player.
*/
    public int sightRange(UnitType unit) {
        return sightRange_native(pointer, unit);
    }

/**
Retrieves the weapon cooldown of a unit type, taking the player's attack speed upgrades into consideration. Parameters unit The UnitType to retrieve the damage cooldown for. Returns Weapon cooldown of the provided unit type for this player.
*/
    public int weaponDamageCooldown(UnitType unit) {
        return weaponDamageCooldown_native(pointer, unit);
    }

/**
Calculates the armor that a given unit type will have, including upgrades. Parameters unit The unit type to calculate armor for, using the current player's upgrades. Returns The amount of armor that the unit will have with the player's upgrades.
*/
    public int armor(UnitType unit) {
        return armor_native(pointer, unit);
    }

/**
Calculates the damage that a given weapon type can deal, including upgrades. Parameters wpn The weapon type to calculate for. Returns The amount of damage that the weapon deals with this player's upgrades.
*/
    public int damage(WeaponType wpn) {
        return damage_native(pointer, wpn);
    }

/**
Retrieves the total unit score, as seen in the end-game score screen. Returns The player's unit score.
*/
    public int getUnitScore() {
        return getUnitScore_native(pointer);
    }

/**
Retrieves the total kill score, as seen in the end-game score screen. Returns The player's kill score.
*/
    public int getKillScore() {
        return getKillScore_native(pointer);
    }

/**
Retrieves the total building score, as seen in the end-game score screen. Returns The player's building score.
*/
    public int getBuildingScore() {
        return getBuildingScore_native(pointer);
    }

/**
Retrieves the total razing score, as seen in the end-game score screen. Returns The player's razing score.
*/
    public int getRazingScore() {
        return getRazingScore_native(pointer);
    }

/**
Retrieves the player's custom score. This score is used in Use Map Settings game types. Returns The player's custom score.
*/
    public int getCustomScore() {
        return getCustomScore_native(pointer);
    }

/**
Checks if the player is an observer player, typically in a Use Map Settings observer game. An observer player does not participate in the game. Returns true if the player is observing, or false if the player is capable of playing in the game.
*/
    public boolean isObserver() {
        return isObserver_native(pointer);
    }

/**
Retrieves the maximum upgrades available specific to the player. This value is only different from UpgradeType::maxRepeats in Use Map Settings games. Parameters upgrade The UpgradeType to retrieve the maximum upgrade level for. Returns Maximum upgrade level of the given upgrade type. Example usage: bool obtainNextUpgrade(BWAPI::UpgradeType upgType) { BWAPI::Player self = BWAPI::Broodwar->self(); int maxLvl = self->getMaxUpgradeLevel(upgType); int currentLvl = self->getUpgradeLevel(upgType); if ( !self->isUpgrading(upgType) && currentLvl < maxLvl && self->completedUnitCount(upgType.whatsRequired(currentLvl+1)) > 0 && self->completedUnitCount(upgType.whatUpgrades()) > 0 ) return self->getUnits().upgrade(upgType); return false; }
*/
    public int getMaxUpgradeLevel(UpgradeType upgrade) {
        return getMaxUpgradeLevel_native(pointer, upgrade);
    }

/**
Checks if a technology can be researched by the player. Certain technologies may be disabled in Use Map Settings game types. Parameters tech The TechType to query. Returns true if the tech type is available to the player for research.
*/
    public boolean isResearchAvailable(TechType tech) {
        return isResearchAvailable_native(pointer, tech);
    }

/**
Checks if a unit type can be created by the player. Certain unit types may be disabled in Use Map Settings game types. Parameters unit The UnitType to check. Returns true if the unit type is available to the player.
*/
    public boolean isUnitAvailable(UnitType unit) {
        return isUnitAvailable_native(pointer, unit);
    }

/**
Verifies that this player satisfies a unit type requirement. This verifies complex type requirements involving morphable Zerg structures. For example, if something requires a Spire, but the player has (or is in the process of morphing) a Greater Spire, this function will identify the requirement. It is simply a convenience function that performs all of the requirement checks. Parameters unit The UnitType to check. amount (optional) The amount of units that are required. Returns true if the unit type requirements are met, and false otherwise. Since 4.1.2
*/
    public boolean hasUnitTypeRequirement(UnitType unit) {
        return hasUnitTypeRequirement_native(pointer, unit);
    }

    public boolean hasUnitTypeRequirement(UnitType unit, int amount) {
        return hasUnitTypeRequirement_native(pointer, unit, amount);
    }


    private static Map<Long, Player> instances = new HashMap<Long, Player>();

    private Player(long pointer) {
        this.pointer = pointer;
    }

    private static Player get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Player instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Player(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native int getID_native(long pointer);

    private native String getName_native(long pointer);

    private native List<Unit> getUnits_native(long pointer);

    private native Race getRace_native(long pointer);

    private native PlayerType getType_native(long pointer);

    private native Force getForce_native(long pointer);

    private native boolean isAlly_native(long pointer, Player player);

    private native boolean isEnemy_native(long pointer, Player player);

    private native boolean isNeutral_native(long pointer);

    private native TilePosition getStartLocation_native(long pointer);

    private native boolean isVictorious_native(long pointer);

    private native boolean isDefeated_native(long pointer);

    private native boolean leftGame_native(long pointer);

    private native int minerals_native(long pointer);

    private native int gas_native(long pointer);

    private native int gatheredMinerals_native(long pointer);

    private native int gatheredGas_native(long pointer);

    private native int repairedMinerals_native(long pointer);

    private native int repairedGas_native(long pointer);

    private native int refundedMinerals_native(long pointer);

    private native int refundedGas_native(long pointer);

    private native int spentMinerals_native(long pointer);

    private native int spentGas_native(long pointer);

    private native int supplyTotal_native(long pointer);

    private native int supplyTotal_native(long pointer, Race race);

    private native int supplyUsed_native(long pointer);

    private native int supplyUsed_native(long pointer, Race race);

    private native int allUnitCount_native(long pointer);

    private native int allUnitCount_native(long pointer, UnitType unit);

    private native int visibleUnitCount_native(long pointer);

    private native int visibleUnitCount_native(long pointer, UnitType unit);

    private native int completedUnitCount_native(long pointer);

    private native int completedUnitCount_native(long pointer, UnitType unit);

    private native int incompleteUnitCount_native(long pointer);

    private native int incompleteUnitCount_native(long pointer, UnitType unit);

    private native int deadUnitCount_native(long pointer);

    private native int deadUnitCount_native(long pointer, UnitType unit);

    private native int killedUnitCount_native(long pointer);

    private native int killedUnitCount_native(long pointer, UnitType unit);

    private native int getUpgradeLevel_native(long pointer, UpgradeType upgrade);

    private native boolean hasResearched_native(long pointer, TechType tech);

    private native boolean isResearching_native(long pointer, TechType tech);

    private native boolean isUpgrading_native(long pointer, UpgradeType upgrade);

    private native Color getColor_native(long pointer);

    private native char getTextColor_native(long pointer);

    private native int maxEnergy_native(long pointer, UnitType unit);

    private native double topSpeed_native(long pointer, UnitType unit);

    private native int weaponMaxRange_native(long pointer, WeaponType weapon);

    private native int sightRange_native(long pointer, UnitType unit);

    private native int weaponDamageCooldown_native(long pointer, UnitType unit);

    private native int armor_native(long pointer, UnitType unit);

    private native int damage_native(long pointer, WeaponType wpn);

    private native int getUnitScore_native(long pointer);

    private native int getKillScore_native(long pointer);

    private native int getBuildingScore_native(long pointer);

    private native int getRazingScore_native(long pointer);

    private native int getCustomScore_native(long pointer);

    private native boolean isObserver_native(long pointer);

    private native int getMaxUpgradeLevel_native(long pointer, UpgradeType upgrade);

    private native boolean isResearchAvailable_native(long pointer, TechType tech);

    private native boolean isUnitAvailable_native(long pointer, UnitType unit);

    private native boolean hasUnitTypeRequirement_native(long pointer, UnitType unit);

    private native boolean hasUnitTypeRequirement_native(long pointer, UnitType unit, int amount);

    // =========================================================
    // ===== Start of ATLANTIS CODE ============================
    // =========================================================
    
    /**
     * Returns true if this is our enemy.
     */
    public boolean isEnemy() {
        return getID() != AtlantisGame.getPlayerUs().getID();
    }
    
}
