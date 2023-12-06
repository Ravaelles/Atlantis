package atlantis.terran.repair.repairer;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.*;

public class RepairerSafety extends Manager {
    public RepairerSafety(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.hp() <= (Enemy.protoss() ? 32 : 18)
            && !unit.isProtector()
            &&
            !unit.targetIsOfType(
                Terran_Bunker, Terran_Siege_Tank_Siege_Mode, Terran_Siege_Tank_Tank_Mode, Terran_Missile_Turret
            );
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
