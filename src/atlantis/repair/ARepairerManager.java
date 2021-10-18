package atlantis.repair;

import atlantis.buildings.managers.TerranFlyingBuildingManager;
import atlantis.combat.micro.avoid.AAvoidEnemyDefensiveBuildings;
import atlantis.combat.micro.avoid.AAvoidEnemyMeleeUnits;
import atlantis.combat.micro.avoid.AAvoidInvisibleEnemyUnits;
import atlantis.combat.missions.Missions;
import atlantis.scout.AScoutManager;
import atlantis.units.AUnit;
import atlantis.units.Select;

import java.util.Collection;
import java.util.Iterator;


public class ARepairerManager {

    private static final int MAX_REPAIRERS = 7;

    public static boolean updateRepairer(AUnit repairer) {
        if (handleRepairerSafety(repairer)) {
            return true;
        }

        return handleRepairs(repairer);
    }

    // =========================================================

    private static boolean handleRepairerSafety(AUnit repairer) {
        if (repairer.getHPPercent() <= 50 && (new AAvoidEnemyMeleeUnits(repairer)).avoid()) {
            return true;
        }

        if (AAvoidInvisibleEnemyUnits.avoid(repairer)) {
            return true;
        }

        return AAvoidEnemyDefensiveBuildings.avoid(repairer, false);
    }

    private static boolean handleRepairs(AUnit repairer) {
        AUnit target = ARepairAssignments.getUnitToRepairFor(repairer);
        if (target == null || !target.isAlive()) {
            repairer.setTooltip("Null unit2repair");
            ARepairAssignments.removeRepairerOrProtector(repairer);
            return true;
        }

        // Target is totally healthy
        if (!target.isWounded()) {
            repairer.setTooltip("Repaired!");
            ARepairAssignments.removeRepairerOrProtector(repairer);
            return handleRepairCompletedTryFindingNewTarget(repairer);
        }

        // Target is wounded
        if (!repairer.isRepairing()) {
            repairer.repair(
                target,
                "Repair " + target.getShortNamePlusId() + "(" + repairer.getLastOrderFramesAgo() + ")"
            );
        }
        return true;
    }

    // =========================================================

    protected static void assignRepairersToWoundedUnits(AUnit unitToRepair, int numberOfRepairersToAssign) {
        for (int i = 0; i < numberOfRepairersToAssign; i++) {
            boolean isCriticallyImportant = unitToRepair.isTank() || unitToRepair.isBunker();
            AUnit worker = defineBestRepairerFor(unitToRepair, isCriticallyImportant);
            if (worker != null) {
                ARepairAssignments.addRepairer(worker, unitToRepair);
            }
        }
    }

    /**
     * The repair of unit assigned has finished, but instead of unproductively going back to base,
     * try finding new repairable unit.
     */
    private static boolean handleRepairCompletedTryFindingNewTarget(AUnit repairer) {
        AUnit closestUnitNeedingRepair = Select.our().repairable(true).inRadius(50, repairer).first();

        if (closestUnitNeedingRepair != null) {
            ARepairAssignments.addRepairer(closestUnitNeedingRepair, closestUnitNeedingRepair);
            repairer.repair(closestUnitNeedingRepair, "Extra repair");
            return true;
        }

        return false;
    }

    protected static boolean handleIdleRepairer(AUnit repairer) {
        if (repairer.isMoving() || !repairer.isRepairing() || repairer.isIdle()) {
            int maxAllowedDistToRoam = Missions.globalMission().isMissionDefend() ? 4 : 12;
            
            // Try finding any repairable and wounded unit nearby
            AUnit nearestWoundedUnit = Select.our().repairable(true)
                    .inRadius(maxAllowedDistToRoam, repairer).nearestTo(repairer);
            if (nearestWoundedUnit != null) {
                repairer.repair(nearestWoundedUnit, "Help near " + nearestWoundedUnit.getShortName());
//                repairer.setTooltip();
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
        if (ARepairAssignments.repairersToUnit.keySet().size() >= MAX_REPAIRERS) {
            return;
        }

        for (AUnit woundedUnit : Select.ourRealUnits().repairable(true).listUnits()) {

            // Some units shouldn't be repaired
            if (
                    AScoutManager.isScout(woundedUnit)
                    || TerranFlyingBuildingManager.isFlyingBuilding(woundedUnit)
                    || (woundedUnit.isRunning() && woundedUnit.lastStartedRunningAgo() > 60)
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
                assignRepairersToWoundedUnits(woundedUnit, 2 - numberOfRepairers);
            }
        }
    }
}
