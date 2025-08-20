package atlantis.combat;

import atlantis.architecture.Manager;
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
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossCombatUnitManager.class,
            TerranCombatUnitManager.class,
            ZergCombatUnitManager.class,
//            ZergCombatManagerTopPriority.class,
//            ZergCombatManagerMediumPriority.class,
//            ZergCombatManagerLowPriority.class,
        };
    }
}
