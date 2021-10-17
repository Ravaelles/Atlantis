package atlantis.combat;

import atlantis.AGame;
import atlantis.AGameSpeed;
import atlantis.ASpecialUnitManager;
import atlantis.combat.micro.*;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.ASquadCohesionManager;
import atlantis.repair.AUnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.util.A;

public class ACombatUnitManager extends AbstractMicroManager {

    protected static boolean update(AUnit unit) {
        preActions(unit);

        // =========================================================
        // === TOP priority ========================================
        // =========================================================

        if (handledTopPriority(unit)) {
            return true;
        }

        // =========================================================
        // === SPECIAL units =======================================
        // =========================================================

        // 1) Overlords or Medics are using entirely dedicated managers.
        //    These are stopping lower level actions.
        // 2) Terran infantry has own managers, but these allow lower
        //    level managers to take control.
        if (ASpecialUnitManager.handledUsingDedicatedUnitManager(unit)) {
            return true;
        }

        // =========================================================
        // === MEDIUM priority - TACTICAL level ====================
        // =========================================================

        if (handledMediumPriority(unit)) {
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
        if (AGameSpeed.isDynamicSlowdownAllowed() && !AGameSpeed.isDynamicSlowdownActive()
                && (unit.lastAttackOrderAgo(1) || unit.isUnderAttack())) {
            AGameSpeed.activateDynamicSlowdown();
            AGameSpeed.disallowToDynamicallySlowdownGameOnFirstFighting();
        }

        unit.setTooltip(unit.getTooltip() + ".");
    }

    // =========================================================

    private static boolean handledTopPriority(AUnit unit) {

        // Handle units getting bugged by Starcraft
        if (handleBuggedUnit(unit)) {
            return true;
        }

        if (ARunManager.shouldStopRunning(unit)) {
            return true;
        }

        if (unit.isRunning()) {
            unit.setTooltip("Running(" + A.digit(unit.distanceTo(unit.getTargetPosition())) + ")");
            return true;
        }

        // Don't INTERRUPT shooting units
        if (DontInterruptStartedAttacks.shouldNotBeInterruptedStartedAttack(unit)) {
            return true;
        }

        // Useful for testing and debugging of shooting/running
//        if (testUnitBehaviorShootAtOwnUnit(unit)) { return true; };

        // Avoid bad weather like:
        // - raining Psionic Storm,
        // - spider mines hail
        if (ABadWeather.avoidSpellsAndMines(unit)) {
            return true;
        }

        return false;
    }

    private static boolean handledMediumPriority(AUnit unit) {
        if (AAvoidInvisibleEnemyUnits.avoidInvisibleUnits(unit)) {
            return true;
        }

        if (!Missions.isGlobalMissionAttack()) {
            if (AAvoidEnemyDefensiveBuildings.avoidCloseBuildings(unit, false)) {
                return true;
            }
        }

        if ((new AAvoidEnemyMeleeUnitsManager(unit)).avoidCloseMeleeUnits()) {
            return true;
        }

        // If nearby enemies would likely defeat us, retreat
        if (shouldRetreat(unit)) {
            return true;
        }

        // Handle repair of mechanical units
        if (AGame.isPlayingAsTerran() && AUnitBeingReparedManager.handleUnitBeingRepaired(unit)) {
            return true;
        }

        if (AAttackEnemyUnit.handleAttackNearbyEnemyUnits(unit)) {
            return true;
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
        if (ASquadCohesionManager.handleExtremeUnitPositioningInSquad(unit)) {
            return true;
        }

        if (AvoidEdgesWhenMoving.handle(unit)) {
            return true;
        }

        unit.setTooltip(unit.squad().getMission().getName());
        return unit.squad().getMission().update(unit);
    }

    // =========================================================

    /**
     * Some units like Reavers should open fire to nearby enemies even when retreating, otherwise they'll
     * just get destroyed without firing even once.
     */
//    private static boolean isAllowedToAttackBeforeRetreating(AUnit unit) {
//        return unit.isType(AUnitType.Protoss_Reaver, AUnitType.Terran_Vulture) && unit.getHPPercent() > 10;
//    }

    private static boolean handleBuggedUnit(AUnit unit) {
        if (unit.isRunning() && unit.getLastOrderFramesAgo() >= 40) {
            if (unit.lastX == unit.getX() && unit.lastY == unit.getY()) {
                System.err.println("UNFREEZE #1!");
                unit.setTooltip("UNFREEZE!");
                unit.unbug();
                return true;
            }
        }

        else if (unit.isUnderAttack() && unit.getLastOrderFramesAgo() >= 40) {
            if (unit.lastX == unit.getX() && unit.lastY == unit.getY()) {
                System.err.println("UNFREEZE #2!");
                unit.setTooltip("UNFREEZE!");
                unit.unbug();
                return true;
            }
        }

        return false;
    }

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
