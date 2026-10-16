package atlantis.combat.retreating.protoss.should;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.retreating.protoss.ProtossForceRetreatDuringDefend;
import atlantis.combat.retreating.protoss.big_scale.ProtossFullRetreat;
import atlantis.combat.retreating.protoss.small_scale.ProtossMeleeSmallScaleRetreat;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ProtossRetreatWrapper extends Manager {
    public ProtossRetreatWrapper(AUnit unit) {
        super(unit);
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            ProtossFullRetreat::new,
            ProtossMeleeSmallScaleRetreat::new,
        };
    }

    public static Selection friends(AUnit unit) {
//        return unit.friendsNear().notRunning();
        return unit.friendsNear().combatUnits().havingAtLeastHp(22);
    }

    public static Selection enemies(AUnit unit) {
        return unit.enemiesNear().combatUnits().canAttack(unit, 7);
    }
}
