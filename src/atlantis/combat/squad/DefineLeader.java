package atlantis.combat.squad;

import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class DefineLeader {
    private Cache<AUnit> cache = new Cache<>();
    protected AUnit _prevLeader = null;
    
    private final Squad squad;

    protected DefineLeader(Squad squad) {
        this.squad = squad;
    }

    protected AUnit leader() {
        int ttl = 103;
        AUnit leader = cache.getIfValid(
            "leader",
            ttl,
            () -> this.define(null)
        );

        if (leader != null && We.protoss() && leader.isZealot() && Count.dragoons() > 0) {
            leader = null;
        }

//        if (leader != null && leader.isAlive() && !leader.isRunning()) {
        if (shouldUsePreviousValid(leader)) {
            return _prevLeader = leader;
        }

        _prevLeader = leader = this.define(null);
        cache.set("leader", ttl, leader);
        return leader;
    }

    private boolean shouldUsePreviousValid(AUnit leader) {
        if (leader == null) return false;
        if (!leader.isAlive()) return false;

        if (leader.nearestChokeDist() <= 5) {
            return true;
        }

        return false;
    }

    protected AUnit define(AUnit exceptUnit) {
        if (squad.isEmpty()) return null;

        Selection units = squad.units();

        Selection candidates = potentialLeaders(units, exceptUnit);
        APosition nearestToPosition = nearestToPosition();
        AUnit unit;

        if (candidates.ranged().atLeast(1)) {
            if ((unit = candidates.ranged().nearestTo(nearestToPosition)) != null) {
                return unit;
            }
        }

        return candidates.groundNearestTo(nearestToPosition);
    }

    private APosition nearestToPosition() {
        AUnit nearestEnemyBuilding = EnemyUnits.nearestEnemyBuilding();
        if (nearestEnemyBuilding != null) return nearestEnemyBuilding.position();

        APosition average = squad.average();
        if (average != null) return average;

        APosition median = squad.median();
        if (median != null) return median;

//        AUnit first = Alpha.get().first();
//        if (first != null && first.lastPosition() != null) {
//            return first.lastPosition();
//        }

        return Select.mainOrAnyBuildingPosition();
    }

    // This was used when there was always one leader, until he dies
//    private APosition nearestToPosition() {
//        if (_prevLeader != null && _prevLeader.lastPosition() != null) {
//            return _prevLeader.lastPosition();
//        }
//
//        AUnit first = Alpha.get().first();
//        if (first != null && first.lastPosition() != null) {
//            return first.lastPosition();
//        }
//
//        return Select.mainOrAnyBuildingPosition();
//    }

    private static Selection potentialLeaders(Selection units, AUnit exceptUnit) {
        Selection candidates = units
            .groundUnits()
            .havingWeapon()
            .notSpecialAction()
            .notRunning()
            .excludeTypes(AUnitType.Protoss_Dark_Templar, AUnitType.Protoss_Reaver)
            .exclude(exceptUnit);

        return candidates;
    }
}
