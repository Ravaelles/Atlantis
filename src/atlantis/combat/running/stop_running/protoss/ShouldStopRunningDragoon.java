package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class ShouldStopRunningDragoon extends Manager {
    public ShouldStopRunningDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isDragoon()
            && unit.lastUnderAttackMoreThanAgo(8)
            && unit.enemiesNear().canAttack(unit, safetyMargin(unit)).empty();
    }

    @Override
    protected Manager handle() {
        return usedManager(this);
    }

    private static double safetyMargin(AUnit unit) {
        return unit.hp() >= 42
            ? 0.2
            : (1 + unit.woundPercent() / 90.0);
    }
}
