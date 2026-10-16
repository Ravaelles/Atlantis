package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.advance.special.WeDontKnowWhereEnemyIs;
import atlantis.units.AUnit;

public class MoveAsLooksIdle extends Manager {
    public MoveAsLooksIdle(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAir() && looksIdle();
    }

    private boolean looksIdle() {
        return unit.isStopped() || unit.position().equals(unit.lastPosition());
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            AttackTargetInRange::new,
            AsAirAttackAnyone::new,
            WeDontKnowWhereEnemyIs::new,
        };
    }
}
