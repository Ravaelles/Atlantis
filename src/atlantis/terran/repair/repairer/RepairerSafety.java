package atlantis.terran.repair.repairer;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;

public class RepairerSafety extends Manager {
    public RepairerSafety(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.hp() <= 18;
    }

    @Override
    public Manager handle() {
        unit.setTooltipTactical("FuckThisJob");
        RepairAssignments.removeRepairer(unit);

        (new AvoidEnemies(unit)).avoidEnemiesIfNeeded();

        return usedManager(this);

//        if (
//            (!unit.isRepairing() || unit.hpPercent() <= 30)
//                && (new AvoidEnemies(unit)).avoidEnemiesIfNeeded() != null
//        ) {
//            return true;
//        }
//
//        return false;
    }
}
