package atlantis.combat.managers.terran;

import atlantis.architecture.Manager;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranCombatManagerTopPriority.class,
            TerranCombatManagerMediumPriority.class,
            TerranCombatManagerLowPriority.class,
        };
    }
}
