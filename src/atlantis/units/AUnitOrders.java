package atlantis.units;

import atlantis.AGame;
import atlantis.position.APosition;
import atlantis.units.actions.UnitAction;
import atlantis.units.actions.UnitActions;
import bwapi.Position;
import bwapi.PositionOrUnit;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UpgradeType;

/**
 * Class using default methods which are extracted from AUnit class to separate this functionality.
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public interface AUnitOrders {

    Unit u();

    AUnit unit();
    
    // =========================================================
    
    default boolean attackUnit(AUnit target) {
        if (!unit().hasRangeToAttack(target, 0.05)) {
            unit().setTooltip("Come closer!");
            move(target.getPosition(), UnitActions.ATTACK_UNIT);
            return false;
        }

        unit().setUnitAction(UnitActions.ATTACK_UNIT);

        // Do NOT issue double orders
        if (unit().isUnitAction(UnitActions.ATTACK_UNIT)
                && u().getTarget() != null && unit().getTarget().equals(target)) {
            return true;
        } else {
//            System.out.println();
//            System.out.println("unit().isJustShooting() = " + unit().isJustShooting());
//            System.out.println("unit().isAttacking() = " + unit().isAttacking());
//            System.out.println("getTarget = " + unit().getTarget());
//            System.out.println(unit().getID() + " attacks " + target.getShortName());
//            AGame.sendMessage("#" + unit().getID() + " attacks #" + target.getID());
            u().attack(target.u());
            unit().setOrderWasIssued();
            return true;
        }
    }

    default boolean attackPosition(APosition target) {

        // Do NOT issue double orders
        if (unit().isUnitAction(UnitActions.ATTACK_POSITION)
                && u().getTargetPosition() != null && unit().getTargetPosition().equals(target)) {
            unit().setUnitAction(UnitActions.ATTACK_POSITION);
            return true;
        } else {
            u().attack(target);
            unit().setUnitAction(UnitActions.ATTACK_POSITION);
            unit().setLastUnitActionNow();
            return true;
        }
    }

    default boolean train(AUnitType unitToTrain) {
        unit().setUnitAction(UnitActions.TRAIN);
        return u().train(unitToTrain.ut());
    }

    default boolean morph(AUnitType into) {
        unit().setUnitAction(UnitActions.MORPH);
        return u().morph(into.ut());
    }

    default boolean build(AUnitType buildingType, TilePosition buildTilePosition, UnitAction unitAction) {
        unit().setUnitAction(UnitActions.BUILD);
        boolean result = u().build(buildingType.ut(), buildTilePosition);
        unit().setTooltip("Construct " + buildingType.getShortName());
        unit().setLastUnitActionNow();
        return result;
    }

    default boolean buildAddon(AUnitType addon) {
        unit().setUnitAction(UnitActions.BUILD);
        return u().buildAddon(addon.ut());
    }

    default boolean upgrade(UpgradeType upgrade) {
        unit().setUnitAction(UnitActions.RESEARCH_OR_UPGRADE);
        return u().upgrade(upgrade);
    }

    default boolean research(TechType tech) {
        unit().setUnitAction(UnitActions.RESEARCH_OR_UPGRADE);
        return u().research(tech);
    }

    default boolean move(Position target, UnitAction unitAction) {
        if (target == null) {
            System.err.println("Null move position for " + this);
            return false;
        }
        
        unit().setUnitAction(unitAction);
        
        // === Handle LOADED/SIEGED units ========================================
        
        if (unit().isLoaded()) {
            unit().unload(unit());
            return true;
        }
        else if (unit().isSieged()) {
            unit().unsiege();
            return true;
        }
            
        // =========================================================

//        if (u().isMoving() && u().getTargetPosition() != null && !u().getTargetPosition().equals(target)) {
        
//        if (unit().isMoving() && AGame.getTimeFrames() % 4 != 0) {
//            return true;
//        }
//        
//        if (!unit().isUnitActionMove() || !target.equals(u().getTargetPosition()) || !u().isMoving()) {
//            System.out.println(u().getID() + " MOVE at " + AGame.getTimeFrames());
//        if (!unit().isMoving() || AGame.getTimeFrames() % 4 != 0) {
        if (!unit().isUnitActionMove() || AGame.getTimeFrames() % 5 == 0) {
            u().move(target);
            unit().setOrderWasIssued();
            unit().setUnitAction(unitAction);
            return true;
        }
        
        return true;
//        }
//        else {
//            return true;
//        }
    }

    /**
     * Orders the unit to patrol between its current position and the specified position. While patrolling,
     * units will attack and chase enemy units that they encounter, and then return u().to its patrol route.
     * Medics will automatically heal units and then return u().to their patrol route. Parameters target The
     * position to patrol to. shiftQueueCommand (optional) If this value is true, then the order will be
     * queued instead of immediately executed. If this value is omitted, then the order will be executed
     * immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also isPatrolling, canPatrol
     */
    default boolean patrol(APosition target, UnitAction unitAction) {
        unit().setUnitAction(UnitActions.PATROL);
        return u().patrol(target);
    }

    /**
     * Orders the unit to hold its position. Parameters shiftQueueCommand (optional) If this value is true,
     * then the order will be queued instead of immediately executed. If this value is omitted, then the order
     * will be executed immediately by default. Returns true if the command was passed to Broodwar, and false
     * if BWAPI determined that the command would fail. Note There is a small chance for a command to fail
     * after it has been passed to Broodwar. See also canHoldPosition, isHoldingPosition
     */
    default boolean holdPosition() {
        unit().setUnitAction(UnitActions.HOLD_POSITION);
        return u().holdPosition();
    }

    /**
     * Orders the unit to stop. Parameters shiftQueueCommand (optional) If this value is true, then the order
     * will be queued instead of immediately executed. If this value is omitted, then the order will be
     * executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also canStop, isIdle
     */
    default boolean stop() {
        unit().setUnitAction(UnitActions.STOP);
        return u().stop();
    }

    /**
     * Orders the unit to follow the specified unit. Units that are following other units will not perform any
     * other actions such as attacking. They will ignore attackers. Parameters target The target unit to start
     * following. shiftQueueCommand (optional) If this value is true, then the order will be queued instead of
     * immediately executed. If this value is omitted, then the order will be executed immediately by default.
     * Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command
     * would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar.
     * See also isFollowing, canFollow, getOrderTarget
     */
    default boolean follow(AUnit target) {
        unit().setUnitAction(UnitActions.FOLLOW);
        return u().follow(target.u());
    }

    /**
     * Orders the unit to gather the specified unit (must be mineral or refinery type). Parameters target The
     * target unit to gather from. shiftQueueCommand (optional) If this value is true, then the order will be
     * queued instead of immediately executed. If this value is omitted, then the order will be executed
     * immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also isGatheringGas, isGatheringMinerals, canGather
     */
    default boolean gather(AUnit target) {
        if (target.getType().isMineralField()) {
            unit().setUnitAction(UnitActions.GATHER_MINERALS);
        } else {
            unit().setUnitAction(UnitActions.GATHER_GAS);
        }
        unit().setLastUnitActionNow();

        return u().gather(target.u());
    }

    /**
     * Orders the unit to return u().its cargo to a nearby resource depot such as a Command Center. Only
     * workers that are carrying minerals or gas can be ordered to return u().cargo. Parameters
     * shiftQueueCommand (optional) If this value is true, then the order will be queued instead of
     * immediately executed. If this value is omitted, then the order will be executed immediately by default.
     * Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command
     * would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar.
     * See also isCarryingGas, isCarryingMinerals, canReturnCargo
     */
    default boolean returnCargo() {
        unit().setUnitAction(UnitActions.MOVE);
        return u().returnCargo();
    }

    /**
     * Orders the unit to repair the specified unit. Only Terran SCVs can be ordered to repair, and the target
     * must be a mechanical Terran unit or building. Parameters target The unit to repair. shiftQueueCommand
     * (optional) If this value is true, then the order will be queued instead of immediately executed. If
     * this value is omitted, then the order will be executed immediately by default. Returns true if the
     * command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There
     * is a small chance for a command to fail after it has been passed to Broodwar. See also isRepairing,
     * canRepair
     */
    default boolean repair(AUnit target) {
        if (target != null && target.distanceTo(unit()) > 0.5) {
            return unit().move(target.getPosition(), UnitActions.MOVE_TO_REPAIR);
        }
        else {
            unit().setUnitAction(UnitActions.REPAIR);
            if (unit().getTarget() == null || !unit().getTarget().equals(target) || !unit().isRepairing()) {
                unit().setLastUnitActionNow();
                return u().repair(target.u());
            }
            else {
                return true;
            }
        }
    }

    /**
     * Orders the unit to burrow. Either the unit must be a Lurker, or the unit must be a Zerg ground unit
     * that is capable of Burrowing, and Burrow technology must be researched. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also unburrow, isBurrowed,
     * canBurrow
     */
    default boolean burrow() {
        unit().setUnitAction(UnitActions.BURROW);
        return u().burrow();
    }

    /**
     * Orders a burrowed unit to unburrow. Returns true if the command was passed to Broodwar, and false if
     * BWAPI determined that the command would fail. Note There is a small chance for a command to fail after
     * it has been passed to Broodwar. See also burrow, isBurrowed, canUnburrow
     */
    default boolean unburrow() {
        unit().setUnitAction(UnitActions.UNBURROW);
        return u().unburrow();
    }

    /**
     * Orders the unit to cloak. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also decloak, isCloaked, canCloak
     */
    default boolean cloak() {
        unit().setUnitAction(UnitActions.CLOAK);
        return u().cloak();
    }

    /**
     * Orders a cloaked unit to decloak. Returns true if the command was passed to Broodwar, and false if
     * BWAPI determined that the command would fail. Note There is a small chance for a command to fail after
     * it has been passed to Broodwar. See also cloak, isCloaked, canDecloak
     */
    default boolean decloak() {
        unit().setUnitAction(UnitActions.LOAD);
        return u().decloak();
    }

    /**
     * Orders the unit to siege. Only works for Siege Tanks. Returns true if the command was passed to
     * Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a
     * command to fail after it has been passed to Broodwar. See also unsiege, isSieged, canSiege
     */
    default boolean siege() {
        unit().setUnitAction(UnitActions.SIEGE);
        unit().setLastUnitActionNow();
        return u().siege();
    }

    /**
     * Orders the unit to unsiege. Only works for sieged Siege Tanks. Returns true if the command was passed
     * to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance
     * for a command to fail after it has been passed to Broodwar. See also siege, isSieged, canUnsiege
     */
    default boolean unsiege() {
        unit().setUnitAction(UnitActions.UNSIEGE);
        unit().setLastUnitActionNow();
        return u().unsiege();
    }

    /**
     * Returns false if unit can't stop what it's doing e.g. sieging or unsieging tank.
     */
    default boolean isInterruptible() {
        return u().isInterruptible();
    }

    /**
     * Orders the unit to lift. Only works for liftable Terran structures. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also land, isLifted, canLift
     */
    default boolean lift() {
        unit().setUnitAction(UnitActions.LIFT);
        return u().lift();
    }

    /**
     * Orders the unit to land. Only works for Terran structures that are currently lifted. Parameters target
     * The tile position to land this structure at. Returns true if the command was passed to Broodwar, and
     * false if BWAPI determined that the command would fail. Note There is a small chance for a command to
     * fail after it has been passed to Broodwar. See also lift, isLifted, canLand
     */
    default boolean land(TilePosition target) {
        unit().setUnitAction(UnitActions.LAND);
        return u().land(target);
    }

    /**
     * Orders the unit to load the target unit. Only works if this unit is a Transport(Dropship, Shuttle,
     * Overlord ) or Bunker type. Parameters target The target unit to load into this Transport(Dropship,
     * Shuttle, Overlord ) or Bunker. shiftQueueCommand (optional) If this value is true, then the order will
     * be queued instead of immediately executed. If this value is omitted, then the order will be executed
     * immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also unload, unloadAll, getLoadedUnits, isLoaded
     */
    default boolean load(AUnit target) {
        unit().setUnitAction(UnitActions.LOAD);
        unit().setLastUnitActionNow();
        return u().load(target.u());
    }

    /**
     * Orders the unit to unload the target unit. Only works for Transports(Dropships, Shuttles, Overlords )
     * and Bunkers. Parameters target Unloads the target unit from this Transport(Dropship, Shuttle, Overlord
     * ) or Bunker. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the
     * command would fail. Note There is a small chance for a command to fail after it has been passed to
     * Broodwar. See also load, unloadAll, getLoadedUnits, isLoaded, canUnload, canUnloadAtPosition
     */
    default boolean unload(AUnit target) {
        unit().setUnitAction(UnitActions.UNLOAD);
        unit().setLastUnitActionNow();
        return u().unload(target.u());
    }

    /**
     * Orders the unit to unload all loaded units at the unit's current position. Only works for
     * Transports(Dropships, Shuttles, Overlords ) and Bunkers. Parameters shiftQueueCommand (optional) If
     * this value is true, then the order will be queued instead of immediately executed. If this value is
     * omitted, then the order will be executed immediately by default. Returns true if the command was passed
     * to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance
     * for a command to fail after it has been passed to Broodwar. See also load, unload, getLoadedUnits,
     * isLoaded, canUnloadAll, canUnloadAtPosition
     */
    default boolean unloadAll() {
        unit().setUnitAction(UnitActions.UNLOAD);
        unit().setLastUnitActionNow();
        return u().unloadAll();
    }

    /**
     * Orders the unit to unload all loaded units at the unit's current position. Only works for
     * Transports(Dropships, Shuttles, Overlords ) and Bunkers. Parameters shiftQueueCommand (optional) If
     * this value is true, then the order will be queued instead of immediately executed. If this value is
     * omitted, then the order will be executed immediately by default. Returns true if the command was passed
     * to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance
     * for a command to fail after it has been passed to Broodwar. See also load, unload, getLoadedUnits,
     * isLoaded, canUnloadAll, canUnloadAtPosition
     */
    default boolean unloadAll(APosition target) {
        unit().setUnitAction(UnitActions.UNLOAD);
        unit().setLastUnitActionNow();
        return u().unloadAll(target);
    }

    /**
     * AVOID USAGE cause it's very tough to debug, don't really know what's it is doing!
     */
//    default boolean rightClick(AUnit target) {
//        if (target != null) {
//            if (!target.u().equals(u().getTarget())) {
//                unit().setUnitAction(UnitActions.RIGHT_CLICK);
//                boolean result = u().rightClick(target.u());
//            }
//            return true;
//        } else {
//            return false;
//        }
//    }

//    default boolean rightClick(PositionOrUnit target) {
//        return u().rightClick(target);
//    }
    
    /**
     * Orders a SCV to stop constructing a structure. This leaves the structure in an incomplete state until
     * it is either cancelled, razed, or completed by another SCV. Returns true if the command was passed to
     * Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a
     * command to fail after it has been passed to Broodwar. See also isConstructing, canHaltConstruction
     */
    default boolean haltConstruction() {
        unit().setUnitAction(null);
        return u().haltConstruction();
    }

    /**
     * Orders this unit to cancel and refund itself from begin constructed. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also isBeingConstructed, build,
     * canCancelConstruction
     */
    default boolean cancelConstruction() {
        unit().setUnitAction(null);
        return u().cancelConstruction();
    }

    /**
     * Orders this unit to cancel and refund an add-on that is being constructed. Returns true if the command
     * was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a
     * small chance for a command to fail after it has been passed to Broodwar. See also canCancelAddon,
     * buildAddon
     */
    default boolean cancelAddon() {
        unit().setUnitAction(null);
        return u().cancelAddon();
    }

    /**
     * Orders the unit to remove the specified unit from its training queue. Parameters slot (optional)
     * Identifies the slot that will be cancelled. If the specified value is at least 0, then the unit in the
     * corresponding slot from the list provided by getTrainingQueue will be cancelled. If the value is either
     * omitted or -2, then the last slot is cancelled. Note The value of slot is passed directly to Broodwar.
     * Other negative values have no effect. See also train, cancelTrain, isTraining, getTrainingQueue,
     * canCancelTrain, canCancelTrainSlot
     */
    default boolean cancelTrain() {
        unit().setUnitAction(null);
        return u().cancelTrain();
    }

    default boolean cancelTrain(int slot) {
        unit().setUnitAction(null);
        return u().cancelTrain(slot);
    }

    /**
     * Orders this unit to cancel and refund a unit that is morphing. Returns true if the command was passed
     * to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance
     * for a command to fail after it has been passed to Broodwar. See also morph, isMorphing, canCancelMorph
     */
    default boolean cancelMorph() {
        return u().cancelMorph();
    }

    /**
     * Orders this unit to cancel and refund a research that is in progress. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also research, isResearching,
     * getTech, canCancelResearch
     */
    default boolean cancelResearch() {
        return u().cancelResearch();
    }

    /**
     * Orders this unit to cancel and refund an upgrade that is in progress. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also upgrade, isUpgrading,
     * getUpgrade, canCancelUpgrade
     */
    default boolean cancelUpgrade() {
        return u().cancelUpgrade();
    }

    /**
     * Orders the unit to use a technology. Parameters tech The technology type to use. target (optional) If
     * specified, indicates the target location or unit to use the tech on. If unspecified, causes the tech to
     * be used without a target (i.e. Stim Packs). Returns true if the command was passed to Broodwar, and
     * false if BWAPI determined that the command would fail. See also canUseTechWithOrWithoutTarget,
     * canUseTech, canUseTechWithoutTarget, canUseTechUnit, canUseTechPosition, TechTypes
     */
    default boolean useTech(TechType tech) {
        unit().setUnitAction(UnitActions.USING_TECH);
        return u().useTech(tech);
    }

    default boolean useTech(TechType tech, APosition target) {
        unit().setUnitAction(UnitActions.USING_TECH);
        return u().useTech(tech, target);
    }

    default boolean useTech(TechType tech, AUnit target) {
        unit().setUnitAction(UnitActions.USING_TECH);
        return u().useTech(tech, target.u());
    }

    default boolean useTech(TechType tech, PositionOrUnit target) {
        unit().setUnitAction(UnitActions.USING_TECH);
        return u().useTech(tech, target);
    }

}
