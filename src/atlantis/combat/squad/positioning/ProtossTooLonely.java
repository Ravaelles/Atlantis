package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossTooLonely extends Manager {
    private AUnit friend;

    public ProtossTooLonely(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.protoss()) return false;
        if (unit.isAir()) return false;
        if (unit.isDT()) return false;
        if (unit.distToNearestChokeLessThan(5)) return false;

        return isTooLonely();
    }

    private boolean isTooLonely() {
        return unit.friendsNear().inRadius(2.5, unit).empty();
    }

    protected Manager handle() {
        if (friend == null) friend = defineFriendToGoTo();
        if (friend == null) return null;

        if (!unit.isMoving() || A.everyNthGameFrame(5)) {
            if (unit.move(friend, Actions.MOVE_FORMATION, "Coordinate")) {
                return usedManager(this);
            }
        }

        return null;
    }

    private AUnit defineFriendToGoTo() {
        if (unit.isLeader()) return unit.friendsNear().combatUnits().groundUnits().nearestTo(unit);

        return unit.squadLeader();
    }
}
