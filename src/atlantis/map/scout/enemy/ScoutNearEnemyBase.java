package atlantis.map.scout.enemy;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.base.define.EnemyMainBase;
import atlantis.map.position.APosition;
import atlantis.map.scout.ScoutState;
import atlantis.units.AUnit;

public class ScoutNearEnemyBase extends Manager {
    private APosition enemyMain;
    private APosition position = null;

    public ScoutNearEnemyBase(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (ScoutState.scoutsKilledCount >= 2) return false;
        if (A.s >= 340) return false;

        enemyMain = EnemyMainBase.get();
        if (enemyMain == null) return false;

        position = enemyMain.position();
        return position != null;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            MoveBetweenEnemyBaseAndEnemyNatural.class,
            RoamAroundEnemyBase.class,
        };
    }
}
