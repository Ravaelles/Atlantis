package atlantis.combat.micro.avoid.dont.protoss.dragoon;

import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.ContinueAttack;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;

public class DragoonDontAvoid {
    public static boolean dontAvoid(AUnit unit) {
        if (!unit.isDragoon()) return false;

//        if (true) return false;

//        System.err.println(A.now() + " - " + unit.typeWithUnitId() + " - ");

//        if (unit.hp() <= 20) return false;

        if (!unit.shotSecondsAgo(4) && unit.isActiveManager(ContinueAttack.class)) return true;
        if (unit.lastActionLessThanAgo(20, Actions.ATTACK_UNIT)) return true;

        if (
            unit.isAttacking()
                && unit.isActiveManager(ContinueAttack.class)
                && unit.hasValidTarget()
                && unit.isTargetInWeaponRangeAccordingToGame()
                && (!unit.shotSecondsAgo(4) || unit.distTo(unit.target()) >= OurDragoonRange.range() - 1)
        ) return true;

//        if (justStartedAttack(unit)) return true;
        if (preventAttackAvoidAttackAvoidLoop(unit)) return true;
        if (healthyAndSafe(unit)) return true;
        if (healthyAndGoodEval(unit)) return true;
        if (hasJustStoppedRunning(unit)) return true;
        if (hasNotShotInAWhile(unit)) return true;
        if (preventRunningAndNotShooting(unit)) return true;

//        if (true) return true;

//        Decision decision;
////        if ((decision = whenMissionSparta(unit)).notIndifferent()) return decision.toBoolean();
//        if ((decision = whenMissionDefend(unit)).notIndifferent()) return decision.toBoolean();

        if (dontAvoidCombatBuildings(unit)) return true;

        if (Enemy.zerg()) return DragoonDontAvoidVsZerg.vsZerg(unit);
        if (Enemy.protoss()) return DragoonDontAvoidVsProtoss.vsProtoss(unit);
        if (Enemy.terran()) return vsTerran(unit);

        return false;
    }

    private static boolean healthyAndGoodEval(AUnit unit) {
        if (unit.woundHp() >= 5) return false;

//        System.out.println(unit.eval());
        return unit.eval() >= 0.8
            && unit.enemiesNearInRadius(3.4) == 0
            && dontAvoid(unit, "HealthyAndGoodEval");
    }

//    private static boolean justStartedAttack(AUnit unit) {
//        return unit.lastActionLessThanAgo(4, Actions.ATTACK_UNIT)
//            && reason(unit, "JustStartedAttack");
//    }

    private static boolean preventRunningAndNotShooting(AUnit unit) {
        if (unit.lastAttackFrameAgo() < unit.lastStartedRunningAgo()) return false;
        if (unit.hpPercent() <= 30) return false;
        if (unit.enemiesNear().inRadius(3.5, unit).notEmpty()) return false;

        return dontAvoid(unit, "PreventRunningNotShooting");
    }

    private static boolean hasJustStoppedRunning(AUnit unit) {
        return unit.lastStoppedRunningLessThanAgo(10)
            && dontAvoid(unit, "JustStoppedRunning(" + unit.lastStoppedRunningAgo() + ")");
//            && A.println("hasJustStoppedRunning (" + unit.lastStoppedRunningAgo() + ")");
    }

//    private static Decision whenMissionSparta(AUnit unit) {
//        if (!unit.isMissionSparta()) return Decision.INDIFFERENT;
//
//        if (unit.woundHp() <= 9) return Decision.TRUE;
////        if (unit.isSafeFromMelee()) return Decision.TRUE;
//
//        return unit.isSafeFromMelee() ? Decision.TRUE : Decision.FALSE;

    /// /        return Decision.INDIFFERENT;
//    }
    private static boolean healthyAndSafe(AUnit unit) {
        return unit.woundHp() <= 9
//            && unit.lastAttackFrameMoreThanAgo(30 * 5)
            && unit.cooldown() <= 13
            && unit.meleeEnemiesNearCount(1.7) <= (Enemy.zerg() ? 3 : 1)
            && dontAvoid(unit, "HealthyAndSafe");
//            && unit.enemiesNear().ranged().canAttack(unit, 1.5).empty();
    }

    private static Decision whenMissionDefend(AUnit unit) {
        if (!unit.isMissionDefend()) return Decision.INDIFFERENT;

//        if (unit.shieldDamageAtMost(9)) {
//            reason(unit, "MD:LowShieldDmg");
//            return Decision.TRUE;
//        }

        if (unit.enemiesNear().countInRadius(3.1, unit) >= 2) return Decision.FALSE;
        if (unit.hp() <= 40 && unit.enemiesNear().countInRadius(2.7, unit) >= 1) return Decision.FALSE;

//        if (unit.isRanged()) {
//            if (unit.cooldown() >= 10) return Decision.FALSE;
//            if (unit.shieldDamageAtMost(39) && unit.lastAttackFrameMoreThanAgo(30 * 6)) return Decision.TRUE;
//            return unit.isSafeFromMelee() ? Decision.TRUE : Decision.FALSE;
//        }

        return Decision.INDIFFERENT;
    }

    private static boolean preventAttackAvoidAttackAvoidLoop(AUnit unit) {
        if (!unit.isRanged()) return false;
        if (unit.hp() <= 46) return false;

        if (
            unit.action().isAttacking()
                && unit.hp() >= 35
                && unit.lastAttackFrameAgo() > 10
                && unit.lastActionLessThanAgo(20)
        ) {
            return dontAvoid(unit, "PreventAvoidLoop");
        }

        return false;
    }

    private static boolean hasNotShotInAWhile(AUnit unit) {
        if (!unit.isRanged()) return false;
        if (unit.hp() <= A.whenEnemyProtoss(42, 32)) return false;

        if (unit.lastAttackFrameMoreThanAgo(30 * 7)) return dontAvoid(unit, "NotShotA");

        if (
            !unit.isRetreating()
                && unit.lastUnderAttackLessThanAgo(30)
                && unit.lastAttackFrameMoreThanAgo(30 * 3)
                && unit.meleeEnemiesNearCount(2.7) > 0
                && unit.nearestChokeCenterDist() >= 5
        ) {
//            unit.paintCircleFilled(14, Color.Green);
            return dontAvoid(unit, "NotShotB");
        }

        return false;

//        if (unit.hp() <= 35) return false;
//        if (unit.cooldown() >= 7) return false;

//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - " + unit.hp());

//        Selection enemies = unit.enemiesNear().melee().effUndetected().groundUnits();
//        return enemies.canAttack(unit, 1.5).empty();
    }

    private static boolean dontAvoidCombatBuildings(AUnit unit) {
        Selection combatBuildings = unit.enemiesNear().combatBuildingsAntiLand().inRadius(10, unit);
        if (combatBuildings.empty()) return false;

        if (unit.isRanged() && unit.hp() <= 43) return false;

        return unit.enemiesNear().combatUnits().nonBuildings().inRadius(1.8, unit).empty()
            && unit.addLog("GoonFightCB")
            && dontAvoid(unit, "DontAvoidCB");
    }

    private static boolean vsTerran(AUnit unit) {
        if (unit.isHealthy()) return true;

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
        if (meleeEnemiesNearCount > 0) {
            return whenMeleeNear(unit);
        }

//        if (unit.enemiesThatCanAttackMe(1.2).onlyOfType(AUnitType.Terran_Vulture)) {
//            return unit.hp() >= 100 && unit.cooldown() <= 7 && reason(unit, "OnlyVultures");
//        }

        return false;
    }

    protected static boolean whenMeleeNear(AUnit unit) {
        return (unit.woundHp() <= 9 || unit.cooldown() <= 4)
            && unit.shields() >= 40
            && dontAvoid(unit, "GoonMeleeNear");
    }

    protected static boolean dontAvoid(AUnit unit, String reason) {
        unit.setTooltip(reason);

        return true;
    }
}
