package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class EarlyGameTooClustered extends Manager {
    private AUnit nearestBuddy;

    public EarlyGameTooClustered(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;
        
        if (A.seconds() <= 300 && unit.isMissionDefend() && unit.friendsNear().inRadius(2, unit).notEmpty()) {
            return true;
        }

        return false;
    }

    public Manager handle() {
        if (unit.lastActionLessThanAgo(5, Actions.MOVE_FORMATION)) {
            return null;
        }

        if (isTooClustered()) {
            unit.moveAwayFrom(nearestBuddy, 1, "MoreSpace", Actions.MOVE_FORMATION);
            return usedManager(this);
        }

        return null;
    }

    // =========================================================

    private boolean isTooClustered() {
        Selection ourCombatUnits = Select.ourCombatUnits().inRadius(5, unit);
        nearestBuddy = ourCombatUnits.clone().nearestTo(unit);
        double minDistBetweenUnits = minDistBetweenUnits();

        return nearestBuddy != null && nearestBuddy.distToLessThan(unit, minDistBetweenUnits);
    }

    private double minDistBetweenUnits() {
        return preferedBaseDistToNextUnit();
    }

    private double preferedBaseDistToNextUnit() {
        if (unit.isTank()) {
            return 2;
        }

        return 1.5;
    }
}
