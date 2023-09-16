package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class CloserToLeader extends MissionManager {
    public CloserToLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isAir()
            && !squad.isLeader(unit)
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
                unit.move(
                    leaderPosition.translateByTiles(0.5, 0),
                    Actions.MOVE_FORMATION,
                    "CloserToLeader"
                );
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean shouldGetBackToLeader() {
        AUnit leader = squad.leader();

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
