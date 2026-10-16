package atlantis.map.scout.enemy;

import atlantis.architecture.Manager;
import atlantis.map.base.define.EnemyMainBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.scout.ScoutState;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * Second-to-last resort: there is almost always something to watch. When every other
 * scout manager declines and at most two scouts died so far, hover near the
 * known enemy main instead of standing idle - danger handling stays with the
 * avoid managers above, which act first every frame.
 */
public class ScoutApproachEnemyBase extends Manager {
    private APosition position = null;

    public ScoutApproachEnemyBase(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        // Owner's gate first: cheap, map-free, and unit-testable as such.
        if (ScoutState.scoutsKilledCount > 2) return false;

        HasPosition enemyMain = EnemyMainBase.get();
        if (enemyMain == null) return false;

        position = enemyMain.position();
        if (position == null) return false;

        if (unit.enemiesThatCanAttackMe(9).notEmpty()) return false;

        return true;
    }

    @Override
    public Manager handle() {
        if (unit.distTo(position) <= 6) {
            unit.setTooltipTactical("WatchEnemyBase");
            return usedManager(this);
        }

        if (unit.move(position, Actions.MOVE_SCOUT, "ApproachEnemyBase", true)) {
            return usedManager(this);
        }
        return null;
    }
}
