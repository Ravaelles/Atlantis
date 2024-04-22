package atlantis.combat.generic.enemy_in_range;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class UnitHasEnemyInRange extends Manager {
    public UnitHasEnemyInRange(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossUnitHasEnemyInRange.class,
        };
    }
}
