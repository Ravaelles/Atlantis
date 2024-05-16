package atlantis.combat.squad.positioning.too_lonely;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
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

        return unit.noCooldown()
            && unit.enemiesNear().notEmpty()
            && unit.squadSize() >= 2
            && unit.lastUnderAttackMoreThanAgo(30 * 3)
            && isTooLonely();
    }

    private boolean tooDangerousToGoToLeader() {
        return unit.meleeEnemiesNearCount(3) > 0;
    }

    private boolean isTooLonely() {
        double closeRadius = unit.isMelee() ? 0.9 : 2;
        Selection friendsInRadius4 = unit.friendsNear().inRadius(4, unit);

        return friendsInRadius4.atMost(2)
            && friendsInRadius4.inRadius(closeRadius, unit).empty();
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
