package atlantis.combat.squad.positioning.protoss.cluster.old;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;

public class ProtossForceClusterDragoon extends Manager {
    private AUnit friend;

    public ProtossForceClusterDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isDragoon()) return false;
        if (unit.isLeader()) return false;

        return unit.isMissionAttack()
            && unit.eval() <= 1.3
            && unit.friendsNear().buildings().empty()
            && unit.distToLeader() <= 4
            && unit.enemiesThatCanAttackMe(1.2).empty()
            && unit.lastUnderAttackMoreThanAgo(30)
            && goonsTooFarFromEachOther()
            && unit.friendsInRadiusCount(6) <= 5
            && (unit.cooldown() >= 7 || unit.enemiesNear().countInRadius(2.8, unit) <= 0)
            && (friend = friend()) != null;
    }

    private boolean goonsTooFarFromEachOther() {
        int dragoons = Count.dragoons();
        if (dragoons <= 1) return false;

        if (unit.distToLeader() >= 6) return true;

        return unit.friendsNear().combatUnits().inRadius(minDistToFriend(), unit).count() == 0
            || unit.friendsNear().combatUnits().inRadius(1.5, unit).count() <= 1;
    }

    private double minDistToFriend() {
//        return unit.rangedEnemiesCount(3) > 0 ? 0.5 : 5;
        return unit.enemiesNearInRadius(8) > 0 ? 0.8 : 5;
    }

    @Override
    public Manager handle() {
//        if (unit.moveToLeader(Actions.MOVE_FORMATION, "ClusterGoon")) {
//            return usedManager(this);
//        }

        if (unit.distTo(friend) >= 1 && friend.isWalkable() && unit.move(friend, Actions.MOVE_FORMATION)) {
            return usedManager(this);
        }

        return null;
    }

    private AUnit friend() {
        Selection combatUnits = unit.friendsNear().groundUnits().exclude(unit).notRunning();

        return combatUnits.nearestTo(unit);
    }
}
