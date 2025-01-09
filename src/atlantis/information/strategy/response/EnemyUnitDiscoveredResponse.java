package atlantis.information.strategy.response;

import atlantis.game.A;

import atlantis.information.strategy.response.protoss.AsProtossUnitDiscoveredResponse;
import atlantis.information.strategy.response.terran.AsTerranUnitDiscoveredResponse;
import atlantis.information.strategy.response.zerg.AsZergUnitDiscoveredResponse;
import atlantis.units.AUnit;
import atlantis.util.We;

public class EnemyUnitDiscoveredResponse {

    public static void updateEnemyUnitDiscovered(AUnit enemyUnit) {
        if (A.isUms()) return;

        if (We.terran()) {
            AsTerranUnitDiscoveredResponse.updateEnemyUnitDiscovered(enemyUnit);
        }
        else if (We.protoss()) {
            AsProtossUnitDiscoveredResponse.updateEnemyUnitDiscovered(enemyUnit);
        }
        else if (We.zerg()) {
            AsZergUnitDiscoveredResponse.updateEnemyUnitDiscovered(enemyUnit);
        }
    }
}
