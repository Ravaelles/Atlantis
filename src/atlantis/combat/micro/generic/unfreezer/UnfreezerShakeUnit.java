package atlantis.combat.micro.generic.unfreezer;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.squad.positioning.protoss.cohesion.ProtossCombat2Combat;
import atlantis.combat.squad.positioning.protoss.formations.ProtossFormation;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

import static atlantis.units.actions.Actions.MOVE_IDLE;

public class UnfreezerShakeUnit {
    public static boolean shake(AUnit unit) {
        if (
            unit.lastActionMoreThanAgo(40, Actions.UNFREEZE)
                || unit.lastPositionChangedLessThanAgo(10)
        ) {
            unit.stop(null);
            unit.setAction(Actions.UNFREEZE);
            return true;
        }
        else {
            if (goToLeader(unit)) return true;

            if((new ProtossFormation(unit)).invokedFrom(null)) {
                return true;
            }

            if (unfreezeToClosePoint(unit)) return true;
        }

        return false;
    }

    private static boolean goToLeader(AUnit unit) {
        AUnit leader = unit.squadLeader();
        if (leader == null) return false;

        if (unit.distTo(leader) > 0.5) {
            unit.move(leader, Actions.UNFREEZE);
            return true;
        }

//        else {
//            if (unit.moveAwayFrom(0.2, leader, Actions.UNFREEZE)) {
//                return true;
//            }
//        }

        return false;
    }

    private static boolean unfreezeToClosePoint(AUnit unit) {
        if (unit.lastActionLessThanAgo(60, MOVE_IDLE)) return false;

        int pixelDX = 10;
        HasPosition goTo = unit.translateByPixels(pixelDX, pixelDX);

        if (goTo != null && !isGoToOkay(goTo, unit)) {
            goTo = unit.translateByPixels(-pixelDX, -pixelDX);
        }
        else if (goTo != null && !isGoToOkay(goTo, unit)) {
            goTo = unit.translateByPixels(pixelDX, -pixelDX);
        }
        else if (goTo != null && !isGoToOkay(goTo, unit)) {
            goTo = unit.translateByPixels(-pixelDX, pixelDX);
        }
        else if (goTo != null && !isGoToOkay(goTo, unit)) {
            goTo = Select.our().exclude(unit).nearestTo(unit);
        }

        if (goTo != null) {
            unit.move(goTo, MOVE_IDLE);
            return true;
        }
        return false;
    }

    private static boolean isGoToOkay(HasPosition goTo, AUnit unit) {
        return goTo.isWalkable() && unit.allUnitsNear().countInRadius(0.1, goTo) == 0;
    }
}
