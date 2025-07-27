package atlantis.combat.squad.positioning.protoss.cluster;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ProtossForceClusterZealot extends Manager {
    public static final double DIST_BETWEEN_ZEALOTS = 0.25;

    private AUnit friend;

    public ProtossForceClusterZealot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMissionAttack()) return false;

        return unit.isZealot()
//            && !unit.isAttacking()
            && unit.lastUnderAttackMoreThanAgo(40)
            && zealotsTooFar()
            && (friend = friend()) != null;
    }

    private boolean zealotsTooFar() {
        return unit.friendsNear().combatUnits().groundUnits().inRadius(DIST_BETWEEN_ZEALOTS, unit).count() == 0
            && unit.distToLeader() >= 4;
    }

    @Override
    public Manager handle() {
//        if (unit.moveToLeader(Actions.MOVE_FORMATION, "ClusterZealot")) {
//            return usedManager(this);
//        }

        if (unit.distTo(friend) >= 1 && unit.move(friend, Actions.MOVE_FORMATION)) {
            return usedManager(this);
        }

        return null;
    }

    private AUnit friend() {
        Selection combatUnits = Select.ourCombatUnits().exclude(unit);

        AUnit zealot = combatUnits.zealots().nearestTo(unit);
        if (zealot != null) return zealot;

        return combatUnits.groundUnits().nearestTo(unit);
    }
}
