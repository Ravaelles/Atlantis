package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.define.BaseLocationsNearestTo;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

/**
 * There is always intel left to gather: visit the nearest enemy base
 * locations with no known base, explored or not - fog may hide an expansion
 * exactly where the map looks empty.
 *
 * <p>
 * The pick rotates through {@code ScoutState.baselessVisited}: standing on
 * (or passing within arrival range of) a site marks it, and a site pursued
 * without any movement for {@link #STUCK_GIVE_UP_FRAMES} is abandoned as
 * unreachable (Wine game: the scout re-issued Move to one site for 100+
 * frames - arrival exclusion alone never fires when the closest approach
 * stays above 3). When everything nearby is visited the pick is null and
 * the chain falls through instead of oscillating back.
 * </p>
 */
public class ScoutEnemyBasesWithoutBase extends Manager {

    /**
     * No movement at all for this long under one target means the site is unarrivable.
     */
    static final int STUCK_GIVE_UP_FRAMES = 240;

    private ABaseLocation target;

    public ScoutEnemyBasesWithoutBase(AUnit unit) {
        super(unit);
    }

    private static String key(HasPosition location) {
        return location.x() + "," + location.y();
    }

    /**
     * Test seam: the picked target's visit key, or null when nothing applies.
     */
    public String targetKeyForTest() {
        return target == null ? null : key(target);
    }

    @Override
    public boolean applies() {
        HasPosition enemyCore = EnemyUnits.enemyMainBase();
        if (enemyCore == null) enemyCore = EnemyUnits.enemyBase();
        if (enemyCore == null) return false;
        if (unit.enemiesThatCanAttackMe(9).notEmpty()) return false;

        target = nearestBaseless(enemyCore);

        return target != null;
    }

    @Override
    protected Manager handle() {
        if (unit.move(target, Actions.MOVE_SCOUT, "ScoutBaselessBase", true)) {
            return usedManager(this);
        }
        return null;
    }

    private ABaseLocation nearestBaseless(HasPosition enemyCore) {
        ABaseLocation best = pick(enemyCore, null);

        // Give up an unarrivable site: same target as last frame and the
        // unit has neither moved nor freshly acquired it for a while.
        // Either clock alone lies (a fresh target on a standing scout, a
        // walking scout under a stale position stamp), both together mean
        // the Move orders change nothing.
        if (best != null) {
            String key = key(best);
            if (key.equals(ScoutState.baselessTrackedKey)
                && Math.min(unit.lastPositionChangedAgo(), A.now() - ScoutState.baselessTrackedSince)
                > STUCK_GIVE_UP_FRAMES) {
                ScoutState.baselessVisited.add(key);
                ScoutState.baselessTrackedKey = null;
                best = pick(enemyCore, key);
            }
            if (best != null) {
                String bestKey = key(best);
                if (!bestKey.equals(ScoutState.baselessTrackedKey)) {
                    ScoutState.baselessTrackedKey = bestKey;
                    ScoutState.baselessTrackedSince = A.now();
                }
            }
            else {
                ScoutState.baselessTrackedKey = null;
            }
        }
        else {
            ScoutState.baselessTrackedKey = null;
        }
        return best;
    }

    private ABaseLocation pick(HasPosition enemyCore, String excluded) {
        Positions<ABaseLocation> candidates = BaseLocationsNearestTo.takeN(4, enemyCore);

        ABaseLocation best = null;
        double bestDist = Double.MAX_VALUE;
        for (ABaseLocation location : candidates.list()) {
            if (hasKnownEnemyDepotWithin(location, 8)) continue;
            String key = key(location);
            if (key.equals(excluded)) continue;
            if (ScoutState.baselessVisited.contains(key)) continue;
            if (unit.distTo(location) <= 3) {
                ScoutState.baselessVisited.add(key);
                continue;
            }
            double dist = unit.distTo(location);
            if (dist < bestDist) {
                bestDist = dist;
                best = location;
            }
        }
        return best;
    }

    private boolean hasKnownEnemyDepotWithin(ABaseLocation location, double radius) {
        return Select.enemy().ofType(
            AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair, AUnitType.Zerg_Hive,
            AUnitType.Protoss_Nexus, AUnitType.Terran_Command_Center
        ).inRadius(radius, location).notEmpty();
    }
}
