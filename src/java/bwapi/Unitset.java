package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
The Unitset is a container for a set of pointers to Unit objects. It is typically used for groups of units instead of having to manage each Unit individually. See also Unit
*/
public class Unitset {

/**
Calculates the average of all valid Unit positions in this set. Returns Average Position of all units in the set. See also UnitInterface::getPosition
*/
    public Position getPosition() {
        return getPosition_native(pointer);
    }

/**
Creates a single set containing all units that are loaded into units of this set. Returns The set of all loaded units. See also UnitInterface::getLoadedUnits
*/
    public List<Unit> getLoadedUnits() {
        return getLoadedUnits_native(pointer);
    }

/**
Creates a single set containing all the Interceptors of all Carriers in this set. Returns The set of all Interceptors . See also UnitInterface::getInterceptors
*/
    public List<Unit> getInterceptors() {
        return getInterceptors_native(pointer);
    }

/**
Creates a single set containing all the Larvae of all Hatcheries, Lairs, and Hives in this set. Returns The set of all Larvae . See also UnitInterface::getLarva
*/
    public List<Unit> getLarva() {
        return getLarva_native(pointer);
    }

/**
Retrieves the set of all units in a given radius of the current unit. Takes into account this unit's dimensions. Can optionally specify a filter that is composed using BWAPI Filter semantics to include only specific units (such as only ground units, etc.) Parameters radius The radius, in pixels, to search for units. pred (optional) The composed function predicate to include only specific (desired) units in the set. Defaults to nullptr, which means no filter. Returns A Unitset containing the set of units that match the given criteria. Example usage: // Get main building closest to start location. BWAPI::Unit pMain = BWAPI::Broodwar->getClosestUnit( BWAPI::Broodwar->self()->getStartLocation(), BWAPI::Filter::IsResourceDepot ); if ( pMain ) // check if pMain is valid { // Get sets of resources and workers BWAPI::Unitset myResources = pMain->getUnitsInRadius(1024, BWAPI::Filter::IsMineralField); if ( !myResources.empty() ) // check if we have resources nearby { BWAPI::Unitset myWorkers = pMain->getUnitsInRadius(512, BWAPI::Filter::IsWorker && BWAPI::Filter::IsIdle && BWAPI::Filter::IsOwned ); while ( !myWorkers.empty() ) // make sure we command all nearby idle workers, if any { for ( auto u = myResources.begin(); u != myResources.end() && !myWorkers.empty(); ++u ) { myWorkers.back()->gather(*u); myWorkers.pop_back(); } } } // myResources not empty } // pMain != nullptr See also getClosestUnit, getUnitsInWeaponRange, Game::getUnitsInRadius, Game::getUnitsInRectangle
*/
    public List<Unit> getUnitsInRadius(int radius) {
        return getUnitsInRadius_native(pointer, radius);
    }

/**
This function issues a command to the unit(s), however it is used for interfacing only, and is recommended to use one of the more specific command functions when writing an AI. Parameters command A UnitCommand containing command parameters such as the type, position, target, etc. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also UnitCommandTypes, Game::getLastError, UnitInterface::canIssueCommand
*/
    public boolean issueCommand(UnitCommand command) {
        return issueCommand_native(pointer, command);
    }

/**
Orders the unit(s) to attack move to the specified position or attack the specified unit. Parameters target A Position or a Unit to designate as the target. If a Position is used, the unit will perform an Attack Move command. shiftQueueCommand (optional) If this value is true, then the order will be queued instead of immediately executed. If this value is omitted, then the order will be executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. A Medic will use Heal Move instead of attack. See also Game::getLastError, UnitInterface::canAttack
*/
    public boolean attack(Position target) {
        return attack_native(pointer, target);
    }

    public boolean attack(Unit target) {
        return attack_native(pointer, target);
    }

    public boolean attack(PositionOrUnit target) {
        return attack_native(pointer, target);
    }

    public boolean attack(Position target, boolean shiftQueueCommand) {
        return attack_native(pointer, target, shiftQueueCommand);
    }

    public boolean attack(Unit target, boolean shiftQueueCommand) {
        return attack_native(pointer, target, shiftQueueCommand);
    }

    public boolean attack(PositionOrUnit target, boolean shiftQueueCommand) {
        return attack_native(pointer, target, shiftQueueCommand);
    }

/**
Orders the worker unit(s) to construct a structure at a target position. Parameters type The UnitType to build. target A TilePosition to specify the build location, specifically the upper-left corner of the location. If the target is not specified, then the function call will be redirected to the train command. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. You must have sufficient resources and meet the necessary requirements in order to build a structure. See also Game::getLastError, UnitInterface::train, UnitInterface::cancelConstruction, UnitInterface::canBuild
*/
    public boolean build(UnitType type) {
        return build_native(pointer, type);
    }

    public boolean build(UnitType type, TilePosition target) {
        return build_native(pointer, type, target);
    }

/**
Orders the Terran structure(s) to construct an add-on. Parameters type The add-on UnitType to construct. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. You must have sufficient resources and meet the necessary requirements in order to build a structure. See also Game::getLastError, UnitInterface::build, UnitInterface::cancelAddon, UnitInterface::canBuildAddon
*/
    public boolean buildAddon(UnitType type) {
        return buildAddon_native(pointer, type);
    }

/**
Orders the unit(s) to add a UnitType to its training queue, or morphs into the UnitType if it is Zerg. Parameters type The UnitType to train. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. You must have sufficient resources, supply, and meet the necessary requirements in order to train a unit. This command is also used for training Interceptors and Scarabs. If you call this using a Hatchery, Lair, or Hive, then it will automatically pass the command to one of its Larvae. See also Game::getLastError, UnitInterface::build, UnitInterface::morph, UnitInterface::cancelTrain, UnitInterface::isTraining, UnitInterface::canTrain
*/
    public boolean train(UnitType type) {
        return train_native(pointer, type);
    }

/**
Orders the unit(s) to morph into a different UnitType. Parameters type The UnitType to morph into. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also Game::getLastError, UnitInterface::build, UnitInterface::morph, UnitInterface::canMorph
*/
    public boolean morph(UnitType type) {
        return morph_native(pointer, type);
    }

/**
Orders the unit to set its rally position to the specified position or unit. Parameters target The target position or target unit that this structure will rally to. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also getRallyPosition, getRallyUnit, canSetRallyPoint, canSetRallyPosition, canSetRallyUnit
*/
    public boolean setRallyPoint(Position target) {
        return setRallyPoint_native(pointer, target);
    }

    public boolean setRallyPoint(Unit target) {
        return setRallyPoint_native(pointer, target);
    }

    public boolean setRallyPoint(PositionOrUnit target) {
        return setRallyPoint_native(pointer, target);
    }

/**
Orders the unit to move from its current position to the specified position. Parameters target The target position to move to. shiftQueueCommand (optional) If this value is true, then the order will be queued instead of immediately executed. If this value is omitted, then the order will be executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also isMoving, canMove
*/
    public boolean move(Position target) {
        return move_native(pointer, target);
    }

    public boolean move(Position target, boolean shiftQueueCommand) {
        return move_native(pointer, target, shiftQueueCommand);
    }

/**
Orders the unit to patrol between its current position and the specified position. While patrolling, units will attack and chase enemy units that they encounter, and then return to its patrol route. Medics will automatically heal units and then return to their patrol route. Parameters target The position to patrol to. shiftQueueCommand (optional) If this value is true, then the order will be queued instead of immediately executed. If this value is omitted, then the order will be executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also isPatrolling, canPatrol
*/
    public boolean patrol(Position target) {
        return patrol_native(pointer, target);
    }

    public boolean patrol(Position target, boolean shiftQueueCommand) {
        return patrol_native(pointer, target, shiftQueueCommand);
    }

/**
Orders the unit to hold its position. Parameters shiftQueueCommand (optional) If this value is true, then the order will be queued instead of immediately executed. If this value is omitted, then the order will be executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also canHoldPosition, isHoldingPosition
*/
    public boolean holdPosition() {
        return holdPosition_native(pointer);
    }

    public boolean holdPosition(boolean shiftQueueCommand) {
        return holdPosition_native(pointer, shiftQueueCommand);
    }

/**
Orders the unit to stop. Parameters shiftQueueCommand (optional) If this value is true, then the order will be queued instead of immediately executed. If this value is omitted, then the order will be executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also canStop, isIdle
*/
    public boolean stop() {
        return stop_native(pointer);
    }

    public boolean stop(boolean shiftQueueCommand) {
        return stop_native(pointer, shiftQueueCommand);
    }

/**
Orders the unit to follow the specified unit. Units that are following other units will not perform any other actions such as attacking. They will ignore attackers. Parameters target The target unit to start following. shiftQueueCommand (optional) If this value is true, then the order will be queued instead of immediately executed. If this value is omitted, then the order will be executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also isFollowing, canFollow, getOrderTarget
*/
    public boolean follow(Unit target) {
        return follow_native(pointer, target);
    }

    public boolean follow(Unit target, boolean shiftQueueCommand) {
        return follow_native(pointer, target, shiftQueueCommand);
    }

/**
Orders the unit to gather the specified unit (must be mineral or refinery type). Parameters target The target unit to gather from. shiftQueueCommand (optional) If this value is true, then the order will be queued instead of immediately executed. If this value is omitted, then the order will be executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also isGatheringGas, isGatheringMinerals, canGather
*/
    public boolean gather(Unit target) {
        return gather_native(pointer, target);
    }

    public boolean gather(Unit target, boolean shiftQueueCommand) {
        return gather_native(pointer, target, shiftQueueCommand);
    }

/**
Orders the unit to return its cargo to a nearby resource depot such as a Command Center. Only workers that are carrying minerals or gas can be ordered to return cargo. Parameters shiftQueueCommand (optional) If this value is true, then the order will be queued instead of immediately executed. If this value is omitted, then the order will be executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also isCarryingGas, isCarryingMinerals, canReturnCargo
*/
    public boolean returnCargo() {
        return returnCargo_native(pointer);
    }

    public boolean returnCargo(boolean shiftQueueCommand) {
        return returnCargo_native(pointer, shiftQueueCommand);
    }

/**
Orders the unit to repair the specified unit. Only Terran SCVs can be ordered to repair, and the target must be a mechanical Terran unit or building. Parameters target The unit to repair. shiftQueueCommand (optional) If this value is true, then the order will be queued instead of immediately executed. If this value is omitted, then the order will be executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also isRepairing, canRepair
*/
    public boolean repair(Unit target) {
        return repair_native(pointer, target);
    }

    public boolean repair(Unit target, boolean shiftQueueCommand) {
        return repair_native(pointer, target, shiftQueueCommand);
    }

/**
Orders the unit to burrow. Either the unit must be a Lurker, or the unit must be a Zerg ground unit that is capable of Burrowing, and Burrow technology must be researched. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also unburrow, isBurrowed, canBurrow
*/
    public boolean burrow() {
        return burrow_native(pointer);
    }

/**
Orders a burrowed unit to unburrow. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also burrow, isBurrowed, canUnburrow
*/
    public boolean unburrow() {
        return unburrow_native(pointer);
    }

/**
Orders the unit to cloak. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also decloak, isCloaked, canCloak
*/
    public boolean cloak() {
        return cloak_native(pointer);
    }

/**
Orders a cloaked unit to decloak. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also cloak, isCloaked, canDecloak
*/
    public boolean decloak() {
        return decloak_native(pointer);
    }

/**
Orders the unit to siege. Only works for Siege Tanks. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also unsiege, isSieged, canSiege
*/
    public boolean siege() {
        return siege_native(pointer);
    }

/**
Orders the unit to unsiege. Only works for sieged Siege Tanks. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also siege, isSieged, canUnsiege
*/
    public boolean unsiege() {
        return unsiege_native(pointer);
    }

/**
Orders the unit to lift. Only works for liftable Terran structures. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also land, isLifted, canLift
*/
    public boolean lift() {
        return lift_native(pointer);
    }

/**
Orders the unit to load the target unit. Only works if this unit is a Transport(Dropship, Shuttle, Overlord ) or Bunker type. Parameters target The target unit to load into this Transport(Dropship, Shuttle, Overlord ) or Bunker. shiftQueueCommand (optional) If this value is true, then the order will be queued instead of immediately executed. If this value is omitted, then the order will be executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also unload, unloadAll, getLoadedUnits, isLoaded
*/
    public boolean load(Unit target) {
        return load_native(pointer, target);
    }

    public boolean load(Unit target, boolean shiftQueueCommand) {
        return load_native(pointer, target, shiftQueueCommand);
    }

/**
Orders the unit to unload all loaded units at the unit's current position. Only works for Transports(Dropships, Shuttles, Overlords ) and Bunkers. Parameters shiftQueueCommand (optional) If this value is true, then the order will be queued instead of immediately executed. If this value is omitted, then the order will be executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also load, unload, getLoadedUnits, isLoaded, canUnloadAll, canUnloadAtPosition
*/
    public boolean unloadAll() {
        return unloadAll_native(pointer);
    }

    public boolean unloadAll(boolean shiftQueueCommand) {
        return unloadAll_native(pointer, shiftQueueCommand);
    }

/**
Orders the unit to unload all loaded units at the unit's current position. Only works for Transports(Dropships, Shuttles, Overlords ) and Bunkers. Parameters shiftQueueCommand (optional) If this value is true, then the order will be queued instead of immediately executed. If this value is omitted, then the order will be executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also load, unload, getLoadedUnits, isLoaded, canUnloadAll, canUnloadAtPosition
*/
    public boolean unloadAll(Position target) {
        return unloadAll_native(pointer, target);
    }

    public boolean unloadAll(Position target, boolean shiftQueueCommand) {
        return unloadAll_native(pointer, target, shiftQueueCommand);
    }

/**
Works like the right click in the GUI. Parameters target The target position or target unit to right click. shiftQueueCommand (optional) If this value is true, then the order will be queued instead of immediately executed. If this value is omitted, then the order will be executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also canRightClick, canRightClickPosition, canRightClickUnit
*/
    public boolean rightClick(Position target) {
        return rightClick_native(pointer, target);
    }

    public boolean rightClick(Unit target) {
        return rightClick_native(pointer, target);
    }

    public boolean rightClick(PositionOrUnit target) {
        return rightClick_native(pointer, target);
    }

    public boolean rightClick(Position target, boolean shiftQueueCommand) {
        return rightClick_native(pointer, target, shiftQueueCommand);
    }

    public boolean rightClick(Unit target, boolean shiftQueueCommand) {
        return rightClick_native(pointer, target, shiftQueueCommand);
    }

    public boolean rightClick(PositionOrUnit target, boolean shiftQueueCommand) {
        return rightClick_native(pointer, target, shiftQueueCommand);
    }

/**
Orders a SCV to stop constructing a structure. This leaves the structure in an incomplete state until it is either cancelled, razed, or completed by another SCV. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also isConstructing, canHaltConstruction
*/
    public boolean haltConstruction() {
        return haltConstruction_native(pointer);
    }

/**
Orders this unit to cancel and refund itself from begin constructed. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also isBeingConstructed, build, canCancelConstruction
*/
    public boolean cancelConstruction() {
        return cancelConstruction_native(pointer);
    }

/**
Orders this unit to cancel and refund an add-on that is being constructed. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also canCancelAddon, buildAddon
*/
    public boolean cancelAddon() {
        return cancelAddon_native(pointer);
    }

/**
Orders the unit to remove the specified unit from its training queue. Parameters slot (optional) Identifies the slot that will be cancelled. If the specified value is at least 0, then the unit in the corresponding slot from the list provided by getTrainingQueue will be cancelled. If the value is either omitted or -2, then the last slot is cancelled. Note The value of slot is passed directly to Broodwar. Other negative values have no effect. See also train, cancelTrain, isTraining, getTrainingQueue, canCancelTrain, canCancelTrainSlot
*/
    public boolean cancelTrain() {
        return cancelTrain_native(pointer);
    }

    public boolean cancelTrain(int slot) {
        return cancelTrain_native(pointer, slot);
    }

/**
Orders this unit to cancel and refund a unit that is morphing. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also morph, isMorphing, canCancelMorph
*/
    public boolean cancelMorph() {
        return cancelMorph_native(pointer);
    }

/**
Orders this unit to cancel and refund a research that is in progress. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also research, isResearching, getTech, canCancelResearch
*/
    public boolean cancelResearch() {
        return cancelResearch_native(pointer);
    }

/**
Orders this unit to cancel and refund an upgrade that is in progress. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar. See also upgrade, isUpgrading, getUpgrade, canCancelUpgrade
*/
    public boolean cancelUpgrade() {
        return cancelUpgrade_native(pointer);
    }

/**
Orders the unit to use a technology. Parameters tech The technology type to use. target (optional) If specified, indicates the target location or unit to use the tech on. If unspecified, causes the tech to be used without a target (i.e. Stim Packs). Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. See also canUseTechWithOrWithoutTarget, canUseTech, canUseTechWithoutTarget, canUseTechUnit, canUseTechPosition, TechTypes
*/
    public boolean useTech(TechType tech) {
        return useTech_native(pointer, tech);
    }

    public boolean useTech(TechType tech, Position target) {
        return useTech_native(pointer, tech, target);
    }

    public boolean useTech(TechType tech, Unit target) {
        return useTech_native(pointer, tech, target);
    }

    public boolean useTech(TechType tech, PositionOrUnit target) {
        return useTech_native(pointer, tech, target);
    }


    private static Map<Long, Unitset> instances = new HashMap<Long, Unitset>();

    private Unitset(long pointer) {
        this.pointer = pointer;
    }

    private static Unitset get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Unitset instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Unitset(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native Position getPosition_native(long pointer);

    private native List<Unit> getLoadedUnits_native(long pointer);

    private native List<Unit> getInterceptors_native(long pointer);

    private native List<Unit> getLarva_native(long pointer);

    private native List<Unit> getUnitsInRadius_native(long pointer, int radius);

    private native boolean issueCommand_native(long pointer, UnitCommand command);

    private native boolean attack_native(long pointer, Position target);

    private native boolean attack_native(long pointer, Unit target);

    private native boolean attack_native(long pointer, PositionOrUnit target);

    private native boolean attack_native(long pointer, Position target, boolean shiftQueueCommand);

    private native boolean attack_native(long pointer, Unit target, boolean shiftQueueCommand);

    private native boolean attack_native(long pointer, PositionOrUnit target, boolean shiftQueueCommand);

    private native boolean build_native(long pointer, UnitType type);

    private native boolean build_native(long pointer, UnitType type, TilePosition target);

    private native boolean buildAddon_native(long pointer, UnitType type);

    private native boolean train_native(long pointer, UnitType type);

    private native boolean morph_native(long pointer, UnitType type);

    private native boolean setRallyPoint_native(long pointer, Position target);

    private native boolean setRallyPoint_native(long pointer, Unit target);

    private native boolean setRallyPoint_native(long pointer, PositionOrUnit target);

    private native boolean move_native(long pointer, Position target);

    private native boolean move_native(long pointer, Position target, boolean shiftQueueCommand);

    private native boolean patrol_native(long pointer, Position target);

    private native boolean patrol_native(long pointer, Position target, boolean shiftQueueCommand);

    private native boolean holdPosition_native(long pointer);

    private native boolean holdPosition_native(long pointer, boolean shiftQueueCommand);

    private native boolean stop_native(long pointer);

    private native boolean stop_native(long pointer, boolean shiftQueueCommand);

    private native boolean follow_native(long pointer, Unit target);

    private native boolean follow_native(long pointer, Unit target, boolean shiftQueueCommand);

    private native boolean gather_native(long pointer, Unit target);

    private native boolean gather_native(long pointer, Unit target, boolean shiftQueueCommand);

    private native boolean returnCargo_native(long pointer);

    private native boolean returnCargo_native(long pointer, boolean shiftQueueCommand);

    private native boolean repair_native(long pointer, Unit target);

    private native boolean repair_native(long pointer, Unit target, boolean shiftQueueCommand);

    private native boolean burrow_native(long pointer);

    private native boolean unburrow_native(long pointer);

    private native boolean cloak_native(long pointer);

    private native boolean decloak_native(long pointer);

    private native boolean siege_native(long pointer);

    private native boolean unsiege_native(long pointer);

    private native boolean lift_native(long pointer);

    private native boolean load_native(long pointer, Unit target);

    private native boolean load_native(long pointer, Unit target, boolean shiftQueueCommand);

    private native boolean unloadAll_native(long pointer);

    private native boolean unloadAll_native(long pointer, boolean shiftQueueCommand);

    private native boolean unloadAll_native(long pointer, Position target);

    private native boolean unloadAll_native(long pointer, Position target, boolean shiftQueueCommand);

    private native boolean rightClick_native(long pointer, Position target);

    private native boolean rightClick_native(long pointer, Unit target);

    private native boolean rightClick_native(long pointer, PositionOrUnit target);

    private native boolean rightClick_native(long pointer, Position target, boolean shiftQueueCommand);

    private native boolean rightClick_native(long pointer, Unit target, boolean shiftQueueCommand);

    private native boolean rightClick_native(long pointer, PositionOrUnit target, boolean shiftQueueCommand);

    private native boolean haltConstruction_native(long pointer);

    private native boolean cancelConstruction_native(long pointer);

    private native boolean cancelAddon_native(long pointer);

    private native boolean cancelTrain_native(long pointer);

    private native boolean cancelTrain_native(long pointer, int slot);

    private native boolean cancelMorph_native(long pointer);

    private native boolean cancelResearch_native(long pointer);

    private native boolean cancelUpgrade_native(long pointer);

    private native boolean useTech_native(long pointer, TechType tech);

    private native boolean useTech_native(long pointer, TechType tech, Position target);

    private native boolean useTech_native(long pointer, TechType tech, Unit target);

    private native boolean useTech_native(long pointer, TechType tech, PositionOrUnit target);


}
