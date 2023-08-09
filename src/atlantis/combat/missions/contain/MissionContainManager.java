package atlantis.combat.missions.contain;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class MissionContainManager extends Manager {
    public MissionContainManager(AUnit unit) {
        super(unit);
    }

    protected Manager handle() {
        if (act()) {
            return usedManager(this);
        }

        return null;
    }

    protected boolean act() {
        return false;

//        AFocusPoint focusPoint = focusPoint();
//        unit.setTooltipTactical("#Contain(" + (focusPoint != null ? A.digit(focusPoint.distTo(unit)) : null) + ")");
//
//        if (focusPoint == null) {
//            MissionChanger.forceMissionAttack("InvalidFocusPoint");
//            return false;
//        }
//
////        if (handleUnitSafety(unit, true, true)) {
////            return true;
////        }
//
////        if (SquadScout.handle()) {
////            return true;
////        }
//
//        if (SquadCohesionManager.handle()) {
//            return true;
//        }
//
//        return (new MoveToContainFocusPoint()).move(unit, focusPoint);
    }
}
