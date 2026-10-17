package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.game.ARandom;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.base.define.EnemyFourthBase;
import atlantis.map.base.define.EnemyMainBase;
import atlantis.map.base.define.EnemyThirdBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * Absolute terminal of the scout chain: a scout never stands idle while any
 * enemy base location is defined. The unexplored-first managers run earlier
 * ({@code ScoutFallbackEnemyBases}, {@code ScoutApproachEnemyBase}), so
 * reaching here means every anchor is already explored - revisit them
 * oldest-first (natural, third, fourth, main) instead of idling. An enemy
 * that expands or re-occupies re-opens the game; a standing scout sees
 * nothing.
 *
 * <p>When no enemy-anchored location is defined at all (nothing
 * discovered yet), roam the enemy start locations instead of idling:
 * early game cycles 1st/2nd/3rd in fixed order, later picks randomly.
 * Only when the map has no start locations either does this decline -
 * that is the one case the commander's idle line still names.</p>
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
        if (target == null) target = nextRoamAnchor(enemyStartPositions(), !EnemyInfo.hasDiscoveredAnyBuilding());
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

    /**
     * Enemy start locations (ours excluded), in map order. Empty when the
     * main base is unknown (no anchor to measure "enemy" from) or the map
     * has no starts at all.
     */
    static List<APosition> enemyStartPositions() {
        List<APosition> positions = new ArrayList<>();
        if (Select.main() == null) return positions;
        for (ABaseLocation base : BaseLocations.startingLocations(true)) {
            if (base != null && base.position() != null) positions.add(base.position());
        }
        return positions;
    }

    /**
     * Idle fallback: roam the enemy starts instead of standing around.
     * Early (enemy never discovered) cycle 1st/2nd/3rd in fixed order;
     * later pick randomly - fog may hide an expansion exactly where the
     * map looks empty. Pure over the given candidates except the shared
     * rotation counter, so the cycle pins in tests.
     */
    /** Test seam: the roam pick behind the idle fallback. */
    public static APosition nextRoamAnchor(List<APosition> enemyStarts, boolean enemyUndiscovered) {
        if (enemyStarts == null || enemyStarts.isEmpty()) return null;
        if (enemyUndiscovered) {
            int i = ScoutState.terminalRoamIndex++ % enemyStarts.size();
            return enemyStarts.get(i);
        }
        return enemyStarts.get(ARandom.rand(0, enemyStarts.size() - 1));
    }

    @Override
    protected Manager handle() {
        if (unit.move(target, Actions.MOVE_SCOUT, "TerminalAnchor", true)) {
            return usedManager(this);
        }
        return null;
    }
}
