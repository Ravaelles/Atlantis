package atlantis.terran.repair;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Protector is a unit that is close to another unit (bunker or tank), ready to repair it,
 * even if it's not wounded (yet) or already repaired.
 */
public class AProtectorManager {

    private static final int MAX_PROTECTORS = 3;
    private static final int MAX_BUNKER_PROTECTORS = 3;

    // =========================================================

    public static void handleProtectors() {
        if (AGame.everyNthGameFrame(13)) {
            assignBunkerProtectorsIfNeeded();

            if (!removeProtectorsIfNeeded()) {
                assignUnitsProtectorsIfNeeded();
            }
        }

        for (Iterator<AUnit> iterator = ARepairAssignments.getProtectors().iterator(); iterator.hasNext(); ) {
            AUnit protector = iterator.next();
            AProtectorManager.updateProtector(protector);
        }
    }

    // =========================================================

    protected static boolean assignBunkerProtectorsIfNeeded() {
//        if (Missions.isGlobalMissionAttack()) {
//            return;
//        }

        if (Count.bunkers() == 0) {
            return false;
        }

        AUnit mainBase = Select.main();
        if (mainBase == null && !A.isUms()) {
            return false;
        }

        // =========================================================

        for (AUnit bunker : Select.ourOfType(AUnitType.Terran_Bunker).list()) {
            Selection enemies = bunker.enemiesNear().havingWeapon();
            if (enemies.isEmpty() && bunker.isHealthy()) {
                ArrayList<AUnit> protectors = ARepairAssignments.getProtectorsFor(bunker);
                for (Iterator<AUnit> it = protectors.iterator(); it.hasNext(); ) {
                    AUnit repairer = it.next();
                    ARepairAssignments.removeRepairerOrProtector(repairer);
                }
                continue;
            }

            int desiredProtectorsForThisBunker = Math.min(MAX_BUNKER_PROTECTORS, enemies.count() - 1);
            assignProtectorsFor(bunker, desiredProtectorsForThisBunker);
        }

        return true;
    }

    protected static boolean assignUnitsProtectorsIfNeeded() {
        int totalProtectors = ARepairAssignments.countTotalProtectors();
        if (totalProtectors >= MAX_PROTECTORS) {
            return false;
        }

        List<AUnit> tanks = Select.ourTanks().list();
        if (tanks.isEmpty()) {
            return false;
        }

        for (int i = 0; i < MAX_PROTECTORS - totalProtectors; i++) {
            assignProtectorsFor(tanks.get(i % tanks.size()), 1);
        }

        if (ARepairAssignments.countTotalProtectors() == 0) {
            assignProtectorsFor(Select.ourOfType(AUnitType.Terran_Medic).last(), 1);
        }

//        if (!Missions.isGlobalMissionAttack() && !Missions.isGlobalMissionContain()) {
//            return;
//        }
//
//        // === Protect Vulture =================================
//
//        int vultures = Select.countOurOfType(AUnitType.Terran_Vulture);
//        if (vultures >= 2) {
//            AUnit firstVulture = Select.ourOfType(AUnitType.Terran_Vulture).first();
//            int protectors = ARepairAssignments.countProtectorsFor(firstVulture);
//            int lackingProtectors = 2 - protectors;
//            if (lackingProtectors > 0) {
//                assignProtectorsFor(firstVulture, lackingProtectors);
//                return;
//            }
//        }
//
//        // === Protect Tank ====================================
//
//        int tanks = Select.ourTanks().count();
//        if (tanks >= 2) {
//            AUnit firstTank = Select.ourTanks().first();
//            int protectors = ARepairAssignments.countProtectorsFor(firstTank);
//            int lackingProtectors = 2 - protectors;
//            if (lackingProtectors > 0) {
//                assignProtectorsFor(firstTank, lackingProtectors);
//                return;
//            }
//        }
        return false;
    }

    private static boolean removeProtectorsIfNeeded() {
//        System.out.println("PROT = " + ARepairAssignments.countTotalProtectors() + " // " + MAX_PROTECTORS);

        if (ARepairAssignments.countTotalProtectors() > MAX_PROTECTORS) {
            for (int i = 0; i < ARepairAssignments.countTotalProtectors() - MAX_PROTECTORS; i++) {
                AUnit protector = ARepairAssignments.getProtectors().get(ARepairAssignments.getProtectors().size() - 1);
                if (ARepairerManager.canSafelyAbandonRepairTarget(protector)) {
                    ARepairAssignments.removeRepairerOrProtector(protector);
                }
            }
            return true;
        }
        return false;
    }

    // =========================================================

    protected static void assignProtectorsFor(AUnit unitToProtect, int numberOfProtectorsToAssign) {
        if (unitToProtect == null || unitToProtect.isDead()) {
            return;
        }

        for (int i = 0; i < numberOfProtectorsToAssign; i++) {
            AUnit worker = ARepairerManager.repairerFor(unitToProtect, false);
            if (worker != null) {
                ARepairAssignments.addProtector(worker, unitToProtect);
            }
        }
    }

    // =========================================================

//    protected static int defineOptimalNumberOfBunkerProtectors() {
//
//        // === Mission DEFEND  =================================
//        if (Missions.isGlobalMissionDefend()) {
//            if (AGame.isPlayingAsTerran()) {
//
//                // === We know enemy strategy ========================================
//                if (EnemyStrategy.isEnemyStrategyKnown()) {
//                    int repairersWhenRush = 1
//                        + (AGame.timeSeconds() > 180 ? 1 : 0)
//                        + (AGame.timeSeconds() > 200 ? 1 : 0);
//
//                    if (EnemyStrategy.get().isGoingCheese()) {
//                        return repairersWhenRush
//                            + 2
//                            + (AGame.timeSeconds() > 210 ? 1 : 0);
//                    }
//                    if (EnemyStrategy.get().isRush()) {
//                        return repairersWhenRush;
//                    }
//                }
//
//                // === We don't know enemy strategy ==================================
//                else {
//                    int enemyRaceBonus = !AGame.isEnemyTerran() && AGame.timeSeconds() > 175 ? 1 : 0;
//                    return 1 + (AGame.timeSeconds() > 280 ? 1 : 0) + enemyRaceBonus;
//                }
//            }
//
//            // === Only Terran can repair buildings ==================================
//            else {
//                return 0;
//            }
//        }
//
//        return 0;
//    }

    // =========================================================

    public static boolean updateProtector(AUnit protector) {
//        if (protector.hpLessThan(10)) {
//            ARepairAssignments.removeRepairerOrProtector(protector);
//            return false;
//        }

        AUnit target = ARepairAssignments.getUnitToProtectFor(protector);
        if (target != null && target.isAlive()) {
//            if (A.everyNthGameFrame(39)) {
//                if (target.enemiesNear().havingWeapon().isEmpty()) {
//                    ARepairAssignments.removeRepairerOrProtector(protector);
//                    return false;
//                }
//            }

            // WOUNDED
//            if (target.isWounded() && A.hasMinerals(1)) {
            if (A.hasMinerals(1)) {
                return protector.repair(target, "Protect" + target.name(), true);
//                return true;
            }

            // Bunker fully HEALTHY
            else {
                double distanceToUnit = target.distTo(protector);
                if (distanceToUnit > 0.7 && !protector.isMoving()) {
                    return protector.move(
                        target.position(), Actions.MOVE_REPAIR, "ProtectNearer" + target.name(), true
                    );
                }
                else {
                    protector.setTooltipTactical("Protecting" + target.name());
                }
            }
        }
        else {
            protector.setTooltipTactical("Null bunker");
            ARepairAssignments.removeRepairerOrProtector(protector);
            return true;
        }

        return ARepairerManager.handleIdleRepairer(protector);
    }
}
