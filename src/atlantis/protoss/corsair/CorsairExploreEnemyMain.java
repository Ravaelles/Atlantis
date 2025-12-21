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
        enemyMain = EnemyInfo.enemyMain();
        if (enemyMain == null) return false;

        updateMainLastSeenAt();

        return A.s % 30 <= 9
            && enemyMain.hasPosition()
            && tooLongNotSeenMain();
    }

    private static boolean tooLongNotSeenMain() {
        return A.secondsAgo(mainLastSeenAt) >= A.whenEnemyZerg(20, 30);
    }

    @Override
    public Manager handle() {
        if (enemyMain != null && unit.move(enemyMain, Actions.MOVE_ADVANCE, "ExploreEnemyMain")) {
            return usedManager(this);
        }

        return null;
    }

    private void updateMainLastSeenAt() {
        if (enemyMain.isPositionVisible()) {
            mainLastSeenAt = A.now;
        }
    }
}
