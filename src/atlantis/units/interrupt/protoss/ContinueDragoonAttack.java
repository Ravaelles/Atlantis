package atlantis.units.interrupt.protoss;

import atlantis.decions.Decision;
import atlantis.units.AUnit;

public class ContinueDragoonAttack {
    public static Decision asDragoon(AUnit unit) {
        if (!unit.isDragoon()) return Decision.INDIFFERENT;
        if (unit.noCooldown() || unit.lastAttackFrameLessThanAgo(30)) return Decision.ALLOWED;

        if (unit.shields() <= 10) return Decision.FORBIDDEN;

        return Decision.INDIFFERENT;
    }
}
