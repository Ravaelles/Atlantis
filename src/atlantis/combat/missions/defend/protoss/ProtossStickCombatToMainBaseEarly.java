package atlantis.combat.missions.defend.protoss;

import atlantis.combat.missions.Missions;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.information.generic.Army;
import atlantis.information.strategy.Strategy;
import atlantis.production.constructions.ConstructionRequests;
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
        if (Missions.isGlobalMissionSparta()) return false;
        if (Missions.isGlobalMissionAttack()) return false;
        if (Count.cannons() > 0) return false;
        if (Count.basesWithUnfinished() >= 2) return false;
        if (ConstructionRequests.countNotFinishedOfType(AtlantisRaceConfig.BASE) > 0) return false;
        if (OurBuildingUnderAttack.notNull()) return false;

        // =========================================================

        combatUnits = Count.ourCombatUnits();
        strength = Army.strength();
        dragoons = Count.dragoons();

        if (Enemy.zerg()) {
            if (combatUnits <= 4) return true;
            if (combatUnits <= 7 && Army.strength() <= 400) return true;
            if (combatUnits <= 7 && dragoons <= 1 && Army.strength() <= 140) return true;
            if (combatUnits <= 10 && dragoons <= 0 && Strategy.get().isGoingTech() && Army.strength() <= 350) return true;
        }
        if (Enemy.protoss()) {
            if (combatUnits <= 10 && dragoons <= 1 && Strategy.get().isGoingTech() && Army.strength() <= 350) return true;
        }

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
