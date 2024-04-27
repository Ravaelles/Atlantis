package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.combat.retreating.protoss.ProtossTooBigBattleToRetreat;
import atlantis.decions.Decision;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class DragoonDontAvoidEnemy {
    public static boolean dontAvoid(AUnit unit) {
        if (!unit.isDragoon()) return false;
//        if (unit.isIdle()) return false;
//        if (true) return false;

        Decision decision;

        if (healthyAndSafe(unit)) return true;
        if (hasNotShotInAWhile(unit)) return true;
        if ((decision = whenMissionSparta(unit)).notIndifferent()) return decision.toBoolean();
        if ((decision = whenMissionDefend(unit)).notIndifferent()) return decision.toBoolean();

        if (dontAvoidCombatBuildings(unit)) return true;

        if (Enemy.protoss()) return vsProtoss(unit);
        if (Enemy.terran()) return vsTerran(unit);
        if (Enemy.zerg()) return vsZerg(unit);

        return false;
    }

    private static Decision whenMissionSparta(AUnit unit) {
        if (!unit.isMissionSparta()) return Decision.INDIFFERENT;

        if (unit.woundHp() <= 9) return Decision.TRUE;
        if (unit.isSafeFromMelee()) return Decision.TRUE;

        return Decision.INDIFFERENT;
    }

    private static boolean healthyAndSafe(AUnit unit) {
        return unit.woundHp() <= 10
            && unit.enemiesNear().ranged().canAttack(unit, 1.5).empty();
    }

    private static Decision whenMissionDefend(AUnit unit) {
        if (!unit.isMissionDefend()) return Decision.INDIFFERENT;

        if (unit.isRanged()) {
            if (unit.cooldown() >= 15) return Decision.FALSE;

            if (
                unit.lastUnderAttackMoreThanAgo(40)
                    && unit.meleeEnemiesNearCount(1.5 + unit.woundPercent() / 38.0) == 0
            ) return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    private static boolean hasNotShotInAWhile(AUnit unit) {
        if (unit.hp() <= 35) return false;

        if (
            unit.cooldown() <= 4
                && unit.lastAttackFrameLessThanAgo(30 * 7)
        ) return true;

//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - " + unit.hp());

        return unit.enemiesNear().effUndetected().groundUnits().canAttack(unit, 1.5).empty();
    }

    private static boolean dontAvoidCombatBuildings(AUnit unit) {
        if (unit.hp() <= 30) return false;

        Selection combatBuildings = unit.enemiesNear().combatBuildingsAntiLand().inRadius(9, unit);
        if (combatBuildings.empty()) return false;

        return unit.enemiesNear().combatUnits().nonBuildings().inRadius(1.8, unit).empty()
            && unit.addLog("GoonFightCB");
    }

    private static boolean vsProtoss(AUnit unit) {
        if (unit.isHealthy()) return true;
        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
        if (meleeEnemiesNearCount > 0) {
            return meleeNearButQuiteHealthy(unit);
        }

        return (unit.woundHp() <= 30 || unit.combatEvalRelative() > 1.05);
    }

    private static boolean vsTerran(AUnit unit) {
        if (unit.isHealthy()) return true;
        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
        if (meleeEnemiesNearCount > 0) {
            return meleeNearButQuiteHealthy(unit);
        }

        return false;
    }

    private static boolean vsZerg(AUnit unit) {
        if (unit.isHealthy()) return true;
//        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        if (unit.cooldown() <= 7) {
            int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
            if (meleeEnemiesNearCount > 0) {
                return meleeNearButQuiteHealthy(unit);
            }
        }

        return unit.lastAttackFrameAgo() > 30 * 3;
    }

    private static boolean meleeNearButQuiteHealthy(AUnit unit) {
        if (
            unit.cooldown() <= 10
                && unit.lastAttackFrameMoreThanAgo(30 * (unit.shields() >= 16 ? 2 : 5))
        ) return true;

        return (unit.woundHp() <= 9 || unit.cooldown() <= 7) && unit.shields() >= 6;
    }
}
