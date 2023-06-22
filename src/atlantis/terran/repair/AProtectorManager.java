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

    // =========================================================

    public static void handleProtectors() {
        if (AGame.everyNthGameFrame(9)) {
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
        if (Count.bunkers() == 0) {
            return false;
        }

        // =========================================================

        for (AUnit bunker : Select.ourOfType(AUnitType.Terran_Bunker).list()) {
            Selection enemies = bunker.enemiesNear().havingWeapon().canAttack(bunker, 10);

            // No enemies + bunker healthy
            if (enemies.size() <= 1 && (enemies.isEmpty() || bunker.loadedUnits().isEmpty()) && bunker.isHealthy()) {
                ArrayList<AUnit> protectors = ARepairAssignments.getProtectorsFor(bunker);
                ArrayList<AUnit> toRemove = new ArrayList<>();
                toRemove.addAll(protectors);

                for (AUnit protector : toRemove) {
                    ARepairAssignments.removeRepairer(protector);
                }
            }

            // Bunker damaged or enemies nearby
            else {
                int desiredBunkerProtectors = RepairerAssigner.optimalRepairersFor(bunker);
                assignProtectorsFor(bunker, desiredBunkerProtectors);
            }
        }

        return true;
    }

    private static int maxProtectors() {
        int workers = Count.workers();

        if (!A.hasMinerals(1) && workers <= 20) {
            return 3;
        }
        else if (!A.hasMinerals(10)) {
            return Math.min(workers / 2, MAX_PROTECTORS);
        }

        return MAX_PROTECTORS;
    }

    protected static boolean assignUnitsProtectorsIfNeeded() {
        int maxProtectors = maxProtectors();
        int totalProtectors = ARepairAssignments.countTotalProtectors();
        if (totalProtectors >= maxProtectors) {
            return false;
        }

        List<AUnit> tanks = Select.ourTanks().list();
        if (tanks.isEmpty()) {
            return false;
        }

        for (int i = 0; i < maxProtectors - totalProtectors; i++) {
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

        int maxProtectors = maxProtectors();
        if (ARepairAssignments.countTotalProtectors() > maxProtectors) {
            for (int i = 0; i < ARepairAssignments.countTotalProtectors() - maxProtectors; i++) {
                AUnit protector = ARepairAssignments.getProtectors().get(ARepairAssignments.getProtectors().size() - 1);
                if (ARepairerManager.canSafelyAbandonUnitToBeRepaired(protector)) {
                    ARepairAssignments.removeRepairer(protector);
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

        numberOfProtectorsToAssign = numberOfProtectorsToAssign - ARepairAssignments.countProtectorsFor(unitToProtect);

        if (numberOfProtectorsToAssign <= 0) {
            return;
        }

        for (int i = 0; i < numberOfProtectorsToAssign; i++) {
            AUnit worker = ARepairerManager.repairerFor(
                unitToProtect,
                unitToProtect.isBunker() || unitToProtect.isTank()
            );
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
        if (protector.isRepairing()) {
            return true;
        }

        AUnit target = ARepairAssignments.getUnitToProtectFor(protector);
//        System.out.println("protecting: " + target + "; "  + protector + " (" + protector.action() + ")");

        if (target != null && target.isAlive()) {

            // WOUNDED
            if (target.isWounded() || target.enemiesNear().canAttack(target, 15).notEmpty()) {
                if (protector.isRepairing()) {
                    return true;
                }

                return protector.repair(target, "Protect" + target.name(), true);
//                return protector.repair(Select.main(), "Protect" + target.name(), true);
//                return protector.doRightClickAndYesIKnowIShouldAvoidUsingIt(target);
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
            ARepairAssignments.removeRepairer(protector);
            return true;
        }

        return ARepairerManager.handleIdleRepairer(protector);
    }
}
