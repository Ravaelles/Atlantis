package atlantis.combat.micro.dancing.away.protoss;

import atlantis.combat.micro.dancing.away.DanceAway;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.protoss.ProtossFlags;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

public class DanceAwayAsDragoon extends DanceAway {
    private int rangedEnemiesCount;

    public DanceAwayAsDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isDragoon()) return false;
        if (unit.isRunning() || unit.isRetreating()) return false;
        if (unit.shieldWound() <= 3) return false;
        if (Enemy.zerg() && unit.hp() >= 60) return false;

//        System.err.println(A.minSec() + " - " + unit + " - DanceAwayAsDragoon");

//        if (unit.cooldown() >= 4) return true;

        if (noEnemiesThatCanAttackUsAndNoCooldown()) {
//            System.err.println(A.minSec() + " - " + unit.typeWithUnitId() + " - " + unit.hp() + " - SAFE?");
            return false;
        }

        if (
            unit.shieldWound() <= 4
                && unit.cooldown() <= 19
                && unit.meleeEnemiesNearCount(2.8) == 0
                && unit.friendsInRadiusCount(3) > 0
        ) return false;

//        if (dontDanceAwayVsEnemyGoonEarly()) return false;

        if (unit.cooldown() >= 15) return true;
        if (unit.cooldown() >= 6 && unit.hp() <= 102) return true;
        if (unit.cooldown() >= 4 && unit.hp() <= 82) return true;

        rangedEnemiesCount = unit.rangedEnemiesCount(0.35);

//        if (rangedEnemiesCount >= 2 && unit.cooldown() >= 10) return true;
//        if (rangedEnemiesCount >= 2 && unit.shieldWound() >= 18 && unit.cooldown() >= 6) return true;

        if (rangedEnemiesCount >= 3 && (unit.cooldown() > 0 || unit.shieldWound() >= 20)) return true;

        double shotSecondsAgo = unit.shotSecondsAgo();

        if (rangedEnemiesCount >= 2) {
            if (shotSecondsAgo <= 3 && unit.hp() <= 100) return true;
            if (unit.cooldown() >= 9 && unit.shieldWound() >= 20) return true;
            if (unit.shieldWound() >= 40 && unit.cooldown() >= 6) return true;
            if (unit.hp() <= 82 && unit.cooldown() >= 3) return true;
            if (unit.hp() <= 60 && shotSecondsAgo <= 4) return true;
            if (unit.hp() <= 44 && shotSecondsAgo <= 5) return true;
        }

        if (rangedEnemiesCount >= 1) {
            if (unit.hp() <= 50 && shotSecondsAgo <= 2.5) return true;
            if (unit.hp() <= 24 && shotSecondsAgo <= 3.5) return true;
        }

        if (Enemy.zerg()) {
            if ((decision = vsEnemyHydra()).notIndifferent()) return decision.toBoolean();
//            if ((decision = vsEnemyZergling()).notIndifferent()) return decision;
        }

        if (unit.cooldown() <= (rangedEnemiesCount > 0 ? 11 : 8)) {
//            if (unit.isMoving() && !unit.isRunning()) {
//                if (unit.lastCommandIssuedAgo() >= 2) unit.holdPosition("HoldAfterDance");
////                unit.paintCircleFilled(6, Color.Blue);
////                PauseAndCenter.on(unit);
//            }
            return false;
        }

//        if (forbidDanceAwayWhenRangedNear()) return false;

//        System.err.println("SIEGED = " + unit.enemiesNear().tanksSieged().countInRadius(6, unit));
        if (Enemy.terran()) {
            if (AUnitType.Terran_Siege_Tank_Siege_Mode.equals(unit._lastTargetType)) return false;
            if (unit.enemiesNear().tanksSieged().countInRadius(8, unit) > 0) return false;
        }

        if (unit.enemiesNear().ranged().canAttack(unit, 0.6).atLeast(1)) return true;

        if (unit.nearestEnemyDist() >= (unit.hp() >= 60 ? 3.1 : OurDragoonRange.range() - 0.5)) return false;

        if (unit.attackState().finishedShooting()) return true;
//        if (unit.lastUnderAttackLessThanAgo(10)) {
//            System.err.println("YUP");
//            return true;
//        }

//        if (unit.nearestEnemyDist() >= OurDragoonRange.range() - 0.5) return false;

        if (unit.lastAttackFrameMoreThanAgo(25)) return false;
        if (unit.shieldWound() <= 10 && unit.meleeEnemiesNearCount(2.6) == 0) return false;

//        if (unit.nearestEnemyDist() <= 3.0) return true;

        if (unit.shieldHealthy()) return false;
        if (unit.lastAttackFrameMoreThanAgo(30 * 2)) return false;
        if (unit.cooldown() <= (unit.shields() <= 30 ? 3 : 12)) return false;

        if (Enemy.protoss()) {
            if ((decision = vsEnemyDragoons()).notIndifferent()) return decision.toBoolean();
        }

        if (!Enemy.zerg() && EnemyInfo.hasRanged()) {
            if (true) return false;
        }

//        if (unit.woundHp() <= 14 && unit.lastAttackFrameMoreThanAgo(30 * 5)) return false;
        if ((unit.cooldown() >= 12 || unit.hp() <= 100) && !unit.isSafeFromMelee()) return true;

        if (tooHealthy()) return false;
        if (provideSupportForMelee()) return false;

        if (unit.enemiesNear().inRadius(8, unit).notEmpty()) {
            if (dragoonLowHpAndStillUnderAttack()) return true;
        }

        if (quiteHealthyAndNotUnderAttack()) return false;

        return false;
    }

    private boolean dontDanceAwayVsEnemyGoonEarly() {
        if (!Enemy.protoss()) return false;

        return unit.shieldWound() <= 35
            && unit.cooldown() <= 19
            && unit.eval() >= 2
            && unit.meleeEnemiesNearCount(OurDragoonRange.range() - 0.25) == 0;
    }

    private boolean noEnemiesThatCanAttackUsAndNoCooldown() {
        return unit.cooldown() <= 6
            && unit.hp() >= 100
            && unit.enemiesNear().canAttack(unit, 0.8).empty();
    }

    private boolean forbidDanceAwayWhenRangedNear() {
        if (rangedEnemiesCount == 0) return false;
        if (unit.cooldown() <= 10 && unit.hp() <= 82) return false;

        if (unit.cooldown() <= 15) return true;

        // 8 enemies = 90%, 3 enemies = 30%
//        int shieldWoundPercentThresholdAgainstRanged = Math.min(95, 10 * rangedEnemiesCount);
        int shieldWoundPercentThresholdAgainstRanged = 5;

        return unit.shieldWoundPercent() >= shieldWoundPercentThresholdAgainstRanged;
    }

    private Decision vsEnemyHydra() {
        int minCooldown = unit.hp() >= 80 ? 10 : 3;
        if (unit.cooldown() <= minCooldown) return Decision.FALSE;

        Selection hydras = unit.enemiesNear().hydras();
        if (hydras.empty()) return Decision.INDIFFERENT;

        double range = OurDragoonRange.range() - 0.08;

        return hydras.countInRadius(range, unit) > 0
            ? Decision.TRUE
            : Decision.INDIFFERENT;
    }

    private boolean provideSupportForMelee() {
        return unit.hp() > 20
            && unit.friendsNear().combatUnits().melee().inRadius(7, unit).notEmpty();
    }

    private boolean quiteHealthyAndNotUnderAttack() {
        return unit.hp() >= 40
            && unit.lastUnderAttackMoreThanAgo(30 * 4);
    }

    private boolean tooHealthy() {
        if (unit.enemiesNear().inRadius(7, unit).onlyMelee()) return unit.shieldDamageAtMost(19);

        return unit.shields() >= 40;
    }

    private Decision vsEnemyDragoons() {
        if (unit.hp() >= 62 && unit.lastAttackFrameMoreThanAgo(100)) return Decision.FALSE;

        if (unit.enemiesNear().dragoons().canAttack(unit, 0).empty()) return Decision.INDIFFERENT;

        if (unit.hp() <= 82 && unit.cooldown() >= 10) return Decision.TRUE;

        return (unit.meleeEnemiesNearCount(meleeEnemiesRadius()) > 0)
            ? Decision.TRUE : Decision.FALSE;

//        if (unit.shields() >= 40) return false;
//
//        if (unit.enemiesNearInRadius(enemiesRadius()) > 0) return true;
//
//        return false;
    }

    private double meleeEnemiesRadius() {
        if (enemy == null) enemy = unit.nearestEnemy();

        return 1.4
            + (enemy == null || enemy.isFacing(unit) ? 0.4 : -1.6)
            + (unit.hp() <= 60 ? 0.7 : 0);
    }

    private boolean dragoonLowHpAndStillUnderAttack() {
        return unit.isDragoon()
            && !ProtossFlags.dragoonBeBrave()
            && unit.hp() <= 60
            && (
            unit.lastUnderAttackLessThanAgo(90)
                || unit.enemiesNearInRadius(meleeEnemiesRadius()) > 0
        );
    }
}
