package atlantis.information.strategy.response;

import atlantis.combat.missions.MissionChanger;
import atlantis.game.A;
import atlantis.information.decisions.OurStrategicBuildings;
import atlantis.information.enemy.EnemyFlags;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.strategy.response.protoss.AsProtossUnitDiscoveredResponse;
import atlantis.information.strategy.response.terran.AsTerranUnitDiscoveredResponse;
import atlantis.information.strategy.response.zerg.AsZergUnitDiscoveredResponse;
import atlantis.map.choke.Chokes;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.production.constructing.position.modifier.PositionModifier;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.ProductionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class EnemyUnitDiscoveredResponse {

    public static void updateEnemyUnitDiscovered(AUnit enemyUnit) {
        if (A.isUms()) return;

        if (We.terran()) {
            AsTerranUnitDiscoveredResponse.updateEnemyUnitDiscovered(enemyUnit);
        } else if (We.protoss()) {
            AsProtossUnitDiscoveredResponse.updateEnemyUnitDiscovered(enemyUnit);
        } else if (We.zerg()) {
            AsZergUnitDiscoveredResponse.updateEnemyUnitDiscovered(enemyUnit);
        }
    }
}
