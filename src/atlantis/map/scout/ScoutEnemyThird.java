package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.base.define.EnemyThirdBase;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class ScoutEnemyThird extends Manager {
    private static boolean noLongerScoutAsBaseFound = false;
    private static int lastSeenAtFrame = -1;

    private APosition enemyThird;

    public ScoutEnemyThird(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (noLongerScoutAsBaseFound) return false;

        enemyThird = EnemyThirdBase.get();
        if (enemyThird == null) return false;

        if (!enemyThird.isExplored()) return true;

        updateLastSeenAtFrame();
        return A.secondsAgo(lastSeenAtFrame) >= 34;
    }

    @Override
    protected Manager handle() {
        if (unit.move(
            enemyThird, Actions.MOVE_SCOUT, "ScoutEnemyThird" + A.now(), true
        )) return usedManager(this);

        return null;
    }

    private void updateLastSeenAtFrame() {
        if (enemyThird.isPositionVisible()) {
            lastSeenAtFrame = A.now();

            if (Select.enemy().buildings().countInRadius(10, enemyThird) >= 1) {
                noLongerScoutAsBaseFound = true;
            }
        }
    }
}
