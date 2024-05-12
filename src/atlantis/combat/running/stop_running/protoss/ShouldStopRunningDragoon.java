package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ShouldStopRunningDragoon extends Manager {
    public ShouldStopRunningDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isDragoon()) return false;

        if (unitBecameIdleAfterRunning()) return true;
        if (!unit.isMoving()) return true;

        if (unit.lastActionMoreThanAgo(2)) return false;
        if (unit.lastStartedRunningLessThanAgo(7)) return false;

//        if (unit.woundHp() <= 11) return true;

        Selection meleeEnemies = unit.enemiesNear().melee();

//        return meleeEnemies.inRadius(2.9, unit).empty()
//            || meleeEnemies.canAttack(unit, safetyMargin(unit)).empty();
        return meleeEnemies.canAttack(unit, safetyMargin(unit)).empty();
    }

    private boolean unitBecameIdleAfterRunning() {
        return (unit.isStopped() || unit.isIdle())
            && unit.noCooldown()
            && unit.lastStartedRunningLessThanAgo(70);
    }

    @Override
    protected Manager handle() {
        ProtossShouldStopRunning.decisionStopRunning(unit);

        if ((new AttackNearbyEnemies(unit)).invoked(this)) return usedManager(this);
        if (unit.mission().handleManagerClass(unit) != null) return usedManager(this);

        return null;
    }

    private static double safetyMargin(AUnit unit) {
        return 1.4 + (unit.woundPercent() / 39.0);
    }
}
