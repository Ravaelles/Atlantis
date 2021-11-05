package atlantis.strategy.response.terran;

import atlantis.combat.missions.Missions;
import atlantis.strategy.AStrategy;
import atlantis.strategy.AStrategyInformations;
import atlantis.strategy.GamePhase;
import atlantis.strategy.response.AStrategyResponse;

public class TerranStrategyResponse extends AStrategyResponse {

    protected boolean rushDefence(AStrategy enemyStrategy) {
        if (GamePhase.isEarlyGame()) {
            Missions.setGlobalMissionDefend();
        }

        if (shouldSkipAntiRushDefensiveBuilding(enemyStrategy)) {
            return false;
        }

        AStrategyInformations.setAntiLandBuildingsNeeded(rushDefenseDefensiveBuildingsNeeded(enemyStrategy));
        return true;
    }

    protected int rushDefenseDefensiveBuildingsNeeded(AStrategy enemyStrategy) {
        return 1;
//        return enemyStrategy.isGoingCheese() ? 2 : 1;
    }

}
