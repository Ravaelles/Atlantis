package atlantis.combat.squad.positioning.protoss;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossAsLeaderTooFarFromOthers extends Manager {

    private HasPosition nearestFriend;

    public ProtossAsLeaderTooFarFromOthers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isLeader()) return false;
        if (unit.isMissionSparta()) return false;

//        nearestFriend = unit.friendsNear().combatUnits().inRadius(6, unit).center();
        nearestFriend = unit.squadCenter();
        if (nearestFriend == null) nearestFriend = unit.squad().selection().exclude(unit).nearestTo(unit);

        return nearestFriend != null && unit.distTo(nearestFriend) > 1.7;
    }

    @Override
    protected Manager handle() {
        if (nearestFriend == null) return null;

        if (!unit.isMoving() || A.everyNthGameFrame(9)) {
            if (unit.move(nearestFriend, Actions.MOVE_FORMATION, "LeaderBack")) return usedManager(this);
        }

        return null;
    }
}
