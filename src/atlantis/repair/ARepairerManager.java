package atlantis.repair;

import atlantis.AGame;
import atlantis.buildings.managers.TerranFlyingBuildingManager;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.combat.missions.Missions;
import atlantis.scout.AScoutManager;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;
import atlantis.units.select.Select;

import java.util.Collection;
import java.util.Iterator;


public class ARepairerManager {

    private static final int MAX_REPAIRERS = 5;

    public static boolean updateRepairer(AUnit repairer) {
        repairer.setTooltip("Repairer");
        if (handleRepairerSafety(repairer)) {
            return true;
        }

        return handleRepairs(repairer);
    }

    // =========================================================

    private static boolean handleRepairerSafety(AUnit repairer) {
        if ((!repairer.isRepairing() || repairer.hpPercent() <= 30) && AAvoidUnits.avoidEnemiesIfNeeded(repairer)) {
            repairer.setTooltip("Aaa!");
            return true;
        }

        return false;
    }

    private static boolean handleRepairs(AUnit repairer) {
        AUnit target = ARepairAssignments.getUnitToRepairFor(repairer);
        if (target == null || !target.isAlive()) {
            repairer.setTooltip("Null unit2repair");
            ARepairAssignments.removeRepairerOrProtector(repairer);
            return false;
        }

        // Target is totally healthy
        if (!target.isWounded()) {
            repairer.setTooltip("Repaired!");
            ARepairAssignments.removeRepairerOrProtector(repairer);
            return handleRepairCompletedTryFindingNewTarget(repairer);
        }

        // Target is wounded
        if (!repairer.isRepairing()) {
            if (target.isWounded() && target.isAlive()) {
                repairer.repair(
                        target,
                        "Repair " + target.shortNamePlusId() + "(" + repairer.getLastOrderFramesAgo() + ")"
                );
                return true;
            }
        }

        if (!ARepairAssignments.isProtector(repairer)) {
            ARepairAssignments.removeRepairerOrProtector(repairer);
        }
        return false;

//        // Move to closest tank
//        AUnit nearestTank = Select.ourTanks().nearestTo(repairer);
//        if (nearestTank != null) {
//            return repairer.move(nearestTank, UnitActions.MOVE, "CoverHim");
//        }
//
//        return false;
    }

    // =========================================================

    protected static boolean assignRepairersToWoundedUnits(AUnit unitToRepair, int numberOfRepairersToAssign) {
        for (int i = 0; i < numberOfRepairersToAssign; i++) {
            boolean isCriticallyImportant = unitToRepair.isTank() || unitToRepair.isBunker();
            AUnit worker = defineBestRepairerFor(unitToRepair, isCriticallyImportant);
            if (worker != null) {
                if (AGame.isUms() && worker.distTo(unitToRepair) > 10 && !worker.hasPathTo(unitToRepair)) {
                    return false;
                }

                ARepairAssignments.addRepairer(worker, unitToRepair);
                return true;
            }
        }
        return false;
    }

    /**
     * The repair of unit assigned has finished, but instead of unproductively going back to base,
     * try finding new repairable unit.
     */
    private static boolean handleRepairCompletedTryFindingNewTarget(AUnit repairer) {
        ARepairAssignments.removeRepairerOrProtector(repairer);

        if (!hasMoreRepairersThanAllowed()) {
            AUnit closestUnitNeedingRepair = Select.our().repairable(true).inRadius(13, repairer).first();
            if (closestUnitNeedingRepair != null) {
                ARepairAssignments.addRepairer(closestUnitNeedingRepair, closestUnitNeedingRepair);
                repairer.repair(closestUnitNeedingRepair, "Extra repair");
            }
            return true;
        }

        return true;
    }

    private static boolean hasMoreRepairersThanAllowed() {
        return ARepairAssignments.countTotalRepairers() < MAX_REPAIRERS;
    }

    protected static boolean handleIdleRepairer(AUnit repairer) {
//        if (repairer.isMoving() || !repairer.isRepairing() || repairer.isIdle()) {
        if (!repairer.isUnitActionRepair() || !repairer.isRepairing() || repairer.isIdle()) {
//            int maxAllowedDistToRoam = Missions.globalMission().isMissionDefend() ? 4 : 13;
            int maxAllowedDistToRoam = 13;

            // Try finding any repairable and wounded unit nearby
            AUnit nearestWoundedUnit = Select.our().repairable(true).inRadius(maxAllowedDistToRoam, repairer).nearestTo(repairer);
            if (nearestWoundedUnit != null) {
                repairer.repair(nearestWoundedUnit, "Help near " + nearestWoundedUnit.shortName());
                return true;
            }
        }
        
        return false;
    }

    protected static AUnit defineBestRepairerFor(AUnit unitToRepair, boolean criticallyImportant) {
        if (criticallyImportant) {
            return Select.ourWorkers().notRepairing().notConstructing().notScout()
                    .exclude(unitToRepair).nearestTo(unitToRepair);
        }

        // Try to use one of the protectors if he's non occupied
        Collection<AUnit> protectors = ARepairAssignments.getProtectors();
        for (Iterator<AUnit> iterator = protectors.iterator(); iterator.hasNext();) {
            AUnit protector = iterator.next();
            if (protector.isUnitActionRepair()) {
                iterator.remove();
            }
        }

        if (!protectors.isEmpty()) {
            return Select.from(protectors).nearestTo(unitToRepair);
        }

        // If no free protector was found, return normal worker.
        else {
            return Select.ourWorkers()
                    .notCarrying()
                    .notRepairing()
                    .notConstructing()
                    .notScout()
                    .exclude(unitToRepair)
                    .nearestTo(unitToRepair);
        }
    }

    protected static void assignRepairersToWoundedUnits() {
//        if (ARepairAssignments.repairersToUnit.keySet().size() >= Count.workers() * MAX_REPAIRERS)
        if (removeExcessiveRepairersIfNeeded()) {
            return;
        }

        for (AUnit woundedUnit : Select.ourRealUnits().repairable(true).listUnits()) {
            if (removeExcessiveRepairersIfNeeded()) {
                return;
            }

            // Some units shouldn't be repaired
            if (
                    AScoutManager.isScout(woundedUnit)
                    || TerranFlyingBuildingManager.isFlyingBuilding(woundedUnit)
                    || (woundedUnit.isRunning() && woundedUnit.lastStartedRunningAgo() > 90)
            ) {
                continue;
            }

            // =========================================================

            int numberOfRepairers = ARepairAssignments.countRepairersForUnit(woundedUnit)
                    + ARepairAssignments.countProtectorsFor(woundedUnit);

            // === Repair bunker ========================================

            if (woundedUnit.type().isBunker()) {
                int shouldHaveThisManyRepairers = ARepairCommander.defineOptimalRepairersForBunker(woundedUnit);
                ARepairCommander.assignProtectorsFor(woundedUnit, shouldHaveThisManyRepairers - numberOfRepairers);
            }

            // === Repair ordinary unit =================================

            else {
                assignRepairersToWoundedUnits(woundedUnit, 1 - numberOfRepairers);
            }
        }
    }

    private static boolean removeExcessiveRepairersIfNeeded() {
//        System.out.println("REPR = " + ARepairAssignments.countTotalRepairers() + " // " + MAX_REPAIRERS);
        if (ARepairAssignments.countTotalRepairers() >= MAX_REPAIRERS) {
            for (int i = 0; i < ARepairAssignments.countTotalRepairers() - MAX_REPAIRERS; i++) {
                AUnit repairer = ARepairAssignments.getRepairers().get(ARepairAssignments.getRepairers().size() - 1);
//                System.out.println("Remove repairer " + repairer);
                ARepairAssignments.removeRepairerOrProtector(repairer);
            }
            return true;
        }

        return false;
    }
}
