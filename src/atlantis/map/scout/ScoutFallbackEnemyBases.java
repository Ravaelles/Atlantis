package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.map.base.BaseLocations;
import atlantis.map.base.define.EnemyFourthBase;
import atlantis.map.base.define.EnemyThirdBase;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * Terminal fallback: an idle scout always has somewhere to go. When every
 * other manager in the chain declines, visit the first unexplored enemy
 * expansion - natural, third, fourth - instead of standing around.
 *
 * <p>Danger handling stays where it was: the avoid managers run first every
 * frame and preempt this one, so reaching here means no immediate threat.
 * When all three anchors are explored this declines too, and the commander's
 * idle line keeps its remaining meaning: coverage is complete.</p>
 */
public class ScoutFallbackEnemyBases extends Manager {
    private APosition target = null;

    public ScoutFallbackEnemyBases(AUnit unit) {
        super(unit);
    }

    public static APosition firstUnexplored(APosition... candidates) {
        for (APosition candidate : candidates) {
            if (candidate != null && !candidate.isExplored()) return candidate;
        }
        return null;
    }

    @Override
    public boolean applies() {
        target = firstUnexplored(
            BaseLocations.enemyNatural(),
            EnemyThirdBase.position(),
            EnemyFourthBase.position()
        );

        return target != null;
    }

    @Override
    protected Manager handle() {
        if (unit.move(target, Actions.MOVE_SCOUT, "FallbackBases", true)) {
            return usedManager(this);
        }
        return null;
    }
}
