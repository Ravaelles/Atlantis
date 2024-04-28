package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.combat.retreating.protoss.ProtossTooBigBattleToRetreat;
import atlantis.decisions.Decision;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class DragoonDontAvoidEnemy {
    public static boolean dontAvoid(AUnit unit) {
//        if (true) return true;

        if (!unit.isDragoon()) return false;
//        if (unit.isIdle()) return false;
//        if (true) return false;

        Decision decision;

        if (healthyAndSafe(unit)) return true;
        if (hasNotShotInAWhile(unit)) return true;
        if ((decision = whenMissionSparta(unit)).notIndifferent()) return decision.toBoolean();
        if ((decision = whenMissionDefend(unit)).notIndifferent()) return decision.toBoolean();
//
        if (dontAvoidCombatBuildings(unit)) return true;

        if (Enemy.protoss()) return vsProtoss(unit);
        if (Enemy.terran()) return vsTerran(unit);
        if (Enemy.zerg()) return vsZerg(unit);

        return false;
    }

    private static Decision whenMissionSparta(AUnit unit) {
        if (!unit.isMissionSparta()) return Decision.INDIFFERENT;

        if (unit.woundHp() <= 9) return Decision.TRUE;
//        if (unit.isSafeFromMelee()) return Decision.TRUE;

        return unit.isSafeFromMelee() ? Decision.TRUE : Decision.FALSE;
//        return Decision.INDIFFERENT;
    }

    private static boolean healthyAndSafe(AUnit unit) {
        return unit.woundHp() <= 10;
//            && unit.enemiesNear().ranged().canAttack(unit, 1.5).empty();
    }

    private static Decision whenMissionDefend(AUnit unit) {
        if (!unit.isMissionDefend()) return Decision.INDIFFERENT;

        if (unit.isRanged()) {
            if (unit.cooldown() >= 10) return Decision.FALSE;
            if (unit.hp() > 40 && unit.lastAttackFrameMoreThanAgo(30 * 6)) return Decision.TRUE;
            return unit.isSafeFromMelee() ? Decision.TRUE : Decision.FALSE;
        }

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
        if (unit.isHealthy()) return true;

        int meleeEnemiesNearCount = unit.meleeEnemiesNearCount(1.5);
        if (meleeEnemiesNearCount > 0) {
//            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - MELEE NEAR");
            return false;
//            return whenMeleeNear(unit);
        }

        Decision decision;
        if ((decision = oneOnOneDragoon(unit)).notIndifferent()) return decision.toBoolean();

        if (unit.hp() <= 41 && !unit.isSafeFromMelee()) return false;

        if (ProtossTooBigBattleToRetreat.PvP_doNotRetreat(unit)) return true;

        return (unit.woundHp() <= 30 || unit.combatEvalRelative() > 1.05);
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
