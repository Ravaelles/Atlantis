package atlantis.combat.micro.avoid.dont.protoss.dragoon;

import atlantis.combat.retreating.protoss.ProtossTooBigBattleToRetreat;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;
import bwapi.Color;

public class DragoonDontAvoidEnemy {
    public static boolean dontAvoid(AUnit unit) {
        if (!unit.isDragoon()) return false;

//        if (true) return false;

//        System.err.println(A.now() + " - " + unit.typeWithUnitId() + " - ");

        if (justStartedAttack(unit)) return true;
        if (preventAttackAvoidAttackAvoidLoop(unit)) return true;
        if (seriouslyWoundedAndEnemiesNear(unit)) return true;
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

        if (Enemy.zerg()) return vsZerg(unit);
        if (Enemy.protoss()) return DragoonDontAvoidVsProtoss.vsProtoss(unit);
        if (Enemy.terran()) return vsTerran(unit);

        return false;
    }

    private static boolean healthyAndGoodEval(AUnit unit) {
        if (unit.woundHp() >= 5) return false;

//        System.out.println(unit.eval());
        return unit.eval() >= 0.8
            && unit.enemiesNearInRadius(3.4) == 0
            && reason(unit, "HealthyAndGoodEval");
    }

    private static boolean justStartedAttack(AUnit unit) {
        return unit.lastActionLessThanAgo(4, Actions.ATTACK_UNIT)
            && reason(unit, "JustStartedAttack");
    }

    private static boolean seriouslyWoundedAndEnemiesNear(AUnit unit) {
        if (!unit.isMissionDefend()) return false;
        if (unit.hp() >= 45) return false;

        return unit.meleeEnemiesNearCount(3.85) > 0
            && reason(unit, "SeriouslyWoundedAndEnemiesNear");
    }

    private static boolean preventRunningAndNotShooting(AUnit unit) {
        if (unit.lastAttackFrameAgo() < unit.lastStartedRunningAgo()) return false;
        if (unit.hpPercent() <= 30) return false;
        if (unit.enemiesNear().inRadius(3.5, unit).notEmpty()) return false;

        return reason(unit, "PreventRunningNotShooting");
    }

    private static boolean hasJustStoppedRunning(AUnit unit) {
        return unit.lastStoppedRunningLessThanAgo(10)
            && reason(unit, "JustStoppedRunning(" + unit.lastStoppedRunningAgo() + ")");
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
            && reason(unit, "HealthyAndSafe");
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

        if (
            unit.action().isAttacking()
                && unit.lastAttackFrameAgo() > 10
                && unit.lastActionLessThanAgo(20)
        ) {
//            System.err.println("PreventAvoidLoop: @" + A.now + " / " + unit.cooldown());
            return reason(unit, "PreventAvoidLoop");
        }

        return false;
    }

    private static boolean hasNotShotInAWhile(AUnit unit) {
        if (!unit.isRanged()) return false;

        if (unit.lastAttackFrameMoreThanAgo(30 * 7)) return reason(unit, "NotShotA");

        if (
            !unit.isRetreating()
                && unit.lastUnderAttackLessThanAgo(30)
                && unit.lastAttackFrameMoreThanAgo(30 * 3)
                && unit.meleeEnemiesNearCount(3.0) > 0
        ) {
//            unit.paintCircleFilled(14, Color.Green);
            return reason(unit, "NotShotB");
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
            && reason(unit, "DontAvoidCB");
    }

    private static boolean dontAvoidWhenOnlyEnemyZerglingsNearby(AUnit unit) {
        if (!Enemy.zerg()) return false;
        if (unit.hp() <= 25) return false;

        Selection enemiesNear = unit.enemiesNear().havingAntiGroundWeapon();

        if (!unit.isHealthy() && enemiesNear.inRadius(1.8, unit).atLeast(3)) return false;
        if (unit.enemiesNear().ranged().inRadius(7.8, unit).empty()) return reason(unit, "OnlyLingsA");

        return unit.shields() >= 5 && unit.cooldown() <= 8
            && reason(unit, "OnlyLingsB");
    }

    private static boolean vsTerran(AUnit unit) {
        if (unit.isHealthy()) return true;
        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
        if (meleeEnemiesNearCount > 0) {
            return whenMeleeNear(unit);
        }

        if (unit.enemiesThatCanAttackMe(1.2).onlyOfType(AUnitType.Terran_Vulture)) {
            return unit.hp() >= 40 && unit.cooldown() <= 7 && reason(unit, "OnlyVultures");
        }

        return false;
    }

    private static boolean vsZerg(AUnit unit) {
        if (dontAvoidZerglingsWhenManyDragoonsNear(unit)) return true;
        if (dontAvoidWhenOnlyEnemyZerglingsNearby(unit)) return true;

        if (unit.isHealthy()) return true;
//        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        if (unit.cooldown() <= 7) {
            int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
            if (meleeEnemiesNearCount > 0) {
                return whenMeleeNear(unit);
            }
        }

        return unit.lastAttackFrameAgo() > 30 * 3
            && reason(unit, "VsZerg");
    }

    private static boolean dontAvoidZerglingsWhenManyDragoonsNear(AUnit unit) {
//        if (!unit.isTarget(AUnitType.Zerg_Zergling)) return false;
//        AUnit ling = unit.target();
        if (unit.hp() <= 30) return false;
        if (unit.cooldown() >= 8) return false;

        AUnit ling = unit.enemiesNear().zerglings().inRadius(OurDragoonRange.range(), unit).nearestTo(unit);
        if (ling == null) return false;

        ling.paintTextCentered("_" + ling.enemiesNear().dragoons().countInRadius(7, ling), Color.Red);

        if (ling.enemiesNear().dragoons().countInRadius(7, ling) >= 2) {
//            System.err.println(A.minSec() + " - " + unit.typeWithUnitId() + " - \"GoonCo-op\"");
            return reason(unit, "GoonCo-op");
        }

        return false;
    }

    private static boolean whenMeleeNear(AUnit unit) {
        return (unit.woundHp() <= 9 || unit.cooldown() <= 4)
            && unit.shields() >= 40
            && reason(unit, "GoonMeleeNear");
    }

    protected static boolean reason(AUnit unit, String reason) {
        return unit.setTooltip(reason);
    }
}
