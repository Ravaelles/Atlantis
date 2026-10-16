package atlantis.combat.targeting;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AliveEnemies;
import bwapi.Color;

public class TargetingWhenDefendingAroundFocus extends Manager {
    public TargetingWhenDefendingAroundFocus(AUnit unit) {
        super(unit);
    }

    public boolean applies() {
        if (true) return false;

        return unit.isMissionDefendOrSparta()
            && unit.mission().focusPoint().isAroundChoke()
            && unit.distTo(unit.mission().focusPoint()) <= 4;
//            && unit.lastAttackFrameMoreThanAgo(60);
    }

    public AUnit targetToAttack() {
        AUnit target = AliveEnemies.get().canBeAttackedBy(unit, 15).nearestTo(unit);

        if (target != null) {
            unit.paintLineDouble(target, Color.Green);
            target.paintCircleFilled(14, Color.Green);
        }

//        System.err.println("DefendingAroundFocusTarget for " + unit.typeWithUnitId() + ": " + target);

        return target;
    }
}
