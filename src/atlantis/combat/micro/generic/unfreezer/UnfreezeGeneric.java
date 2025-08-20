package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.terran.chokeblockers.ChokeBlockersAssignments;
import atlantis.terran.chokeblockers.ChokeToBlock;
import atlantis.units.AUnit;
import atlantis.util.We;

public class UnfreezeGeneric extends Manager {
    public UnfreezeGeneric(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.hasCooldown()) return false;
        if (We.terran()) return false;
        if (unit.isMedic()) return false;
        if (unit.isDragoon()) return false;
        if (unit.isReaver()) return false;

        if (unit.lastAttackFrameLessThanAgo(40)) return false;
        if (unit.lastPositionChangedLessThanAgo(72)) return false;

        if (ChokeBlockersAssignments.get().isChokeBlocker(unit)) return false;

        return true;
    }

    @Override
    public Manager handle() {
        if (UnfreezerShakeUnit.shake(unit)) return usedManager(this);

        return null;
    }
}
