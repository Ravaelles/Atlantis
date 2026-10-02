package atlantis.combat.generic.enemy_in_range;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.generic.enemy_in_range.protoss.ProtossRangedAttackEnemiesInRange;
import atlantis.units.AUnit;

public class AttackEnemiesInRange extends Manager {
    public AttackEnemiesInRange(AUnit unit) {
        super(unit);
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossRangedAttackEnemiesInRange::new,
        };
    }
}
