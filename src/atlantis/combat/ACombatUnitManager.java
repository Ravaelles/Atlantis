package atlantis.combat;

import atlantis.AGame;
import atlantis.AGameSpeed;
import atlantis.combat.micro.*;
import atlantis.combat.micro.terran.TerranInfantryManager;
import atlantis.combat.micro.terran.TerranMedic;
import atlantis.combat.micro.terran.TerranSiegeTankManager;
import atlantis.combat.micro.terran.TerranVultureManager;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.repair.ARepairManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

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
        if (handledSpecially(unit)) {
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
        unit.removeTooltip();
        if (AGameSpeed.isDynamicSlowdownActive() && (unit.isAttacking() || unit.isUnderAttack())) {
            AGameSpeed.disableDynamicSlowdown();
        }
    }

    // =========================================================

    private static boolean handledTopPriority(AUnit unit) {

        // Don't INTERRUPT shooting units
        if (shouldNotDisturbUnit(unit)) {
            unit.setTooltip("#DontDisturb");
            return true;
        }

        // Avoid bad weather like:
        // - raining Psionic Storm,
        // - spider mines hail
        if (ABadWeather.avoidSpellsMines(unit)) {
            return true;
        }

        // Dark Templars are deadly!
        if (AAvoidInvisibleEnemyManager.avoidInvisibleUnits(unit)) {
            return true;
        }

        if (AAvoidDefensiveBuildings.avoidCloseBuildings(unit)) {
            return true;
        }

        return false;
    }

    private static boolean handledMediumPriority(AUnit unit) {
        if (AAvoidMeleeUnitsManager.avoidCloseMeleeUnits(unit)) {
            return true;
        }

        // Early mode - Attack enemy units when in range (and choose the best target)
//        boolean isAllowedToAttackDespiteRetreating = isAllowedToAttackBeforeRetreating(unit);
//        if (isAllowedToAttackDespiteRetreating && AAttackEnemyUnit.handleAttackEnemyUnits(unit)) {
//            return true;
//        }

        // If nearby enemies would likely defeat us, retreat
        if (shouldRetreat(unit)) {
            return true;
        }

        // Handle repair of mechanical units
        if (AGame.isPlayingAsTerran() && ARepairManager.handleUnitUnderRepair(unit)) {
            return true;
        }

        if (AAttackEnemyUnit.handleAttackEnemyUnits(unit)) {
            return true;
        }

        // Normal mode - Attack enemy units when in range (and choose the best target)
//        if (!isAllowedToAttackDespiteRetreating && AAttackEnemyUnit.handleAttackEnemyUnits(unit)) {
//            return true;
//        }

        return false;
    }

    private static boolean canHandleLowPriority(AUnit unit) {
        if (unit.isRunning() && unit.getLastUnitOrderWasFramesAgo() <= 6) {
            unit.setTooltip("Run");
            return true;
        }

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

    private static boolean handledSpecially(AUnit unit) {
        if (handledUsingDedicatedUnitManager(unit)) {
            return true;
        }

        if (handledSpecialUnit(unit)) {
            return true;
        }

        return false;
    }

    /**
     * Some actions are too important/costly. No matter what happens, don't interrupt unit at this point.
     */
    private static boolean shouldNotDisturbUnit(AUnit unit) {
        return
                // is in middle of shooting
                unit.isAttackFrame()
                // OR is starting attack
                || (unit.isStartingAttack() && unit.getGroundWeaponCooldown() <= 0)
//                ((!unit.type().isTank() || unit.getGroundWeaponCooldown() <= 0) && unit.isStartingAttack())
//                && unit.getGroundWeaponCooldown() <= 0 && unit.getAirWeaponCooldown() <= 0;
                ;
    }

    /**
     * There are some units that should have individual micro managers like Zerg Overlord. If unit is "dedicated unit"
     * it will use its own manager and return true, meaning no other managers should be used.
     *
     * Returning false allows standard micro managers to be used.
     */
    private static boolean handledUsingDedicatedUnitManager(AUnit unit) {
        // === Terran ========================================

        if (AGame.isPlayingAsTerran()) {
            if (unit.isType(AUnitType.Terran_Medic)) {
                unit.setTooltip("Medic");
                return TerranMedic.update(unit);
            }
        }

        // === Zerg ========================================

        else if (AGame.isPlayingAsZerg()) {
            if (unit.getType().equals(AUnitType.Zerg_Overlord)) {
                ZergOverlordManager.update(unit);
                return true;
            }
        }

        // =========================================================

        return false;
    }

    /**
     * There are some units that should have additional micro manager actions like Siege Tank. If unit is
     * considered "special" it will use its micro managers first. If it then returns false, standard micro managers
     * can then be executed.
     */
    private static boolean handledSpecialUnit(AUnit unit) {
        if (unit.getType().isSiegeTank()) {
            return TerranSiegeTankManager.update(unit);
        } else if (unit.getType().isVulture()) {
            return TerranVultureManager.update(unit);
        } else if (unit.getType().isTerranInfantry()) {
            return TerranInfantryManager.update(unit);
        }

        // No special action happened - fallback to standard micro managers.
        else {
            return false;
        }
    }

    /**
     * Some units like Reavers should open fire to nearby enemies even when retreating, otherwise they'll
     * just get destroyed without firing even once.
     */
//    private static boolean isAllowedToAttackBeforeRetreating(AUnit unit) {
//        return unit.isType(AUnitType.Protoss_Reaver, AUnitType.Terran_Vulture) && unit.getHPPercent() > 10;
//    }

}
