package atlantis.combat.managers.zerg;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ZergCombatUnitManager extends Manager {
    public ZergCombatUnitManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.zerg() && unit.isCombatUnit() && !unit.isABuilding();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ZergCombatManagerTopPriority::new,
            ZergCombatManagerMediumPriority::new,
            ZergCombatManagerLowPriority::new,
        };
    }
}
