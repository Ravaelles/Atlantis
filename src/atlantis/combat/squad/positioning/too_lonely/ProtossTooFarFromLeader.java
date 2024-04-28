package atlantis.combat.squad.positioning.too_lonely;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Have;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;

public class ProtossTooFarFromLeader extends Manager {
    private double distToLeader;
    private AUnit leader;

    public ProtossTooFarFromLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

//        if (unit.enemiesNear().inRadius(6, unit).notEmpty()) return false;
        if (unit.squad().isLeader(unit)) return false;

        if (A.supplyUsed() >= 170 && (
            unit.enemiesNear().empty() || EnemyUnits.discovered().buildings().atMost(1)
        )) return false;

        leader = unit.squad().leader();
        if (leader == null) return false;

        distToLeader = unit.distTo(leader);
        boolean wayTooFarFromLeader = wayTooFarFromLeader();

        if (wayTooFarFromLeader) return true;

        if (unit.distToNearestChokeLessThan(5)) return false;

        if (distToLeader >= 5 && unit.lastPositionChangedMoreThanAgo(30)) return true;

        if (leaderIsOvercrowded()) return false;
        if (unitIsOvercrowded()) return false;

        return tooFarFromLeader();
    }

    private boolean wayTooFarFromLeader() {
        int maxDistance = A.inRange(
            unit.isRanged() ? 5 : 2,
            (Have.dragoon() ? 5 : 3) + unit.squadSize() / 5,
            8
        );

        return distToLeader >= maxDistance;
    }

    private boolean unitIsOvercrowded() {
        return
//            unit.friendsNear().groundUnits().countInRadius(1, unit) >= 2
//            || (
            unit.friendsNear().groundUnits().countInRadius(1.5, unit) >= 5;
//                && unit.friendsNear().groundUnits().countInRadius(3, unit) >= 8
//        );

//        return unit.friendsNear().groundUnits().countInRadius(1, unit) >= 2
//            || (
//            unit.friendsNear().groundUnits().countInRadius(1.5, unit) >= 5
//                && unit.friendsNear().groundUnits().countInRadius(3, unit) >= 8
//        );
    }

    private boolean leaderIsOvercrowded() {
        return leader.isStuck()
            || unit.friendsNear().groundUnits().countInRadius(3, unit) >= 4
            || unit.friendsNear().groundUnits().countInRadius(5, unit) >= 9;
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