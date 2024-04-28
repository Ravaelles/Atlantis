package atlantis.combat.squad.positioning.too_lonely;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossTooLonelyGetCloser extends Manager {
    private AUnit friend;

    public ProtossTooLonelyGetCloser(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (unit.lastStartedAttackLessThanAgo(40)) return false;
//        if (unit.isLeader()) return false;

        return isTooLonely();
    }

    private boolean tooDangerousToGoToLeader() {
        return unit.meleeEnemiesNearCount(3) > 0;
    }

    private boolean isTooLonely() {
        return unit.friendsNear().inRadius(0.9, unit).empty();
    }

    protected Manager handle() {
        if (friend == null) friend = defineGoTo();
        if (friend == null) return null;

        if (!unit.isMoving() || A.everyNthGameFrame(5)) {
            if (unit.move(friend, Actions.MOVE_FORMATION, "Coordinate")) {
                return usedManager(this);
            }
        }

        return null;
    }

    private AUnit defineGoTo() {
        if (unit.isLeader()) return unit.friendsNear().combatUnits().groundUnits().nearestTo(unit);

        return unit.squadLeader();
    }
}
