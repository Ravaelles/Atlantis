package atlantis.combat;

import atlantis.architecture.Manager;
import atlantis.combat.managers.*;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.units.AUnit;
import atlantis.units.special.SpecialUnitsManager;

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
            AvoidSpellsAndMines.class,
            SkipCombatManager.class,
            SpecialUnitsManager.class,
            CombatManagerTopPriority.class,
            ImproveCombatPerformance.class,
            CombatManagerMediumPriority.class,
            UnitUnderAttackAndNotAttacking.class,
            CombatManagerLowPriority.class,
        };
    }

}
