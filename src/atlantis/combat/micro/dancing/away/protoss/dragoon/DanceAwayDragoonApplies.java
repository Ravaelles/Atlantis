package atlantis.combat.micro.dancing.away.protoss.dragoon;

import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.protoss.ProtossFlags;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;

public class DanceAwayDragoonApplies extends HasUnit {
    private int rangedEnemiesCount;
    private String _lastF = "";
    private String _lastT = "";

    public DanceAwayDragoonApplies(AUnit unit) {
        super(unit);
    }

    public boolean applies() {
//        if (true) return false;

        if (!unit.isDragoon()) return false;
//        if (A.now % unit.cooldownAbsolute() <= 15) return false;
//        System.err.println("unit.cooldown() = " + unit.cooldown() + " / " + unit.isRunningOrRetreating());
//        if (unit.isRunning()) {
//            System.out.println(A.now() + ": unit.isRunning");
//        }
//        unit.paintTextCentered(unit.enemiesNear().tanks().countInRadius(13, unit) + "", Color.Green, 2);

        int cooldown = unit.cooldown();

//        if (unit.isHealthy()) return f("_healthy");
        if (unit.lastAttackFrameMoreThanAgo(A.whenEnemyZerg(90, 60))) return f("_A");
        if (unit.isRunning()) return f("_R1");
        if (unit.isRetreating()) return f("_R2");
        if (unit.isActiveManager(ForceStopDancingDragoon.class)) return f("_FStop");
        if (unit.lastTarget() != null && unit.lastTarget().isCombatBuilding()) return f("_B5");

        if (Enemy.protoss()) {
            if (cooldown <= 18 && unit.meleeEnemiesNearCount(3) == 0) {
                if (unit.hp() >= 150) return f("_GvG_A");
                if (cooldown <= 16 && unit.hp() >= 100) return f("_GvG_B");
                if (cooldown <= 14 && unit.hp() >= 80) return f("_GvG_C");

                if (cooldown >= 6 && unit.hp() <= 22) return t("GvG_D");
            }
        }

        if (Enemy.zerg()) {
            if (cooldown <= 14 && unit.meleeEnemiesNearCount(3.0) == 0) {
                double shieldWound = unit.shieldWound();
                if (shieldWound <= 9) return f("_GvG_A");
                if (cooldown <= 12 && shieldWound <= 19) return f("_GvG_B");

                if (unit.meleeEnemiesNearCount(1.8) >= 1) return f("_DontWhenMeleeNear");
            }
        }

        if (cooldown >= 11) {
            Selection melees = unit.enemiesNear()
                    .combatUnits()
                    .inRadius(OurDragoonRange.range() - unit.hpPercent() / 70.0, unit);
            if (melees.notEmpty() && melees.notShowingBackToUs(unit).notEmpty()) {
                return t("Melee");
            }
        }

        if (unit.shieldHealthy() && cooldown <= 15) return f("_BZ");

        if (
            cooldown <= 15 && unit.shieldWound() <= A.whenEnemyZerg(3, 14)
        ) return f("_C");

        if (
            unit.shieldWound() <= A.whenEnemyProtoss(19, 9)
                && unit.meleeEnemiesNearCount(3.3) == 0
                && (!Enemy.zerg() || unit.enemiesThatCanAttackMe(0.5).empty())
        ) return f("_B6");

        if (Enemy.terran()) {
            if (AUnitType.Terran_Siege_Tank_Siege_Mode.equals(unit._lastTargetType)) return f("_P");
            if (unit.enemiesNear().tanksSieged().countInRadius(12.5, unit) > 0) return f("_Q");
            if (unit.enemiesNear().tanks().countInRadius(7.5, unit) > 0) return f("_Ta");

//            System.out.println(unit.enemiesNear().tanksSieged().countInRadius(12.5, unit)
//                + " / " + unit.enemiesNear().tanks().countInRadius(7.5, unit));
        }

        if (Enemy.zerg()) {
            if (cooldown <= 14 && unit.shieldWound() <= 18) return f("_BraveA_vZ");
            if (yesVsZerg()) return t("YvZ");
            if (cooldown <= 12 && unit.shieldWound() <= 35) return f("_BraveB_vZ");
            if (noVsZerg()) return t("_NvZ");
        }

        if (Enemy.protoss()) {
//            if (cooldown >= 14 && unit.hp() >= 80) return f("_GvG");
            if (cooldown >= 18 && unit.shieldWound() <= 3) return f("_D");
            if (unit.hp() >= 45 && unit.cooldown() <= 12 && unit.enemiesThatCanAttackMe(0.6).empty()) return true;
            if (unit.shieldWound() <= 6 && unit.friendsNear().countInRadius(3, unit) >= 1) return f("_D2");

            if (cooldown >= 7) {
                if (
                    unit.hp() <= 100
                    || unit.enemiesThatCanAttackMe(0.2).notEmpty()
                ) {
                    return t("B");
    //                if (unit.cooldown() >= 10 && unit.shields() <= 10) return t("C");
                }
            }

//            if ((decision = DanceAwayDragoonVsDragoon.check(unit, enemy)).notIndifferent()) return decision.toBoolean();
            if (
                unit.enemiesNear().melee().notEmpty() && unit.meleeEnemiesNearCount(3.2) == 0
            ) return f("_E");
        }

        if (unit.hp() >= 41 && !unit.shotSecondsAgo(2)) return f("_F");
//        if (
//            unit.meleeEnemiesNearCount(3) > 0
//                && unit.enemiesThatCanAttackMe(0.5 + unit.woundPercent() / 40.0).empty()
//        ) return f("_G");
        if (
            unit.shieldHealthy()
                && unit.meleeEnemiesNearCount(3.3) == 0
                && unit.enemiesNear().ranged().empty()
        ) return f("_H");

        if (noEnemiesThatCanAttackUsAndNoCooldown()) {
//            System.err.println(A.minSec() + " - " + unit.typeWithUnitId() + " - " + unit.hp() + " - SAFE?");
            return f("_J");
        }

        if (
            unit.shieldWound() <= 4
                && cooldown <= 19
                && unit.meleeEnemiesNearCount(2.8) == 0
                && unit.friendsInRadiusCount(3) > 0
        ) return f("_K");

//        if (
//            unit.hp() >= 60
////                && unit.cooldown() <= 19
//                && unit.meleeEnemiesNearCount(OurDragoonRange.range() - (unit.shieldHealthy() ? 1.2 : 0.5)) == 0
//                && unit.enemiesNear().ranged().countInRadius(7, unit) == 0
//        ) return f("_LMR");

        if (dontDanceAwayVsEnemyGoonEarly()) return f("_M");

        if (cooldown >= 15) return t("D1");
        if (cooldown >= 6 && unit.hp() <= 102) return t("D2");
        if (cooldown >= 4 && unit.hp() <= 82) return t("D3");

        rangedEnemiesCount = unit.rangedEnemiesCount(0.35);

//        if (rangedEnemiesCount >= 2 && unit.cooldown() >= 10) return t("");
//        if (rangedEnemiesCount >= 2 && unit.shieldWound() >= 18 && unit.cooldown() >= 6) return t("");

        if (rangedEnemiesCount >= 3 && (cooldown > 0 || unit.shieldWound() >= 20)) return t("E");

        double shotSecondsAgo = unit.shotSecondsAgo();

        if (rangedEnemiesCount >= 2) {
            if (shotSecondsAgo <= 3 && unit.hp() <= 100) return t("E1");
            if (cooldown >= 9 && unit.shieldWound() >= 20) return t("E2");
            if (unit.shieldWound() >= 40 && cooldown >= 6) return t("E3");
            if (unit.hp() <= 82 && cooldown >= 3) return t("E4");
            if (unit.hp() <= 60 && shotSecondsAgo <= 4) return t("E5");
            if (unit.hp() <= 44 && shotSecondsAgo <= 5) return t("E6");
        }

        if (rangedEnemiesCount >= 1) {
            if (unit.hp() <= 50 && shotSecondsAgo <= 2.5) return t("F1");
            if (unit.hp() <= 24 && shotSecondsAgo <= 3.5) return t("F2");
        }

        Decision decision;

        if (Enemy.zerg()) {
            if ((decision = vsEnemyHydra()).notIndifferent()) return decision.toBoolean();
//            if ((decision = vsEnemyZergling()).notIndifferent()) return decision;
        }

//        if (cooldown <= (rangedEnemiesCount > 0 ? 11 : 8)) return f("_N");

//        if (forbidDanceAwayWhenRangedNear()) return f("");

//        System.err.println("SIEGED = " + unit.enemiesNear().tanksSieged().countInRadius(6, unit));

        if (unit.enemiesNear().ranged().canAttack(unit, 0.6).atLeast(1)) return t("G");

//        if (unit.nearestEnemyDist() >= (unit.hp() >= 60 ? 3.1 : OurDragoonRange.range() - 0.5)) return f("_R");
        if (unit.nearestEnemyDist() >= OurDragoonRange.range() - 0.4) return f("_R");

        if (unit.attackState().finishedShooting()) return t("finishedShooting");
//        if (unit.lastUnderAttackLessThanAgo(10)) {
//            System.err.println("YUP");
//            return t("");
//        }

//        if (unit.nearestEnemyDist() >= OurDragoonRange.range() - 0.5) return f("");

        if (unit.lastAttackFrameMoreThanAgo(25)) return f("_S");
        if (unit.shieldWound() <= 10 && unit.meleeEnemiesNearCount(2.6) == 0) return f("_T");

//        if (unit.nearestEnemyDist() <= 3.0) return t("");

        if (unit.shieldHealthy()) return f("_U");
        if (unit.lastAttackFrameMoreThanAgo(30 * 2)) return f("_V");
        if (cooldown <= (unit.shields() <= 30 ? 3 : 12)) return f("_X");

//        if (!Enemy.zerg() && EnemyInfo.hasRanged()) {
//            if (true) return f("_Y");
//        }

//        if (unit.woundHp() <= 14 && unit.lastAttackFrameMoreThanAgo(30 * 5)) return f("");
        if ((cooldown >= 12 || unit.hp() <= 100) && !unit.isSafeFromMelee()) return t("");

        if (tooHealthy()) return f("_tooHealthy");
        if (provideSupportForMelee()) return f("_supportForMelee");

        if (unit.enemiesNear().inRadius(8, unit).notEmpty()) {
            if (dragoonLowHpAndStillUnderAttack()) return t("dragoonLowHp");
        }

        if (quiteHealthyAndNotUnderAttack()) return f("_quiteHealthy");

        return f("_GenericNo");
    }

    private boolean noVsZerg() {
        if (unit.cooldown() <= 17 && unit.shieldWound() <= 35) return true;

        return false;
    }

    private boolean t(String reason) {
//        if (!reason.equals(_lastT)) {
//            System.out.println("@" + A.now + ":  " + unit.idWithHash() + " - DanceAway: " + reason);
//        }
//        _lastT = reason;

        // =====

        Selection enemies = unit.enemiesNear().canAttack(unit, 2.2);

        if (enemies.ranged().notShowingBackToUs(unit).notEmpty()) return true;
        if (enemies.melee().facing(unit).empty()) return false;

        return true;
    }

    private boolean f(String reason) {
//        if (!reason.equals(_lastF)) {
//            System.err.println("@" + A.now + ":  " + unit.idWithHash() + " - NO DANCE: " + reason);
//        }
//        _lastF = reason;

        return false;
    }


    private boolean yesVsZerg() {
        Selection enemiesThatCanAttackMe = unit.enemiesThatCanAttackMe(3);

        if (enemiesThatCanAttackMe.onlyMelee()) {
            double minDist = OurDragoonRange.range() - 0.6 - (unit.shields() / 80.0);
            if (enemiesThatCanAttackMe.nearestToDist(unit) > minDist) {
                return false;
            }
        }

        if (
            unit.cooldown() >= 10
                && unit.enemiesNear().groundUnits().countInRadius(5.2, unit) > 0
        ) return true;

        return false;
    }

    private boolean dontDanceAwayVsEnemyGoonEarly() {
        if (!Enemy.protoss()) return false;

        return unit.shieldWound() <= 15
            && unit.cooldown() <= 19
            && unit.eval() >= 2
            && unit.meleeEnemiesNearCount(OurDragoonRange.range() - 0.25) == 0;
    }

    private boolean noEnemiesThatCanAttackUsAndNoCooldown() {
        return unit.cooldown() <= 6
            && unit.hp() >= 100
            && unit.meleeEnemiesNearCount(OurDragoonRange.range() - 0.9) == 0
            && unit.enemiesNear().canAttack(unit, A.whenEnemyProtoss(0.3, 0.8)).empty();
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

    protected static double meleeEnemiesRadius(AUnit unit) {
        AUnit enemy = unit.nearestEnemy();

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
                || unit.enemiesNearInRadius(meleeEnemiesRadius(unit)) > 0
        );
    }
}
