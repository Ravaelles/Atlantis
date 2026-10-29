package atlantis.information.strategy.response.protoss;

import atlantis.information.strategy.response.enemy_hidden.EnemyHiddenUnits;
import atlantis.units.AUnit;

public class AsProtossUnitDiscoveredResponse {
    public static boolean updateEnemyUnitDiscovered(AUnit enemyUnit) {
        // Single policy (NEXT #228): the legacy base/CB discovery writes
        // died with the drain - V2 expansion goals own that demand now.
        return EnemyHiddenUnits.handleHiddenUnitDetected(enemyUnit)
            || EnemyHiddenUnits.handleBuildingLeadingToHiddenUnits(enemyUnit);
    }
}
