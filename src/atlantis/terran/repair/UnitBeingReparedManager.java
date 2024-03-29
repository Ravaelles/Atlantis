package atlantis.terran.repair;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class UnitBeingReparedManager extends Manager {
    public UnitBeingReparedManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMechanical() && unit.isWounded() && unit.isTerran() && unit.repairer() != null && unit.enemiesNear().groundUnits().inRadius(7, unit).isEmpty();
    }

    public Manager handle() {
        if (!unit.isTerran() || !unit.isMechanical() || !unit.isWounded()) {
            return null;
        }

//        if (true) return true;

        AUnit repairer = RepairAssignments.getClosestRepairerAssignedTo(unit);
        if (repairer == null) {
            return null;
        }

        double distanceToRepairer = repairer.distTo(unit);
        if (unit.isRunning()) {
            return null;
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
            if (unit.enemiesNear().groundUnits().countInRadius(7, unit) > 0) {
                return null;
            }

            if (unit.isMoving()) {
                unit.holdPosition("UnderRepair");
                return usedManager(this);
            }

            return null;
        }

        // Air units should be repaired thoroughly
        if (unit.isAir() && (unit.isBeingRepaired() || unit.distTo(repairer) > 0.05)) {
            if (!unit.isRunning() && unit.isMoving()) {
//                unit.setTooltip("HoldTheFuckDown");
                unit.holdPosition("HoldTheFuckDown");
                return usedManager(this);
            }
        }

        // Ignore going closer to repairer if unit is still relatively healthy
        if (!unit.isAir() && unit.hpPercent() > 70 && distanceToRepairer >= 3) {
            return null;
        }

        // =========================================================

        if (unit.nearestEnemyDist() <= (unit.hp() <= 60 ? 2.8 : 1.7)) {
            unit.setTooltip("DontRepairEnemy");
            return null;
        }

        // Go to repairer if he's close
        if (distanceToRepairer > 0.2) {
            if (unit.move(repairer.position(), Actions.MOVE_REPAIR, "To repairer", false)) {
                return usedManager(this);
            }
        }

        unit.holdPosition("Be repaired");
        return usedManager(this);
    }

    public Manager handleDontRunWhenBeingRepared() {
        if (unit.enemiesNear().melee().inRadius(1.9, unit).canAttack(unit, 5).notEmpty()) {
            return null;
        }

        if (
            !unit.woundPercentMin(50)
                && unit.enemiesNear().ranged().inRadius(7, unit).notEmpty()
        ) {
            return null;
        }

        AUnit repairer = unit.repairer();
        if (repairer != null && repairer.distToLessThan(unit, 1.1) && repairer.isRepairing()) {
            unit.move(repairer, Actions.MOVE_REPAIR, "BeFixed");
            return usedManager(this);
        }

        return null;
    }
}
