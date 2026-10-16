package atlantis.combat.managers.protoss;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossCombatManagerTopPriority::new,
            ProtossCombatManagerMediumPriority::new,
            ProtossCombatManagerLowPriority::new,
        };
    }
}
