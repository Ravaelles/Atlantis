package atlantis.strategy.response.terran;

import atlantis.strategy.AStrategy;
import atlantis.strategy.AStrategyInformations;
import atlantis.strategy.response.AStrategyResponse;

public class TerranStrategyResponse extends AStrategyResponse {

    protected boolean rushDefense(AStrategy enemyStrategy) {
        if (shouldSkipAntiRushDefensiveBuilding(enemyStrategy)) {
            return false;
        }

        AStrategyInformations.setAntiLandBuildingsNeeded(rushDefenseDefensiveBuildings(enemyStrategy));
        return true;
    }

    protected int rushDefenseDefensiveBuildings(AStrategy enemyStrategy) {
        return enemyStrategy.isGoingCheese() ? 2 : 1;
    }

}
