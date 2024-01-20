package atlantis.combat.targeting;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class DefendingAroundFocus extends Manager {
    public DefendingAroundFocus(AUnit unit) {
        super(unit);
    }

    public boolean applies() {
        return unit.mission().focusPoint().isAroundChoke()
            && unit.isMissionDefendOrSparta()
            && unit.lastAttackFrameMoreThanAgo(80);
    }

    public AUnit targetToAttack() {
        AUnit target = ATargeting.possibleEnemyUnitsToAttack(unit, 12).nearestTo(unit);

//        System.err.println("DefendingAroundFocusTarget for " + unit.typeWithUnitId() + ": " + target);

        return target;
    }
}
