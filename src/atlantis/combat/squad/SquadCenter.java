package atlantis.combat.squad;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;
import atlantis.util.cache.Cache;

import java.util.ArrayList;
import java.util.Collections;

public class SquadCenter {
    private Cache<AUnit> cache = new Cache<>();

    private Squad squad;
    private AUnit _prevLeader = null;

    // =========================================================

    public SquadCenter(Squad squad) {
        this.squad = squad;
    }

    // =========================================================

    protected boolean isInvalid(AUnit _leader) {
        return _leader == null || _leader.isDead() || (!_leader.isTank() && Count.tanks() >= 2);
    }

    protected AUnit leader() {
        int ttl = 103;
        AUnit leader = cache.getIfValid(
            "leader",
            ttl,
            this::defineLeader
        );

//        if (leader != null && leader.isAlive() && !leader.isRunning()) {
        if (leader != null && leader.isAlive()) {
            return _prevLeader = leader;
        }

        _prevLeader = leader = this.defineLeader();
        cache.set("leader", ttl, leader);
        return leader;
    }

    protected AUnit defineLeader() {
        if (squad.isEmpty()) return null;

        Selection units = squad.units();
//        APosition median = squad.average();
//        APosition median = squad.median();

        Selection candidates = potentialLeaders(units);
        APosition nearestToPosition = nearestToPosition();
        AUnit unit;

        if ((unit = candidates.ranged().nearestTo(nearestToPosition)) != null) {
            return unit;
        }

        return candidates.nearestTo(nearestToPosition);
    }

    private APosition nearestToPosition() {
        if (_prevLeader != null && _prevLeader.lastPosition() != null) {
            return _prevLeader.lastPosition();
        }

        AUnit first = Alpha.get().first();
        if (first != null && first.lastPosition() != null) {
            return first.lastPosition();
        }

        return Select.mainOrAnyBuilding().position();
    }

    private static Selection potentialLeaders(Selection units) {
        Selection candidates = units
            .groundUnits()
            .havingWeapon()
            .notSpecialAction();

        return candidates;
    }

}
