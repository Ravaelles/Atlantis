package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class UnfreezeGeneric extends Manager {
    public UnfreezeGeneric(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isAttacking()) return false;
        if (unit.hasCooldown() || unit.lastPositionChangedLessThanAgo(20)) return false;
        if (unit.lastStartedAttackLessThanAgo(13)) return false;

        return true;
    }

    @Override
    public Manager handle() {
        if (UnfreezerShakeUnit.shake(unit)) return usedManager(this);

        return null;
    }
}
