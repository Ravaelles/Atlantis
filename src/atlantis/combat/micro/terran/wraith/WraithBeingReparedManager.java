package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class WraithBeingReparedManager extends Manager {
    private AUnit repairer;
    private Selection enemiesNear;

    public WraithBeingReparedManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWraith()
            && unit.isWounded()
            && (repairer = unit.repairer()) != null
            && (
            (enemiesNear = unit.enemiesNear().canAttack(unit, -1 - unit.woundPercent() / 40.0)).isEmpty()
        );
    }

    protected Manager handle() {
        double distanceToRepairer = repairer.distTo(unit);

        int repairers = RepairAssignments.countRepairersForUnit(unit);
        if (
            (distanceToRepairer <= 1.7 || repairers >= 2)
                && repairer.isRepairing()
                && (unit.woundPercent() >= 15 || unit.enemiesNear().groundUnits().canBeAttackedBy(unit, -0.2).empty())
        ) {
            if (unit.isAccelerating()) {
                unit.holdPosition(null);
            }
            unit.setTooltip("WaitRepair(" + repairers + ")");
            return usedManager(this);
        }

//        if (unit.cooldown() >= 3 && distanceToRepairer > 1 && distanceToRepairer <= 5) {
        if (unit.cooldown() >= 3 && distanceToRepairer > 1) {
            unit.move(repairer, Actions.MOVE_REPAIR, "2Repair");
            return usedManager(this);
        }

        return null;
    }
}
