package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.combat.retreating.protoss.ProtossTooBigBattleToRetreat;
import atlantis.decisions.Decision;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import bwapi.Color;

public class DragoonDontAvoidEnemy {
    public static boolean dontAvoid(AUnit unit) {
        if (!unit.isDragoon()) return false;
//        if (unit.isIdle()) return false;
//        if (true) return false;

        Decision decision;

        if (healthyAndSafe(unit)) return true;
        if (hasNotShotInAWhile(unit)) return true;
        if (dontAvoidWhenOnlyEnemyZealotsNearby(unit)) return true;
        if (dontAvoidWhenOnlyEnemyZerglingsNearby(unit)) return true;

//        if ((decision = whenMissionSparta(unit)).notIndifferent()) return decision.toBoolean();
        if ((decision = whenMissionDefend(unit)).notIndifferent()) return decision.toBoolean();
//
        if (dontAvoidCombatBuildings(unit)) return true;

        if (Enemy.protoss()) return vsProtoss(unit);
        if (Enemy.terran()) return vsTerran(unit);
        if (Enemy.zerg()) return vsZerg(unit);

        return false;
    }

//    private static Decision whenMissionSparta(AUnit unit) {
//        if (!unit.isMissionSparta()) return Decision.INDIFFERENT;
//
//        if (unit.woundHp() <= 9) return Decision.TRUE;
////        if (unit.isSafeFromMelee()) return Decision.TRUE;
//
//        return unit.isSafeFromMelee() ? Decision.TRUE : Decision.FALSE;
////        return Decision.INDIFFERENT;
//    }

    private static boolean healthyAndSafe(AUnit unit) {
        return unit.woundHp() <= 9 && unit.lastAttackFrameMoreThanAgo(30 * 5);
//            && unit.enemiesNear().ranged().canAttack(unit, 1.5).empty();
    }

    private static Decision whenMissionDefend(AUnit unit) {
        if (!unit.isMissionDefend()) return Decision.INDIFFERENT;

        if (unit.shieldDamageAtMost(9)) return Decision.TRUE;
        if (unit.enemiesNear().countInRadius(3.1, unit) >= 2) return Decision.FALSE;
        if (unit.hp() <= 40 && unit.enemiesNear().countInRadius(2.7, unit) >= 1) return Decision.FALSE;

//        if (unit.isRanged()) {
//            if (unit.cooldown() >= 10) return Decision.FALSE;
//            if (unit.shieldDamageAtMost(39) && unit.lastAttackFrameMoreThanAgo(30 * 6)) return Decision.TRUE;
//            return unit.isSafeFromMelee() ? Decision.TRUE : Decision.FALSE;
//        }

        return Decision.INDIFFERENT;
    }

    private static boolean hasNotShotInAWhile(AUnit unit) {
        if (!unit.isRanged()) return false;

        if (unit.lastAttackFrameMoreThanAgo(30 * 7)) return true;

        return false;

//        if (unit.hp() <= 35) return false;
//        if (unit.cooldown() >= 7) return false;

//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - " + unit.hp());

//        Selection enemies = unit.enemiesNear().melee().effUndetected().groundUnits();
//        return enemies.canAttack(unit, 1.5).empty();
    }

    private static boolean dontAvoidCombatBuildings(AUnit unit) {
        if (unit.hp() <= 30) return false;

        Selection combatBuildings = unit.enemiesNear().combatBuildingsAntiLand().inRadius(9, unit);
        if (combatBuildings.empty()) return false;

        return unit.enemiesNear().combatUnits().nonBuildings().inRadius(1.8, unit).empty()
            && unit.addLog("GoonFightCB");
    }

    private static boolean vsProtoss(AUnit unit) {
//        if (unit.isHealthy()) return true;

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
        if (meleeEnemiesNearCount > 0) {
//            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - MELEE NEAR");
            return false;
//            return whenMeleeNear(unit);
        }

        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        Decision decision;
        if ((decision = oneOnOneDragoon(unit)).notIndifferent()) return decision.toBoolean();

//        if (unit.hp() <= 41 && !unit.isSafeFromMelee()) return false;
        if (!unit.isSafeFromMelee()) return false;

        return (unit.woundHp() <= 30 || unit.combatEvalRelative() > 1.05);
    }

    private static boolean dontAvoidWhenOnlyEnemyZealotsNearby(AUnit unit) {
        if (!Enemy.protoss()) return false;
        if (unit.hp() <= 33) return false;

        Selection enemiesNear = unit.enemiesNear().havingAntiGroundWeapon();

//        if (unit.shieldWounded() && enemiesNear.inRadius(2.5, unit).notEmpty()) return false;
//        if (enemiesNear.ranged().canAttack(unit, 0.5).notEmpty()) return false;

        if (unit.shieldWound() >= 23 && enemiesNear.inRadius(2.5, unit).atLeast(2)) return false;

        if (enemiesNear.melee().inRadius(2.9, unit).atMost(0)) {
//            unit.paintCircleFilled(14, Color.Green);
            return true;
        }
        if (unit.enemiesNear().ranged().canAttack(unit, 0.2 + unit.woundPercent() / 60.0).empty()) return true;

        return unit.shieldDamageAtMost(29) && unit.meleeEnemiesNearCount(2.5) <= 0;
    }

    private static boolean dontAvoidWhenOnlyEnemyZerglingsNearby(AUnit unit) {
        if (!Enemy.zerg()) return false;
        if (unit.hp() <= 25) return false;

        Selection enemiesNear = unit.enemiesNear().havingAntiGroundWeapon();

        if (!unit.isHealthy() && enemiesNear.inRadius(2.5, unit).atLeast(2)) return false;
        if (unit.enemiesNear().ranged().inRadius(9, unit).empty()) return true;

        return unit.shieldDamageAtMost(19);
    }

    private static Decision oneOnOneDragoon(AUnit unit) {
        Selection enemies = unit.enemiesNear().inRadius(7, unit);
        if (enemies.onlyOfType(AUnitType.Protoss_Dragoon)) {
            if (enemies.first().hp() <= unit.hp()) return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    private static boolean vsTerran(AUnit unit) {
        if (unit.isHealthy()) return true;
        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
        if (meleeEnemiesNearCount > 0) {
            return whenMeleeNear(unit);
        }

        return false;
    }

    private static boolean vsZerg(AUnit unit) {
        if (unit.isHealthy()) return true;
//        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        if (unit.cooldown() <= 7) {
            int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
            if (meleeEnemiesNearCount > 0) {
                return whenMeleeNear(unit);
            }
        }

        return unit.lastAttackFrameAgo() > 30 * 3;
    }

    private static boolean whenMeleeNear(AUnit unit) {
//        if (
//            unit.cooldown() <= 10
//                && unit.lastAttackFrameMoreThanAgo(30 * (unit.shields() >= 16 ? 2 : 5))
//        ) return true;

        return (unit.woundHp() <= 9 || unit.cooldown() <= 3) && unit.shields() >= 40;
    }
}
