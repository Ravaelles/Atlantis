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

    @Override
    public boolean applies() {
        return unit.isRepairerOfAnyKind();
    }

    @Override
    protected Manager handle() {
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

        if (shouldRemoveRepairer(target)) {
            RepairAssignments.removeRepairer(unit);
            return null;
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
            if (unit.lastActionMoreThanAgo(30 * 3, Actions.REPAIR) || unit.isIdle() || unit.looksIdle()) {
                RepairAssignments.removeRepairer(unit);
                unit.setTooltipTactical("IdleGTFO");
                unit.gatherBestResources();
                return usedManager(this);
            }

            // Don't interrupt tanks when running
            if (
                target.isTankUnsieged()
                    && target.isRunning()
                    && target.enemiesNear().groundUnits().canAttack(target, 1.4).notEmpty()
            ) {
                unit.runningManager().runFrom(target, 0.50, Actions.MOVE_SPACE, false);
                return usedManager(this);
            }

            unit.repair(
                target,
                "Repair " + target.nameWithId() + "(" + unit.lastActionFramesAgo() + ")"
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

    private boolean shouldRemoveRepairer(AUnit target) {
        if (target == null || !target.isAlive()) return true;
        if ((unit.looksIdle() && !unit.isProtector()) || (!unit.isRepairing() && !unit.isMoving())) return true;
        if (unit.lastActionMoreThanAgo(10, Actions.REPAIR)) return true;
        if (ShouldNotRepairUnit.shouldNotRepairUnit(target)) return true;

        return false;
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
        if (target.isABuilding() && target.isCombatBuilding()) {
            if (target.type().isMilitaryBuildingAntiAir()) {
                return target.enemiesNear().groundUnits().inRadius(4, target).atLeast(2);
            }
            return false;
        }

        if (target.isABuilding() && target.hp() >= 600 && !target.isBase()) return true;

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
            unit.repair(closestUnitNeedingRepair, "Extra repair");
            return usedManager(this);
        }

        return null;
    }
}
