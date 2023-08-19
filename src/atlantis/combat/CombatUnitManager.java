package atlantis.combat;

import atlantis.architecture.Manager;
import atlantis.combat.managers.*;
import atlantis.units.AUnit;
import atlantis.units.special.SpecialUnitsManager;

public class CombatUnitManager extends Manager {
    public CombatUnitManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            SkipCombatManager.class,
            SpecialUnitsManager.class,
            CombatManagerTopPriority.class,
            ImproveCombatManagerPerformance.class,
            CombatManagerMediumPriority.class,
            CombatManagerLowPriority.class,
//            AttackNearbyEnemies.class,
            DebugIdleUnitsManager.class,
        };
    }

}
