package atlantis.combat.generic.enemy_in_range;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.generic.enemy_in_range.protoss.ProtossHasEnemyInRange;
import atlantis.combat.generic.enemy_in_range.terran.TerranHasEnemyInRange;
import atlantis.units.AUnit;

public class UnitHasEnemyInRange extends Manager {
    public UnitHasEnemyInRange(AUnit unit) {
        super(unit);
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossHasEnemyInRange::new,
            TerranHasEnemyInRange::new,
        };
    }
}
