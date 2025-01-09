package atlantis.information.strategy.response.protoss;

import atlantis.information.strategy.response.enemy_base.WhenEnemyBaseDiscovered;
import atlantis.information.strategy.response.enemy_cb.WhenCBDiscovered;
import atlantis.information.strategy.response.enemy_hidden.EnemyHiddenUnits;
import atlantis.units.AUnit;

public class AsProtossUnitDiscoveredResponse {
    public static boolean updateEnemyUnitDiscovered(AUnit enemyUnit) {
        if (enemyUnit.isBase()) WhenEnemyBaseDiscovered.whenBaseDiscovered(enemyUnit);
        if (enemyUnit.isCombatBuilding()) WhenCBDiscovered.whenCBDiscovered(enemyUnit);

        return EnemyHiddenUnits.handleHiddenUnitDetected(enemyUnit)
            || EnemyHiddenUnits.handleBuildingLeadingToHiddenUnits(enemyUnit);
    }
}
