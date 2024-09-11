package atlantis.combat.generic.enemy_in_range;

import atlantis.architecture.Manager;
import atlantis.combat.generic.enemy_in_range.protoss.ProtossRangedAttackEnemiesInRange;
import atlantis.units.AUnit;

public class AttackEnemiesInRange extends Manager {
    public AttackEnemiesInRange(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossRangedAttackEnemiesInRange.class,
        };
    }
}
