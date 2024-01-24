package atlantis.combat.micro.dancing;

import atlantis.architecture.Manager;
import atlantis.decions.Decision;
import atlantis.game.A;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.protoss.ProtossFlags;
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

        Decision decision = dragoonDecision();
        if (decision.notIndifferent()) {
            if (decision.isForbidden()) return false;
            if (decision.isAllowed()) return true;
        }

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

    private Decision dragoonDecision() {
        if (dragoonLowHpAndStillUnderAttack()) return Decision.FORBIDDEN;
        if (dragoonEnemyClose()) return Decision.FORBIDDEN;

        return Decision.INDIFFERENT;
    }

    private boolean dragoonEnemyClose() {
        return unit.isDragoon()
//            && unit.lastAttackFrameLessThanAgo(150)
            && unit.enemiesNearInRadius(enemiesRadius()) > 0;
    }

    private boolean dragoonLowHpAndStillUnderAttack() {
        return unit.isDragoon()
            && !ProtossFlags.dragoonBeBrave()
            && unit.hp() <= 40
            && (
            unit.lastUnderAttackLessThanAgo(90)
                || unit.enemiesNearInRadius(enemiesRadius()) > 0
        );
    }

    private boolean enemyIsTooClose(double dist) {
        double woundBonus = unit.woundPercent() / 70.0 + (unit.hp() <= 30 ? 0.8 : 0);

        return enemy.weaponRangeAgainst(unit) + 0.85 + woundBonus >= dist;
    }

    private boolean continueDancingAway() {
//        return (unit.isMoving() || unit.isAccelerating())
//            && unit.lastActionLessThanAgo((int) (90 + unit.woundPercent() / 39.0), Actions.MOVE_DANCE_AWAY)
        return unit.isMoving()
            && unit.lastActionLessThanAgo((int) (90 + unit.woundPercent() / 36.0), Actions.MOVE_DANCE_AWAY)
            && (unit.cooldown() >= 3 || unit.enemiesNearInRadius(enemiesRadius()) > 0);
//            && (
//            unit.hasCooldown()
//                || unit.hp() <= minHp()
//                || unit.lastUnderAttackLessThanAgo((int) (20 + unit.woundPercent() / 13.0))
//                || unit.enemiesNearInRadius(4) > 0
//        );
    }

    private double enemiesRadius() {
        if (unit.isDragoon()) return 4.1
            + (enemy.isFacing(unit) ? 0.4 : -1)
            + (unit.hp() <= 40 ? 0.6 : 0);

        return 4;
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
//            unit.paintCircleFilled(18, Color.Teal);
            return usedManager(this);
        }

        System.err.println("@@@@@@@@@@@@ " + A.now() + " - " + unit.id() + " - DANCE AWAY ERROR " + enemy);
        return null;
    }

    private boolean danceAwayFromTarget(String logString) {
        return unit.moveAwayFrom(enemy.position(), enemiesRadius(), Actions.MOVE_DANCE_AWAY, logString);
    }
}
