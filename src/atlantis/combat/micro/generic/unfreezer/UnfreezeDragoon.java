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
    }

    @Override
    public Manager handle() {
        if (A.isUms()) {
//            unit.commandHistory().print("UnfreezeDragoon#" + unit.id() + " command history (now: " + A.now + ")");
//            PauseAndCenter.on(unit, true);
        }

//        CenterCamera.on(unit, true, Color.Blue);
//        GameSpeed.changeSpeedTo(70);
//        unit.paintCircleFilled(10, Color.Blue);

//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - UnfreezeDragoon");

        if (UnfreezerShakeUnit.shake(unit)) return usedManager(this);

        return null;
    }
}
