package atlantis.terran.repair;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;


public class RepairerManager extends Manager {

    public RepairerManager(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (!unit.isScv()) {
            throw new RuntimeException(unit + " is not SCV!");
        }

        unit.setTooltipTactical("Repairer");

        if (handleRepairerSafety()) {
            return usedManager(this);
        }

        return handleRepairs();
    }

    // =========================================================

    private Manager handleRepairs() {
        AUnit target = RepairAssignments.getUnitToRepairFor(unit);

        if (target == null || !target.isAlive()) {
            unit.setTooltipTactical("TargetRIP");
//            System.err.println("Invalid repair target: " + target + ", alive:" + (target != null ? target.isAlive() : "-"));
            RepairAssignments.removeRepairer(unit);
            return usedManager(this);
        }

        // Target is totally healthy
        if (!target.isWounded()) {
            unit.setTooltipTactical("Repaired!");
            RepairAssignments.removeRepairer(unit);
            return handleRepairCompletedTryFindingNewTarget();
        }

        if (unit.lastActionLessThanAgo(3, Actions.REPAIR)) {
            unit.setTooltip("Repairin");
            return usedManager(this);
        }

        // Target is wounded
        if (!unit.isRepairing() && target.isAlive() && A.hasMinerals(5)) {
            if (unit.lastActionMoreThanAgo(30 * 3, Actions.REPAIR) || unit.isIdle() || unit.isStopped()) {
                RepairAssignments.removeRepairer(unit);
                unit.setTooltipTactical("IdleGTFO");
                unit.gatherBestResources();
                return usedManager(this);
            }

            unit.repair(
                    target,
                    "Repair " + target.nameWithId() + "(" + unit.lastActionFramesAgo() + ")",
                    true
            );
            return usedManager(this);
        }

        if (unit.isRepairing()) {
            unit.setTooltipTactical("::repair::");
            return usedManager(this);
        }

        if (!RepairAssignments.isProtector(unit) && unit.lastActionMoreThanAgo(30 * 2)) {
//            System.err.println("Idle unit, remove. Target was = " + target + " // " + target.hp() + " // " + target.isAlive());
            RepairAssignments.removeRepairer(unit);
            unit.setTooltipTactical("GoHome");
            unit.gatherBestResources();
            return usedManager(this);
        }

        return null;
    }

    private boolean handleRepairerSafety() {
        if (
            (!unit.isRepairing() || unit.hpPercent() <= 30)
            && (new AvoidEnemies(unit)).avoidEnemiesIfNeeded() != null
        ) {
            unit.setTooltipTactical("FuckThisJob");
            return true;
        }

        return false;
    }

    public static boolean itIsForbiddenToRepairThisUnitNow(AUnit target) {
        if (target.isBuilding() && target.isCombatBuilding()) {
            if (target.type().isMilitaryBuildingAntiAir()) {
                return target.enemiesNear().groundUnits().inRadius(4, target).atLeast(2);
            }
            return false;
        }

        if (target.isBuilding() && target.hp() >= 600 && !target.isBase()) {
            return true;
        }

        return false;
//
//        return target.enemiesNear().inRadius(12, target).atLeast(1);
    }

    // =========================================================

    /**
     * The repair of unit assigned has finished, but instead of unproductively going back to base,
     * try finding new repairable unit.
     */
    private Manager handleRepairCompletedTryFindingNewTarget() {
        RepairAssignments.removeRepairer(unit);

        AUnit closestUnitNeedingRepair = Select.our().repairable(true).inRadius(15, unit).first();
        if (closestUnitNeedingRepair != null && A.hasMinerals(5)) {
            RepairAssignments.addRepairer(unit, closestUnitNeedingRepair);
            unit.repair(closestUnitNeedingRepair, "Extra repair", true);
            return usedManager(this);
        }

        return null;
    }
}
