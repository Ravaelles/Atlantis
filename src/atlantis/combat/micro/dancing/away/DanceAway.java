package atlantis.combat.micro.dancing.away;

import atlantis.architecture.Manager;
import atlantis.decions.Decision;
import atlantis.game.A;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import bwapi.Color;

public class DanceAway extends Manager {
    private AUnit enemy;
    private Decision decision;

    public DanceAway(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (unit.noCooldown()) return false;
        if (!unit.isRanged()) return false;
        if (unit.lastAttackFrameMoreThanAgo(30 * 5)) return false;
        if (unit.lastStartedRunningLessThanAgo(5)) return false;

        if (unit.enemiesNear().ranged().canAttack(unit, 2.1).empty()) return false;

        enemy = defineUnitToDanceAwayFrom();
        if (enemy == null) return false;
        if (!enemy.effVisible()) return false;
//        if (runningFromIsDead()) return false;

//        if (continueDancingAway()) {
//            unit.paintCircleFilled(24, Color.Blue);
////            unit.paintLine(unit.targetPosition(), Color.Teal);
////            unit.paintLine(unit.targetPosition().translateByPixels(1, 1), Color.Teal);
//            return true;
//        }

        if (continueDancingAway()) {
            unit.paintCircleFilled(24, Color.Blue);
            unit.paintLine(unit.targetPosition(), Color.Teal);
            unit.paintLine(unit.targetPosition().translateByPixels(1, 1), Color.Teal);
            return true;
        }

        if (unit.isMissionSparta() && EnemyWhoBreachedBase.noone()) return false;

        decision = (new DanceAwayAsDragoon(unit, enemy)).applies();
        if (decision.notIndifferent()) return decision.toBoolean();

//        if (continueDancingAway()) return true;

//        if (unit.cooldown() <= 7 && unit.woundPercent() <= 1) return false;

//        System.err.println("@ " + A.now() + " - " + unit.id() + " - awayFrom = " + awayFrom);

        double dist = unit.distTo(enemy);

        return (enemyIsTooClose(dist) || unit.hp() <= minHp())
            && !unit.isStartingAttack()
            && !unit.isAttackFrame();
//            && dist >= (unit.enemyWeaponRangeAgainstThisUnit(awayFrom));
    }

//    private boolean runningFromIsDead() {
//        return unit.isActiveManager(this)
//            && unit.isRunning()
//            && (unit.runningFrom() == null || !unit.runningFrom().isAlive());
//    }

    private boolean enemyIsTooClose(double dist) {
        double woundBonus = unit.woundPercent() / 70.0 + (unit.hp() <= 30 ? 0.8 : 0);

        return enemy.weaponRangeAgainst(unit) + 0.85 + woundBonus >= dist;
    }

    private boolean continueDancingAway() {
        if (!unit.isMoving()) return false;
        if (
            unit.lastActionMoreThanAgo(40, Actions.MOVE_DANCE_AWAY)
                && unit.lastUnderAttackMoreThanAgo((int) (10 + unit.woundPercent() / 2))
        ) return false;
        if (unit.targetPosition() == null) return false;
//        if (unit.distTo(unit.targetPosition()) <= 0.5) return false;

//        Selection enemiesNear = unit.enemiesNear().inRadius(7 + unit.woundPercent() >= 50 ? 1 : 0, unit);
        Selection enemiesNear = unit.enemiesNear().inRadius(enemiesRadius(), unit);

        if (enemiesNear.empty()) return false;

        if (decision != null && decision.isTrue()) {
            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - CONTINUE DANCE AWAY");
            return true;
        }

        if (unit.enemiesNearInRadius(enemiesRadius()) > 0) return true;

//        return unit.isMoving()
        return unit.isActiveManager(DanceAway.class)
            && (unit.cooldownRemaining() <= 5);

//        if (!unit.isMoving()) return false;
//        if (unit.isStopped()) return false;
//        if (!unit.isRunning()) return false;

//        return (unit.isMoving() || unit.isAccelerating())
//            && unit.lastActionLessThanAgo((int) (90 + unit.woundPercent() / 39.0), Actions.MOVE_DANCE_AWAY)
//        return unit.lastActionLessThanAgo((int) (90 + unit.woundPercent() / 36.0), Actions.MOVE_DANCE_AWAY)
//            && (unit.cooldown() >= 3 || unit.enemiesNearInRadius(enemiesRadius()) > 0);
//            && (
//            unit.hasCooldown()
//                || unit.hp() <= minHp()
//                || unit.lastUnderAttackLessThanAgo((int) (20 + unit.woundPercent() / 13.0))
//                || unit.enemiesNearInRadius(4) > 0
//        );
    }

    private double enemiesRadius() {
        return 4 + unit.woundPercent() / 40.0;
    }

    private int minHp() {
        if (unit.isDragoon()) return 59;

        return 34;
    }

    private AUnit defineUnitToDanceAwayFrom() {
        return unit.enemiesNear().havingPosition().havingWeapon().canAttack(unit, 3).nearestTo(unit);
    }

    @Override
    public Manager handle() {
//        unit.paintCircleFilled(24, Color.Purple);

//        if (continueDancingAway()) {
//            return usedManager(this);
//        }

        String logString = "DanceAway-" + unit.cooldownRemaining();
        unit.addLog(logString);
//        System.err.println("@ " + A.now() + " - " + unit.id() + " - DANCE AWAY FROM " + awayFrom);

        if (danceAwayFromTarget(logString)) {
//            unit.paintCircleFilled(18, Color.Teal);
            return usedManager(this);
        }

        return danceAwayError();
    }

    private Manager danceAwayError() {
//        System.err.println("@@@@@@@@@@@@ " + A.now() + " - " + unit.id() + " - DANCE AWAY ERROR " + enemy);

        if (unit.hasCooldown()) {
            unit.moveToMain(Actions.MOVE_DANCE_AWAY, "DanceAwayError");
            return usedManager(this);
        }

        return null;
    }

    private boolean danceAwayFromTarget(String logString) {
//        return unit.moveAwayFrom(enemy.position(), enemiesRadius(), Actions.MOVE_DANCE_AWAY, logString);
        return unit.runningManager().runFrom(
            enemy.position(), danceAwayDist(), Actions.MOVE_DANCE_AWAY, false
        );
    }

    private double danceAwayDist() {
        return 2.2 + unit.woundPercent() / 40.0;
    }
}
