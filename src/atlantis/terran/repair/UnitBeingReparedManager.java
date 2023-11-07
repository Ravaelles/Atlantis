package atlantis.terran.repair;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
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
            && (repairer = unit.repairer()) != null
            && (
            (enemiesNear = unit.enemiesNear().groundUnits().canAttack(unit, -0.2)).isEmpty()
                || (unit.hp() >= 32 && RepairAssignments.countRepairersForUnit(unit) >= 2)
                && repairer != null
                && !unit.isRunning()
        );
    }

    protected Manager handle() {
        double distanceToRepairer = repairer.distTo(unit);

        if (unit.cooldown() >= 3 && distanceToRepairer > 1 && distanceToRepairer <= 5) {
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
                unit.holdPosition("UnderRepair");
                return usedManager(this);
            }

            return null;
        }

        // Air units should be repaired thoroughly
        if (unit.isAir() && distanceToRepairer >= 0.2) {
            if (!unit.isRunning() && unit.isMoving()) {
//                unit.setTooltip("HoldTheFuckDown");
                unit.holdPosition("HoldTheFuckDown");
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
                if (!repairer.isRepairing()) {
                    repairer.repair(unit, ":: rep ::");
                }
                return usedManager(this);
            }
        }

        if (!unit.isAttacking() || unit.enemiesNear().nonBuildings().empty()) {
            unit.holdPosition("Be repaired");
        }

        return usedManager(this);
    }
}
