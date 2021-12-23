package atlantis.strategy.response.zerg;

import atlantis.combat.missions.Missions;
import atlantis.strategy.AStrategy;
import atlantis.strategy.GamePhase;
import atlantis.strategy.decisions.OurStrategicBuildings;
import atlantis.strategy.response.AStrategyResponse;

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
