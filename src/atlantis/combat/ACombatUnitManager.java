package atlantis.combat;

import atlantis.AGame;
import atlantis.GameSpeed;
import atlantis.ASpecialUnitManager;
import atlantis.combat.micro.*;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.missions.Mission;
import atlantis.combat.retreating.ARunningManager;
import atlantis.interrupt.DontDisturbInterrupt;
import atlantis.repair.AUnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.actions.UnitActions;
import atlantis.util.A;

public class ACombatUnitManager {

    protected static boolean update(AUnit unit) {
        preActions(unit);

        // =========================================================
        // === SPECIAL units =======================================
        // =========================================================

        // Medics are using dedicated managers.
        // These are stopping standard top priority managers
        if (ASpecialUnitManager.handledUsingDedicatedUnitManager(unit)) {
            return true;
        }

        // =========================================================
        // === TOP priority ========================================
        // =========================================================

        if (handledTopPriority(unit)) {
            return true;
        }

        // =========================================================
        // === MEDIUM priority - TACTICAL level ====================
        // =========================================================

        if (handledMediumPriority(unit)) {
            return true;
        }

        // =========================================================
        // === SPECIAL units =======================================
        // =========================================================

        // Terran infantry has own managers, but these allow higher
        // level managers to take control.
        if (ASpecialUnitManager.handledUsingSpecialUnitManager(unit)) {
            return true;
        }

        // =========================================================
        // === LOW priority - STRATEGY level =======================
        // =========================================================

        return handleLowPriority(unit);
//        if (canHandleLowPriority(unit)) {
//        }
//        return false;
    }

    private static void preActions(AUnit unit) {
//        if (
//                A.seconds() >= 1
//                && GameSpeed.isDynamicSlowdownAllowed()
//                && !GameSpeed.isDynamicSlowdownActive()
//                && (unit.lastActionLessThanAgo(2, UnitActions.ATTACK_UNIT) || unit.isUnderAttack(3)))
//        {
//            GameSpeed.activateDynamicSlowdown();
//        }

        unit.setTooltip(unit.getTooltip() + ".");
    }

    // =========================================================

    private static boolean handledTopPriority(AUnit unit) {
        if (DontDisturbInterrupt.dontInterruptImportantActions(unit)) {
            return true;
        }

        if (unit.isRunning() && TransportUnits.loadRunningUnitsIntoTransport(unit)) {
            return true;
        }

        // Handle units getting bugged by Starcraft
        if (Unfreezer.handleUnfreeze(unit)) {
            return true;
        }

        if (ARunningManager.shouldStopRunning(unit)) {
            return true;
        }

        if (unit.isRunning()) {
//            unit.setTooltip("Running(" + A.digit(unit.distTo(unit.getTargetPosition())) + ")");
            return true;
        }

        // Useful for testing and debugging of shooting/running
//        if (testUnitBehaviorShootAtOwnUnit(unit)) { return true; };

        // Avoid bad weather like:
        // - raining Psionic Storm,
        // - spider mines hail
        return ABadWeather.avoidSpellsAndMines(unit);
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

        return AAttackEnemyUnit.handleAttackNearbyEnemyUnits(unit);
    }

//    private static boolean canHandleLowPriority(AUnit unit) {
//        return unit.isStopped() || unit.isIdle();
//    }

    /**
     * If we're here, mission manager is allowed to take control over this unit.
     * Meaning no action was needed on *tactical* level - stick to *strategic* level.
     */
    private static boolean handleLowPriority(AUnit unit) {
        if (AvoidEdgesWhenMoving.handle(unit)) {
            return true;
        }

        Mission mission = unit.mission();
        if (mission != null) {
            unit.setTooltip(mission.getName());
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
        if (Select.our().first().getID() != unit.getID()) {
            unit.attackUnit(Select.our().first());
        }
        return true;
    }

}
