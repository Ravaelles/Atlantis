package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import bwapi.Color;

public class ShouldStopRunningDragoon extends Manager {
    public ShouldStopRunningDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isDragoon()) return false;

//        if (unitBecameIdleAfterRunning()) return true;
//        if (!unit.isMoving()) return true;

//        if (unit.lastActionMoreThanAgo(2)) return false;
//        if (unit.lastStartedRunningLessThanAgo(7)) return false;

        Selection enemies = unit.enemiesNear();
        Selection meleeEnemies = enemies.melee();
        if (meleeEnemies.inRadius(3.7, unit).notEmpty()) return false;

//        unit.paintCircleFilled(10, Color.Yellow);

//        if (unit.woundHp() <= 11) return true;

//        return meleeEnemies.inRadius(2.9, unit).empty()
//            || meleeEnemies.canAttack(unit, safetyMargin(unit)).empty();
        return enemies.canAttack(unit, safetyMargin(unit)).empty();
    }

    private boolean unitBecameIdleAfterRunning() {
        return (unit.isStopped() || unit.isIdle())
            && unit.noCooldown()
            && unit.lastStartedRunningLessThanAgo(70);
    }

    @Override
    protected Manager handle() {
//        unit.paintCircleFilled(28, Color.Yellow);
        ProtossShouldStopRunning.decisionStopRunning(unit);

//        if ((new AttackNearbyEnemies(unit)).invokedFrom(this)) return usedManager(this);
//        if (unit.mission().handleManagerClass(unit) != null) return usedManager(this);

        unit.paintCircleFilled(11, Color.Brown);

        if (unit.moveToLeader(Actions.MOVE_FORMATION, "StopGoon")) return usedManager(this);

//        if ((new HandleFocusPointPositioning(unit)).invokeFrom(this) != null) return usedManager(this);
//        if (unit.isMoving()) {
//            unit.holdPosition("StopDragoon&Hold");
//            return usedManager(this);
//        }

        return null;
    }

    private static double safetyMargin(AUnit unit) {
        return 0.4 + (unit.woundPercent() / 60.0);
    }
}
