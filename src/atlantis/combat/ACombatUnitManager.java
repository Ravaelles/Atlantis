package atlantis.combat;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.combat.micro.AAvoidSpells;
import atlantis.combat.micro.Unfreezer;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.combat.micro.managers.DanceAfterShoot;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.missions.Mission;
import atlantis.combat.retreating.ARunningManager;
import atlantis.combat.retreating.RetreatManager;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.terran.repair.AUnitBeingReparedManager;
import atlantis.units.ASpecialUnitManager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.DontDisturbInterrupt;
import atlantis.units.select.Select;

public class ACombatUnitManager {

    public static boolean update(AUnit unit) {
        //if (true)System.out.println("A0 " + unit.nameWithId() + " at " + A.now());

        if (preActions(unit)) {
            return true;
        }

        //if (true)System.out.println("A " + unit.nameWithId() + " at " + A.now());

        // =========================================================
        // === SPECIAL units =======================================
        // =========================================================

        // Medics are using dedicated managers.
        // These are stopping standard top priority managers
        if (ASpecialUnitManager.updateAndOverrideAllOtherManagers(unit)) {
            return true;
        }

        //if (true)System.out.println("B " + unit.nameWithId());

        // =========================================================
        // === TOP priority ========================================
        // =========================================================

        if (handledTopPriority(unit)) {
            return true;
        }

        //if (true)System.out.println("C " + unit.nameWithId());

        // =========================================================
        // === SPECIAL units =======================================
        // =========================================================

        // Terran infantry has own managers, but these allow higher
        // level managers to take control.
        if (ASpecialUnitManager.updateAndAllowTopManagers(unit)) {
            return true;
        }

        //if (true)System.out.println("D " + unit.nameWithId());

        // =========================================================
        // === MEDIUM priority - TACTICAL level ====================
        // =========================================================

        if (handledMediumPriority(unit)) {
            return true;
        }

        //if (true)System.out.println("E " + unit.nameWithId());

        // =========================================================
        // === LOW priority - MISSION level =======================
        // =========================================================

        return handleLowPriority(unit);
    }

    private static boolean preActions(AUnit unit) {
        if (unit.isPatrolling() || unit.lastActionLessThanAgo(15, Actions.RIGHT_CLICK)) {
            unit.setTooltip("Manual", true);
            return true;
        }

//        if (
//                A.seconds() >= 1
//                && GameSpeed.isDynamicSlowdownAllowed()
//                && !GameSpeed.isDynamicSlowdownActive()
//                && (unit.lastActionLessThanAgo(2, UnitActions.ATTACK_UNIT) || unit.isUnderAttack(3)))
//        {
//            GameSpeed.activateDynamicSlowdown();
//        }

        if (!unit.isRealUnit()) {
            System.err.println("Not real unit: " + unit.name());
            return true;
        }

        if (unit.isWorker() && unit.squad() == null) {
            System.err.println("Worker being ACUM");
            return true;
        }

        if (unit.lastActionLessThanAgo(90, Actions.PATROL) || unit.isPatrolling()) {
            if (A.now() > 90) {
                unit.setTooltipTactical("#Manual");
                return true;
            }
        }

        unit.setTooltipTactical(unit.tooltip() + ".");
        return false;
    }

    // =========================================================

    private static boolean handledTopPriority(AUnit unit) {
        if (DanceAfterShoot.handle(unit)) {
            return true;
        }

        if (DontDisturbInterrupt.dontInterruptImportantActions(unit)) {
            return true;
        }

        if ((unit.isMoving() && !unit.isAttackingOrMovingToAttack()) && TransportUnits.handleLoad(unit)) {
            return true;
        }

        if (unit.isLoaded() && TransportUnits.unloadFromTransport(unit)) {
            return true;
        }

        // Handle units getting bugged by Starcraft
//        if (Unfreezer.handleUnfreeze(unit)) {
//            return true;
//        }

        if (unit.isRunning()) {
            if (ARunningManager.shouldStopRunning(unit)) {
                unit.runningManager().stopRunning();
            }
    //        if (unit.isRunning() && unit.lastStartedRunningLessThanAgo(2)) {
            else if (A.everyNthGameFrame(3)) {
    //            unit.setTooltip("Running(" + A.digit(unit.distTo(unit.getTargetPosition())) + ")");
    //            return A.everyNthGameFrame(2) ? AAvoidUnits.avoidEnemiesIfNeeded(unit) : true;
                return AAvoidUnits.avoidEnemiesIfNeeded(unit);
            }
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

        // If Near enemies would likely defeat us, retreat
//        if (RetreatManager.shouldRetreat(unit)) {
//            return true;
//        }

        // Handle repair of mechanical units
        if (AGame.isPlayingAsTerran() && AUnitBeingReparedManager.handleUnitBeingRepaired(unit)) {
            return true;
        }

        if (RetreatManager.handleRetreat(unit)) {
            return true;
        }

        return AAttackEnemyUnit.handleAttackNearEnemyUnits(unit);
    }

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
            unit.setTooltipTactical(mission.name());
            return mission.update(unit);
        }

        return false;
    }

    // =========================================================

    /**
     * Can be used for testing.
     */
//    private static boolean testUnitBehaviorShootAtOwnUnit(AUnit unit) {
//        if (Select.our().first().id() != unit.id()) {
//            unit.attackUnit(Select.our().first());
//        }
//        return true;
//    }

}
