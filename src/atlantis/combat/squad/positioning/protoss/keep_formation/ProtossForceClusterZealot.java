package atlantis.combat.squad.positioning.protoss.keep_formation;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ProtossForceClusterZealot extends Manager {
    private AUnit friend;

    public ProtossForceClusterZealot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isZealot()
//            && !unit.isAttacking()
            && unit.lastUnderAttackMoreThanAgo(30)
            && unit.friendsNear().combatUnits().groundUnits().inRadius(1, unit).count() == 0
            && (friend = friend()) != null;
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

        AUnit zealot = combatUnits.zealots().nearestTo(unit);
        if (zealot != null) return zealot;

        return combatUnits.groundUnits().nearestTo(unit);
    }
}
