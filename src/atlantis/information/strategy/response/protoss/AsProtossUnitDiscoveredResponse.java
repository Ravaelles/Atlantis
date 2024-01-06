package atlantis.information.strategy.response.protoss;

import atlantis.information.enemy.EnemyFlags;
import atlantis.units.AUnit;

public class AsProtossUnitDiscoveredResponse {
    public static void updateEnemyUnitDiscovered(AUnit enemyUnit) {
        handleHiddenUnitDetected(enemyUnit);
    }

    // =========================================================

    private static void handleHiddenUnitDetected(AUnit enemyUnit) {
        if (!enemyUnit.isLurker() && !enemyUnit.isDT()) return;

        EnemyFlags.HAS_HIDDEN_COMBAT_UNIT = true;

        RequestProtossDetection.needDetectionAgainst(enemyUnit);
    }
}
