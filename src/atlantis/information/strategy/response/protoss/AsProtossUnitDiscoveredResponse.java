package atlantis.information.strategy.response.protoss;

import atlantis.information.enemy.EnemyFlags;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class AsProtossUnitDiscoveredResponse {
    public static boolean updateEnemyUnitDiscovered(AUnit enemyUnit) {
        return handleHiddenUnitDetected(enemyUnit)
            || handleBuildingLeadingToHiddenUnits(enemyUnit);
    }

    // =========================================================

    private static boolean handleBuildingLeadingToHiddenUnits(AUnit enemyUnit) {
        if (!enemyUnit.isABuilding()) return false;

        return enemyUnit.is(AUnitType.Protoss_Citadel_of_Adun, AUnitType.Protoss_Templar_Archives);
    }

    private static boolean handleHiddenUnitDetected(AUnit enemyUnit) {
        if (!enemyUnit.isLurker() && !enemyUnit.isLurkerEgg() && !enemyUnit.isDT()) return false;

        EnemyFlags.HAS_HIDDEN_COMBAT_UNIT = true;

        return RequestProtossDetection.needDetectionAgainst(enemyUnit);
    }
}
