package atlantis.combat.advance.terran;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class TerranCloserToLeader extends MissionManager {
    public TerranCloserToLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isAir()
            && !unit.isLeader()
//            && unit.mission().focusPoint() != null
            && A.seconds() % 6 <= 3
            && (unit.noCooldown() && unit.enemiesNearInRadius(8) == 0)
            && unit.friendsInRadiusCount(3) <= 9
            && unit.friendsInRadiusCount(1.3) <= 3
            && unit.friendsInRadiusCount(0.5) <= 2;
    }

    @Override
    protected Manager handle() {
        if (shouldGetBackToLeader()) {
            APosition leaderPosition = squad.leader().position();
            if (leaderPosition != null) {
                APosition position = leaderPosition.translateByTiles(0.5, 0);
                if (!position.isWalkable()) position = leaderPosition;

                unit.move(
                    position,
                    Actions.MOVE_FORMATION,
                    "TerranCloserToLeader"
                );
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean shouldGetBackToLeader() {
        if (squad == null) return false;

        AUnit leader = squad.leader();

        if (leader == null) return false;
        if (!isLeaderOvercrowded(leader)) return false;

        if (unit.distTo(leader) >= 7) return true;
        if (squad.cohesionPercent() <= 70 && unit.friendsInRadiusCount(1) <= 3) return true;

        return false;
    }

    private static boolean isLeaderOvercrowded(AUnit leader) {
//        return leader.friendsNear().inRadius(2, leader).count() >= 7
        return Select.all().inRadius(1, leader).count() >= 5;
    }
}
