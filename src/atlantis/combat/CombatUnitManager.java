package atlantis.combat;

import atlantis.combat.managers.*;
import atlantis.units.SpecialUnitsManager;
import atlantis.units.AUnit;
import atlantis.architecture.Manager;

public class CombatUnitManager extends Manager {

    public CombatUnitManager(AUnit unit) {
        super(unit);
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
        };
    }

}
