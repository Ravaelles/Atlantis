package atlantis.combat.running.stop_running.protoss;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class ShouldStopRunningDragoon extends Manager {
    public ShouldStopRunningDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isDragoon()) return false;
        if (unit.cooldown() >= 15) return false;
//        if (unit.isDancing() && unit.shields() >= 3) return false;
//
//        if (unit.hp() <= 60) return false;

        if (stopVsZerg()) return true;

        Selection enemies = unit.enemiesNear();
        Selection meleeEnemies = enemies.melee();
        if (meleeEnemies.inRadius(3.1, unit).atLeast(2)) return false;

//        unit.paintCircleFilled(10, Color.Yellow);

//        if (unit.woundHp() <= 11) return true;

//        return meleeEnemies.inRadius(2.9, unit).empty()
//            || meleeEnemies.canAttack(unit, safetyMargin(unit)).empty();
        return enemies.canAttack(unit, safetyMargin(unit)).empty();
    }

    private boolean stopVsZerg() {
        if (!Enemy.zerg()) return false;
//        if (!unit.isDragoon()) return false;

//        System.err.println("unit.eval() = " + unit.eval());

//        System.err.println("unit.eval() = " + unit.eval() + " / " + unit.enemiesThatCanAttackMe(0.5).count());
//        return unit.eval() >= 0.85 && unit.shieldWound() <= 9 && unit.cooldown() <= 6
//        return unit.shields() >= 5
        return unit.cooldown() <= 6
            && (unit.eval() >= 1.1 || unit.enemiesThatCanAttackMe(0.5).count() <= 1);
//            && A.println("DragoonStopVsZerg:" + A.now());
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

//        unit.paintCircleFilled(11, Color.Brown);

//        System.err.println(A.minSec() + " - " + unit.typeWithUnitId() + " - StopGoon");

//        if (unit.moveToLeader(Actions.MOVE_FORMATION, "StopGoon")) return usedManager(this);
//        if (unit.mission().handleManagerClass(unit) != null) return usedManager(this);

//        if ((new HandleUnitPositioningOnMap(unit)).invokeFrom(this) != null) return usedManager(this);
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
