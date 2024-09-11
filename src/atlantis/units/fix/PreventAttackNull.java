package atlantis.units.fix;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import bwapi.Color;

public class PreventAttackNull extends Manager {
    public PreventAttackNull(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (unit.isMoving()) return false;
//        if (unit.isRunning()) return false;
        if (unit.lastActionLessThanAgo(3)) return false;

        return isAttackingNullTarget();
    }

    @Override
    public Manager handle() {
        return DoPreventFreezesLogic.handle(unit) ? usedManager(this) : null;
    }

    private boolean isAttackingNullTarget() {
        if (unit.isAttacking() || "Attack_Unit".equals(unit.action().toString())) {
            if (unit.target() == null || unit.target().hp() <= 0 || unit.orderTarget() == null) {
                A.errPrintln(
                    unit.typeWithUnitId() + " NULL ATTACK target:" + unit.target()
                        + " / hp=" + unit.hp() + " / alive:" + unit.isAlive()
                        + " / orderTarget=" + unit.orderTarget()
                        + " / orderTargetHP=" + (unit.orderTarget() != null ? unit.orderTarget().hp() : null)
                        + " / orderTargetAlive=" + (unit.orderTarget() != null ? unit.orderTarget().isAlive() : null)
                        + " / isAttacking()=" + unit.isAttacking()
                );
//                unit.paintCircleFilled(32, Color.Black);
                return true;
            }
        }

        return false;
    }
}
