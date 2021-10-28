package atlantis.strategy.response;

import atlantis.strategy.AStrategy;
import atlantis.strategy.AStrategyInformations;

public class TerranStrategyResponse extends AStrategyResponse {

    protected boolean rushDefense(AStrategy enemyStrategy) {
        if (shouldSkipAntiRushDefensiveBuilding(enemyStrategy)) {
            return false;
        }

        AStrategyInformations.antiLandBuildingsNeeded(rushDefenseDefensiveBuildings(enemyStrategy));
        return true;
    }

    protected int rushDefenseDefensiveBuildings(AStrategy enemyStrategy) {
        return enemyStrategy.isGoingCheese() ? 2 : 1;
    }

}
