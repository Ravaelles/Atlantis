package atlantis.information.strategy.response.zerg;

import atlantis.combat.missions.Missions;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.decisions.OurStrategicBuildings;
import atlantis.information.strategy.response.AStrategyResponse;

public class ZergStrategyResponse extends AStrategyResponse {

    protected boolean rushDefence(AStrategy enemyStrategy) {
        if (GamePhase.isEarlyGame()) {
            Missions.setGlobalMissionDefend();
        }

        if (shouldSkipAntiRushCombatBuilding(enemyStrategy)) {
            return false;
        }

        OurStrategicBuildings.setAntiLandBuildingsNeeded(rushDefenseCombatBuildingsNeeded(enemyStrategy));
        return true;
    }

    protected int rushDefenseCombatBuildingsNeeded(AStrategy enemyStrategy) {
        return 0;
//        return enemyStrategy.isGoingCheese() ? 2 : 1;
    }

}
