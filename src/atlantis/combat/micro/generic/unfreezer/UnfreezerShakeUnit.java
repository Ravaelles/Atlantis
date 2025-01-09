package atlantis.combat.micro.generic.unfreezer;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class UnfreezerShakeUnit {
    public static boolean shake(AUnit unit) {
        int framesModulo = A.now % 50;

        if (framesModulo <= 19) {
            if (framesModulo % 3 == 0) unit.stop("UnfreezeC");
            return true;
        }

        if (framesModulo <= 26) {
            if (!unit.isHoldingPosition()) return unit.holdPosition(Actions.HOLD_POSITION, "UnfreezeHold");
            return true;
        }

        if (framesModulo <= 31) {
            if (framesModulo % 3 == 0 && unit.moveToLeader(Actions.SPECIAL, "UnfreezeC")) return true;
            return true;
        }

        if (framesModulo <= 35) {
            if (framesModulo % 3 == 0 && unfreezeByTakingSmallStep(unit)) return true;
            return true;
        }

        return false;
    }

    private static boolean unfreezeByTakingSmallStep(AUnit unit) {
        int sign = A.s % 2 == 0 ? 1 : -1;
        APosition moveTo = unit.position().translateByPixels(4 * sign, 4 * sign);
        if (moveTo == null || !moveTo.isWalkable()) return false;

        if (unit.move(
            moveTo, Actions.MOVE_UNFREEZE, "UnfreezeStep"
        )) {
            return true;
        }

        return false;
    }

    private static boolean goToFocus(AUnit unit) {
        AFocusPoint focus = unit.focusPoint();
        if (focus != null) {
            HasPosition goTo = focus;
            if (focus.fromSide() != null) {
                goTo = focus.translateTilesTowards(2.5, focus.fromSide());
            }

            double distToFocus = unit.distTo(focus);

            if (distToFocus <= 3) {
//                unit.moveTo(Actions.MOVE_UNFREEZE, "UnfreezeByMoveBase");
                unit.moveAwayFrom(focus, 0.2, Actions.MOVE_UNFREEZE, "UnfreezeByMoveBase");
                return true;
            }
            if (distToFocus >= 6) {
                unit.move(goTo, Actions.MOVE_UNFREEZE, "UnfreezeByMove");
                return true;
            }
        }
        return false;
    }

    private static boolean goToLeader(AUnit unit) {
        AUnit leader = unit.squadLeader();

        if (leader != null && leader.distTo(unit) >= 6) {
            if (unit.move(leader, Actions.MOVE_FORMATION, "2Leader")) {
                return true;
            }
        }

        return false;
    }

    private static boolean goToNearestCombatFriend(AUnit unit) {
        AUnit nearest = unit.friendsNear().combatUnits().groundUnits().nearestTo(unit);

        if (nearest != null && nearest.distTo(unit) >= 2) {
            if (unit.move(nearest, Actions.MOVE_FORMATION, "PreventMove2Friend")) {
                return true;
            }
        }

        return false;
    }

    private static APosition goToPositionNearby(AUnit unit) {
        int moduloId = unit.id() % 5;
        return unit.position().translateByPixels(-16 + moduloId * 8, 16 - moduloId * 8);
    }

    private static boolean shouldNotDoAnythingButContinue(AUnit unit) {
        return unit.lastActionLessThanAgo(10, Actions.HOLD_POSITION, Actions.STOP, Actions.MOVE_UNFREEZE);
    }
}
