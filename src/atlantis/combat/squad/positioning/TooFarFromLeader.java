package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class TooFarFromLeader extends Manager {
    public TooFarFromLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit() && unit.friendsNear().groundUnits().countInRadius(1, unit) <= 3;
    }

    protected Manager handle() {
        if (check()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean check() {
        AUnit leader = squad.leader();
        if (leader == null) return false;

        double distToLeader = unit.distTo(leader);
        if (
            distToLeader > 12
                ||
                (
                    distToLeader > 6
                        && unit.friendsInRadiusCount(3) <= 8
                        && leader.friendsInRadiusCount(4) <= 10
                )
        ) {
            unit.move(leader, Actions.MOVE_FORMATION, "Coordinate");
            return true;
        }

        return false;
    }
}
