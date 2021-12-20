package atlantis.repair;

import atlantis.AGame;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.A;

import java.util.Collection;
import java.util.Iterator;


public class ARepairerManager {

    public static boolean updateRepairer(AUnit repairer) {
        if (!repairer.isScv()) {
            throw new RuntimeException(repairer + " is not SCV!");
        }

        repairer.setTooltip("Repairer");

        if (handleRepairerSafety(repairer)) {
            return true;
        }

        return handleRepairs(repairer);
    }

    // =========================================================

    private static boolean handleRepairs(AUnit repairer) {
        AUnit target = ARepairAssignments.getUnitToRepairFor(repairer);

        if (target == null || !target.isAlive()) {
            repairer.setTooltip("TargetRIP");
//            System.err.println("Invalid repair target: " + target + ", alive:" + (target != null ? target.isAlive() : "-"));
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
        if (!repairer.isRepairing() && target.isAlive() && A.hasMinerals(5)) {
            if (repairer.lastActionMoreThanAgo(30 * 3)) {
                ARepairAssignments.removeRepairerOrProtector(repairer);
                repairer.setTooltip("IdleGTFO");
                return false;
            }

            return repairer.repair(
                    target,
                    "Repair " + target.shortNameWithId() + "(" + repairer.lastOrderFramesAgo() + ")"
            );
        }

        if (repairer.isRepairing()) {
            repairer.setTooltip("::repair::");
            return true;
        }

        if (!ARepairAssignments.isProtector(repairer) && repairer.lastActionMoreThanAgo(30 * 2)) {
            System.err.println("Idle repairer, remove. Target was = " + target + " // " + target.hp() + " // " + target.isAlive());
            ARepairAssignments.removeRepairerOrProtector(repairer);
            repairer.setTooltip("GoHome");
        }
        return false;
    }

    private static boolean handleRepairerSafety(AUnit repairer) {
        if ((!repairer.isRepairing() || repairer.hpPercent() <= 30) && AAvoidUnits.avoidEnemiesIfNeeded(repairer)) {
            repairer.setTooltip("FuckThisJob");
            return true;
        }

        return false;
    }

    public static boolean itIsForbiddenToRepairThisUnitNow(AUnit target) {
        if (!target.isBuilding() || target.isCombatBuilding()) {
            return false;
        }

        return Select.enemyCombatUnits().inRadius(12, target).atLeast(1);
    }

    // =========================================================

    protected static boolean assignRepairersToWoundedUnits(AUnit unitToRepair, int numberOfRepairersToAssign) {
        for (int i = 0; i < numberOfRepairersToAssign; i++) {
            boolean isCriticallyImportant = unitToRepair.isTank() || unitToRepair.isBunker();
            AUnit worker = repairerFor(unitToRepair, isCriticallyImportant);
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

        AUnit closestUnitNeedingRepair = Select.our().repairable(true).inRadius(15, repairer).first();
        if (closestUnitNeedingRepair != null && A.hasMinerals(5)) {
            ARepairAssignments.addRepairer(repairer, closestUnitNeedingRepair);
            repairer.repair(closestUnitNeedingRepair, "Extra repair");
            return true;
        }

        return false;
    }

    protected static boolean handleIdleRepairer(AUnit repairer) {
        if (!repairer.isUnitActionRepair() || !repairer.isRepairing() || repairer.isIdle()) {
            int maxAllowedDistToRoam = 13;

            // Try finding any repairable and wounded unit nearby
            AUnit nearestWoundedUnit = Select.our().repairable(true).inRadius(maxAllowedDistToRoam, repairer).nearestTo(repairer);
            if (nearestWoundedUnit != null && A.hasMinerals(5)) {
                repairer.repair(nearestWoundedUnit, "Help near " + nearestWoundedUnit.shortName());
                return true;
            }
        }
        
        return false;
    }

    protected static AUnit repairerFor(AUnit unitToRepair, boolean criticallyImportant) {
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
            return Select.from(protectors, "protectors").nearestTo(unitToRepair);
        }

        // If no free protector was found, return normal worker.
        else {
            return Select.ourWorkers()
                    .notCarrying()
                    .notRepairing()
                    .notGatheringGas()
                    .notConstructing()
                    .notScout()
                    .exclude(unitToRepair)
                    .nearestTo(unitToRepair);
        }
    }

}
