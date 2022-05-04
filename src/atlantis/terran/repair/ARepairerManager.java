package atlantis.terran.repair;

import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.Collection;
import java.util.Iterator;


public class ARepairerManager {

    public static boolean updateRepairer(AUnit repairer) {
        if (!repairer.isScv()) {
            throw new RuntimeException(repairer + " is not SCV!");
        }

        repairer.setTooltipTactical("Repairer");

        if (handleRepairerSafety(repairer)) {
            return true;
        }

        return handleRepairs(repairer);
    }

    // =========================================================

    private static boolean handleRepairs(AUnit repairer) {
        AUnit target = ARepairAssignments.getUnitToRepairFor(repairer);

        if (target == null || !target.isAlive()) {
            repairer.setTooltipTactical("TargetRIP");
//            System.err.println("Invalid repair target: " + target + ", alive:" + (target != null ? target.isAlive() : "-"));
            ARepairAssignments.removeRepairer(repairer);
            return false;
        }

        // Target is totally healthy
        if (!target.isWounded()) {
            repairer.setTooltipTactical("Repaired!");
            ARepairAssignments.removeRepairer(repairer);
            return handleRepairCompletedTryFindingNewTarget(repairer);
        }

        // Target is wounded
        if (!repairer.isRepairing() && target.isAlive() && A.hasMinerals(5)) {
            if (repairer.lastActionMoreThanAgo(30 * 3, Actions.REPAIR) || repairer.isIdle() || repairer.isStopped()) {
                ARepairAssignments.removeRepairer(repairer);
                repairer.setTooltipTactical("IdleGTFO");
                repairer.gatherBestResources();
                return true;
            }

            return repairer.repair(
                    target,
                    "Repair " + target.nameWithId() + "(" + repairer.lastActionFramesAgo() + ")",
                    true
            );
        }

        if (repairer.isRepairing()) {
            repairer.setTooltipTactical("::repair::");
            return true;
        }

        if (!ARepairAssignments.isProtector(repairer) && repairer.lastActionMoreThanAgo(30 * 2)) {
//            System.err.println("Idle repairer, remove. Target was = " + target + " // " + target.hp() + " // " + target.isAlive());
            ARepairAssignments.removeRepairer(repairer);
            repairer.setTooltipTactical("GoHome");
            repairer.gatherBestResources();
            return true;
        }
        return false;
    }

    private static boolean handleRepairerSafety(AUnit repairer) {
        if ((!repairer.isRepairing() || repairer.hpPercent() <= 30) && AvoidEnemies.avoidEnemiesIfNeeded(repairer)) {
            repairer.setTooltipTactical("FuckThisJob");
            return true;
        }

        return false;
    }

    public static boolean itIsForbiddenToRepairThisUnitNow(AUnit target) {
        if (!target.isBuilding() || target.isCombatBuilding()) {
            return false;
        }

        return target.enemiesNear().inRadius(12, target).atLeast(1);
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
        ARepairAssignments.removeRepairer(repairer);

        AUnit closestUnitNeedingRepair = Select.our().repairable(true).inRadius(15, repairer).first();
        if (closestUnitNeedingRepair != null && A.hasMinerals(5)) {
            ARepairAssignments.addRepairer(repairer, closestUnitNeedingRepair);
            repairer.repair(closestUnitNeedingRepair, "Extra repair", true);
            return true;
        }

        return false;
    }

    protected static boolean handleIdleRepairer(AUnit repairer) {
        if (!repairer.isUnitActionRepair() || !repairer.isRepairing() || repairer.isIdle()) {
            int maxAllowedDistToRoam = 13;

            // Try finding any repairable and wounded unit Near
            AUnit nearestWoundedUnit = Select.our().repairable(true).inRadius(maxAllowedDistToRoam, repairer).nearestTo(repairer);
            if (nearestWoundedUnit != null && A.hasMinerals(5)) {
                repairer.repair(nearestWoundedUnit, "HelpNear" + nearestWoundedUnit.name(), true);
                return true;
            }
        }
        
        return false;
    }

    protected static AUnit repairerFor(AUnit unitToRepair, boolean criticallyImportant) {
        if (criticallyImportant) {
            Selection candidates = Select.ourWorkers().notRepairing();
            if (!OurStrategy.get().isRush()) {
                candidates = candidates.notScout().notConstructing();
            }
            return candidates.exclude(unitToRepair).nearestTo(unitToRepair);
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

    public static boolean canSafelyAbandonUnitToBeRepaired(AUnit repairer) {
        if (!repairer.isAlive()) {
            return true;
        }

        AUnit target = ARepairAssignments.getUnitToRepairFor(repairer);
        if (target == null) {
            target = ARepairAssignments.getUnitToProtectFor(repairer);
        }

        if (target == null || target.isNeutral() || !target.isAlive()) {
//            System.err.println("repairer = " + repairer);
//            System.err.println("target = " + target);
//            A.printStackTrace("WTF, why here?");
            return true;
        }

        if (target.isWounded()) {
            return false;
        }

        if (repairer.isProtector() && target.isBunker() && target.enemiesNear().count() <= 1) {
            return true;
        }

        Selection enemies = target.enemiesNear().canAttack(target, 14);
        int workersNearby = target.friendsNear().workers().inRadius(1.5, target).count();

        return enemies.isEmpty() || (workersNearby >= 3 && enemies.count() < workersNearby);
//        return target.enemiesNear().canAttack(target, 14).isEmpty();
    }
}
