package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.game.GameSpeed;
import atlantis.units.AUnit;
import atlantis.util.CenterCamera;
import atlantis.util.PauseAndCenter;
import bwapi.Color;

public class UnfreezeDragoon extends Manager {
    public UnfreezeDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isDragoon()) return false;
        if (unit.hasCooldown()) return false;
        if (unit.lastAttackFrameLessThanAgo(40)) return false;

        boolean underAttackRecently = unit.lastUnderAttackLessThanAgo(15);
        if (!underAttackRecently) return false;

        boolean movedVeryRecently = unit.lastPositionChangedLessThanAgo(30);
        if (movedVeryRecently) return false;

        if (unit.isLeader()) {
            Squad squad = unit.squad();
            if (squad != null) squad.changeLeader();
        }

        return true;
//            && (unit.isStopped() || unit.u().getOrder().equals(Order.Stop))
//            && !unit.isHoldingPosition()
//            && (
//            unit.isStopped()
//                || unit.u().getOrder().equals(Order.Stop)
//            unit.lastPositionChangedMoreThanAgo(120)
//                ||
//            (underAttackRecently && !movedVeryRecently)
//        )
//            && (underAttackRecently || unit.lastPositionChangedMoreThanAgo(80));
//            && unit.u().isSt
//            && (unit.isStopped() || unit.isHoldingPosition())
//            && unit.lastActionMoreThanAgo(20, Actions.STOP)
//            && (unit.isMoving() || unit.isStopped())
//            && (!unit.isLeader() || unit.lastPositionChangedMoreThanAgo(30 * 6));
//            && unit.noCooldown();
//            && unit.lastActionMoreThanAgo(20, Actions.HOLD_POSITION);
    }

    @Override
    public Manager handle() {
        if (A.isUms()) {
            unit.commandHistory().print("UnfreezeDragoon#" + unit.id() + " command history (now: " + A.now + ")");
        }

//        CenterCamera.on(unit, true, Color.Blue);
//        GameSpeed.changeSpeedTo(70);
//        unit.paintCircleFilled(10, Color.Blue);

//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - UnfreezeDragoon");

        if (UnfreezerShakeUnit.shake(unit)) return usedManager(this);

        return null;
    }
}
