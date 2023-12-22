package atlantis.units;

import atlantis.combat.micro.terran.tank.unsieging.ShouldUnsiegeToMove;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.actions.Action;
import atlantis.units.actions.Actions;
import atlantis.util.log.ErrorLog;
import bwapi.*;
import tests.unit.FakeUnitData;

/**
 * Class using default methods which are extracted from AUnit class to separate this functionality.
 */
public interface AUnitOrders {
    int DEBUG_MIN_FRAMES = 0;

//    boolean DEBUG_ALL = false;
    boolean DEBUG_ALL = true;

    boolean DEBUG_COMBAT = false;
//    boolean DEBUG_COMBAT = true;

    Unit u();

    AUnit unit();

    // =========================================================

    default boolean attackUnit(AUnit target) {
//        if (DEBUG && A.now() > DEBUG_MIN_FRAMES) {

//                    "@ @" + A.now() + " ATTACK  / " +
//                            "" + unit().typeWithHash() + " // " +
//                            "cooldown " + unit().cooldownRemaining()+ " // " +
//                            "attackFrame " + unit()._lastAttackFrame + " // " +
//                            "StartingAttack " + unit()._lastStartedAttack + " // " +
//                            unit().tooltip()
//            );
//        }

        if (target == null) {
            System.err.println("Null attack unit target for " + this.unit().typeWithHash());
            return false;
        }

        if (target.u() == null) {
            move(target, Actions.MOVE_ATTACK, null, false);
            return true;

//            // This likes to happen to sieged tanks. What matters is that we return false here.
////            if (!unit().isTankSieged()) {
////                if (Env.isLocal()) A.printStackTrace();
//
//            if (unit().distTo(target) >= 12.5) {
//                return move(target, Actions.MOVE_ATTACK, null, false);
//            }
//            else {
//                System.err.println("Null attack u(nit) for " + this.unit().typeWithHash());
//                System.err.println("target = " + target.getClass());
//                System.err.println("toString = " + target.toString());
//                System.err.println("isVisibleUnitOnMap " + target.isVisibleUnitOnMap());
//                System.err.println("hasPosition = " + target.hasPosition());
//                System.err.println("isPositionVisible = " + target.isPositionVisible());
//            }
////            }
//            return false;
        }

        if (!target.isDetected()) {
            System.err.println("Trying to attack not detected unit for " + this.unit().typeWithHash());
            System.err.println(target);
            System.err.println(target.position());
            System.err.println(target.isPositionVisible());
            System.err.println(target.hp());
            if (Env.isLocal()) A.printStackTrace();
            return false;
        }

        if (!target.hasPosition()) {
            System.err.println("Target (" + target + ") has no position " + this.unit().typeWithHash());
            return false;
        }

        if (!target.isAlive()) {
            System.err.println("Dead target (" + target + ") for " + this.unit().typeWithHash());
            return false;
        }


        // Do NOT issue double orders
//        if (unit().isAttacking() && unit().isCommand(UnitCommandType.Attack_Unit) && target.equals(unit().target())) {
        if (unit().isCommand(UnitCommandType.Attack_Unit) && target.equals(unit().target())) {
            unit().setTooltipTactical("Attacking...");
            return true;
        }

        if (shouldPrint() && A.now() > DEBUG_MIN_FRAMES) {
            System.out.println(unit().typeWithHash() + " @ " + A.now() + " ATTACK_UNIT " + target);
        }

//        if (unit().outsideSquadRadius()) {
//            A.printStackTrace("hmmm " + unit().distToSquadCenter() + " / " + unit().squadRadius());
//        }

        unit().setTooltipTactical("ATTACK-UNIT");
        unit().setAction(Actions.ATTACK_UNIT);
        return u().attack(target.u());
//        return true;
    }

    // To avoid confusion: NEVER USE IT.
    // When moving units always use "Move" mission.
    // Use "Attack" only for targeting actual units.
    default boolean attackPosition(APosition target) {
        if (true) throw new RuntimeException("DO NOT USE IT, PLEASE");

        if (u().getTargetPosition() != null && !u().getTargetPosition().equals(target)) {
            u().attack(target.p());
            unit().setAction(Actions.ATTACK_POSITION);


            return true;
        }

        return false;
    }

    default boolean train(AUnitType unitToTrain, ProductionOrder productionOrder) {
        unit().setAction(Actions.TRAIN);
        unit().setProductionOrder(productionOrder);
        return u() != null ? u().train(unitToTrain.ut()) : FakeUnitData.TRAIN.add(unitToTrain);
    }

    default boolean trainForced(AUnitType unitToTrain) {
        unit().setAction(Actions.TRAIN);
        unit().setProductionOrder(ForcedDirectProductionOrder.create(unitToTrain));
        return u() != null ? u().train(unitToTrain.ut()) : FakeUnitData.TRAIN.add(unitToTrain);
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

//    default boolean move(AUnit target, Action unitAction, String tooltip, boolean strategicLevel) {
//        return move(target.position(), unitAction, tooltip, strategicLevel);
//    }

    default boolean moveStrategic(HasPosition target, Action unitAction, String tooltip) {
        return move(target, unitAction, tooltip, true);
    }

    default boolean moveTactical(HasPosition target, Action unitAction, String tooltip) {
        return move(target, unitAction, tooltip, false);
    }

    default boolean move(HasPosition target, Action unitAction, String tooltip) {
        return move(target, unitAction, tooltip, false);
    }

    default boolean move(HasPosition target, Action unitAction, String tooltip, boolean strategicLevel) {
        if (shouldPrint() && A.now() > DEBUG_MIN_FRAMES) {
            System.out.println(unit().nameWithId() + " @" + A.now() + " MOVE / " + tooltip);
        }
        if (target == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Null move position for " + unit().typeWithHash());
            return false;
        }

        if (tooltip != null) {
            unit().setTooltip(tooltip, strategicLevel);
        }

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

//        if (!unit().isMoving() || A.now() % 4 != 0) {
//        if (!unit().isUnitActionMove() || A.now() % 5 == 0) {

        APosition currentTarget = unit().targetPosition();

        if (currentTarget == null || (!currentTarget.equals(target) || unit().lastOrderMinFramesAgo(6))) {
            if (unit().isSieged() && ShouldUnsiegeToMove.shouldUnsiege(unit())) {
                unit().unsiege();
                return true;
            }

//            if (unit().isFirstCombatUnit()) {

//            }
            u().move(target.position().p());

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
        return u().patrol(target.p());
    }

    /**
     * Orders the unit to hold its position. Parameters shiftQueueCommand (optional) If this value is true,
     * then the order will be queued instead of immediately executed. If this value is omitted, then the order
     * will be executed immediately by default. Returns true if the command was passed to Broodwar, and false
     * if BWAPI determined that the command would fail. Note There is a small chance for a command to fail
     * after it has been passed to Broodwar. See also canHoldPosition, isHoldingPosition
     */
    default boolean holdPosition(String tooltip) {
        if (unit().isCommand(UnitCommandType.Hold_Position)) return false;

        if (shouldPrint() && A.now() > DEBUG_MIN_FRAMES) {
            System.out.println(unit().typeWithHash() + " HOLD @" + A.now() + " / " + tooltip);
        }

//        System.err.println(tooltip);
//        System.err.println(unit().manager());
//        System.err.println(unit().managerLogs().toString());
//        System.err.println("-------------------------");

        unit().setTooltip(tooltip).setAction(Actions.HOLD_POSITION);
        return u().holdPosition();
    }

    /**
     * Orders the unit to stop. Parameters shiftQueueCommand (optional) If this value is true, then the order
     * will be queued instead of immediately executed. If this value is omitted, then the order will be
     * executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also canStop, isIdle
     */
    default boolean stop(String tooltip) {
        if (shouldPrint() && A.now() > DEBUG_MIN_FRAMES) {
            System.out.println(unit().typeWithHash() + "STOP @" + A.now() + " / " + tooltip);
        }

        unit().setTooltip(tooltip)
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
        if (shouldPrint() && A.now() >= DEBUG_MIN_FRAMES) {
            System.out.println("GATHER @" + A.now() + " / worker:" + unit().typeWithHash() + " / " + target);
        }

        if (target.type().isMineralField()) {
            unit().setAction(Actions.GATHER_MINERALS);
        }
        else {
            unit().setAction(Actions.GATHER_GAS);
        }

        return u().gather(target.u());
    }

    /**
     * Orders the unit to return u().its cargo to a Near resource depot such as a Command Center. Only
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
    default boolean repair(AUnit target, String tooltip) {
        if (target == null) {
            System.err.println("Null repair target");
            return false;
        }

        unit().setTooltip(tooltip);

//        if (unit().isRepairing() && unit().isCommand(UnitCommandType.Repair) && target.u().equals(u().getTarget())) {
        if (unit().isRepairing() && target.equals(unit().target())) {
//            System.err.println(this + " avoid double command / " + unit().getLastCommand() + " // " + unit().target());
            return true;
        }

        unit().setLastActionReceivedNow().setAction(Actions.REPAIR);

        if (!unit().isRepairing()) {
            if (unit().distToMoreThan(target, 2) && !unit().isMoving()) {
                //            u().move(target.position());
                // A fix to avoid stucking SCVs that go to repair in line.
                // We send them in slightly different places, hoping they don't stuck in line

//                AUnit moveTo = target;
                APosition moveTo = target.position().translateByTiles(
                    -0.9 + 1.9 * ((1 + unit().id()) % 4) / 4.0,
                    -0.9 + 1.9 * ((-1 + unit().id()) % 5) / 4.0
                );
//                A.println(A.now() + " / " + unit() + " moveTo = " + moveTo);
                move(moveTo, Actions.MOVE_REPAIR, tooltip, false);
            }
            else {
                u().repair(target.u());
            }
        }

        if (shouldPrint() && A.now() >= DEBUG_MIN_FRAMES) {
            System.out.println(unit().typeWithHash() + " REPAIR @" + A.now() + " / " + target + " (" + target.hp() + ")");
        }

        return true;
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

        if (Env.isTesting()) return true;

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
//        System.err.println(unit().idWithHash() + " LOAD " + target);
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
//        A.printStackTrace("Unloaded...");
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
        return u().unloadAll(target.p());
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
        unit().setAction(Actions.CANCEL);
//        throw new RuntimeException("Cancel!");
        return unit() != null && u() != null ? u().cancelConstruction() : FakeUnitData.CANCEL.add(unit());
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

//        }
//
//        unit().setAction(UnitActions.USING_TECH, tech, null);
//        unit().setLastUnitOrderNow();
//        return u().useTech(tech);
//    }
    default boolean useTech(TechType tech, APosition target) {
        if (shouldPrint() && A.now() >= DEBUG_MIN_FRAMES) {
            System.out.println("TECH_2 @" + A.now() + " / " + unit().typeWithHash());
        }

        unit().setAction(Actions.USING_TECH, tech, target);
        return u().useTech(tech, target.p());
    }

    default boolean useTech(TechType tech) {
        if (shouldPrint() && A.now() >= DEBUG_MIN_FRAMES) {
            System.out.println("TECH_1 @" + A.now() + " / " + unit().typeWithHash());
        }

        unit().setAction(Actions.USING_TECH, tech, unit());
        return u().useTech(tech);
    }

    default boolean useTech(TechType tech, AUnit target) {
        if (shouldPrint() && A.now() >= DEBUG_MIN_FRAMES) {
            System.out.println("TECH_3 @" + A.now() + " / " + unit().typeWithHash());
        }

        unit().setAction(Actions.USING_TECH, tech, target);
        return u().useTech(tech, target.u());
    }

    default boolean doRightClickAndYesIKnowIShouldAvoidUsingIt(AUnit target) {
        if (shouldPrint() && A.now() > DEBUG_MIN_FRAMES) {
            System.out.println("RIGHT_CLICK @" + A.now() + " / " + unit().typeWithHash() + " // " + target);
        }

        unit().setAction(Actions.RIGHT_CLICK);
        return u().rightClick(target.u());
    }

    default boolean shouldPrint() {
        return (DEBUG_ALL || (DEBUG_COMBAT && unit().isCombatUnit()));
    }

}
