package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.map.base.BaseLocations;
import atlantis.map.base.define.EnemyFourthBase;
import atlantis.map.base.define.EnemyMainBase;
import atlantis.map.base.define.EnemyThirdBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * Absolute terminal of the scout chain: a scout never stands idle while any
 * enemy base location is defined. The unexplored-first managers run earlier
 * ({@code ScoutFallbackEnemyBases}, {@code ScoutApproachEnemyBase}), so
 * reaching here means every anchor is already explored - revisit them
 * oldest-first (natural, third, fourth, main) instead of idling. An enemy
 * that expands or re-occupies re-opens the game; a standing scout sees
 * nothing.
 *
 * <p>Declines only when no enemy base location exists at all (map data
 * absent) - that is the one case the commander's idle line still names.</p>
 */
public class ScoutTerminalAnchor extends Manager {
    private APosition target = null;

    public ScoutTerminalAnchor(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        target = firstDefined(
            BaseLocations.enemyNatural(),
            EnemyThirdBase.position(),
            EnemyFourthBase.position(),
            EnemyMainBase.get()
        );
        return target != null;
    }

    public static APosition firstDefined(HasPosition... candidates) {
        for (HasPosition candidate : candidates) {
            if (candidate == null) continue;
            APosition position = candidate.position();
            if (position != null) return position;
        }
        return null;
    }

    @Override
    protected Manager handle() {
        if (unit.move(target, Actions.MOVE_SCOUT, "TerminalAnchor", true)) {
            return usedManager(this);
        }
        return null;
    }
}
