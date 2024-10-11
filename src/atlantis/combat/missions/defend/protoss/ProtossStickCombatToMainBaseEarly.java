package atlantis.combat.missions.defend.protoss;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.select.Count;
import atlantis.util.Enemy;
import atlantis.util.We;

public class ProtossStickCombatToMainBaseEarly {
    public static boolean should() {
        if (!We.protoss()) return false;
        if (!Enemy.zerg()) return false;
        if (Missions.isGlobalMissionAttack()) return false;
        if (Count.cannons() > 0) return false;
        if (Count.bases() > 0) return false;

        int combatUnits = Count.ourCombatUnits();

        if (combatUnits >= 8) return false;

        if (whenWeDoRushDontStickTooLong(combatUnits)) return false;

        if (combatUnits >= 5 && EnemyUnitBreachedBase.notNull() && OurArmy.strength() >= 170) return false;

        if (
            (combatUnits <= 8 || OurArmy.strength() <= 120)
                && (Count.dragoons() <= 3 || OurArmy.strength() <= 120)
//                && OurArmy.strength() <= 240
                && Count.basesWithUnfinished() <= 1
        ) {
            return true;
        }

        return false;
    }

    private static boolean whenWeDoRushDontStickTooLong(int combatUnits) {
        double desiredCombatUnitsRatio = A.whenEnemyProtossTerranZerg(1.25, 0.7, 0.38);

        return OurStrategy.get().isRushOrCheese() && combatUnits >= 2 && (
            OurArmy.strength() >= 115 && EnemyInfo.ourCombatUnitsToEnemyRatio() >= desiredCombatUnitsRatio
        );
    }
}
