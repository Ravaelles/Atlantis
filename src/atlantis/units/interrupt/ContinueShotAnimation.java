package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.protoss.ContinueShootingAsDragoon;
import atlantis.util.We;

public class ContinueShotAnimation extends Manager {
    public ContinueShotAnimation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.cooldown() >= 1 && unit.cooldown() <= 24) return false;

        if (unit.isStartingAttack()) return true;
//        System.out.println(A.now() + " / " + unit.cooldown() + " / AF=" + unit.isAttackFrame());
        if (unit.isAttackFrame()) return true;

        return false;
    }

    public Manager handle() {
//        return this;
        return usedManager(this);
    }
}
