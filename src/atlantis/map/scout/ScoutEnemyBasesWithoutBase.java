package atlantis.map.scout;

import atlantis.architecture.Manager;
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
 * exactly where the map looks empty. Picks the nearest beyond arrival range,
 * so the scout rotates without any stored state.
 */
public class ScoutEnemyBasesWithoutBase extends Manager {
    private ABaseLocation target;

    public ScoutEnemyBasesWithoutBase(AUnit unit) {
        super(unit);
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
        Positions<ABaseLocation> candidates = BaseLocationsNearestTo.takeN(4, enemyCore);

        ABaseLocation best = null;
        double bestDist = Double.MAX_VALUE;
        for (ABaseLocation location : candidates.list()) {
            if (hasKnownEnemyDepotWithin(location, 8)) continue;
            double dist = unit.distTo(location);
            if (dist <= 3) continue;
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
