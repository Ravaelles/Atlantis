package atlantis.combat.managers.terran;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.units.AUnit;
import atlantis.util.We;

public class TerranCombatUnitManager extends Manager {
    public TerranCombatUnitManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.terran() && unit.isCombatUnit() && !unit.isABuilding();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            TerranCombatManagerTopPriority::new,
            TerranCombatManagerMediumPriority::new,
            TerranCombatManagerLowPriority::new,
        };
    }
}
