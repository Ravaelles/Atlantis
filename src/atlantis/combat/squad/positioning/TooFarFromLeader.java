package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.util.Enemy;
import atlantis.util.We;

public class TooFarFromLeader extends Manager {
    private double distToLeader;
    private AUnit leader;

    public TooFarFromLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isDT()) return false;
        
        if (We.terran() && !Enemy.terran() && A.seconds() <= 220) return false;
        if (unit.enemiesNear().inRadius(6, unit).notEmpty()) return false;
        if (unit.squad().isLeader(unit)) return false;

        if (A.supplyUsed() >= 170 && (
            unit.enemiesNear().empty() || EnemyUnits.discovered().buildings().atMost(1)
        )) return false;

        leader = unit.squad().leader();
        if (leader == null) return false;

        if (unit.distToNearestChokeLessThan(4)) return false;

        distToLeader = unit.distTo(leader);
        boolean wayTooFarFromLeader = wayTooFarFromLeader();

        if (wayTooFarFromLeader) return true;

        return tooFarFromLeader()
            && !leaderIsOvercrowded()
            && !unitIsOvercrowded();
    }

    private boolean wayTooFarFromLeader() {
        int maxDistance = We.terran() ? 8 : 12;

        return distToLeader >= maxDistance;
    }

    private boolean unitIsOvercrowded() {
        return unit.friendsNear().groundUnits().countInRadius(1, unit) >= 2
            || (
            unit.friendsNear().groundUnits().countInRadius(1.5, unit) >= 5
                && unit.friendsNear().groundUnits().countInRadius(3, unit) >= 8
        );
    }

    private boolean leaderIsOvercrowded() {
        return leader.isStuck()
            || unit.friendsNear().groundUnits().countInRadius(2, unit) >= 4
            || unit.friendsNear().groundUnits().countInRadius(5, unit) >= 11;
    }

    private boolean tooFarFromLeader() {
        return distToLeader > maxDistFromLeader();
    }

    private double maxDistFromLeader() {
        if (unit.squadSize() >= 30) return 20;

        return Math.min(7, 4 + unit.squadSize() / 4);
    }

    protected Manager handle() {
        if (leader == null) leader = unit.squadLeader();

        if (!unit.isMoving() || A.everyNthGameFrame(5)) {
            unit.move(leader, Actions.MOVE_FORMATION, "Coordinate");
        }
        return usedManager(this);
    }
}
