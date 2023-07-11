package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class TooFarFromLeader extends Manager {
    public TooFarFromLeader(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (check()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean check() {
        AUnit leader = squad.leader();
        if (leader == null) {
            return false;
        }

        if (unit.distTo(leader) > 6 && unit.friendsInRadiusCount(3) <= 8) {
            unit.move(leader, Actions.MOVE_FORMATION, "Coordinate");
            return true;
        }

        return false;
    }
}
