package atlantis.units.special;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

//public class FixStoppedUnits extends Manager {
//    public FixStoppedUnits(AUnit unit) {
//        super(unit);
//    }
//
//    @Override
//    public boolean applies() {
//        if (!unit.isCombatUnit()) return false;
//
//        return (unit.isStopped() || unit.isIdle() || unit.isStuck())
//            && unit.lastPositionChangedMoreThanAgo(90);
//    }
//
//    @Override
//    public Manager handle() {
/// /        unit.paintCircleFilled(16, Color.Teal);
//
////        if (A.everyNthGameFrame(39) && unit.moveToSafety(Actions.MOVE_UNFREEZE)) {
////            return usedManager(this, "FixStopped:Coh");
////        }
//
//        if (unit.lastActionMoreThanAgo(11, Actions.HOLD_POSITION)) {
//            unit.holdPosition("FixStopped:Hold");
//            return usedManager(this);
//        }
//
//        return null;
//    }
//}
