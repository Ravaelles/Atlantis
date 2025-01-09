package atlantis.protoss.corsair;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class CorsairExploreEnemyMain extends Manager {
    private static int mainLastSeenAt = -9999;
    private APosition enemyMain;

    public CorsairExploreEnemyMain(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        updateMainLastSeenAt();

        return enemyMain != null
            && A.s % 30 <= 13
            && enemyMain.hasPosition()
            && A.secondsAgo(mainLastSeenAt) >= 20;
    }

    @Override
    public Manager handle() {
        if (unit.move(enemyMain, Actions.MOVE_ADVANCE, "ExploreEnemyMain")) {
            return usedManager(this);
        }

        return null;
    }

    private void updateMainLastSeenAt() {
        enemyMain = EnemyInfo.enemyMain();
        if (enemyMain != null && enemyMain.isPositionVisible()) {
            mainLastSeenAt = A.now;
        }
    }
}
