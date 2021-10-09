package atlantis.combat;

import atlantis.AGame;
import atlantis.AGameSpeed;
import atlantis.ASpecialUnitManager;
import atlantis.combat.micro.*;
import atlantis.combat.missions.Missions;
import atlantis.debug.APainter;
import atlantis.repair.AUnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.Select;
import bwapi.Color;

public class ACombatUnitManager extends AbstractMicroManager {

    protected static boolean update(AUnit unit) {
        preActions(unit);
        if (Select.our().count() == 1) {
            AGame.exit("FINISHED AT @" + AGame.getTimeFrames());
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

        if (canHandleLowPriority(unit)) {
            return handleLowPriority(unit);
        }

        return false;
    }

    private static void preActions(AUnit unit) {
        if (AGameSpeed.isDynamicSlowdownActive() && (unit.isAttacking() || unit.isUnderAttack())) {
            AGameSpeed.disableDynamicSlowdown();
        }
    }

    // =========================================================

    private static boolean handledTopPriority(AUnit unit) {
        if (unit.isRunning()) {
            APainter.paintCircle(unit.getTargetPosition(), 2, Color.Grey);
            APainter.paintCircle(unit.getTargetPosition(), 3, Color.Grey);
            APainter.paintCircle(unit.getTargetPosition(), 1, Color.Grey);
            APainter.paintLine(unit, unit.getTargetPosition(), Color.Black);
        }

        // Handle units getting bugged by Starcraft
        if (handleBuggedUnit(unit)) {
            return true;
        }

        // Don't INTERRUPT shooting units
        if (shouldNotDisturbUnit(unit)) {
            return true;
        }

        // Can be used for tests
//        if (testUnitBehavior(unit)) { return true; };

        // Avoid bad weather like:
        // - raining Psionic Storm,
        // - spider mines hail
        if (ABadWeather.avoidSpellsMines(unit)) {
            return true;
        }

        if (AAvoidInvisibleEnemyUnits.avoidInvisibleUnits(unit)) {
            return true;
        }

        if (!Missions.isGlobalMissionAttack()) {
            if (AAvoidDefensiveBuildings.avoidCloseBuildings(unit, false)) {
                return true;
            }
        }

        if (AAvoidEnemyMeleeUnitsManager.avoidCloseMeleeUnits(unit)) {
            return true;
        }

        return false;
    }

    private static boolean handledMediumPriority(AUnit unit) {

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

    private static boolean canHandleLowPriority(AUnit unit) {
        return unit.isStopped() || unit.isIdle();
    }

    /**
     * If we're here, mission manager is allowed to take control over this unit.
     * Meaning no action was needed on *tactical* level - stick to *strategic* level.
     */
    private static boolean handleLowPriority(AUnit unit) {
        unit.setTooltip(unit.getSquad().getMission().getName());

        return unit.getSquad().getMission().update(unit);
    }

    // =========================================================

    /**
     * Some actions are too important/costly. No matter what happens, don't interrupt unit at this point.
     */
    private static boolean shouldNotDisturbUnit(AUnit unit) {
//        if (unit.isAttackFrame()) {
//            System.out.println(AGame.getTimeFrames() +" // #" + unit.getID());
//        }

        if (unit.isAttacking() && unit.getLastOrderFramesAgo() <= unit.getCooldown() - 3) {
            double minDistToContinueAttack = 1.8 + unit.getWoundPercent() / 40.0;
            if (Select.enemyRealUnits().melee().inRadius(minDistToContinueAttack, unit).isEmpty()) {
                unit.setTooltip("@ATTACK");
                return true;
            }
        }

        if (unit.isRunning() && unit.getLastOrderFramesAgo() <= 4) {
            unit.setTooltip("Run...(" + unit.getLastOrderFramesAgo() + ")");
            return true;
        }

        if (unit.isAttackFrame()) {
            unit.setTooltip("Attack frame");
            return true;
        }

        if (unit.isStartingAttack()) {
            unit.setTooltip("Starts attack");
            return true;
        }

//        if (!unit.isAttacking() && unit.getLastOrderFramesAgo() <= 2) {
//            unit.setTooltip("Dont disturb (" + unit.getLastOrderFramesAgo() + ")");
//            return true;
//        }

//                ((!unit.type().isTank() || unit.getGroundWeaponCooldown() <= 0) && unit.isStartingAttack())
//                && unit.getGroundWeaponCooldown() <= 0 && unit.getAirWeaponCooldown() <= 0;
                ;

        return false;
    }

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

        if (unit.isUnderAttack() && unit.getLastOrderFramesAgo() >= 40) {
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
    private static boolean testUnitBehavior(AUnit unit) {
        if (Select.our().first().getID() != unit.getID()) {
            unit.attackUnit(Select.our().first());
        }
        return true;
    }

}
