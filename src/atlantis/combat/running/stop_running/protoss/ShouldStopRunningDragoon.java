package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class ShouldStopRunningDragoon {
    public static boolean shouldStopRunning(AUnit unit) {
        return unit.isDragoon()
            && unit.lastUnderAttackMoreThanAgo(8)
            && unit.enemiesNear().canAttack(unit, safetyMargin(unit)).empty();
    }

    private static double safetyMargin(AUnit unit) {
        return unit.hp() >= 42
            ? 0.2
            : (1 + unit.woundPercent() / 90.0);
    }
}
