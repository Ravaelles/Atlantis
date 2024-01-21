package atlantis.units.interrupt.protoss;

import atlantis.decions.Decision;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonWeaponRange;

public class ContinueDragoonAttack {
    public static Decision asDragoon(AUnit unit) {
        if (!unit.isDragoon()) return Decision.INDIFFERENT;

        if (preventMissionSpartaTooFarTargets(unit)) return Decision.FORBIDDEN;

        if (unit.noCooldown() || unit.lastAttackFrameLessThanAgo(30)) return Decision.ALLOWED;

        if (dontShootWhenAlmostDeadAndRangedEnemyIsNear(unit)) return Decision.FORBIDDEN;

        return Decision.INDIFFERENT;
    }

    private static boolean dontShootWhenAlmostDeadAndRangedEnemyIsNear(AUnit unit) {
        return unit.shields() <= 18
            && unit.enemiesNear().ranged().inRadius(4.3, unit).notEmpty();
    }

    private static boolean preventMissionSpartaTooFarTargets(AUnit unit) {
        return unit.hasTarget()
            && unit.isMissionDefendOrSparta()
            && unit.mission().focusPoint().isAroundChoke()
            && unit.distToTarget() > OurDragoonWeaponRange.range();
    }
}
