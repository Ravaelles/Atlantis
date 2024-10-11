package atlantis.information.strategy.response.protoss;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.enemy.EnemyFlags;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class AsProtossUnitDiscoveredResponse {
    public static boolean updateEnemyUnitDiscovered(AUnit enemyUnit) {
        if (enemyUnit.isBase()) whenBaseDiscovered(enemyUnit);
        if (enemyUnit.isCombatBuilding()) whenCBDiscovered(enemyUnit);

        return handleHiddenUnitDetected(enemyUnit)
            || handleBuildingLeadingToHiddenUnits(enemyUnit);
    }

    // =========================================================

    private static boolean handleBuildingLeadingToHiddenUnits(AUnit enemyUnit) {
        if (!enemyUnit.isABuilding()) return false;

        return enemyUnit.is(
            AUnitType.Protoss_Citadel_of_Adun, AUnitType.Protoss_Templar_Archives,
            AUnitType.Zerg_Hydralisk_Den
        );
    }

    private static boolean handleHiddenUnitDetected(AUnit enemyUnit) {
        if (
            !enemyUnit.isLurker() && !enemyUnit.isLurkerEgg() && !enemyUnit.isDT()
        ) return false;

        EnemyFlags.HAS_HIDDEN_COMBAT_UNIT = true;

        return RequestProtossDetection.needDetectionAgainst(enemyUnit);
    }

    private static void whenBaseDiscovered(AUnit enemyUnit) {
//        if (A.s <= 700 && Count.basesWithPlanned() <= 1) AddToQueue.withHighPriority(AtlantisRaceConfig.BASE);
    }

    private static void whenCBDiscovered(AUnit enemyUnit) {

        // When enemy goes combat buildings, expand.
        if (A.s <= 700 && EnemyInfo.combatBuildingsAntiLand() >= 2 && Count.basesWithPlanned() <= 1) {
            A.println(A.s + "s ----------- Enemy goes combat buildings, expand");
            AddToQueue.withTopPriority(AtlantisRaceConfig.BASE);
        }
    }
}
