package atlantis.terran.repair;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.managers.Manager;

public class UnitBeingReparedManager extends Manager {

    public UnitBeingReparedManager(AUnit unit) {
        super(unit);
    }

    public Manager handleUnitShouldBeRepaired() {
        if (!unit.isTerran() || !unit.isMechanical() || !unit.isWounded()) {
            return null;
        }

//        if (true) return true;

        AUnit repairer = ARepairAssignments.getClosestRepairerAssignedTo(unit);
        if (repairer == null) {
            return null;
        }

        double distanceToRepairer = repairer.distTo(unit);
        if (unit.isRunning()) {
            return null;
        }

        if (!unit.isWounded()) {
            ARepairAssignments.removeRepairer(repairer);
            return null;
        }

        // Air units should be repaired thoroughly
        if (unit.isAir() && unit.isBeingRepaired()) {
            if (!unit.isRunning() && unit.isMoving()) {
                unit.setTooltip("HoldTheFuckDown");
                return usedManager(this);
            }
        }

        // Ignore going closer to repairer if unit is still relatively healthy
        if (!unit.isAir() && unit.hpPercent() > 80 && distanceToRepairer >= 3) {
            return null;
        }

        // =========================================================

        if (unit.nearestEnemyDist() <= 1.7) {
            unit.setTooltip("DontRepairEnemy");
            return null;
        }

        // Go to repairer if he's close
        if (distanceToRepairer > 1) {
            if (unit.move(repairer.position(), Actions.MOVE_REPAIR, "To repairer", false)) {
                return usedManager(this);
            }
        }

        if (distanceToRepairer <= 1) {
            unit.holdPosition("Be repaired");
            return usedManager(this);
        }

        return null;
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