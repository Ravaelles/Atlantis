package atlantis.combat;

import atlantis.AGame;
import atlantis.ASpecialUnitManager;
import atlantis.combat.micro.*;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.missions.Mission;
import atlantis.combat.retreating.ARunningManager;
import atlantis.combat.retreating.RetreatManager;
import atlantis.debug.APainter;
import atlantis.interrupt.DontDisturbInterrupt;
import atlantis.repair.AUnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;
import atlantis.units.select.Select;
import atlantis.util.A;
import bwapi.Color;

public class ACombatUnitManager {

    private static boolean debug = false;
//    private static boolean debug = true;

    public static boolean update(AUnit unit) {
        if (preActions(unit)) {
            return true;
        }

        // =========================================================
        // === SPECIAL units =======================================
        // =========================================================

        // Medics are using dedicated managers.
        // These are stopping standard top priority managers
        if (ASpecialUnitManager.updateAndOverrideAllOtherManagers(unit)) {
            return true;
        }

        // =========================================================
        // === TOP priority ========================================
        // =========================================================

        if (handledTopPriority(unit)) {
            return true;
        }

        // =========================================================
        // === SPECIAL units =======================================
        // =========================================================

        // Terran infantry has own managers, but these allow higher
        // level managers to take control.
        if (ASpecialUnitManager.updateAndAllowTopManagers(unit)) {
            return true;
        }

        // =========================================================
        // === MEDIUM priority - TACTICAL level ====================
        // =========================================================

        if (debug) System.out.println("A");

        if (handledMediumPriority(unit)) {
            return true;
        }

        if (debug) System.out.println("B");

        // =========================================================
        // === LOW priority - STRATEGY level =======================
        // =========================================================

        return handleLowPriority(unit);
//        if (canHandleLowPriority(unit)) {
//        }
//        return false;
    }

    private static boolean preActions(AUnit unit) {
//        if (
//                A.seconds() >= 1
//                && GameSpeed.isDynamicSlowdownAllowed()
//                && !GameSpeed.isDynamicSlowdownActive()
//                && (unit.lastActionLessThanAgo(2, UnitActions.ATTACK_UNIT) || unit.isUnderAttack(3)))
//        {
//            GameSpeed.activateDynamicSlowdown();
//        }

//        if (unit.isUnderAttack(1) && !GameSpeed.oneTimeSlowdownUsed) {
//            GameSpeed.changeSpeedTo(30);
//            GameSpeed.oneTimeSlowdownUsed = true;
//        }

        if (unit.targetPosition() != null) {
            APainter.paintLine(unit, unit.targetPosition(), Color.Grey);
        }

        if (unit.isNotRealUnit()) {
            System.err.println("Not real unit: " + unit.shortName());
            return true;
        }

        if (unit.isWorker() && unit.squad() == null) {
            System.err.println("Worker being ACUM");
            return true;
        }

        if (unit.lastActionLessThanAgo(90, UnitActions.PATROL) || unit.isPatrolling()) {
            if (A.now() > 90) {
                unit.setTooltip("#Manual");
                return true;
            }
        }

        unit.setTooltip(unit.tooltip() + ".");
        return false;
    }

    // =========================================================

    private static boolean handledTopPriority(AUnit unit) {
        if (DontDisturbInterrupt.dontInterruptImportantActions(unit)) {
            return true;
        }

        if (unit.isRunning() && TransportUnits.loadRunningUnitsIntoTransport(unit)) {
            return true;
        }

        if (unit.isLoaded() && TransportUnits.unloadFromTransport(unit)) {
            return true;
        }

        // Handle units getting bugged by Starcraft
        if (Unfreezer.handleUnfreeze(unit)) {
            return true;
        }

        if (ARunningManager.shouldStopRunning(unit)) {
            return true;
        }

//        if (unit.isRunning() && unit.lastStartedRunningLessThanAgo(2)) {
        if (unit.isRunning()) {
//            unit.setTooltip("Running(" + A.digit(unit.distTo(unit.getTargetPosition())) + ")");
            return A.everyNthGameFrame(2) ? AAvoidUnits.avoidEnemiesIfNeeded(unit) : true;
        }

        // Useful for testing and debugging of shooting/running
//        if (testUnitBehaviorShootAtOwnUnit(unit)) { return true; };

        // Avoid bad weather like:
        // - raining Psionic Storm,
        // - spider mines hail
        return AAvoidSpells.avoidSpellsAndMines(unit);
    }

    private static boolean handledMediumPriority(AUnit unit) {

        // Avoid:
        // - invisible units (Dark Templars)
        // - close melee units (Zealots)
        // - ranged units that can shoot at us (Dragoons)
        // - defensive buildings (Cannons)
        if (AAvoidUnits.avoidEnemiesIfNeeded(unit)) {
            return true;
        }

        // If nearby enemies would likely defeat us, retreat
//        if (RetreatManager.shouldRetreat(unit)) {
//            return true;
//        }

        // Handle repair of mechanical units
        if (AGame.isPlayingAsTerran() && AUnitBeingReparedManager.handleUnitBeingRepaired(unit)) {
            return true;
        }

//        System.out.println("RetreatManager.shouldRetreat(unit) = " + RetreatManager.shouldRetreat(unit));
        if (!RetreatManager.shouldRetreat(unit)) {
            return AAttackEnemyUnit.handleAttackNearbyEnemyUnits(unit);
        }

        return false;
    }

//    private static boolean canHandleLowPriority(AUnit unit) {
//        return unit.isStopped() || unit.isIdle();
//    }

    /**
     * If we're here, mission manager is allowed to take control over this unit.
     * Meaning no action was needed on *tactical* level - stick to *strategic* level.
     */
    private static boolean handleLowPriority(AUnit unit) {
//        if (AvoidEdgesWhenMoving.handle(unit)) {
//            return true;
//        }

        Mission mission = unit.mission();
        if (mission != null) {
            unit.setTooltip(mission.name());
            return mission.update(unit);
        }

        return false;
    }

    // =========================================================

    /**
     * Some units like Reavers should open fire to nearby enemies even when retreating, otherwise they'll
     * just get destroyed without firing even once.
     */
//    private static boolean isAllowedToAttackBeforeRetreating(AUnit unit) {
//        return unit.isType(AUnitType.Protoss_Reaver, AUnitType.Terran_Vulture) && unit.getHPPercent() > 10;
//    }

    /**
     * Can be used for testing.
     */
    private static boolean testUnitBehaviorShootAtOwnUnit(AUnit unit) {
        if (Select.our().first().id() != unit.id()) {
            unit.attackUnit(Select.our().first());
        }
        return true;
    }

}
