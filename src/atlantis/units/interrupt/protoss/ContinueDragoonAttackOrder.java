package atlantis.units.interrupt.protoss;

import atlantis.architecture.Manager;
import atlantis.decions.Decision;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonWeaponRange;
import bwapi.Color;

public class ContinueDragoonAttackOrder extends Manager {
    public ContinueDragoonAttackOrder(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAttacking() && unit.isDragoon() && asDragoon(unit).isTrue();
    }

    @Override
    protected Manager handle() {
        return usedManager(this);
    }

    public static Decision asDragoon(AUnit unit) {
        if (!unit.isDragoon()) return Decision.INDIFFERENT;

        if (preventMissionSpartaTooFarTargets(unit)) {
            unit.paintCircleFilled(28, Color.Grey);
            return Decision.FORBIDDEN;
        }

        if (unit.noCooldown() || unit.lastAttackFrameLessThanAgo(30)) return Decision.ALLOWED;

//        if (dontShootWhenAlmostDeadAndRangedEnemyIsNear(unit)) return Decision.FORBIDDEN;

        return Decision.INDIFFERENT;
    }

//    private static boolean dontShootWhenAlmostDeadAndRangedEnemyIsNear(AUnit unit) {
//        return unit.shields() <= 18
//            && unit.enemiesNear().ranged().inRadius(4.3, unit).notEmpty();
//    }

    private static boolean preventMissionSpartaTooFarTargets(AUnit unit) {
        return unit.hasTarget()
            && unit.isMissionDefendOrSparta()
            && unit.mission().focusPoint().isAroundChoke()
            && unit.distToNearestChoke() <= 4
            && unit.distToTarget() > OurDragoonWeaponRange.range();
    }
}