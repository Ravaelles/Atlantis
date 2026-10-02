package atlantis.terran.repair.repairer;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.avoid.special.AvoidPsionicStorm;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.units.AUnit;


public class RepairerManager extends Manager {
    public RepairerManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isScv() && unit.isRepairerOfAnyKind();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            AvoidSpellsAndMines::new,
            RepairerSafety::new,
            RemoveRepairer::new,
            IssueRepairCommand::new,
            SeparateFromRunningTanks::new,
        };
    }

    public static boolean itIsForbiddenToRepairThisUnitNow(AUnit unit, AUnit target) {
        if (target.isABuilding() && target.isCombatBuilding()) {
            if (target.type().isMilitaryBuildingAntiAir()) {
                return (unit == null || unit.distTo(target) >= 8)
                    && target.enemiesNear().groundUnits().inRadius(4, target).atLeast(2);
            }
            return false;
        }

        if (target.isABuilding() && target.hp() >= 600 && !target.isBase()) return true;

        return false;
    }
}
