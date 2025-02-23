package atlantis.combat.squad.positioning.protoss.keep_formation;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ProtossForceClusterDragoon extends Manager {
    private AUnit friend;

    public ProtossForceClusterDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isDragoon()
            && unit.lastUnderAttackMoreThanAgo(30)
            && unit.friendsNear().combatUnits().groundUnits().inRadius(minDistToFriend(), unit).count() == 0
            && (friend = friend()) != null;
    }

    private int minDistToFriend() {
        return unit.rangedEnemiesCount(3) > 0 ? 1 : 5;
    }

    @Override
    public Manager handle() {
        if (unit.move(friend, Actions.MOVE_FORMATION)) {
            return usedManager(this);
        }

        return null;
    }

    private AUnit friend() {
        Selection combatUnits = Select.ourCombatUnits().exclude(unit);

        return combatUnits.groundUnits().nearestTo(unit);
    }
}
