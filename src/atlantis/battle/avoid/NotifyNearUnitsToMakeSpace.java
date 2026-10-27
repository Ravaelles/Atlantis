package atlantis.battle.avoid;

import atlantis.game.A;
import atlantis.units.AUnit;

public class NotifyNearUnitsToMakeSpace {
    private final AUnit unit;

    public NotifyNearUnitsToMakeSpace(AUnit unit) {
        this.unit = unit;
    }

    public static boolean allowed(AUnit unit) {
        return new NotifyNearUnitsToMakeSpace(unit).allowedToNotifyNearUnitsToMakeSpace();
    }

    private boolean allowedToNotifyNearUnitsToMakeSpace() {
        return asDragoonAllowedToNotifyNearUnitsToMakeSpace()
            || unit.nearestChokeCenterDist() <= 4;
    }

    private boolean asDragoonAllowedToNotifyNearUnitsToMakeSpace() {
        if (!unit.isDragoon()) return false;

        return unit.hp() <= 121
            || unit.meleeEnemiesNearCount(4) >= A.whenEnemyProtossZerg(2, 3);
    }
}
