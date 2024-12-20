package atlantis.combat.squad.positioning.protoss.formation.crescent;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossCrescentFriendTooFar extends Manager {
    private AUnit friend = null;

    public ProtossCrescentFriendTooFar(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        friend = unit.friendsNear().combatUnits().groundUnits().nearestTo(unit);

        return friend != null && friend.distTo(unit) > 0.3 && friend.distTo(unit) <= 4;
    }

    @Override
    protected Manager handle() {
        if (friend != null && unit.move(friend, Actions.MOVE_FORMATION, "CrescentToFriend")) {
            return usedManager(this, "CrescentToFriend");
        }

        return null;
    }
}
