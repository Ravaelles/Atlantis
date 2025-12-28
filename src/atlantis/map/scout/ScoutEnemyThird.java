package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.define.EnemyThirdBase;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

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

        enemyThird = EnemyThirdBase.position();
        if (enemyThird == null) return false;

        updateLastSeenAtFrame();

        // =========================================================

        if (unit.distTo(enemyThird) <= 5) return false;
        if (A.s % 30 <= 15) return false;
        if (A.secondsAgo(lastSeenAtFrame) <= 24) return false;

        if (unit.enemiesThatCanAttackMe(8).notEmpty()) return false;
        if (!Enemy.zerg() && EnemyUnits.combatUnits() <= 2) return false;
        if (unit.enemiesThatCanAttackMe(9).notEmpty()) return false;

        if (enemyThird.isPositionVisible()) return false;

        if (unit.lastPositionChangedAgo() >= 5 && unit.lastCommandIssuedAgo() >= 10) return true;

        if (!enemyThird.isExplored()) return true;
        if (unit.lastPositionChangedAgo() >= 20) return true;

        return false;
    }

    @Override
    protected Manager handle() {
        if (unit.move(
            enemyThird, Actions.MOVE_SCOUT, "ScoutEnemyThird" + A.now(), true
        )) return usedManager(this);

        return null;
    }

    private void updateLastSeenAtFrame() {
//        AAdvancedPainter.paintCircleFilled(enemyThird, 120, Color.Cyan);

        if (enemyThird.isPositionVisible()) {
            lastSeenAtFrame = A.now();
//            System.err.println("    >> Enemy third location seen at frame " + lastSeenAtFrame + " / " + A.minSec());

//            if (Select.enemy().buildings().countInRadius(10, enemyThird) >= 1) {
//                noLongerScoutAsBaseFound = true;
//            }
        }
    }
}
