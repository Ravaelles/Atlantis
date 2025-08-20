package atlantis.combat.managers.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossCombatUnitManager extends Manager {
    public ProtossCombatUnitManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss() && unit.isCombatUnit() && !unit.isABuilding();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossCombatManagerTopPriority.class,
            ProtossCombatManagerMediumPriority.class,
            ProtossCombatManagerLowPriority.class,
        };
    }
}
