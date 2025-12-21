package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class TooCloseToFocusPoint extends MoveToFocusPoint {
    private DistFromFocus distFromFocus;
    private double margin;

    public TooCloseToFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isLoaded()) return false;
//        if (unit.isMissionAttackOrGlobalAttack()) return false;
        if (unit.isSpecialMission() && unit.isMelee()) return false;
        if (Count.ourCombatUnits() >= 8) return false;
        if (focus == null || !focus.isValid() || !focus.isAroundChoke()) return false;
//        if (EnemyUnitBreachedBase.notNull()) return false;
        if (unit.enemiesNear().canAttack(unit, 4).notEmpty()) return false;
        if (unit.lastActionLessThanAgo(60, Actions.LOAD)) return false;
        if (unit.groundDistToMain() <= 15) return false;

        margin = unit.distTo(focus) - optimalDist(focus);

        return margin < 0.5;
    }

    protected Manager handle() {
        if (focus == null) return null;

        if (act()) return usedManager(this);

        return null;
    }

    private double optimalDist(AFocusPoint focusPoint) {
        return OptimalDistanceToFocusPoint.forUnit(unit, focusPoint);
    }

    protected boolean act() {
//        if (asDragoon()) return true;
//
////        APosition goTo = isTooFar ? focusPoint : unit.translateTilesTowards(0.1, focusPoint);
//        if (goAway()) return true;
//        if (goAwayFromCenter()) return true;
//        if (goToMain()) return true;

        if (margin > 0.1) {
            if (unit.isMelee()) return false;

            if (A.everyNthGameFrame(3)) {
                unit.stop("TooCloseStop");
                return true;
            }
        }

        if (margin >= -0.3 && goAway()) return true;
//        if (margin >= -2 && goAwayFromCenter()) return true;

        if (goToMain()) return true;

        return false;
    }

    private boolean goAway() {
//        if (A.fr % 50 <= 15) return unit.moveToMain(Actions.MOVE_FOCUS);

        return unit.moveAwayFrom(focus, 0.06, Actions.MOVE_FOCUS, "TooCloseF");
    }

    private boolean asDragoon() {
        if (!unit.isDragoon()) return false;

//        System.err.println("TOO CLOSE = " + unit.distToFocusPoint() + " / " + unit);
//        if (unit.distToFocusPoint() <= 2.6) {

        if (Missions.isGlobalMissionDefendOrSparta()) {
            if (A.everyNthGameFrame(10)) unit.holdPosition(Actions.MOVE_FORMATION, "DragoonTooCloseA");
//            else unit.moveToMain(Actions.MOVE_FOCUS, "DragoonTooCloseB");
            else goAway();
        }
//        }

        return false;

//        if (unit.hp() >= 30) {
//            unit.holdPosition("DragoonHold");
//            return true;
//        }
//
//        return false;
    }

    private boolean goAwayFromCenter() {
        AChoke choke = focus.choke();
        HasPosition goTo = choke;

        if (goTo == null) return false;

        goTo = goTo.translateTilesTowards(-0.2, choke);

        if (goTo.isWalkable()) {
            if (unit.move(goTo, Actions.MOVE_FOCUS, "TooClose", true)) return true;
        }

        return false;
    }

    private boolean goToMain() {
//        if (unit.isDragoon()) {
//            if (A.everyNthGameFrame(3)) {
//                unit.holdPosition("SlowlyTooClose");
//            }
//            return false;
//        }

        HasPosition goTo = fromSide != null ? fromSide : Select.main();

        if (goTo != null && goTo.isWalkable()) {
            if (unit.move(goTo, Actions.MOVE_FOCUS, "TooCloseM", true)) return true;
        }

        return false;
    }
}
