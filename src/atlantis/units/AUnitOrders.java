package atlantis.units;

import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.actions.Action;
import atlantis.units.actions.Actions;
import bwapi.*;

/**
 * Class using default methods which are extracted from AUnit class to separate this functionality.
 */
public interface AUnitOrders {

    Unit u();
    AUnit unit();

    boolean DEBUG = false;
//    boolean DEBUG = true;
    int DEBUG_MIN_FRAMES = 0;

    // =========================================================
    
    default boolean attackUnit(AUnit target) {
        if (DEBUG && A.now() > DEBUG_MIN_FRAMES) {
            System.out.println(
                    "@ @" + A.now() + " ATTACK  / " +
                            "unit#" + unit().id() + " // " +
                            "cooldown " + unit().cooldownRemaining()+ " // " +
                            "attackFrame " + unit()._lastAttackFrame + " // " +
                            "StartingAttack " + unit()._lastStartedAttack + " // " +
                            unit().tooltip()
            );
        }

        if (target == null) {
            System.err.println("Null attack unit target for unit " + this);
            return false;
        }

        // Do NOT issue double orders
        if (unit().isCommand(UnitCommandType.Attack_Unit) && target.equals(unit().target())) {
//            System.out.println("         ** DOUBLE ORDER");
            return true;
        }

//        if (DEBUG && A.now() > DEBUG_MIN_FRAMES) {
//            System.out.println("                  ------> ATTACK #" + target.getID());
//        }
        unit().setAction(Actions.ATTACK_UNIT);
        return u().attack(target.u());
    }

    // To avoid confusion: NEVER UE IT.
    // When moving units always use "Move" mission.
    // Use "Attack" only for targeting actual units.
    /**
     * ONLY TANKS ARE ALLOWED TO USE IT!!!
     */
    default boolean attackPosition(APosition target) {
        if (u().getTargetPosition() != null && !u().getTargetPosition().equals(target)) {
            u().attack(target);
            unit().setAction(Actions.ATTACK_POSITION);
            return true;
        }

        return false;
    }

    default boolean train(AUnitType unitToTrain) {
        unit().setAction(Actions.TRAIN);
        return u().train(unitToTrain.ut());
    }

    default boolean morph(AUnitType into) {
        unit().setAction(Actions.MORPH);
        return u().morph(into.ut());
    }

    default boolean build(AUnitType buildingType, TilePosition buildTilePosition) {
        unit().setAction(Actions.BUILD);
        boolean result = u().build(buildingType.ut(), buildTilePosition);
        unit().setTooltipTactical("Construct " + buildingType.name());
        return result;
    }

    default boolean buildAddon(AUnitType addon) {
        unit().setAction(Actions.BUILD);
        return u().buildAddon(addon.ut());
    }

    default boolean upgrade(UpgradeType upgrade) {
        unit().setAction(Actions.RESEARCH_OR_UPGRADE);
        ATech.markAsBeingUpgraded(upgrade);
        return u().upgrade(upgrade);
    }

    default boolean research(TechType tech) {
        unit().setAction(Actions.RESEARCH_OR_UPGRADE);
        ATech.markAsBeingResearched(tech);
        return u().research(tech);
    }

    default boolean move(AUnit target, Action unitAction, String tooltip, boolean strategicLevel) {
        return move(target.position(), unitAction, tooltip, strategicLevel);
    }

    default boolean moveStrategic(HasPosition target, Action unitAction, String tooltip) {
        return move(target, unitAction, tooltip, true);
    }

    default boolean moveTactical(HasPosition target, Action unitAction, String tooltip) {
        return move(target, unitAction, tooltip, false);
    }

    default boolean move(HasPosition target, Action unitAction, String tooltip, boolean strategicLevel) {
        if (DEBUG && A.now() > DEBUG_MIN_FRAMES) {
            System.out.println("MOVE @" + A.now() + " / unit#" + unit().id() + " // " + tooltip);
        }
        if (target == null) {
            System.err.println("Null move position for " + this);
            return false;
        }

        unit().setTooltip(tooltip, strategicLevel);

//        if (unit().isCommand(UnitCommandType.Move) && target.equals(u().getTargetPosition())) {
//            return true;
//        }
        
        // === Handle LOADED/SIEGED units ========================================
        
//        if (unit().isLoaded()) {
//            unit().unload(unit());
//            unit().setLastUnitOrderNow();
//            return true;
//        }

//        if (unit().isSieged()) {
//            if (unit().lastActionMoreThanAgo(30 * 12, UnitActions.SIEGE) && A.chance(1.5)) {
//                unit().unsiege();
//                unit().setLastUnitOrderNow();
//                return true;
//            } else {
//                return false;
//            }
//        }
            
        // =========================================================

//        if (u().isMoving() && u().getTargetPosition() != null && !u().getTargetPosition().equals(target)) {
//        if (unit().isMoving() && A.now() % 4 != 0) {
//            return true;
//        }
//        
//        if (!unit().isUnitActionMove() || !target.equals(u().getTargetPosition()) || !u().isMoving()) {
//            System.out.println(u().getID() + " MOVE at @" + A.now());
//        if (!unit().isMoving() || A.now() % 4 != 0) {
//        if (!unit().isUnitActionMove() || A.now() % 5 == 0) {
        
        APosition currentTarget = unit().targetPosition();

        if (currentTarget == null || (!currentTarget.equals(target) || unit().lastOrderMinFramesAgo(6))) {
//            if (unit().isFirstCombatUnit()) {
//                System.out.println(A.now() + " move");
//            }
            u().move(target.position());

            unit().setLastActionReceivedNow()
                .setAction(unitAction);
            return true;
        }
        
        return true;
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
    default boolean patrol(APosition target, Action unitAction, String tooltip, boolean strategicLevel) {
        unit().setTooltip(tooltip, strategicLevel)
                .setAction(Actions.PATROL);
        return u().patrol(target);
    }

    /**
     * Orders the unit to hold its position. Parameters shiftQueueCommand (optional) If this value is true,
     * then the order will be queued instead of immediately executed. If this value is omitted, then the order
     * will be executed immediately by default. Returns true if the command was passed to Broodwar, and false
     * if BWAPI determined that the command would fail. Note There is a small chance for a command to fail
     * after it has been passed to Broodwar. See also canHoldPosition, isHoldingPosition
     */
    default boolean holdPosition(String tooltip, boolean strategicLevel) {
        if (DEBUG && A.now() > DEBUG_MIN_FRAMES) {
            System.out.println("HOLD @" + A.now() + " / unit#" + unit().id() + " // " + tooltip);
        }

        unit().setTooltip(tooltip, strategicLevel)
                .setAction(Actions.HOLD_POSITION);
        return u().holdPosition();
    }

    /**
     * Orders the unit to stop. Parameters shiftQueueCommand (optional) If this value is true, then the order
     * will be queued instead of immediately executed. If this value is omitted, then the order will be
     * executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also canStop, isIdle
     */
    default boolean stop(String tooltip, boolean strategicLevel) {
        if (DEBUG && A.now() > DEBUG_MIN_FRAMES) {
            System.out.println("STOP @" + A.now() + " / unit#" + unit().id() + " // " + tooltip);
        }

        unit().setTooltip(tooltip, strategicLevel)
                .setAction(Actions.STOP);
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
    default boolean follow(AUnit target, String tooltip, boolean strategicLevel) {
        unit().setTooltip(tooltip, strategicLevel)
                .setAction(Actions.MOVE_FOLLOW);
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
        if (DEBUG && A.now() >= DEBUG_MIN_FRAMES) {
            System.out.println("GATHER @" + A.now() + " / unit#" + unit().id());
        }
        
        if (target.type().isMineralField()) {
            unit().setAction(Actions.GATHER_MINERALS);
        } else {
            unit().setAction(Actions.GATHER_GAS);
        }

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
    // Bugged, doesn't work
//    default boolean returnCargo() {
//        if (DEBUG && A.now() >= DEBUG_MIN_FRAMES) {
//            System.out.println("RETURN_CARGO @" + A.now() + " / unit#" + unit().id());
//        }
//
//        AUnit base = Select.ourBases().nearestTo(unit());
//        if (base != null) {
//            unit().doRightClickAndYesIKnowIShouldAvoidUsingIt(base);
//
//            unit().setAction(UnitActions.RETURN_CARGO);
//            unit().setLastUnitOrderNow();
//            unit().setTooltip("ReturnCargo");
//
//            return true;
//        }
//
//        return false;
////        return u().returnCargo();
//    }

    /**
     * Orders the unit to repair the specified unit. Only Terran SCvs. can be ordered to repair, and the target
     * must be a mechanical Terran unit or building. Parameters target The unit to repair. shiftQueueCommand
     * (optional) If this value is true, then the order will be queued instead of immediately executed. If
     * this value is omitted, then the order will be executed immediately by default. Returns true if the
     * command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There
     * is a small chance for a command to fail after it has been passed to Broodwar. See also isRepairing,
     * canRepair
     */
    default boolean repair(AUnit target, String tooltip, boolean strategicLevel) {
        if (DEBUG && A.now() >= DEBUG_MIN_FRAMES) {
            System.out.println("REPAIR @" + A.now() + " / unit#" + unit().id() + " // " + tooltip);
        }

        if (target == null) {
            return false;
        }

        unit().setTooltip(tooltip, strategicLevel);

        if (unit().isCommand(UnitCommandType.Repair) && !target.u().equals(u().getTarget())) {
            return true;
        }

        unit().setAction(Actions.REPAIR);
        u().repair(target.u());

        return true;

//        if (target.getPosition().distanceTo(unit()) > 1.5) {
//            unit().setLastUnitOrderNow();
//            return unit().move(target.getPosition(), UnitActions.MOVE_TO_REPAIR);
//        }
//        else {
//            unit().setAction(UnitActions.REPAIR);
//            if (unit().getTarget() == null || !unit().getTarget().equals(target) || !unit().isRepairing()) {
//                unit().setLastUnitOrderNow();
//                unit().setLastUnitOrderNow();
//                return u().repair(target.u());
//            }
//            else {
//                return true;
//            }
//        }
    }

    /**
     * Orders the unit to burrow. Either the unit must be a Lurker, or the unit must be a Zerg ground unit
     * that is capable of Burrowing, and Burrow technology must be researched. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also unburrow, isBurrowed,
     * canBurrow
     */
    default boolean burrow() {
        unit().setAction(Actions.BURROW);
        return u().burrow();
    }

    /**
     * Orders a burrowed unit to unburrow. Returns true if the command was passed to Broodwar, and false if
     * BWAPI determined that the command would fail. Note There is a small chance for a command to fail after
     * it has been passed to Broodwar. See also burrow, isBurrowed, canUnburrow
     */
    default boolean unburrow() {
        unit().setAction(Actions.UNBURROW);
        return u().unburrow();
    }

    /**
     * Orders the unit to cloak. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also decloak, isCloaked, canCloak
     */
    default boolean cloak() {
        unit().setAction(Actions.CLOAK);
        return u().cloak();
    }

    /**
     * Orders a cloaked unit to decloak. Returns true if the command was passed to Broodwar, and false if
     * BWAPI determined that the command would fail. Note There is a small chance for a command to fail after
     * it has been passed to Broodwar. See also cloak, isCloaked, canDecloak
     */
    default boolean decloak() {
        unit().setAction(Actions.LOAD);
        return u().decloak();
    }

    /**
     * Orders the unit to siege. Only works for Siege Tanks. Returns true if the command was passed to
     * Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a
     * command to fail after it has been passed to Broodwar. See also unsiege, isSieged, canSiege
     */
    default boolean siege() {
        unit().setAction(Actions.SIEGE);
        return u().siege();
    }

    /**
     * Orders the unit to unsiege. Only works for sieged Siege Tanks. Returns true if the command was passed
     * to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance
     * for a command to fail after it has been passed to Broodwar. See also siege, isSieged, canUnsiege
     */
    default boolean unsiege() {
        unit().setAction(Actions.UNSIEGE);
        return u().unsiege();
    }

    /**
     * Orders the unit to lift. Only works for liftable Terran structures. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also land, isLifted, canLift
     */
    default boolean lift() {
        unit().setAction(Actions.LIFT);
        return u().lift();
    }

    /**
     * Orders the unit to land. Only works for Terran structures that are currently lifted. Parameters target
     * The tile position to land this structure at. Returns true if the command was passed to Broodwar, and
     * false if BWAPI determined that the command would fail. Note There is a small chance for a command to
     * fail after it has been passed to Broodwar. See also lift, isLifted, canLand
     */
    default boolean land(TilePosition target) {
        unit().setAction(Actions.LAND);
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
        unit().setAction(Actions.LOAD);
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
        unit().setAction(Actions.UNLOAD);
        target.setAction(Actions.UNLOAD);
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
        unit().setAction(Actions.UNLOAD);
        for (AUnit loaded : unit().loadedUnits()) {
            loaded.setAction(Actions.UNLOAD);
        }
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
        unit().setAction(Actions.UNLOAD);
        for (AUnit loaded : unit().loadedUnits()) {
            loaded.setAction(Actions.UNLOAD);
        }
        return u().unloadAll(target);
    }

    /**
     * AVOID - it's very tough to debug, don't really know what it is doing!
     */
//    default boolean rightClick(AUnit target) {
//        if (target != null) {
//            if (!target.u().equals(u().getTarget())) {
//                unit().setAction(UnitActions.RIGHT_CLICK);
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
        unit().setAction(null);
        return u().haltConstruction();
    }

    /**
     * Orders this unit to cancel and refund itself from begin constructed. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also isBeingConstructed, build,
     * canCancelConstruction
     */
    default boolean cancelConstruction() {
        unit().setAction(null);
        return u().cancelConstruction();
    }

    /**
     * Orders this unit to cancel and refund an add-on that is being constructed. Returns true if the command
     * was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a
     * small chance for a command to fail after it has been passed to Broodwar. See also canCancelAddon,
     * buildAddon
     */
    default boolean cancelAddon() {
        unit().setAction(null);
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
        unit().setAction(null);
        return u().cancelTrain();
    }

    default boolean cancelTrain(int slot) {
        unit().setAction(null);
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
//    default boolean useTech(TechType tech) {
//        if (DEBUG && A.now() >= DEBUG_MIN_FRAMES) {
//            System.out.println("TECH_1 @" + A.now() + " / unit#" + unit().id());
//        }
//
//        unit().setAction(UnitActions.USING_TECH, tech, null);
//        unit().setLastUnitOrderNow();
//        return u().useTech(tech);
//    }

    default boolean useTech(TechType tech, APosition target) {
        if (DEBUG && A.now() >= DEBUG_MIN_FRAMES) {
            System.out.println("TECH_2 @" + A.now() + " / unit#" + unit().id());
        }

        unit().setAction(Actions.USING_TECH, tech, target);
        return u().useTech(tech, target);
    }

    default boolean useTech(TechType tech) {
        if (DEBUG && A.now() >= DEBUG_MIN_FRAMES) {
            System.out.println("TECH_1 @" + A.now() + " / unit#" + unit().id());
        }

        unit().setAction(Actions.USING_TECH, tech, unit());
        return u().useTech(tech);
    }

    default boolean useTech(TechType tech, AUnit target) {
        if (DEBUG && A.now() >= DEBUG_MIN_FRAMES) {
            System.out.println("TECH_3 @" + A.now() + " / unit#" + unit().id());
        }

        unit().setAction(Actions.USING_TECH, tech, target);
        return u().useTech(tech, target.u());
    }

    default boolean doRightClickAndYesIKnowIShouldAvoidUsingIt(AUnit target) {
        if (DEBUG && A.now() > DEBUG_MIN_FRAMES) {
            System.out.println("RIGHT_CLICK @" + A.now() + " / unit#" + unit().id() + " // " + target);
        }

        unit().setAction(Actions.RIGHT_CLICK);
        return u().rightClick(target.u());
    }

}
