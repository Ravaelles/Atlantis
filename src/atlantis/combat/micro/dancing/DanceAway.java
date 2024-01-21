package atlantis.combat.micro.dancing;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class DanceAway extends Manager {
    private AUnit enemy;

    public DanceAway(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        enemy = defineUnitToDanceAwayFrom();
        if (enemy == null) return false;
        if (!enemy.effVisible()) return false;

        if (dragoonLowHpAndStillUnderAttack()) return true;

//        if (continueDancingAway()) return true;

//        if (unit.cooldown() <= 7 && unit.woundPercent() <= 1) return false;

//        System.err.println("@ " + A.now() + " - " + unit.id() + " - awayFrom = " + awayFrom);

        if (unit.isMissionSparta() && EnemyWhoBreachedBase.noone()) return false;

        double dist = unit.distTo(enemy);

        return (enemyIsTooClose(dist) || unit.hp() <= minHp())
            && !unit.isStartingAttack()
            && !unit.isAttackFrame();
//            && dist >= (unit.enemyWeaponRangeAgainstThisUnit(awayFrom));
    }

    private boolean dragoonLowHpAndStillUnderAttack() {
        return unit.isDragoon()
            && unit.hp() <= 40
            && unit.lastUnderAttackLessThanAgo(70)
            && unit.enemiesNearInRadius(4) > 0;
    }

    private boolean enemyIsTooClose(double dist) {
        double woundBonus = unit.woundPercent() / 70.0 + (unit.hp() <= 30 ? 0.8 : 0);

        return enemy.weaponRangeAgainst(unit) + 0.85 + woundBonus >= dist;
    }

    private boolean continueDancingAway() {
        return unit.isMoving()
            && unit.lastActionLessThanAgo((int) (70 + unit.woundPercent() / 39.0), Actions.MOVE_DANCE_AWAY)
            && (
            unit.hasCooldown()
                || unit.hp() <= minHp()
                || unit.lastUnderAttackLessThanAgo((int) (20 + unit.woundPercent() / 20.0))
        );
    }

    private int minHp() {
        if (unit.isDragoon()) return 59;

        return 34;
    }

    private AUnit defineUnitToDanceAwayFrom() {
        return unit.enemiesNear().havingPosition().havingWeapon().nearestTo(unit);

//        if (target != null) return target;
//
//        return unit.enemiesNear().nearestTo(unit);
    }

    @Override
    public Manager handle() {
//        unit.paintCircleFilled(24, Color.Purple);

        if (continueDancingAway()) {
            unit.paintCircleFilled(24, Color.Blue);
//            unit.paintLine(unit.targetPosition(), Color.Teal);
//            unit.paintLine(unit.targetPosition().translateByPixels(1, 1), Color.Teal);
            return usedManager(this);
        }

        String logString = "DanceAway-" + unit.cooldownRemaining();
        unit.addLog(logString);
//        System.err.println("@ " + A.now() + " - " + unit.id() + " - DANCE AWAY FROM " + awayFrom);

        if (danceAwayFromTarget(logString)) {
            unit.paintCircleFilled(18, Color.Teal);
            return usedManager(this);
        }

        System.err.println("@@@@@@@@@@@@ " + A.now() + " - " + unit.id() + " - DANCE AWAY ERROR " + enemy);
        return null;
    }

    private boolean danceAwayFromTarget(String logString) {
        return unit.moveAwayFrom(enemy.position(), 1.7, Actions.MOVE_DANCE_AWAY, logString);
    }
}
