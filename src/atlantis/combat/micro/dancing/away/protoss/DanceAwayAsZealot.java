package atlantis.combat.micro.dancing.away.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;
import bwapi.Color;

public class DanceAwayAsZealot extends Manager {
    private AUnit enemy;

    public DanceAwayAsZealot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!unit.isZealot()) return false;
        if (unit.isRunningOrRetreating()) return false;
        if (unit.isMissionSparta() && unit.nearestChokeDist() <= 1) return false;
        if (!unit.shotSecondsAgo(1)) return false;

        enemy = nearestMeleeEnemy();
        if (enemy == null) return false;

        if (noCloseMeleeFacingThisUnit()) return false;
        if (dontApplyWhenAttackingRangedEnemy()) return false;

        if (appliesVsProtoss()) return true;
        if (appliesVsZerg()) return true;

        return false;

//        int cooldownThresholdToApply = cooldownThresholdToApply();
//        if (unit.cooldown() < cooldownThresholdToApply) return false;
//        if (unit.hp() >= 21 && unit.lastUnderAttackMoreThanAgo(15)) return false;
//
//        if (
//            unit.hp() <= 35
//                && unit.lastUnderAttackLessThanAgo(40)
//                && unit.lastAttackFrameLessThanAgo(30)
//        ) return true;
//
//        if (unit.cooldown() >= cooldownThresholdToApply) return true;
//        if (unit.cooldown() >= 4 && unit.hp() <= 60) return true;
//        if (unit.moreMeleeEnemiesThanOurUnits()) return true;
//
//        if (unit.hp() >= 35) return false;
//
//        boolean fairlyWounded = unit.hp() <= 38;
//
//        // @ToDo Tweak these values
//        return unit.cooldown() >= (fairlyWounded ? 4 : 16)
//            && (fairlyWounded || unit.lastUnderAttackLessThanAgo(60));
    }

    private boolean noCloseMeleeFacingThisUnit() {
        for (AUnit meleeEnemy : unit.meleeEnemiesNear().inRadius(2, unit).list()) {
            if (unit.isOtherUnitFacingThisUnit(meleeEnemy)) return false;
        }

        return true;
    }

    private boolean appliesVsProtoss() {
        if (!Enemy.protoss()) return false;
        if (unit.shieldHealthy()) return false;

        if (unit.hp() <= 17 && unit.eval() <= 3) return true;

        if (unit.cooldown() >= 17 && unit.meleeEnemiesNearCount(1.3) >= 2) {
            return true;
        }

        return false;
    }

    private boolean appliesVsZerg() {
        if (!Enemy.zerg()) return false;

        if (unit.cooldown() >= 12) return true;
        if (unit.cooldown() >= 4 && unit.hp() <= 40) return true;

        if (unit.shields() >= 2 && unit.meleeEnemiesNearCount(1.5) <= 1) return false;

//            System.out.println("meleeEN = " + unit.meleeEnemiesNearCount(1.3));
//        if (
//            unit.shieldWounded() && unit.cooldown() >= 17
//        ) return true;
//            if (unit.shields() <= 10 && unit.cooldown() >= 5) return true;
//            if (unit.cooldown() >= 9 && unit.eval() <= 3) return true;
//
//            if (unit.shieldWound() <= 4 && unit.lastUnderAttackMoreThanAgo(20)) return false;
//            if (unit.hp() >= 70 && unit.lastUnderAttackMoreThanAgo(45)) return false;

        return false;
    }

    @Override
    protected Manager handle() {
        unit.paintCircleFilled(7, Color.Yellow);

        if (danceAwayFrom(enemy)) {
//            System.err.println(A.now() + " - " + unit.typeWithUnitId() + " - Dancing away ");
            return usedManager(this);
        }

        return null;
    }

    // =========================================================

    private int cooldownThresholdToApply() {
//        if (Enemy.zerg()) return 3;
        if (Enemy.protoss()) return unit.hp() <= 34 ? 1 : 7;

        return 3;
    }

    private boolean dontApplyWhenAttackingRangedEnemy() {
        return unit.isAttacking()
            && unit.hasValidTarget()
            && unit.target().isRanged();
    }

    private boolean dontApplyWhenOnlyRangedNear() {
        return unit.enemiesNear().inRadius(3.5, unit).onlyRanged();
    }

    private boolean dontApplyWhenRangedEnemiesNear() {
        Selection rangedEnemies = unit.enemiesNear().ranged();

        if (rangedEnemies.empty()) return false;

        return unit.hp() >= 32 || rangedEnemies.canAttack(unit, 1.5).notEmpty();
    }

    private int minMeleeEnemiesNear() {
        if (unit.hp() < 80) return 1;

        return 2;
    }

    private boolean danceAwayFrom(AUnit enemy) {
        return unit.runningManager().runFrom(
            enemy.position(), 1, Actions.MOVE_DANCE_AWAY, false
        );
    }

    private AUnit nearestMeleeEnemy() {
        return unit.meleeEnemiesNear().nearestTo(unit);
    }
}
