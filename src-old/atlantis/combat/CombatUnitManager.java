package atlantis.combat;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.managers.protoss.ProtossCombatUnitManager;
import atlantis.combat.managers.terran.TerranCombatUnitManager;
import atlantis.combat.managers.zerg.ZergCombatUnitManager;
import atlantis.units.AUnit;

public class CombatUnitManager extends Manager {
    public CombatUnitManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit() && !unit.isABuilding();
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossCombatUnitManager::new,
            TerranCombatUnitManager::new,
            ZergCombatUnitManager::new,
//            ZergCombatManagerTopPriority.class,
//            ZergCombatManagerMediumPriority.class,
//            ZergCombatManagerLowPriority.class,
        };
    }
}
