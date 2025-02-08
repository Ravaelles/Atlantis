package atlantis.combat.missions.defend.protoss;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.information.strategy.Strategy;
import atlantis.units.select.Count;
import atlantis.game.player.Enemy;
import atlantis.util.We;

public class ProtossStickCombatToMainBaseEarly {

    private static int strength;
    private static int combatUnits;
    private static int dragoons;

    public static boolean should() {
        if (!We.protoss()) return false;
        if (!Enemy.zerg()) return false;
        if (Missions.isGlobalMissionAttack()) return false;
        if (Count.cannons() > 0) return false;
        if (Count.bases() >= 2) return false;

        combatUnits = Count.ourCombatUnits();
        strength = Army.strength();
        dragoons = Count.dragoons();

        if (combatUnits <= 3 && Enemy.zerg()) return true;
        if (combatUnits <= 5 && Enemy.zerg() && dragoons == 0) return true;

        if (combatUnits >= 9) return false;
        if (dragoons >= 1 && strength >= 115) return false;
        if (dragoons >= 3) return false;

        if (Enemy.zerg()) {
            if (strength <= 150 && EnemyUnits.zerglings() >= 3 * Count.ourCombatUnits()) return true;
        }

        if (strength >= 170) return false;

        if (whenWeDoRushDontStickTooLong(combatUnits)) return false;

        if (combatUnits >= 5 && EnemyUnitBreachedBase.notNull() && Army.strength() >= 170) return false;

        if (
            (combatUnits <= 8 || Army.strength() <= 120)
                && (dragoons <= 3 || Army.strength() <= 120)
//                && Army.strength() <= 240
                && Count.basesWithUnfinished() <= 1
        ) {
            return true;
        }

        return false;
    }

    private static boolean whenWeDoRushDontStickTooLong(int combatUnits) {
        double desiredCombatUnitsRatio = A.whenEnemyProtossTerranZerg(1.25, 0.7, 0.38);

        return Strategy.get().isRushOrCheese() && combatUnits >= 2 && (
            Army.strength() >= 115 && EnemyInfo.ourCombatUnitsToEnemyRatio() >= desiredCombatUnitsRatio
        );
    }
}
