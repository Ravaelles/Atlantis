package atlantis.map.scout.enemy;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
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
        if (ScoutState.scoutsKilledCount >= 3) return false;
        if (A.s >= 340) return false;

        enemyMain = EnemyMainBase.get();
        if (enemyMain == null) return false;

        position = enemyMain.position();

        return position != null;
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            // Roam first: a found main is circled (owner 2026-10-23, NEXT
            // #187 - the main<->natural ping-pong below used to starve the
            // roam forever, so the scout never actually watched the base).
            // The natural leg stays as the fallback when there is nothing
            // to circle around.
            RoamAroundEnemyBase::new,
            MoveBetweenEnemyBaseAndEnemyNatural::new,
        };
    }
}
