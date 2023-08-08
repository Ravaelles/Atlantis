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
            && (enemiesNear = unit.enemiesNear().groundUnits().canAttack(unit, 2.6)).isEmpty();
    }

    public Manager handle() {
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
}
