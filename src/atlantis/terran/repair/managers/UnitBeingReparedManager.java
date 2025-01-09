package atlantis.terran.repair.managers;

import atlantis.architecture.Manager;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class UnitBeingReparedManager extends Manager {
    private AUnit repairer;
    private Selection enemiesNear;

    public UnitBeingReparedManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMechanical()
            && unit.isWounded()
            && unit.isTerran()
//            && !unit.isRunning()
            && (repairer = unit.repairer()) != null
            && (
            (
                (enemiesNear = unit.enemiesNear().groundUnits().canAttack(unit, 0.5)).isEmpty()
                    || RepairAssignments.countRepairersForUnit(unit) >= 2
            )
        );
    }

    protected Manager handle() {
        double distanceToRepairer = repairer.distTo(unit);

        if (handleRun()) return usedManager(this, "ShitRun");

        if (
            distanceToRepairer <= 1
//                && repairer.isRepairing()
                && (unit.woundPercent() >= 10 || unit.enemiesNear().groundUnits().canBeAttackedBy(unit, -0.2).empty())
        ) {
            if (!unit.isAttacking()) unit.holdPosition(Actions.HOLD_POSITION, "WaitRepair");
            return usedManager(this);
        }

        if (distanceToRepairer > 1 && distanceToRepairer <= 5) {
            unit.move(repairer, Actions.MOVE_REPAIR, "2Repair");
            return usedManager(this);
        }

        if (!unit.isWounded()) {
            RepairAssignments.removeRepairer(repairer);
            return null;
        }

        if (
            unit.isTank()
                && distanceToRepairer <= 0.1
                && unit.hp() <= 60
        ) {
            if (unit.isMoving() && unit.meleeEnemiesNearCount(2.5) == 0) {
                unit.holdPosition(Actions.HOLD_POSITION, "UnderRepair");
                return usedManager(this);
            }

            return null;
        }

        // Air units should be repaired thoroughly
        if (unit.isAir() && distanceToRepairer >= 0.2) {
            if (!unit.isRunning() && unit.isMoving()) {
//                unit.setTooltip("HoldTheFuckDown");
                unit.holdPosition(Actions.HOLD_POSITION, "HoldTheFuckDown");
                return usedManager(this);
            }
        }

        // Ignore going closer to repairer if unit is still relatively healthy
        if (!unit.isAir() && unit.hpPercent() > 80 && distanceToRepairer <= 1.7) return null;

        // =========================================================

        if (unit.nearestEnemyDist() <= (unit.hp() <= 60 ? 2.8 : 1.9)) {
            unit.setTooltip("DontRepairEnemy");
            return null;
        }

        // Go to repairer if he's close
        if (distanceToRepairer >= 0.4) {
            if (unit.move(repairer.position(), Actions.MOVE_REPAIR, "To repairer", false)) {
//                if (!repairer.isRepairing()) {
//                    repairer.repair(unit, ":: rep ::");
//                }
                return usedManager(this);
            }
        }

        if (!unit.isAttacking() || unit.enemiesNear().nonBuildings().empty()) {
            unit.holdPosition(Actions.HOLD_POSITION, "Be repaired");
        }

        return usedManager(this);
    }

    private boolean handleRun() {
        return shouldRun() && run();
    }

    private boolean run() {
        AUnit main = Select.mainOrAnyBuilding();
        if (main == null) return false;

        boolean shouldRunToMain = main.distTo(unit) >= 30;

        if (shouldRunToMain) {
            return unit.move(main, Actions.RUN_ENEMY, "BackToMain");
        }
        else {
            if (unit.runningManager().runFrom(main, 3, Actions.RUN_ENEMY, false)) {
                unit.setTooltip("RunFromClose");
                return true;
            }
        }

        return false;
    }

    private boolean shouldRun() {
        return (unit.woundPercent() >= 40 || unit.hasCooldown())
            && (
            unit.lastUnderAttackLessThanAgo(50)
                || unit.enemiesNear().canAttack(unit, 1.7).atLeast(1)
        );
    }
}
